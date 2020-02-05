package com.systex.jbranch.host.landbank;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.jms.JMSException;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;

import com.systex.jbranch.host.landbank.broadcast.BroadCastSender;
//20190902
//MatsudairaSyume
import com.systex.jbranch.host.util.CharsetCnv;
//----
import com.systex.jbranch.host.util.TelegramKeyUtil;
import com.systex.jbranch.host.utlsysf.Utlsysf;
import com.systex.jbranch.platform.common.security.des.DESEngine;
import com.systex.jbranch.platform.host.transform.JMSGatewayOutputVO;

public class TelegramService {

    // ------------------------------ FIELDS ------------------------------
    @Autowired
    private TelegramKeyUtil telegramKeyUtil;

    @Autowired
    private SubportStatus subportStatus;

    @Autowired
    private ReceiveHandler receiveHandler;

    private static final String ENCODE = "UTF-8";
    private static final int CONTROL_BUFFER_SIZE = 12;
    private InputStream inputStream;
    private OutputStream outputStream;
    private byte[] buffer;
    private Socket socket;
    private KeyStore keyStore;
    private Logger logger = LoggerFactory.getLogger(TelegramService.class);
    private Logger faslog = LoggerFactory.getLogger("faslog");
    private String localAddress;
    private String serverAddress;
    private int localPort;
    private int serverPort;
    private boolean runReceive = true;

    private File seqNoFile;
    private boolean isMask = true;

    private BroadCastSender broadCastSender;
    private AtomicInteger seqCounter = null;

    private long reTryInterval = 10000L; // 重新連線間隔

    private int connectTimeout = 10000;
    
    private int receiveBufferSize = 65535;
	private String fasSendPtrn = "-->FAS len %4d :[............%s]";
	private String fasRecvPtrn = "<--FAS len %4d :[............%s]";

    // --------------------------- CONSTRUCTORS ---------------------------
	// ----
	//20190902
	private CharsetCnv charcnv = new CharsetCnv();
	//

    public TelegramService() {

    }

    public TelegramService(String serverAddress, int serverPort, String localAddress, int localPort,
            BroadCastSender broadCastSender, AtomicInteger seqCounter) throws Exception {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.localAddress = localAddress;
        this.localPort = localPort;
        this.broadCastSender = broadCastSender;
        this.seqCounter = seqCounter;
    }

    public void init() throws IOException, InterruptedException {

    	putMDC();

        System.setProperty(KeyStore.GATEWAY_KEY_FOLDER, "resources");
        keyStore = new KeyStore(serverAddress + "_" + serverPort + "_" + localAddress + "_" + localPort);
        if (logger.isInfoEnabled()) {
            logger.info("initialized KeyStore");
            logger.info("binding to host");
        }
		//20181105
        if(subportStatus.isDisableKey()
       	 && broadCastSender == null) { // 排除broadcast
          subportStatus.setAlive(this.localPort, true);
  		  if (logger.isInfoEnabled())
            logger.info("don't use gateway key");
		}
		//----

        seqNoFile = new File("SEQNO", "SEQNO_" + localPort);
        boolean neetReTry = true;
        do {
            try {
                bindToHost();
                neetReTry = false;
                init2();
            } catch (Exception e) {
                subportStatus.setAlive(this.localPort, false);
                subportStatus.setKeyStatus(this.localPort, false);
                logger.error(e.getMessage(), e);
                logger.info("bind error wait [" + reTryInterval + "]ms reTry...", e);
                Thread.sleep(reTryInterval);
            } catch (Throwable e) {
                subportStatus.setAlive(this.localPort, false);
                subportStatus.setKeyStatus(this.localPort, false);
                logger.error(e.getMessage(), e);
            }
        } while (neetReTry);

        Thread hook = new Thread(new Runnable() {
            public void run() {
                shutdownHandler();
            }
        });
        Runtime.getRuntime().addShutdownHook(hook);

        new Thread() {
            public void run() {
                receiveLoop();
            }
        }.start();
    }

    public void putMDC() {
        MDC.put("SERVER_ADDRESS", serverAddress);
        MDC.put("SERVER_PORT", String.valueOf(serverPort));
        MDC.put("LOCAL_ADDRESS", localAddress);
        MDC.put("LOCAL_PORT", String.valueOf(localPort));
    }

    public void receiveLoop() {

    	boolean needVerify = false;
        while (runReceive) {
            try {
                if (inputStream == null) {
                    Thread.sleep(reTryInterval);
                    continue;
                }

            if (broadCastSender != null && seqCounter != null) {
                MDC.put(TelegramHostGateway.$REQUEST_ID, this.localAddress + "_" + seqCounter.getAndIncrement());
                }

                buffer = new byte[CONTROL_BUFFER_SIZE];

                int realControlHeaderSize = inputStream.read(buffer);

                if (realControlHeaderSize < CONTROL_BUFFER_SIZE) {
                	needVerify = true;
                    String errMsg = "receive control header length error expect, 12byte，real[{}]";
                    logger.error(errMsg, realControlHeaderSize);
                    logger.error("receive control header data[" + Hex.encodeHexString(buffer) + "]");
                    int remainSize = CONTROL_BUFFER_SIZE - realControlHeaderSize;
                    byte[] secondBuffer = new byte[remainSize];
                    int secondRealControlHeaderSize = inputStream.read(secondBuffer);
                    if(secondRealControlHeaderSize != remainSize){
                        logger.error(errMsg, secondRealControlHeaderSize);
                        logger.error("second receive control header data[" + Hex.encodeHexString(buffer) + "]");
                    	throw new RuntimeException("receive control header error");
                    }
                    logger.info("second receive control header complete");
                    buffer = ArrayUtils.addAll(buffer, secondBuffer);  
                }

                logger.info("in receiveLoop control header=" + Hex.encodeHexString(buffer));

                String controlHeaderString = Hex.encodeHexString(buffer);
                if(needVerify){
                	needVerify = false;
                	if (controlHeaderString.matches("^0f0f0f.{16}0f$") == false) {
                		throw new RuntimeException("verify control header error");
					}
                	logger.info("verify control header successful");
                }
                
                int contentSize = Integer.parseInt(controlHeaderString.substring(6, 12));// header.getLength() -
                                                                                                 // CONTROL_BUFFER_SIZE;
                logger.info("contentSize=" + contentSize);
                byte[] bufferBody = new byte[0];
                boolean isLengthError = false;
                if (contentSize - CONTROL_BUFFER_SIZE > 0) {

                    bufferBody = new byte[contentSize - CONTROL_BUFFER_SIZE];
                    logger.info("read body size=" + bufferBody.length);
                    int realContentSize = inputStream.read(bufferBody);

                    //模擬長度不正確
//                     String flagContent = FileUtils.readFileToString(new File("/gw/flag.txt"));
//                     String[] flags = flagContent.split(",");
//                     if ("y".equalsIgnoreCase(flags[0])) {
//                     bufferBody = ArrayUtils.subarray(bufferBody, 0, Integer.parseInt(flags[1]));
//                     realContentSize = bufferBody.length;
//                     }

                    logger.info("realContentSize=" + realContentSize);
                    if (realContentSize != (contentSize - CONTROL_BUFFER_SIZE)) {
                        String errMsg = "receive length error，expect[" + (contentSize - CONTROL_BUFFER_SIZE)
                                + "]，real[" + realContentSize + "]";
                        logger.error(errMsg);
                        logger.error("TOTA origi data[" + Hex.encodeHexString(bufferBody) + "]");
                        needVerify = true;
                        int remainSize= contentSize - CONTROL_BUFFER_SIZE - realContentSize;
                        byte[] secondBufferBody = new byte[remainSize];
                        int secondRealContentSize = inputStream.read(secondBufferBody);
                        if(secondRealContentSize != remainSize){
                            errMsg = "second receive length error，expect[" + remainSize
                                    + "]，real[" + secondRealContentSize + "]";
                            logger.error(errMsg);
                            logger.error("second TOTA origi data[" + Hex.encodeHexString(bufferBody) + "]");
                            isLengthError = true;
                        	throw new RuntimeException("second receive length error");
                        }
                        logger.info("second receive length complete");
                        bufferBody = ArrayUtils.addAll(bufferBody, secondBufferBody);
                        realContentSize = realContentSize + secondRealContentSize;
                    }
                    if (realContentSize < telegramKeyUtil.getKeyLength()) {

                    	logger.warn("receiver body size[{}] data[{}]", realContentSize, Hex.encodeHexString(bufferBody));
                    	continue;
                    }
                }

                byte[] source = ArrayUtils.addAll(buffer, bufferBody);

                printTotaOrigi(source);
                boolean isCommand = decrypt(source);
                logger.debug("isCommand={}", isCommand);
                source = remove03(source);
                HostMessage response = new HostMessage(source);
                if (isCommand || response.matchStatus((byte) 0x0f) == false) {
                    hostCommand(response);
                    continue;
                }

                byte[] payLoad = response.getPayload().getPayload();

                String telegramKey = telegramKeyUtil.getTelegramKey(payLoad);

                //20190316
                if(!subportStatus.isDisableKey())
                    printTotaDecrypto(source, telegramKey);

                List<byte[]> list = receiveHandler.remove(telegramKey);

                if (list == null) {
                    list = new ArrayList<byte[]>();
                }

                list.add(payLoad);
                putData(isLengthError, source, telegramKey, list);

            } catch (Exception e) {
                try {
                    if (runReceive) {
                        logger.error(e.getMessage(), e);
                        subportStatus.setAlive(this.localPort, false);
                        subportStatus.setKeyStatus(this.localPort, false);
                        bindToHost();
                        init2();
                                //20190609
                        subportStatus.setAlive(this.localPort, true);
                                //----
                    } else {
                        logger.info("Socket closed");
                    }

                } catch (Exception e1) {
                    subportStatus.setAlive(this.localPort, false);
                    subportStatus.setKeyStatus(this.localPort, false);
                    logger.error(e1.getMessage(), e1);
                    logger.info("bind error wait [" + reTryInterval + "]ms reTry...", e);
                    try {
                        Thread.sleep(reTryInterval);
                    } catch (InterruptedException e2) {
                        // ignore
                    }
                }
            }
        }
    }
	//20190902
	//MatsudairaSyume
	// add Exception for charcnv.BIG5bytesUTF8str()
    private void printTotaOrigi(byte[] source) throws UnsupportedEncodingException, Exception {
        //MatsudairaSyume
        // convert BIG5 to UTF8 on telegram log
//        faslog.debug(String.format(fasRecvPtrn, source.length, new String(source, 12, source.length - 12)));
        byte[] fastelbytes = new byte[source.length - 12];
        System.arraycopy(source, 12, fastelbytes, 0, source.length - 12);
        faslog.debug(String.format(fasRecvPtrn, source.length, charcnv.BIG5bytesUTF8str(fastelbytes)));
            //----

        if (logger.isDebugEnabled()) {
            List<String> hexLog = LogUtils.toLog(source, isMask, ENCODE);
            logger.debug("TOTA origi length[" + source.length + "] \r\n" + LogUtils.listToString(hexLog) + "\r\n");
            return;
        }
        logger.info("TOTA origi length[" + source.length + "]");
    }

    private void printTotaDecrypto(byte[] source, String telegramKey) throws UnsupportedEncodingException {
        if (logger.isDebugEnabled()) {
            List<String> hexLog = LogUtils.toLog(source, isMask, ENCODE);
            logger.debug("TOTA decrypto length[" + source.length + "] SN:[" + telegramKey + "]\r\n"
                    + LogUtils.listToString(hexLog) + "\r\n");
            return;
        }
        logger.info("TOTA decrypto length[" + source.length + "] SN:[" + telegramKey + "]");
    }

    private void putData(boolean isLengthError, byte[] source, String telegramKey, List<byte[]> list)
            throws JMSException {
        if (broadCastSender != null) {
            // boradcase時，直接傳回mq
            JMSGatewayOutputVO outputVO = new JMSGatewayOutputVO();
            outputVO.setRequestID(MDC.get(TelegramHostGateway.$REQUEST_ID));
            outputVO.setCode(TelegramHostGateway.SCCESS);
            outputVO.setCdKey(this.getCdKey());
            outputVO.setContent(list);
            broadCastSender.send(outputVO);
            return;
        }

        // if (isLengthError) {
        // receiveHandler.put(telegramKey, "E007");
        // logger.info("TOTA decrypto length[{}] SN:[{}] HEX:[{}]", new Object[]{source.length, telegramKey,
        // Hex.encodeHexString(source)});
        // return;
        // }

        receiveHandler.put(telegramKey, list);
    }

    private byte[] remove03(byte[] source) {
        if (source[source.length - 1] == 0x03) {
            source = ArrayUtils.subarray(source, 0, source.length - 1);
            logger.debug("remove03");
        }
        return source;
    }

    private void hostCommand(HostMessage response) throws Exception {
        byte[] source = response.getSource();
        String command = Hex.encodeHexString(new byte[] { response.getHeader().getStatus() });
        if (logger.isDebugEnabled()) {
            List<String> hexLog = LogUtils.toLog(source, isMask, ENCODE);
            logger.debug("TOTA hostCommand length[" + source.length + "] command:[" + command + "]\r\n"
                    + LogUtils.listToString(hexLog) + "\r\n");
        } else {
            logger.info("TOTA hostCommand length[" + source.length + "] command:[" + command + "]");
        }

        int status = response.getHeader().getStatus();
        switch (status) {
            case 0x1a:// 中心要求更換First Key
                subportStatus.setKeyStatus(this.localPort, false);
                put(response);
                break;
            case 0x1c:// 中心送出CD、Mac Key檢核及更新
                subportStatus.setKeyStatus(this.localPort, false);
                hostExFirstKey(response);
                break;
            case 0x1b:// 中心要求更換Mac Key
                subportStatus.setKeyStatus(this.localPort, false);
                put(response);
                break;
            case 0x2b:// 正確
            case 0x1d:// 中心送出Mac Key更新
                updateCDKeyAndMacKey(response);
                break;
            case 0x2c: // Mac Key有誤
                MacKeyError(response);
                break;
            case 0x3a:
                logger.error("換key失敗次數超過限制，中心拒絕連接");
                break;
            case 0x3d:
                if (broadCastSender == null) {// 排除broadcast
                    subportStatus.setKeyStatus(this.localPort, true);
                }
                logger.info("中心解除換key功能，不需交換");
                break;
            case 0x5a:
            case 0x5c:
            case 0x5d:
                if (broadCastSender == null) {// 排除broadcast
                    subportStatus.setAlive(this.localPort, true);
                }
                put(response);
                exchangeMACKey();// change mack key
                break;
            default:
                logger.info("無法識別的status[" + Integer.toHexString(status) + "]");
                break;
        }

    }

    public void close() throws IOException {
        runReceive = false;
        logger.info("TelegramService.close");
        if (inputStream != null) {
            inputStream.close();
        }
        if (outputStream != null) {
            outputStream.close();
        }
        if (socket != null) {
            socket.close();
        }
        subportStatus.setAlive(this.localPort, false);
        subportStatus.setKeyStatus(this.localPort, false);
    }

    public void shutdownHandler() {
        try {
            close();
        } catch (IOException e) {
            logger.warn(e.getMessage(), e);
        }
        logger.info("shutdown.");
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public void setLocalAddress(String localAddress) {
        this.localAddress = localAddress;
    }

    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    public void bindToHost() throws IOException {
        if (socket != null) {
            try {
                socket.close();
                logger.info("socket close");
            } catch (IOException e) {
                // ignore
            }
        }
        socket = new Socket();
        socket.setReceiveBufferSize(receiveBufferSize);
        logger.info("receiveBufferSize={}", socket.getReceiveBufferSize());
        socket.setReuseAddress(true);
        socket.setSoLinger(true, 0);
        socket.bind(new InetSocketAddress(localAddress, localPort));
        socket.connect(new InetSocketAddress(serverAddress, serverPort), connectTimeout);
        logger.info("bind successful.");
        logger.info("TcpNoDelay=[{}]", socket.getTcpNoDelay());
        logger.info("TrafficClass=[{}]", socket.getTrafficClass());
        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();
    }

    private void init2() throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("begin init");
        }
        //20181105 by Scott Hong
        if (!subportStatus.isDisableKey()) {

            HostMessage hostMessage = getBindMessage();
            if (logger.isDebugEnabled()) {
                logger.debug("hostMessage.toHexString() = " + hostMessage.toHexString());
            }
            if (subportStatus.isDisableCrypt()) {
                putWithoutEncrypting(hostMessage);
            } else {
                byte[] source = hostMessage.getSource();
                int srclen = source.length;
                byte[] destination = source;
                int[] destlen = new int[2];
                destlen[0] = destination.length;
                long startTime = -1;
                if (logger.isDebugEnabled()) {
                    startTime = System.currentTimeMillis();
                }
                Utlsysf utlsysf = new Utlsysf();
                int result = utlsysf.UTLEnableCryptoData(localPort % 50, source, srclen, destination, destlen);
                if (logger.isDebugEnabled()) {
                    logger.debug("crypto [{}]ms", (System.currentTimeMillis() - startTime));
                }
                logger.debug("UTLEnableCryptoData result[" + result + "]");
                putWithoutEncrypting(new HostMessage(destination));
            }
        }
        //----
        logger.debug("seqNoFile local=" + seqNoFile.getAbsolutePath());
        if (seqNoFile.exists() == false) {
            File parent = seqNoFile.getParentFile();
            if (parent.exists() == false) {
                parent.mkdirs();
            }
            seqNoFile.createNewFile();
            FileUtils.writeStringToFile(seqNoFile, "0");
        }
    }

    private HostMessage getBindMessage() throws Exception {
        Header header = new Header();
        header.setLength(21);
        header.setStatus((byte) 0x5b);
        byte[] payload = new byte[9];
        return new HostMessage(header, new Payload(fill(payload, (byte) 0x20)));
    }

    private void putWithoutEncrypting(HostMessage hostMessage) throws IOException {
        byte[] msg = hostMessage.getSource();
        String command = Hex.encodeHexString(new byte[] { hostMessage.getHeader().getStatus() });
        if (logger.isDebugEnabled()) {
            List<String> hexLog = LogUtils.toLog(msg, isMask, ENCODE);
            logger.debug("TITA hostCommand length[" + msg.length + "] command:[" + command + "]\r\n"
                    + LogUtils.listToString(hexLog) + "\r\n");
        } else {
            logger.info("TITA hostCommand length[" + msg.length + "] command:[" + command + "]");
        }

        IOUtils.write(msg, outputStream);
        outputStream.flush();
    }

    private void exchangeMACKey() throws Exception {
        HostMessage hostMessage = getMACKeyMsg();
        if (logger.isDebugEnabled()) {
            logger.debug("exchangeMACKey hostMessage.toHexString() = " + hostMessage.toHexString());
        }
        put(hostMessage);
    }

    private void MacKeyError(HostMessage response) throws IllegalBlockSizeException, BadPaddingException, Exception,
            IOException {
        if (logger.isInfoEnabled()) {
            logger.info("中心檢核0x2a MAC Key有誤");
        }
        byte[] payload = response.getPayload().getPayload();
        byte[] encryptedMACKey = ArrayUtils.subarray(payload, 0, 8);
        byte[] macKeyChecksum = ArrayUtils.subarray(payload, 8, 16);
        byte[] decryptedMACKey = new DESEngine(keyStore.getCdKey()).decrypt(encryptedMACKey);
        if (verifyChecksum(decryptedMACKey, macKeyChecksum)) {
            logger.info("saving MAC Key");
            keyStore.setMacKey(decryptedMACKey);
            keyStore.loadKeys();
            logger.info("redo exchange MAC Key");
            // redo again
            exchangeMACKey();
        } else {
            logger.error("MAC key verify fail");
            put(getExMACKeyMsg(false));
        }
    }

    private void updateCDKeyAndMacKey(HostMessage response) throws IllegalBlockSizeException, BadPaddingException,
            Exception, IOException {
        byte[] payload = response.getPayload().getPayload();
        byte[] encryptedMACKey = ArrayUtils.subarray(payload, 0, 8);
        byte[] macKeyChecksum = ArrayUtils.subarray(payload, 8, 16);
        byte[] decryptedMACKey = new DESEngine(keyStore.getCdKey()).decrypt(encryptedMACKey);

        if (verifyChecksum(decryptedMACKey, macKeyChecksum)) {
            logger.info("saving MAC key");
            // save new MAC key
            keyStore.setMacKey(decryptedMACKey);
            if (broadCastSender == null) {// 排除broadcast
                subportStatus.setKeyStatus(this.localPort, true);
            }
            logger.info("saved MAC key");

            // send 0x3e
            HostMessage exMACKeyMsg = getExMACKeyMsg(true);
            put(exMACKeyMsg);
            if (logger.isInfoEnabled()) {
                logger.info("exchange MAC Key successful");
            }
        } else {
            logger.error("MAC key verify fail");
            // send 0x3f
            put(getExMACKeyMsg(false));
        }
    }

    private HostMessage getMACKeyMsg() throws Exception {
        DESEngine desEngine = new DESEngine(keyStore.getMacKey());
        byte[] random = getRandom(8);
        byte[] encryptedRandom = desEngine.encrypt(random);
        Header header = new Header();
        header.setLength(28);
        header.setStatus((byte) 0x2a);
        Payload payload = new Payload(ArrayUtils.addAll(encryptedRandom, random));
        return new HostMessage(header, payload);
    }

    public byte[] getRandom(int length) {
        Random random = new Random();
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return bytes;
    }

    private HostMessage getExMACKeyMsg(boolean successful) throws Exception {
        Header header = new Header();
        header.setLength(12);
        byte status = (byte) (successful ? 0x3e : 0x3f);
        header.setStatus(status);
        return new HostMessage(header, new Payload());
    }

    // -------------------------- OTHER METHODS --------------------------

    public void hostExFirstKey(HostMessage response) throws Exception {
        byte[] payload = response.getPayload().getPayload();
        byte[] encryptedCDKey = ArrayUtils.subarray(payload, 0, 8);
        byte[] cdKeyCheckSum = ArrayUtils.subarray(payload, 8, 16);
        byte[] encryptedMACKey = ArrayUtils.subarray(payload, 16, 24);
        byte[] macKeyCheckSum = ArrayUtils.subarray(payload, 24, 32);
        byte[] initialKey = keyStore.getInitialKey();
        DESEngine desEngines = new DESEngine(initialKey);
        byte[] cdKey = desEngines.decrypt(encryptedCDKey);
        boolean isSuccessful = false;
        byte[] macKey = null;
        if (verifyChecksum(cdKey, cdKeyCheckSum)) {
            macKey = new DESEngine(cdKey).decrypt(encryptedMACKey);
            if (verifyChecksum(macKey, macKeyCheckSum)) {
                isSuccessful = true;
            } else {
                logger.error("MAC key verify fail");
            }
        } else {
            logger.error("CD key verify fail");
        }

        HostMessage hostMessage;
        if (isSuccessful) {
            logger.info("saving CD key");
            // save CD key
            keyStore.setCdKey(cdKey);
            logger.info("saved CD key");
            logger.info("saving MAC key");
            // save MAC key
            keyStore.setMacKey(macKey);
            logger.info("saved MAC key");
        }
        hostMessage = getExFirstKeyMsg(isSuccessful);
        put(hostMessage);
        if (isSuccessful) {
            exchangeMACKey();
        }
    }

    private boolean verifyChecksum(byte[] key, byte[] checksum) throws Exception {
        return Arrays.equals(checksum, new DESEngine(key).encrypt(fill(new byte[8], (byte) 0x00)));
    }

    private byte[] fill(byte[] bytes, byte fillItem) {
        Arrays.fill(bytes, fillItem);
        return bytes;
    }

    private HostMessage getExFirstKeyMsg(boolean successful) throws DecoderException {
        Header header = new Header();
        header.setLength(12);
        byte status = (byte) (successful ? 0x3b : 0x3c);
        header.setStatus(status);
        return new HostMessage(header, new Payload());
    }

    private HostMessage getFirstKeyMsg() throws Exception {
        Header header = new Header();
        header.setLength(12);
        header.setStatus((byte) 0x1a);
        return new HostMessage(header, new Payload());
    }

    public void put(HostMessage hostMessage) throws Exception {
        byte[] szSource = hostMessage.getSource();
        String command = Hex.encodeHexString(new byte[] { hostMessage.getHeader().getStatus() });
        if (logger.isDebugEnabled()) {
            List<String> hexLog = LogUtils.toLog(szSource, isMask, ENCODE);
            logger.debug("TITA hostCommand origi length[" + szSource.length + "] command:[" + command + "]\r\n"
                    + LogUtils.listToString(hexLog) + "\r\n");
        } else {
            logger.info("TITA hostCommand origi length[" + szSource.length + "] command:[" + command + "]");
        }

        byte[] szDestination = encrypt(szSource);
        hostMessage = new HostMessage(szDestination);
        command = Hex.encodeHexString(new byte[] { hostMessage.getHeader().getStatus() });

        if (logger.isDebugEnabled()) {
            List<String> hexLog = LogUtils.toLog(szDestination, isMask, ENCODE);
            logger.debug("TITA hostCommand encrypto length[" + szDestination.length + "] command:[" + command + "]\r\n"
                    + LogUtils.listToString(hexLog) + "\r\n");
        } else {
            logger.info("TITA hostCommand encrypto length[" + szDestination.length + "] command:[" + command + "]");
        }
        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();
        IOUtils.write(szDestination, outputStream);
        outputStream.flush();
    }

    private byte[] encrypt(byte[] szSource) {
        if (subportStatus.isDisableCrypt()) {
            return szSource;
        }
        int sLen = szSource.length;
        byte[] szDestination = new byte[sLen];
        int[] dLen = new int[2];
        dLen[0] = sLen;
        logger.info("sess=" + (localPort % 50));
        long startTime = -1;
        if (logger.isDebugEnabled()) {
            startTime = System.currentTimeMillis();
        }
        Utlsysf utlsysf = new Utlsysf();
        int result = utlsysf.UTLEncryptBlock(localPort % 50, szSource, sLen, szDestination, dLen);
        if (logger.isDebugEnabled()) {
            logger.debug("UTLEncryptBlock [{}]ms", (System.currentTimeMillis() - startTime));
        }
        logger.info("UTLEncryptBlock result = " + result);
        return szDestination;
    }

    private boolean decrypt(byte[] szSource) throws IOException {
        if (subportStatus.isDisableCrypt()) {
            return false;
        }
        int sLen = szSource.length;
        byte[] szDestination = new byte[sLen];
        int[] dLen = new int[2];
        dLen[0] = sLen;
        int[] iXmt = new int[2];
        iXmt[0] = -1;
        long startTime = -1;
        if (logger.isDebugEnabled()) {
            startTime = System.currentTimeMillis();
        }
        Utlsysf utlsysf = new Utlsysf();
        int result = utlsysf.UTLDecryptBlock(localPort % 50, szSource, sLen, szDestination, dLen, iXmt);
        if (logger.isDebugEnabled()) {
            logger.debug("UTLDecryptBlock [{}]ms", (System.currentTimeMillis() - startTime));
        }
        logger.info("UTLDecryptBlock result = " + result);
        System.arraycopy(szDestination, 0, szSource, 0, szDestination.length);
        return iXmt[0] == 1;
    }

    public void send(Object content) throws Exception {
        byte[] msg = (byte[]) content;
        int len = msg.length + CONTROL_BUFFER_SIZE;
        logger.info("send len=" + len);
        String lenString = StringUtils.leftPad(String.valueOf(len), 6, "0");
        int seqno = 0;
        try {
            seqno = Integer.parseInt(FileUtils.readFileToString(seqNoFile)) + 1;
            if (seqno > 999) {
                seqno = 0;
            }
        } catch (Exception e) {
            logger.warn(e.getMessage());
        }

        FileUtils.writeStringToFile(seqNoFile, String.valueOf(seqno));
        String seqnoString = StringUtils.leftPad(Hex.encodeHexString(String.valueOf(seqno).getBytes()), 6, "0");

        String controlHeaderHexString = "0f0f0f" + lenString + "01" + seqnoString + "0f0f";

        try {
            byte[] controlHeaderBytes = Hex.decodeHex(controlHeaderHexString.toCharArray());

            byte[] allBytes = ArrayUtils.addAll(controlHeaderBytes, msg);
            String sn = "";
            if (allBytes.length > 18) {
                sn = telegramKeyUtil.getTelegramKey(msg);
            }
            //20190902
            //MatsudairaSyume
            // convert BIG5 to UTF8 on telegram log
//            faslog.debug(String.format(fasSendPtrn, allBytes.length, new String(allBytes, 12, allBytes.length - 12)));
            byte[] fastelbytes = new byte[allBytes.length - 12];
            System.arraycopy(allBytes, 12, fastelbytes, 0, allBytes.length - 12);
            faslog.debug(String.format(fasSendPtrn, allBytes.length, charcnv.BIG5bytesUTF8str(fastelbytes)));
                //----
            logger.debug("origi Hex.encodeHexString[" + Hex.encodeHexString(allBytes) + "]");
            if (logger.isDebugEnabled()) {
                List<String> hexLog = LogUtils.toLog(allBytes, isMask, ENCODE);
                logger.debug("TITA length[" + allBytes.length + "] SN:[" + sn + "]\r\n" + LogUtils.listToString(hexLog)
                        + "\r\n");
            } else {
                logger.info("TITA length[" + allBytes.length + "] SN:[" + sn + "]");
            }

            byte[] encryptHeaderAndBody = encrypt(allBytes);
            //20190316
            if(!subportStatus.isDisableKey()) {
            //----
                logger.debug("encrypt Hex.encodeHexString[" + Hex.encodeHexString(encryptHeaderAndBody) + "]");
                logger.info("TITA crypt length[" + encryptHeaderAndBody.length + "]");
                if (logger.isDebugEnabled()) {
                    List<String> hexLog = LogUtils.toLog(encryptHeaderAndBody, isMask, ENCODE);
                    logger.debug("TITA crypt length[" + encryptHeaderAndBody.length + "]\r\n"
                            + LogUtils.listToString(hexLog) + "\r\n");
                } else {
                    logger.info("TITA crypt length[" + encryptHeaderAndBody.length + "]");
                }
            //20190316
            }
            //----
            IOUtils.write(encryptHeaderAndBody, outputStream);
            outputStream.flush();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public boolean validate() {
//        return socket != null && socket.isClosed() == false && subportStatus.getAlive(localPort)
//                && subportStatus.getKeyStatus(localPort);
		return socket != null && socket.isClosed() == false && subportStatus.getAlive(localPort);
    }

    /**
     * @return the localAddress
     */
    public String getLocalAddress() {
        return localAddress;
    }

    /**
     * @return the serverAddress
     */
    public String getServerAddress() {
        return serverAddress;
    }

    /**
     * @return the localPort
     */
    public int getLocalPort() {
        return localPort;
    }

    /**
     * @return the serverPort
     */
    public int getServerPort() {
        return serverPort;
    }

    /**
     * @return the broadCastSender
     */
    public BroadCastSender getBroadCastSender() {
        return broadCastSender;
    }

    /**
     * @param broadCastSender
     *        the broadCastSender to set
     */
    public void setBroadCastSender(BroadCastSender broadCastSender) {
        this.broadCastSender = broadCastSender;
    }

    /**
     * @return the seqCounter
     */
    public AtomicInteger getSeqCounter() {
        return seqCounter;
    }

    /**
     * @param seqCounter
     *        the seqCounter to set
     */
    public void setSeqCounter(AtomicInteger seqCounter) {
        this.seqCounter = seqCounter;
    }

    /**
     * @return the telegramKeyUtil
     */
    public TelegramKeyUtil getTelegramKeyUtil() {
        return telegramKeyUtil;
    }

    /**
     * @param telegramKeyUtil
     *        the telegramKeyUtil to set
     */
    public void setTelegramKeyUtil(TelegramKeyUtil telegramKeyUtil) {
        this.telegramKeyUtil = telegramKeyUtil;
    }

    /**
     * @return the cdKey
     */
    public byte[] getCdKey() {
        return keyStore.getCdKey();
    }

    /**
     * @return the reTryInterval
     */
    public long getReTryInterval() {
        return reTryInterval;
    }

    /**
     * @param reTryInterval
     *        the reTryInterval to set
     */
    public void setReTryInterval(long reTryInterval) {
        this.reTryInterval = reTryInterval;
    }

	public int getConnectTimeout() {
		return connectTimeout;
	}

	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public int getReceiveBufferSize() {
		return receiveBufferSize;
	}

	public void setReceiveBufferSize(int receiveBufferSize) {
		this.receiveBufferSize = receiveBufferSize;
	}

    
}
