package com.systex.jbranch.host.landbank;
/**
 * @author Scott Hong 2019/9/20 Telegram Service for
 *         connect to HEXA and AA
 *         channelType == 0 (default for HEXA channel, > 0 for AA channel)
 */

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.jms.JMSException;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
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
import com.systex.jbranch.platform.host.transform.JMSGatewayOutputVO;
import com.systex.jbranch.host.util.Dom4jtool;

public class HexaTelegramService {
	// ------------------------------ FIELDS ------------------------------
	@Autowired
	private TelegramKeyUtil telegramKeyUtil;

	@Autowired
	private SubportStatus subportStatus;

	@Autowired
	private ReceiveHandler receiveHandler;

	private static final String ENCODE = "UTF-8";
	private static final int DEFAULT_BUFFER_SIZE = 65535;
	private InputStream inputStream;
	private OutputStream outputStream;
//	private byte[] buffer;
	private Socket socket;
	private KeyStore keyStore;
	private Logger logger = LoggerFactory.getLogger(HexaTelegramService.class);
	private Logger hexalog = LoggerFactory.getLogger("hexalog");
	//20190920
	private Logger aalog = LoggerFactory.getLogger("aalog");
	//----
	private String localAddress;
	private String serverAddress;
	private int localPort;
	private int serverPort;
	// 20190331
	private boolean runReceive = false;
	//20190920
	private int channelType = 0; // > 0 for AA channel else for HEXA channel
	//-----

	private File seqNoFile;
	private boolean isMask = true;

	private BroadCastSender broadCastSender;
	private AtomicInteger seqCounter = null;

	private long reTryInterval = 10000L; // 重新連線間隔

	private int connectTimeout = 10000;
	private Dom4jtool dj = new Dom4jtool();

	private int receiveBufferSize = DEFAULT_BUFFER_SIZE;
	private String hexaSendPtrn = "-->HEXA sn:[%15s] len %4d :[%s]";
	private String hexaRecvPtrn = "<--HEXA sn:[%15s] len %4d :[%s]";
	//20190920
	private String aaSendPtrn = "-->AA sn:[%15s] len %4d :[%s]";
	private String aaRecvPtrn = "<--AA sn:[%15s] len %4d :[%s]";
	//----
	private String TITA_MsgidParentTAG = "/RqXMLData/Header";
	private String MsgidTAG = "FrnMsgID";
	private String TOTA_MsgidParentTAG = "/RsXMLData/Header";
	private String TOTA_StatusParentTAG = "/RsXMLData/Header";
	private String StatusTAG = "StatusCode";
	//----
	private String mapTelegramKey = "";
	private String chkFrnMsgID = "";
	// --------------------------- CONSTRUCTORS ---------------------------
	// 20190902
	private CharsetCnv charcnv = new CharsetCnv();
	//

	public HexaTelegramService() {

	}

	public HexaTelegramService(String serverAddress, int serverPort, String localAddress, int localPort,
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
		// 20190331 排除broadcast
		subportStatus.setAlive(this.localPort, true);

		//20190920
		if (channelType != 0) {
			seqNoFile = new File("AASEQNO", "AASEQNO_" + localPort);
			TITA_MsgidParentTAG = "/IFX/Header";
			MsgidTAG = "ClientAppSeq";
			TOTA_MsgidParentTAG = "/IFX/Header";
			TOTA_StatusParentTAG = "/IFX/Header/Status";
		} else
			seqNoFile = new File("HEXASEQNO", "HEXASEQNO_" + localPort);
		boolean neetReTry = true;
		do {
			try {
//				bindToHost();
				neetReTry = false;
				init2();
			} catch (Exception e) {
				subportStatus.setAlive(this.localPort, false);
				logger.error(e.getMessage(), e);
				logger.info("bind error wait [" + reTryInterval + "]ms reTry...", e);
				Thread.sleep(reTryInterval);
			} catch (Throwable e) {
				subportStatus.setAlive(this.localPort, false);
				logger.error(e.getMessage(), e);
			}
		} while (neetReTry);

		Thread hook = new Thread(new Runnable() {
			public void run() {
				shutdownHandler();
			}
		});
		Runtime.getRuntime().addShutdownHook(hook);
		/*
		 * 20190331 new Thread() { public void run() { receiveLoop(); } }.start();
		 */
	}

	public void putMDC() {
		MDC.put("SERVER_ADDRESS", serverAddress);
		MDC.put("SERVER_PORT", String.valueOf(serverPort));
		MDC.put("LOCAL_ADDRESS", localAddress);
		MDC.put("LOCAL_PORT", String.valueOf(localPort));
	}

	public void receiveLoop() {
		byte[] bufferBody = new byte[this.receiveBufferSize];
		while (runReceive) {
			try {
				if (inputStream == null) {
					Thread.sleep(reTryInterval);
					continue;
				}
//				if (broadCastSender != null && seqCounter != null)
//					MDC.put(TelegramHostGateway.$REQUEST_ID, this.localAddress + "_" + seqCounter.getAndIncrement());
//				buffer = new byte[0];
				int contentSize = 0;
				boolean isLengthError = false;
				if ((contentSize = inputStream.read(bufferBody)) < 0) {
					logger.info("read body size=" + bufferBody.length);
					int realContentSize = inputStream.read(bufferBody);
					logger.info("recive realContentSize=" + realContentSize);
					if (realContentSize <= 0) {
						isLengthError = true;
						logger.error("recive again error realContentSize=" + realContentSize);
					} else {
						isLengthError = false;
						contentSize = realContentSize;
					}
				}
				logger.info("read content size=" + contentSize);
				byte[] source = new byte[contentSize];
				System.arraycopy(bufferBody, 0, source, 0, contentSize);
				logger.info("read content =[{}]", new String(source));
				dj.loadXMLData(new String(source));
				//20190920
//				String frnMsgID = dj.getDataBySubPath("/RsXMLData/Header", "FrnMsgID", 0, "").trim();
//				String statusCode = dj.getDataBySubPath("/RsXMLData/Header", "StatusCode", 0, "").trim();
				String frnMsgID = dj.getDataBySubPath(TOTA_MsgidParentTAG, MsgidTAG, 0, "").trim();
				String statusCode = dj.getDataBySubPath(TOTA_StatusParentTAG, StatusTAG, 0, "").trim();
				//----
				if ((frnMsgID.length() <= 0) && (statusCode.equals("0000") == false)) {
					logger.debug("receive error Telegram get StatusCode [{}] and no FrnMsgID", statusCode);
				}
//				logger.debug("src=[{}] key=[{}]",new String(source), this.mapTelegramKey);
				printTotaOrigi(source, this.mapTelegramKey);

				List<byte[]> list = receiveHandler.remove(this.mapTelegramKey);

				if (list == null) {
					list = new ArrayList<byte[]>();
				}

				list.add(source);
				putData(isLengthError, source, this.mapTelegramKey, list);
				close();
				this.mapTelegramKey = "";
			} catch (Exception e) {
				try {
					if (runReceive) {
						logger.error(e.getMessage(), e);
						////
						close();
						this.mapTelegramKey = "";
						////
/////						subportStatus.setAlive(this.localPort, false);
//						bindToHost();
//						init2();
					} else {
						logger.info("Socket closed");
					}

				} catch (Exception e1) {
/////					subportStatus.setAlive(this.localPort, false);
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

	// 20190902
	// MatsudairaSyume
	// add Exception for charcnv.BIG5bytesUTF8str()
	private void printTotaOrigi(byte[] source, String telegramkey) throws UnsupportedEncodingException, Exception {
		// 20190316
//		hexalog.debug(String.format(hexaRecvPtrn, telegramkey, source.length, new String(source)));
		// 20190902
		// MatsudairaSyume
		// convert BIG5 to UTF8 for telegram log
		//20190920
		if (channelType != 0)
			aalog.debug(String.format(aaRecvPtrn, telegramkey, source.length, charcnv.BIG5bytesUTF8str(source)));
		else
		//----
			hexalog.debug(String.format(hexaRecvPtrn, telegramkey, source.length, charcnv.BIG5bytesUTF8str(source)));
		// ----
		if (logger.isDebugEnabled()) {
			List<String> hexLog = LogUtils.toLog(source, isMask, ENCODE);
			logger.debug("TOTA origi length[" + source.length + "] \r\n" + LogUtils.listToString(hexLog) + "\r\n");
			return;
		}
		logger.info("TOTA origi length[" + source.length + "]");
	}

	private void putData(boolean isLengthError, byte[] source, String telegramKey, List<byte[]> list)
			throws JMSException {
		receiveHandler.put(telegramKey, list);
	}

	public void close() throws IOException {
		runReceive = false;
		logger.info("HexaTelegramService.close");
		if (inputStream != null) {
			inputStream.close();
		}
		if (outputStream != null) {
			outputStream.close();
		}
		if (socket != null) {
			socket.close();
		}
//		subportStatus.setAlive(this.localPort, false);
	}

	public void shutdownHandler() {
		try {
			close();
		} catch (IOException e) {
			logger.warn(e.getMessage(), e);
		}
		logger.info("HexaTeleramService shutdown.");
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
				logger.info("HexaTelegramService socket close");
			} catch (IOException e) {
				// ignore
			}
		}
		socket = new Socket();
		socket.setReceiveBufferSize(receiveBufferSize);
		logger.info("HexaTelegramService receiveBufferSize={}", socket.getReceiveBufferSize());
		socket.setReuseAddress(true);
		socket.setSoLinger(true, 0);
		socket.bind(new InetSocketAddress(localAddress, localPort));
		socket.connect(new InetSocketAddress(serverAddress, serverPort), connectTimeout);
		logger.info("HexaTelegramService bind successful.");
		logger.info("HexaTelegramService TcpNoDelay=[{}]", socket.getTcpNoDelay());
		logger.info("HexaTelegramService TrafficClass=[{}]", socket.getTrafficClass());
		inputStream = socket.getInputStream();
		outputStream = socket.getOutputStream();
	}

	private void init2() throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("HexaTelegramService begin init");
		}
		logger.debug("HexaTelegramService seqNoFile local=" + seqNoFile.getAbsolutePath());
		if (seqNoFile.exists() == false) {
			File parent = seqNoFile.getParentFile();
			if (parent.exists() == false) {
				parent.mkdirs();
			}
			seqNoFile.createNewFile();
			FileUtils.writeStringToFile(seqNoFile, "0");
		}
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

	public void send(Object content) throws Exception {
		runReceive = true;
		bindToHost();
		byte[] tmpmsg = (byte[]) content;
		this.mapTelegramKey = telegramKeyUtil.getTelegramKey2(tmpmsg);
		byte[] msg = new byte[tmpmsg.length - telegramKeyUtil.getKeyLength()];
		System.arraycopy(tmpmsg, telegramKeyUtil.getKeyLength(), msg, 0, msg.length);
		int len = msg.length;
		tmpmsg = null;

		len = msg.length;
		logger.info("send len=" + len);

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

		try {
			dj.loadXMLData(new String(msg));
//20190920
//			this.chkFrnMsgID = dj.getDataBySubPath("/RqXMLData/Header", "FrnMsgID", 0, "");
			this.chkFrnMsgID = dj.getDataBySubPath(TITA_MsgidParentTAG, MsgidTAG, 0, "");
			//----
			String sn = this.mapTelegramKey;
			// 20190902
			// MatsudairaSyume
			// convert BIG5 to UTF8 on telegram log
//			hexalog.debug(String.format(hexaSendPtrn, sn, msg.length, new String(msg)));
			//20190920
			if (channelType != 0)
				aalog.debug(String.format(aaSendPtrn, sn, msg.length, charcnv.BIG5bytesUTF8str(msg)));
			else
			//----
				hexalog.debug(String.format(hexaSendPtrn, sn, msg.length, charcnv.BIG5bytesUTF8str(msg)));
			// ----
			logger.debug("origi Hex.encodeHexString[" + Hex.encodeHexString(msg) + "]");
			if (logger.isDebugEnabled()) {
				List<String> hexLog = LogUtils.toLog(msg, isMask, ENCODE);
				logger.debug(
						"TITA length[" + msg.length + "] SN:[" + sn + "]\r\n" + LogUtils.listToString(hexLog) + "\r\n");
			} else {
				logger.info("TITA length[" + msg.length + "] SN:[" + sn + "]");
			}

			if (!subportStatus.isDisableKey()) {
				byte[] encryptHeaderAndBody = encrypt(msg);
				logger.debug("encrypt Hex.encodeHexString[" + Hex.encodeHexString(encryptHeaderAndBody) + "]");
				logger.info("TITA crypt length[" + encryptHeaderAndBody.length + "]");
				if (logger.isDebugEnabled()) {
					List<String> hexLog = LogUtils.toLog(encryptHeaderAndBody, isMask, ENCODE);
					logger.debug("TITA crypt length[" + encryptHeaderAndBody.length + "]\r\n"
							+ LogUtils.listToString(hexLog) + "\r\n");
				} else
					logger.info("TITA crypt length[" + encryptHeaderAndBody.length + "]");
			}
			// ----
			IOUtils.write(msg, outputStream);
			outputStream.flush();
			receiveLoop();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			close();
		}
	}

	public boolean validate() {
		return subportStatus.getAlive(localPort);
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
	 * @param broadCastSender the broadCastSender to set
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
	 * @param seqCounter the seqCounter to set
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
	 * @param telegramKeyUtil the telegramKeyUtil to set
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
	 * @param reTryInterval the reTryInterval to set
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

	//20190920
	public int getChannelType() {
		return channelType;
	}

	public void setChannelType(int channelType) {
		this.channelType = channelType;
	}
	//--------

}
