/**
 * 20190114
 * Scott Hong
 *  Fund Telegram service main program
 */
package com.systex.jbranch.host.landbank;

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
//import org.apache.commons.lang.StringUtils;
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
//import com.systex.jbranch.platform.host.transform.JMSGatewayOutputVO;

/**
 * @author 1800389 Scott Hong 2019/1/10 main Telegram Service program form
 *         Funding connect to AS/400
 *
 */
public class FundTelegramService {
	// ------------------------------ FIELDS ------------------------------
	@Autowired
	private TelegramKeyUtil telegramKeyUtil;

	@Autowired
	private SubportStatus subportStatus;

	@Autowired
	private ReceiveHandler receiveHandler;

	private static final String ENCODE = "UTF-8";
	private static final int CONTROL_BUFFER_SIZE = 4;
	private InputStream inputStream;
	private OutputStream outputStream;
	private byte[] buffer;
	private Socket socket;
	private KeyStore keyStore;
	private Logger logger = LoggerFactory.getLogger(FundTelegramService.class);
	private Logger fundlog = LoggerFactory.getLogger("fundlog");
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
	private String fundSendPtrn = "-->FUND sn:[%15s] len %4d :[%s]";
	private String fundRecvPtrn = "<--FUND sn:[%15s] len %4d :[%s]";
	// 20190316 add for internal hashtable key
	private String mapTelegramKey = "";
	//
	// 20190403 Keepalive
	private static final int MAXRETRYTIME = 3;
	private int keepAliveTime = -1;
	private int reTrytime = 0;
	// ----
	// 20190527
	private String resetSessIfRevTimeout = "";
	private boolean recvFormatErr = false;

	// ----
	//20190902
	private CharsetCnv charcnv = new CharsetCnv();
	//

	public FundTelegramService() {

	}

	public FundTelegramService(String serverAddress, int serverPort, String localAddress, int localPort,
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
		subportStatus.setAlive(this.localPort, true);

		seqNoFile = new File("FUNDSEQNO", "FUNDSEQNO_" + localPort);
		boolean neetReTry = true;
		do {
			try {
				bindToHost();
				neetReTry = false;
				init2();
			} catch (Exception e) {
				subportStatus.setAlive(this.localPort, false);
				logger.error(e.getMessage(), e);
				logger.info("FundTelegramService bind error wait [" + reTryInterval + "]ms reTry...", e);
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
//		logger.error("=============>>>runReceive=" + runReceive);
		while (runReceive) {
			try {
				if (inputStream == null) {
					logger.info("inputStream == null reTryInterval");
					Thread.sleep(reTryInterval);
					continue;
				}

				buffer = new byte[CONTROL_BUFFER_SIZE];

				// 20190403
//				logger.info("00000=start to read keepAliveTime=" + this.keepAliveTime + ": mapTelegramKey=" + this.mapTelegramKey);
				int realControlHeaderSize = inputStream.read(buffer);
//				logger.info("2222=after pass timeout check read keepAliveTime=" + this.keepAliveTime + ": mapTelegramKey=" + this.mapTelegramKey);

				// ----
				// read header
				if (realControlHeaderSize < CONTROL_BUFFER_SIZE) {
					needVerify = true;
					String errMsg = "FundTelegramService receive control header length error expect, "
							+ CONTROL_BUFFER_SIZE + " byte，real[{}]";
					logger.error(errMsg, realControlHeaderSize);
					logger.error(
							"FundTelegramService receive control header data[" + Hex.encodeHexString(buffer) + "]");
					int remainSize = CONTROL_BUFFER_SIZE - realControlHeaderSize;
					byte[] secondBuffer = new byte[remainSize];
					int secondRealControlHeaderSize = inputStream.read(secondBuffer);
					if (secondRealControlHeaderSize != remainSize) {
						logger.error(errMsg, secondRealControlHeaderSize);
						logger.error(
								"FundTelegramService receive control header data[" + Hex.encodeHexString(buffer) + "]");
						throw new RuntimeException("Fund receive control header error");
					}
					logger.info("FundTelegramService second receive control header complete");
					buffer = ArrayUtils.addAll(buffer, secondBuffer);
				}

				String controlHeaderString = new String(buffer, StandardCharsets.UTF_8);
				if (needVerify) {
					needVerify = false;
					if (controlHeaderString.matches("[0-9]+") == false) {
						throw new RuntimeException("FundTelegramService verify control header error");
					}
					logger.info("FundTelegramService verify control header successful");
				}

				int contentSize = Integer.parseInt(new String(buffer, StandardCharsets.UTF_8));// header.getLength()

				logger.info("FundTelegramService contentSize=" + contentSize);
				byte[] bufferBody = new byte[0];
				boolean isLengthError = false;
				// 20190528
				this.setRecvFormatErr(false);
				// ----
				// read body
				if (contentSize - CONTROL_BUFFER_SIZE > 0) {

					bufferBody = new byte[contentSize - CONTROL_BUFFER_SIZE];
					logger.info("FundTelegramService read body size=" + bufferBody.length);
					int realContentSize = inputStream.read(bufferBody);

					logger.info("FundTelegramService realContentSize=" + realContentSize);
					if (realContentSize != (contentSize - CONTROL_BUFFER_SIZE)) {
						String errMsg = "FundTelegramService receive length error，expect["
								+ (contentSize - CONTROL_BUFFER_SIZE) + "]，real[" + realContentSize + "]";
						logger.error(errMsg);
						logger.error("FundTelegramService TOTA origi data[" + Hex.encodeHexString(bufferBody) + "]");
						// 20190528
						if (this.resetSessIfRevTimeout.trim().toUpperCase().equals("Y") && realContentSize > 0) {
							logger.error("FundTelegramService TOTA origi len=" + realContentSize + " source data["
									+ new String(bufferBody) + "]");
							isLengthError = true;
							this.setRecvFormatErr(true);
						} else {
							// ----
							needVerify = true;
							int remainSize = contentSize - CONTROL_BUFFER_SIZE - realContentSize;
							byte[] secondBufferBody = new byte[remainSize];
							int secondRealContentSize = inputStream.read(secondBufferBody);
							if (secondRealContentSize != remainSize) {
								errMsg = "FundTelegramService second receive length error，expect[" + remainSize
										+ "]，real[" + secondRealContentSize + "]";
								logger.error(errMsg);
								logger.error("FundTelegramService second TOTA origi data["
										+ Hex.encodeHexString(bufferBody) + "]");
								isLengthError = true;
								throw new RuntimeException("FundTelegramService second receive length error");
							}
							logger.info("FundTelegramService second receive length complete");
							bufferBody = ArrayUtils.addAll(bufferBody, secondBufferBody);
							realContentSize = realContentSize + secondRealContentSize;
							// 20190528
						}
						// ----
					}
				}
//20190528
				byte[] source = null;
				if (!this.isRecvFormatErr()) {
					// ----

//				byte[] source = ArrayUtils.addAll(buffer, bufferBody);
					source = ArrayUtils.addAll(buffer, bufferBody);

					printTotaOrigi(source, this.mapTelegramKey);
					// 20190315 expend key to 20 bytes
//				byte[] payLoad = new byte[telegramKeyUtil.getKeyLength()];
//				System.arraycopy(source, CONTROL_BUFFER_SIZE, payLoad, 0, telegramKeyUtil.getKeyLength());

//				String telegramKey = telegramKeyUtil.getTelegramKey(payLoad);
//				byte[] bytes = ArrayUtils.subarray(actualContentByteary, 0, 20);
					if (source.length > 25) {
						byte[] chkbytes = new byte[12];
						System.arraycopy(source, CONTROL_BUFFER_SIZE, chkbytes, 0, chkbytes.length);
						if (new String(chkbytes).equals("AL4I  SOCKET")
								|| new String(chkbytes).equals("*DATALENERR-")) {
							logger.debug("ignore AS/400 protocol control message");
							continue;
						}
					}
					// 20190316 modify for using mapTelegramKey
//				byte[] payLoad ;
//				String telegramKey = Hex.encodeHexString(payLoad).replace("f", "");

//				printTotaDecrypto(source, telegramKey);
					// 2090316
					if (!subportStatus.isDisableKey())
						// ----
						printTotaDecrypto(source, this.mapTelegramKey);
					// 20190928
				}
				// ----
//				List<byte[]> list = receiveHandler.remove(telegramKey);
				List<byte[]> list = receiveHandler.remove(this.mapTelegramKey);
				// --

				if (list == null) {
					list = new ArrayList<byte[]>();
				}
				// 20190528
				if (!this.isRecvFormatErr())
					// ----
					list.add(source);
				// 20190316 use maoTelegramKey
//				putData(isLengthError, source, telegramKey, list);
				putData(isLengthError, source, this.mapTelegramKey, list);
				// ----
				// 20190403
			} catch (Exception e) {
				try {
					if (runReceive) {
						// 20190403
						if (e instanceof java.net.SocketTimeoutException) {
							boolean tmpPutKeepAlive = false;
////							logger.info("timeout <<<<<<<<<<<<<<<<<>>>>>>>>>>>>>this.mapTelegramKey=" + this.mapTelegramKey.trim());
							if (this.mapTelegramKey.trim().length() == 0)
								tmpPutKeepAlive = true;
							else {
								List<byte[]> chkist = receiveHandler.remove(this.mapTelegramKey);
								this.reTrytime += 1;
								if ((chkist == null || chkist.size() == 0) && (this.reTrytime > MAXRETRYTIME)) {
//									logger.info("=====> SN:[{}] list is null already been read or no response exceed the waiting time just clear internal mapTelegramKey reTrytime=[{}]", this.mapTelegramKey,this.reTrytime);
									this.mapTelegramKey = "";
									tmpPutKeepAlive = true;
									this.reTrytime = 0;
								} else { // not yet been read by queue just put back keep receiving from remote
									if (chkist != null)
										receiveHandler.put(this.mapTelegramKey, chkist);
//									else
//										logger.info("wait reTrytime[{}]",this.reTrytime);
								}
							}
							if (tmpPutKeepAlive) {
								// 20190527
								this.mapTelegramKey = "";
								// ----
								IOUtils.write("0010*ALIVE".getBytes(), outputStream);
								outputStream.flush();
								logger.info("==>send single way 0010*ALIVE");
							}
							continue;
						} else {
							// ----
							logger.error(e.getMessage(), e);
							subportStatus.setAlive(this.localPort, false);
							bindToHost();
							init2();
							// 20190403
							// 20190609
							subportStatus.setAlive(this.localPort, true);
							// ----

						}
						// ----
					} else {
						logger.info("FundTelegramService Socket closed");
					}

				} catch (Exception e1) {
					subportStatus.setAlive(this.localPort, false);
					logger.error(e1.getMessage(), e1);
					logger.info("FundTelegramService bind error wait [" + reTryInterval + "]ms reTry...", e);
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
	private void printTotaOrigi(byte[] source, String telegramKey) throws UnsupportedEncodingException, Exception {
		// 20190316
//		fundlog.debug(String.format(fundRecvPtrn, telegramKey, source.length, new String(source)));
		// ----
		//20190902
		//MatsudairaSyume
		// convert BIG5 to UTF8 for telegram log
		fundlog.debug(String.format(fundRecvPtrn, telegramKey, source.length, charcnv.BIG5bytesUTF8str(source)));
		//
		if (logger.isDebugEnabled()) {
			List<String> hexLog = LogUtils.toLog(source, isMask, ENCODE);
			logger.debug("FundTelegramService TOTA origi length[" + source.length + "] \r\n"
					+ LogUtils.listToString(hexLog) + "\r\n");
			return;
		}
		logger.info("FundTelegramService TOTA origi length[" + source.length + "]");
	}

	private void printTotaDecrypto(byte[] source, String telegramKey) throws UnsupportedEncodingException {
		// fundlog.debug(String.format(fundRecvPtrn, telegramKey, source.length, new
		// String(source)));
		if (logger.isDebugEnabled()) {
			List<String> hexLog = LogUtils.toLog(source, isMask, ENCODE);
			logger.debug("FundTelegramService TOTA decrypto length[" + source.length + "] SN:[" + telegramKey + "]\r\n"
					+ LogUtils.listToString(hexLog) + "\r\n");
			return;
		}
		logger.info("FundTelegramService TOTA decrypto length[" + source.length + "] SN:[" + telegramKey + "]");
	}

	private void putData(boolean isLengthError, byte[] source, String telegramKey, List<byte[]> list)
			throws JMSException {
		logger.info("TOTA SN:[{}] list size=[{}]", telegramKey, list.size());

		receiveHandler.put(telegramKey, list);
	}

	public void close() throws IOException {
		runReceive = false;
		logger.info("FundTelegramService.close");
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
		// 20190122 test
		subportStatus.setKeyStatus(this.localPort, false);
		// 2019057
		socket = null;
		inputStream = null;
		outputStream = null;
		// ----
	}

	public void shutdownHandler() {
		try {
			close();
		} catch (IOException e) {
			logger.warn(e.getMessage(), e);
		}
		logger.info("FundTeleramService shutdown.");
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
				logger.info("FundTelegramService socket close");
			} catch (IOException e) {
				// ignore
			}
		}
		socket = new Socket();
		socket.setReceiveBufferSize(receiveBufferSize);
		logger.info("FundTelegramService receiveBufferSize={}", socket.getReceiveBufferSize());
		socket.setReuseAddress(true);
		socket.setSoLinger(true, 0);
		socket.bind(new InetSocketAddress(localAddress, localPort));
		socket.connect(new InetSocketAddress(serverAddress, serverPort), connectTimeout);
		// 20190403
		if (this.keepAliveTime > 0) {
			socket.setSoTimeout(this.keepAliveTime);
			logger.info("FundTelegramService single way keepAliveTime=[{}].", this.keepAliveTime);
		} else
			logger.info("FundTelegramService not use single way keepAliveTime.");
		// ----
		logger.info("FundTelegramService bind successful.");
		logger.info("FundTelegramService TcpNoDelay=[{}]", socket.getTcpNoDelay());
		logger.info("FundTelegramService TrafficClass=[{}]", socket.getTrafficClass());
		inputStream = socket.getInputStream();
		outputStream = socket.getOutputStream();
	}

	private void init2() throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("FundTelegramService begin init");
		}
		logger.debug("FundTelegramService seqNoFile local=" + seqNoFile.getAbsolutePath());
		if (seqNoFile.exists() == false) {
			File parent = seqNoFile.getParentFile();
			if (parent.exists() == false) {
				parent.mkdirs();
			}
			seqNoFile.createNewFile();
			FileUtils.writeStringToFile(seqNoFile, "0");
		}
		// 20190527
		this.mapTelegramKey = "";
		// ----
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
		// 20190316
//		logger.info("send start==========================");
//		byte[] msg = (byte[]) content;
		// ----
		byte[] tmpmsg = (byte[]) content;
		// 20190316
		this.mapTelegramKey = telegramKeyUtil.getTelegramKey2(tmpmsg);
		// ----
		byte[] msg = new byte[tmpmsg.length - telegramKeyUtil.getKeyLength()];
		System.arraycopy(tmpmsg, telegramKeyUtil.getKeyLength(), msg, 0, msg.length);
		int len = msg.length;
		tmpmsg = null;
//		System.gc();
		// 20190316
		logger.info("send len=[{}] mapTelegramKey=[{}]", len, this.mapTelegramKey);
		// ---

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
			byte[] allBytes = msg;
			byte[] allByteswithoutLen = new byte[msg.length - CONTROL_BUFFER_SIZE];
			System.arraycopy(msg, CONTROL_BUFFER_SIZE, allByteswithoutLen, 0, allByteswithoutLen.length);
			// 20190316 change to use maptelegramKey
			String sn = this.mapTelegramKey;
//			String sn = "";
//			if (allBytes.length > 19) {
//				sn = telegramKeyUtil.getTelegramKey(allByteswithoutLen);
//			}
			// ----
			//20190902
			//MatsudairaSyume
			// convert BIG5 to UTF8 on telegram log
//			fundlog.debug(String.format(fundSendPtrn, sn, allBytes.length, new String(allBytes)));
			fundlog.debug(String.format(fundSendPtrn, sn, allBytes.length, charcnv.BIG5bytesUTF8str(allBytes)));
			//----
			logger.debug("origi Hex.encodeHexString[" + Hex.encodeHexString(allBytes) + "]");
			if (logger.isDebugEnabled()) {
				List<String> hexLog = LogUtils.toLog(allBytes, isMask, ENCODE);
				logger.debug("TITA length[" + allBytes.length + "] SN:[" + sn + "]\r\n" + LogUtils.listToString(hexLog)
						+ "\r\n");
			} else {
				logger.info("TITA length[" + allBytes.length + "] SN:[" + sn + "]");
			}

			// 20190316
			if (!subportStatus.isDisableKey()) {
				byte[] encryptHeaderAndBody = encrypt(allBytes);
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
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public boolean validate() {

//		return socket != null && socket.isClosed() == false && subportStatus.getAlive(localPort)
//				&& (subportStatus.getKeyStatus(localPort) || subportStatus.isDisableSecurity());
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

	// 20190403
	public int getKeepAliveTime() {
		return keepAliveTime;
	}

	public void setKeepAliveTime(int keepAliveTime) {
		this.keepAliveTime = keepAliveTime;
	}
	// 20190527

	/**
	 * @param setrunReceive the runReceive to set
	 */

	public void setrunReceive(boolean runReceive) {
		this.runReceive = runReceive;
	}

	/**
	 * @param setmapTelegramKey the mapTelegramKey to set
	 */

	public void setmapTelegramKey(String mapTelegramKey) {
		this.mapTelegramKey = mapTelegramKey;
	}

	/**
	 * @return the resetSessIfRevTimeout
	 */

	public String getResetSessIfRevTimeout() {
		return resetSessIfRevTimeout;
	}

	/**
	 * @param setResetSessIfRevTimeou the resetSessIfRevTimeout to set
	 */

	public void setResetSessIfRevTimeout(String resetSessIfRevTimeout) {
		this.resetSessIfRevTimeout = resetSessIfRevTimeout;
	}

	public boolean isRecvFormatErr() {
		return recvFormatErr;
	}

	public void setRecvFormatErr(boolean recvFormatErr) {
		this.recvFormatErr = recvFormatErr;
	}

	// ----
}
