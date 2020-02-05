package com.systex.jbranch.host.landbank;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.pool.ObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;

import com.systex.jbranch.host.context.Context;
import com.systex.jbranch.host.server.HostGateway;
import com.systex.jbranch.host.util.TelegramKeyUtil;
import com.systex.jbranch.platform.host.transform.JMSGatewayInputVO;
import com.systex.jbranch.platform.host.transform.JMSGatewayOutputVO;
//20190325
import com.systex.jbranch.host.util.Dom4jtool;
//----

public class TelegramHostGateway implements HostGateway {

	private Logger logger = LoggerFactory.getLogger(TelegramHostGateway.class);

	@Autowired
	private TelegramKeyUtil telegramKeyUtil;

	public static final String $REQUEST_ID = "$REQUEST_ID";
	// 20190920 add for PROCAACHL PROCAAREQ
	public static final int PROCFASCHL = 0;
	public static final int PROCFUNDCHL = 1;
	public static final int PROCHEXACHL = 2;
	public static final int PROCAACHL = 3;
	public static final int PROCFASREQ = 4;
	public static final int PROCFUNDREQ = 5;
	public static final int PROCHEXAREQ = 6;
	public static final int PROCAAREQ = 7;
	public static final int UNKNOWNCHL = -1;
    //----
	// -------
	private ObjectPool telegramServicePool;
	// 20190121
	// Scott Hong
	private ObjectPool fundTelegramServicePool;
	private FundTelegramService fundTelegramService;
	// 20190325
	private ObjectPool hexaTelegramServicePool;
	private HexaTelegramService hexaTelegramService;
	// ----
	// ----------------
	private TelegramService telegramService;
	private String telegramKey;
	private int responseTimeout = 60 * 1000;// 毫秒
	private int interval = 200;// 毫秒
	private boolean hasAlive = false;
	private boolean hasKeyNomal = false;
	final public static String SCCESS = "0000";
	private long startTime = -1;
	private int listenerPort;
	private boolean isReceive = false;
	// 2019/01/21
	public static final int CHANNEL_BUFFER_SIZE = 10;
	// 20190325 default process fas channel
	private int channelUsed = PROCFASCHL;
	private Dom4jtool dj = new Dom4jtool();
	//20200205 mark
	//20191224
//	private boolean waitTOTA = true;
	//----
	// ---------

	//20190528
	private boolean fundtitaformaterr = false;
	//----
	/*
	 * Scott Hong for Malt-Channel
	 */
	public void send(Object content) throws Exception {
		startTime = System.currentTimeMillis();
		JMSGatewayInputVO inputVO = (JMSGatewayInputVO) content;
		// 20190318 modified for receive map request
		String chlSel = "";
		String reqId = "";
		if (inputVO.getRequestID() != null && inputVO.getRequestID().trim().length() > 0)
			reqId = inputVO.getRequestID();
		//20200205 mark
		/*
		this.waitTOTA = inputVO.isWaitTota();
		logger.debug("<><><><><>{}", this.waitTOTA);
		*/
		try {
			chlSel = new String(inputVO.getContent(), 0, CHANNEL_BUFFER_SIZE);
			if (logger.isDebugEnabled())
				logger.debug("chlSel = [{}] REQUEST_ID=[{}]", chlSel, reqId);
			byte[] actualContentByteary = new byte[inputVO.getContent().length - CHANNEL_BUFFER_SIZE];
			System.arraycopy(inputVO.getContent(), CHANNEL_BUFFER_SIZE, actualContentByteary, 0,
					inputVO.getContent().length - CHANNEL_BUFFER_SIZE);
			inputVO.setContent(actualContentByteary);
		} catch (Exception e) {
			e.printStackTrace();
			if (logger.isDebugEnabled()) {
				logger.debug("CHANNAL null or buffer to short REQUEST_ID=[{}]", reqId);
			}
			chlSel = "CHLERR";
		}
		// ----
		/*
		 * 20190920 add HEXA channel channelUsed : PROCHEXACHL channelUsed : PROCHEXAREQ : AA channelUsed : PROCAACHL PROCAAREQ : AA channelUsed 
		 */
		// 20190316 add receive map request
		if (chlSel.trim().equals("FAS") || chlSel.trim().equals("FUND") || chlSel.trim().equals("HEXA") || chlSel.trim().equals("AA")
				|| chlSel.trim().equals("FASREQ") || chlSel.trim().equals("FUNDREQ")
				|| chlSel.trim().equals("HEXAREQ")|| chlSel.trim().equals("AAREQ")) {
			// 20190315 modified for receive map request
//			byte[] actualContentByteary = new byte[inputVO.getContent().length - CHANNEL_BUFFER_SIZE];
//			System.arraycopy(inputVO.getContent(), CHANNEL_BUFFER_SIZE, actualContentByteary, 0,
//					inputVO.getContent().length - CHANNEL_BUFFER_SIZE);
//			inputVO.setContent(actualContentByteary);
			// --------
			if (chlSel.trim().equals("FAS")) {
				channelUsed = PROCFASCHL;
				fassend(content);
			} else if (chlSel.trim().equals("FUND")) {
				channelUsed = PROCFUNDCHL;
				fundsend(content);
			} else if (chlSel.trim().equals("HEXA")) {
				channelUsed = PROCHEXACHL;
				hexasend(content);
				//20190920
			} else if (chlSel.trim().equals("AA")) {
				channelUsed = PROCAACHL;
				hexasend(content);
				//----
			} else if (chlSel.trim().equals("FASREQ")) {
				// 20190412 FASREQ process the same as FAS
//				this.telegramKey = inputVO.getRequestID().trim();
				channelUsed = PROCFASREQ;
				fassend(content);
			} else if (chlSel.trim().equals("FUNDREQ")) {
				this.telegramKey = inputVO.getRequestID().trim();
				channelUsed = PROCFUNDREQ;
			} else if (chlSel.trim().equals("HEXAREQ")) {
				this.telegramKey = inputVO.getRequestID().trim();
				channelUsed = PROCHEXAREQ;
				//20190920
			} else if (chlSel.trim().equals("AAREQ")) {
				this.telegramKey = inputVO.getRequestID().trim();
				channelUsed = PROCAAREQ;
			}
			//----
		} else {
			channelUsed = UNKNOWNCHL;
			receive();
		}
	}

	/*
	 * 20190121 Scott Hong
	 */
	private void fassend(Object content) throws Exception {

		JMSGatewayInputVO inputVO = (JMSGatewayInputVO) content;
		MDC.put($REQUEST_ID, inputVO.getRequestID());
		logger.info("gateway fassend in:");

		byte[] requestBytes = inputVO.getContent();
		// ------
		telegramKey = telegramKeyUtil.getTelegramKey(requestBytes);
		logger.info("fassend send telegramKey=" + telegramKey);

		SubportStatus subportStatus = Context.getBean(SubportStatus.class);

		hasKeyNomal = subportStatus.hasKeyNomal();
		hasAlive = subportStatus.hasAlive();
		logger.info("fassend send hasKeyNomal=" + hasKeyNomal + ": hasAlive =" + hasAlive);

		if ((hasKeyNomal || subportStatus.isDisableSecurity()) && hasAlive) {
			try {

				telegramService = (TelegramService) telegramServicePool.borrowObject();
				listenerPort = telegramService.getLocalPort();
				// 20190412 add request Bytes length == 12
				isReceive = requestBytes.length == 8 || requestBytes.length == 15 || requestBytes.length == 12;

				if (telegramService != null && isReceive == false) {
					telegramService.send(requestBytes);
				}
			} finally {
				if (telegramService != null) {
					telegramServicePool.returnObject(telegramService);
				}
			}
		}
	}

	/*
	 * 20190121 Scott Hong
	 */
	private void fundsend(Object content) throws Exception {
		JMSGatewayInputVO inputVO = (JMSGatewayInputVO) content;
//		MDC.put($REQUEST_ID, inputVO.getRequestID());
		// 20190316
		//20190528
		this.fundtitaformaterr = false;
		if (inputVO.getRequestID() != null && inputVO.getRequestID().trim().length() > 0) {
			logger.info("gateway fundsend in REQUEST_ID=[{}]", inputVO.getRequestID());
			if (inputVO.getRequestID().trim().length() != telegramKeyUtil.getKeyLength()) {
				//20190528
				telegramKey = inputVO.getRequestID();
				//----
				logger.error("gateway fundsend in REQUEST_ID=[{}] format error", inputVO.getRequestID());
				//20190528
				this.fundtitaformaterr = true;
				//----
				return;
			}
		} else {
			logger.info("gateway fundsend in REQUEST_ID=[]");
		}
		// ----
		byte[] actualContentByteary = new byte[inputVO.getContent().length - 4];
		System.arraycopy(inputVO.getContent(), 4, actualContentByteary, 0, inputVO.getContent().length - 4);

		byte[] requestBytes = inputVO.getContent();
		// 20190316 use REQUEST_ID as telegram key if not null
		if (inputVO.getRequestID() != null && inputVO.getRequestID().trim().length() > 0)
			telegramKey = inputVO.getRequestID();
		else
			telegramKey = telegramKeyUtil.getTelegramKey(actualContentByteary);
		// ----
		logger.info("fundsend send telegramKey=" + telegramKey);
		//20190528
		if (inputVO.getContent().length != Integer.parseInt(new String(inputVO.getContent(), 0, 4))) {
			logger.error("gateway fundsend in REQUEST_ID=[{}] length [{}] format error", inputVO.getRequestID(), Integer.parseInt(new String(inputVO.getContent(), 0, 4)));
			this.fundtitaformaterr = true;
			return;
		}
		//----

		SubportStatus subportStatus = Context.getBean(SubportStatus.class);
		hasKeyNomal = subportStatus.hasKeyNomal();
		hasAlive = subportStatus.hasAlive();
		logger.info("fundsend send hasKeyNomal=" + hasKeyNomal + ": hasAlive =" + hasAlive);

		if (hasAlive) {
			try {
				fundTelegramService = (FundTelegramService) fundTelegramServicePool.borrowObject();
				listenerPort = fundTelegramService.getLocalPort();

				if (fundTelegramService != null) {
					// 20190316 add current time for hash table key
//					fundTelegramService.send(requestBytes);
					byte[] sendBytes = new byte[telegramKey.getBytes().length + requestBytes.length];
					System.arraycopy(telegramKey.getBytes(), 0, sendBytes, 0, telegramKey.getBytes().length);
					System.arraycopy(requestBytes, 0, sendBytes, telegramKey.getBytes().length, requestBytes.length);
//--
					fundTelegramService.send(sendBytes);
				}
			} finally {
				if (fundTelegramService != null) {
					fundTelegramServicePool.returnObject(fundTelegramService);
				}
			}
		}
	}

	/*
	 * 20190325 HEXA send
	 */
	private void hexasend(Object content) throws Exception {
		JMSGatewayInputVO inputVO = (JMSGatewayInputVO) content;
		// 20190408
		MDC.put($REQUEST_ID, inputVO.getRequestID());
		if (inputVO.getRequestID() != null && inputVO.getRequestID().trim().length() > 0) {
			logger.info("gateway hexasend in REQUEST_ID=[{}]", inputVO.getRequestID());
			if (inputVO.getRequestID().trim().length() != telegramKeyUtil.getKeyLength()) {
				logger.error("gateway hexasend in REQUEST_ID=[{}] format error", inputVO.getRequestID());
				return;
			}
		} else {
			logger.info("gateway hexasend in REQUEST_ID=[]");
		}
		byte[] requestBytes = inputVO.getContent();
		if (requestBytes == null || new String(requestBytes).trim().length() == 0) {
			isReceive = true;
			logger.error("gateway hexasend getContent() null or length == 0");
			return;
		}
		if (inputVO.getRequestID() != null && inputVO.getRequestID().trim().length() > 0)
			telegramKey = String.format("%-15s", inputVO.getRequestID());
		else {
			dj.loadXMLData(new String(requestBytes));
			telegramKey = String.format("%-15s", dj.getDataBySubPath("/RqXMLData/Header", "FrnMsgID", 0, ""));
		}
		if (telegramKey.trim().length() == 0) {
			isReceive = true;
			logger.error("gateway hexasend telegramKey length == 0");
			return;
		}

		logger.info("hexasend send telegramKey=" + telegramKey);

		SubportStatus subportStatus = Context.getBean(SubportStatus.class);

		hasKeyNomal = subportStatus.hasKeyNomal();
		hasAlive = subportStatus.hasAlive();
		logger.info("hexasend send hasKeyNomal=" + hasKeyNomal + ": hasAlive =" + hasAlive);

		if ((hasKeyNomal || subportStatus.isDisableSecurity()) && hasAlive) {
			try {

				hexaTelegramService = (HexaTelegramService) hexaTelegramServicePool.borrowObject();
				listenerPort = hexaTelegramService.getLocalPort();

				if (hexaTelegramService != null) {
					byte[] sendBytes = new byte[telegramKey.getBytes().length + requestBytes.length];
					System.arraycopy(telegramKey.getBytes(), 0, sendBytes, 0, telegramKey.getBytes().length);
					System.arraycopy(requestBytes, 0, sendBytes, telegramKey.getBytes().length, requestBytes.length);
//					hexaTelegramService.send(requestBytes);
					hexaTelegramService.send(sendBytes);
				}
			} finally {
				if (hexaTelegramService != null)
					hexaTelegramServicePool.returnObject(hexaTelegramService);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.systex.jbranch.host.server.HostGateway#receive()
	 */
	@SuppressWarnings("unchecked")
	public Object receive() throws Exception {
		//20200205 mark
		//20191224
		/*
		if (!this.waitTOTA) {
			String rid = "";
			if (MDC.get($REQUEST_ID) != null && MDC.get($REQUEST_ID).trim().length() > 0)
			MDC.get($REQUEST_ID);
			logger.info("no need to receive TOTA channelUsed=[{}]", rid);
			return receivenulltita();
		}
		*/
		//--------
		// 20190116, 20190315
		logger.info("receive() channelUsed=[{}]", channelUsed);
		// 20190325 change to using defined id
		if (channelUsed == PROCFASCHL || channelUsed == PROCFASREQ) {
//			channelUsed = 0;
			// ----
			return fasreceive();
			// 20190325 change to using defined id
		} else if (channelUsed == PROCFUNDCHL || channelUsed == PROCFUNDREQ) {
//			channelUsed = 0;
			// ----
			//20190528
			if (this.fundtitaformaterr)
			    return fundtitaformatreceive();
			else
				//----
			    return fundreceive();
			// 20190920 add for HEXA/AA
		} else if (channelUsed == PROCHEXACHL || channelUsed == PROCHEXAREQ || channelUsed == PROCAACHL || channelUsed == PROCAAREQ)
			//----
			return hexareceive();
		else {
			// ----
			long processTime = System.currentTimeMillis() - startTime;
			String code = "E900";
			String desc = "Channel Error";
			JMSGatewayOutputVO outputVO = new JMSGatewayOutputVO();
			// 20190315
			if (MDC.get($REQUEST_ID) != null && MDC.get($REQUEST_ID).trim().length() > 0)
				outputVO.setRequestID((String) MDC.get($REQUEST_ID));
			// ----
			outputVO.setCode(code);
			outputVO.setDesc(desc);
			outputVO.setProcessTime(processTime);
			outputVO.setHostName(InetAddress.getLocalHost().getHostName());
			logger.info("processTime={}", processTime);
			logger.info("gateway out");
			// 20190315
			channelUsed = UNKNOWNCHL;
			// 20190315
			return outputVO;
		}
	}

	/*
	 * Original receive
	 * 
	 * @SuppressWarnings("unchecked") public Object receive() throws Exception {
	 * 
	 * String code = SCCESS; String desc = ""; List<byte[]> receiveData = null;
	 * SubportStatus subportStatus = Context.getBean(SubportStatus.class); if
	 * ((hasKeyNomal || subportStatus.isDisableSecurity()) && hasAlive) {
	 * 
	 * long startTime = System.currentTimeMillis(); ReceiveHandler receiveHandler =
	 * Context.getBean(ReceiveHandler.class); receiveData =
	 * receiveHandler.remove(telegramKey);
	 * logger.info("hostGateway receive telegramKey=" + telegramKey); boolean isWait
	 * = true;
	 * 
	 * boolean isAlive = true; if (isReceive == false) {// 有發送tita時，才檢查subport是否斷線
	 * isAlive = subportStatus.getAlive(listenerPort); } while ((receiveData ==
	 * null) && isWait && isAlive) {
	 * 
	 * receiveData = receiveHandler.remove(telegramKey); try {
	 * Thread.sleep(interval); } catch (InterruptedException e) { // ignore } long
	 * now = System.currentTimeMillis(); if ((now - startTime) > responseTimeout) {
	 * isWait = false; code = "E001"; desc = "中心回應逾時[" + responseTimeout + "]ms";
	 * logger.warn("receive timeout telegramKey=" + telegramKey);
	 * receiveHandler.addTimeoutKey(telegramKey); continue; } if (isReceive ==
	 * false) {// 有發送tita時，才檢查subport是否斷線 isAlive =
	 * subportStatus.getAlive(listenerPort); } } if (isAlive == false) { code =
	 * "EABG001"; desc = "電文異常，請與資訊處連管科聯絡，並檢查該筆交易是否成功"; }
	 * 
	 * } else if (hasAlive == false) { code = "E002"; desc = "未與中心主機連線"; } else if
	 * (hasKeyNomal == false) { code = "E006"; desc = "與中心交換key異常"; }
	 * 
	 * if (SCCESS.equals(code) == false) { logger.info("code[{}], desc=[{}]", code,
	 * desc); }
	 * 
	 * JMSGatewayOutputVO outputVO = new JMSGatewayOutputVO();
	 * outputVO.setRequestID((String) MDC.get($REQUEST_ID));
	 * outputVO.setContent((List<byte[]>) receiveData); if (telegramService != null)
	 * { outputVO.setCdKey(telegramService.getCdKey()); } long processTime =
	 * System.currentTimeMillis() - startTime; outputVO.setCode(code);
	 * outputVO.setDesc(desc); outputVO.setProcessTime(processTime);
	 * outputVO.setHostName(InetAddress.getLocalHost().getHostName());
	 * logger.info("processTime={}", processTime); logger.info("gateway out");
	 * return outputVO; }
	 */
	/*
	 * 20191224 Scott Hong
	 */
	private Object receivenulltita() throws Exception {
		String code = SCCESS;
		String desc = "";
		List<byte[]> receiveData = null;
		SubportStatus subportStatus = Context.getBean(SubportStatus.class);
		boolean isAlive = subportStatus.getAlive(listenerPort);
	    hasAlive = false;
		JMSGatewayOutputVO outputVO = new JMSGatewayOutputVO();
		long processTime = System.currentTimeMillis() - startTime;
		outputVO.setRequestID((String) MDC.get($REQUEST_ID));
		outputVO.setCode(code);
		outputVO.setDesc(desc);
		outputVO.setProcessTime(processTime);
		outputVO.setHostName(InetAddress.getLocalHost().getHostName());
		logger.info("receive null processTime={}", processTime);
		logger.info("receive null gateway out");
		return outputVO;
	}

	/*
	 * 20190121 Scott Hong
	 */
	private Object fasreceive() throws Exception {

		String code = SCCESS;
		String desc = "";
		List<byte[]> receiveData = null;
		SubportStatus subportStatus = Context.getBean(SubportStatus.class);
		// 20190325 used defined channel id
		// 20190318 mark for PROCFAS and FASREQ is the same
//		if (channelUsed == PROCFASREQ)
//			hasAlive = true;
		// ----

		if ((hasKeyNomal || subportStatus.isDisableSecurity()) && hasAlive) {

			long startTime = System.currentTimeMillis();
			ReceiveHandler receiveHandler = Context.getBean(ReceiveHandler.class);
			receiveData = receiveHandler.remove(telegramKey);
			logger.info("fasreceive receive telegramKey=" + telegramKey);
			boolean isWait = true;

			boolean isAlive = true;
			// 20190325 used defined channel id
//			if (channelUsed != PROCFASREQ && isReceive == false) {// 有發送tita時，才檢查subport是否斷線
//				isAlive = subportStatus.getAlive(listenerPort);
//			}
			// 20190412 PROCFAS and PROCFASREQ is the same
			if (isReceive == false) {// 有發送tita時，才檢查subport是否斷線
				isAlive = subportStatus.getAlive(listenerPort);
			}
			while ((receiveData == null) && isWait && isAlive) {

				receiveData = receiveHandler.remove(telegramKey);
				try {
					Thread.sleep(interval);
				} catch (InterruptedException e) {
					// ignore
				}
				long now = System.currentTimeMillis();
				if ((now - startTime) > responseTimeout) {
					isWait = false;
					code = "E001";
					desc = "中心回應逾時[" + responseTimeout + "]ms";
					logger.warn("fasreceive timeout telegramKey=" + telegramKey);
					receiveHandler.addTimeoutKey(telegramKey);
					continue;
				}
//20190412 PROCFAS and PROCFASREQ is the same
//				if (channelUsed != PROCFASREQ && isReceive == false) {// 有發送tita時，才檢查subport是否斷線
//					isAlive = subportStatus.getAlive(listenerPort);
//				}
				if (isReceive == false) {// 有發送tita時，才檢查subport是否斷線
					isAlive = subportStatus.getAlive(listenerPort);
				}

			}
			if (isAlive == false) {
				code = "EABG001";
				desc = "電文異常，請與資訊處連管科聯絡，並檢查該筆交易是否成功";
			}

		} else if (hasAlive == false) {
			code = "E002";
			desc = "未與中心主機連線";
		} else if (hasKeyNomal == false) {
			code = "E006";
			desc = "與中心交換key異常";
		}

		if (SCCESS.equals(code) == false) {
			logger.info("fasreceive code[{}], desc=[{}]", code, desc);
		}

		JMSGatewayOutputVO outputVO = new JMSGatewayOutputVO();
		outputVO.setRequestID((String) MDC.get($REQUEST_ID));
		outputVO.setContent((List<byte[]>) receiveData);
		// -- 20190320
		int _list_no = 0;
		if (SCCESS.equals(code) != false)
			_list_no = receiveData.size();
		// ----
		// 20190325 not set CdKey if no security key
		if (telegramService != null && subportStatus.isDisableKey() == false) {
			outputVO.setCdKey(telegramService.getCdKey());
		}
		long processTime = System.currentTimeMillis() - startTime;
		outputVO.setCode(code);
		outputVO.setDesc(desc);
		outputVO.setProcessTime(processTime);
		outputVO.setHostName(InetAddress.getLocalHost().getHostName());
		logger.info("fasreceive processTime={}", processTime);
		logger.info("fasreceive gateway out total receive cnt=[{}]", _list_no);
		return outputVO;
	}

	//20190528
	private Object fundtitaformatreceive() throws Exception {
		SubportStatus subportStatus = Context.getBean(SubportStatus.class);
		JMSGatewayOutputVO outputVO = new JMSGatewayOutputVO();
		outputVO.setRequestID((String) telegramKey);

		outputVO.setContent(null);
		long processTime = System.currentTimeMillis() - startTime;
		outputVO.setCode("EABG001");
		outputVO.setDesc("傳送電文格式異常，請與資訊處連管科聯絡");
		outputVO.setProcessTime(processTime);
		outputVO.setHostName(InetAddress.getLocalHost().getHostName());
		logger.info("fasreceive processTime={}", processTime);
		logger.info("fasreceive gateway out total receive cnt=0");


		return outputVO;
	}
	//

	private Object fundreceive() throws Exception {
		String code = SCCESS;
		String desc = "";
		List<byte[]> receiveData = null;
		SubportStatus subportStatus = Context.getBean(SubportStatus.class);
		// 20190325 used defined channel id
		// 20190318
		if (channelUsed == PROCFUNDREQ)
			hasAlive = true;
		// ----
		if (hasAlive) {
			long startTime = System.currentTimeMillis();
			ReceiveHandler receiveHandler = Context.getBean(ReceiveHandler.class);
			receiveData = receiveHandler.remove(telegramKey);

			boolean isWait = true;

			boolean isAlive = true;
			if (channelUsed != PROCFUNDREQ) {// 有發送tita時，才檢查subport是否斷線
				isAlive = subportStatus.getAlive(listenerPort);
			}

			while ((receiveData == null) && isWait && isAlive) {

				receiveData = receiveHandler.remove(telegramKey);

				try {
					Thread.sleep(interval);
				} catch (InterruptedException e) {
					// ignore
				}
				long now = System.currentTimeMillis();
				if ((now - startTime) > responseTimeout) {
					isWait = false;
					code = "E001";
					desc = "AS/400回應逾時[" + responseTimeout + "]ms";
					logger.warn("fundreceive timeout telegramKey=" + telegramKey);
					receiveHandler.addTimeoutKey(telegramKey);
					continue;
				}
				if (channelUsed != PROCFUNDREQ)
					isAlive = subportStatus.getAlive(listenerPort);
			}
			// 20190528
			if (fundTelegramService != null && fundTelegramService.isRecvFormatErr()) {
				code = "EABG002";
				desc = "接收電文異常，請與資訊處連管科聯絡，並檢查該筆交易是否成功";

			} else if (isAlive == false) {
			//----
				code = "EABG001";
				desc = "電文異常，請與資訊處連管科聯絡，並檢查該筆交易是否成功";
			}
		} else if (hasAlive == false) {
			code = "E002";
			desc = "未與AS/400主機連線";
		}
		if (SCCESS.equals(code) == false) {
			logger.info("fundreceive code[{}], desc=[{}]", code, desc);
		}
		JMSGatewayOutputVO outputVO = new JMSGatewayOutputVO();
		// 20190312
//		if (MDC.get($REQUEST_ID) != null && ((String) MDC.get($REQUEST_ID)).trim().length() > 0)
//		    outputVO.setRequestID((String) MDC.get($REQUEST_ID));
		outputVO.setRequestID((String) telegramKey);
		// ----
		outputVO.setContent((List<byte[]>) receiveData);
		// -- 20190320
		int _list_no = 0;
		if (SCCESS.equals(code) != false)
			_list_no = receiveData.size();
		// ----
		long processTime = System.currentTimeMillis() - startTime;
		outputVO.setCode(code);
		outputVO.setDesc(desc);
		outputVO.setProcessTime(processTime);
		outputVO.setHostName(InetAddress.getLocalHost().getHostName());
		logger.info("fundreceive processTime={}", processTime);
		if (outputVO.getRequestID() != null && ((String) outputVO.getRequestID()).trim().length() > 0)
			logger.info("fundreceive gateway out REQUEST_ID=[{}] total receive cnt=[{}]", outputVO.getRequestID(),
					_list_no);
		else
			logger.info("fundreceive gateway out REQUEST_ID=[] total receive cnt=[{}]", _list_no);
		// 20190527
//		logger.info("========getResetSessIfRevTimeout====>"
//				+ fundTelegramService.getResetSessIfRevTimeout().trim().toUpperCase());
		if (code.equals("E001") && fundTelegramService != null
				&& fundTelegramService.getResetSessIfRevTimeout().trim().toUpperCase().equals("Y")) {
			String sa = fundTelegramService.getServerAddress();
			int sp = fundTelegramService.getServerPort();
			String la = fundTelegramService.getLocalAddress();
			int lp = fundTelegramService.getLocalPort();
			int ka = fundTelegramService.getKeepAliveTime();
			String rs = fundTelegramService.getResetSessIfRevTimeout();

			fundTelegramService.shutdownHandler();
			Thread.sleep(3000);
			fundTelegramService.setServerAddress(sa);
			fundTelegramService.setServerPort(sp);
			fundTelegramService.setLocalAddress(la);
			fundTelegramService.setLocalPort(lp);
			fundTelegramService.setKeepAliveTime(ka);
			fundTelegramService.setrunReceive(true);
			fundTelegramService.setResetSessIfRevTimeout(rs);

			fundTelegramService.init();
		}
		// --------
//		if (fundTelegramService != null)
//			fundTelegramService.setmapTelegramKey("");
		return outputVO;
	}

	// --------
	/*
	 * 20190325 Scott Hong
	 */
	private Object hexareceive() throws Exception {

		String code = SCCESS;
		String desc = "";
		List<byte[]> receiveData = null;
		SubportStatus subportStatus = Context.getBean(SubportStatus.class);
		if (channelUsed == PROCHEXAREQ)
			hasAlive = true;

		if ((hasKeyNomal || subportStatus.isDisableSecurity()) && hasAlive) {

			long startTime = System.currentTimeMillis();
			ReceiveHandler receiveHandler = Context.getBean(ReceiveHandler.class);
			receiveData = receiveHandler.remove(telegramKey);
			logger.info("hexareceive receive telegramKey=" + telegramKey);
			boolean isWait = true;

			boolean isAlive = false;
			// 20190412 PROCFAS ahd PROCFASREQ is the same
//			if (channelUsed != PROCFASREQ && isReceive == false) {// 有發送tita時，才檢查subport是否斷線
//				isAlive = subportStatus.getAlive(listenerPort);
//			}
			if (isReceive == false) {// 有發送tita時，才檢查subport是否斷線
				isAlive = subportStatus.getAlive(listenerPort);
			}
			while ((receiveData == null) && isWait && isAlive) {

				receiveData = receiveHandler.remove(telegramKey);
				try {
					Thread.sleep(interval);
				} catch (InterruptedException e) {
					// ignore
				}
				long now = System.currentTimeMillis();
				if ((now - startTime) > responseTimeout) {
					isWait = false;
					code = "E001";
					desc = "中心回應逾時[" + responseTimeout + "]ms";
					logger.warn("hexareceive timeout telegramKey=" + telegramKey);
					receiveHandler.addTimeoutKey(telegramKey);
					continue;
				}
				if (channelUsed != PROCHEXAREQ && isReceive == false) {// 有發送tita時，才檢查subport是否斷線
					isAlive = subportStatus.getAlive(listenerPort);
				}
			}
			if (isAlive == false) {
				code = "EABG001";
				desc = "電文異常，請與資訊處連管科聯絡，並檢查該筆交易是否成功";
			}

		} else if (hasAlive == false) {
			code = "E002";
			desc = "未與中心主機連線";
		} else if (hasKeyNomal == false) {
			code = "E006";
			desc = "與中心交換key異常";
		}

		if (SCCESS.equals(code) == false) {
			logger.info("hexareceive code[{}], desc=[{}]", code, desc);
		}

		JMSGatewayOutputVO outputVO = new JMSGatewayOutputVO();
		outputVO.setRequestID((String) MDC.get($REQUEST_ID));
//		outputVO.setRequestID((String) telegramKey);
		outputVO.setContent((List<byte[]>) receiveData);
		int _list_no = 0;
		if (SCCESS.equals(code) != false)
			_list_no = receiveData.size();
		if (telegramService != null && subportStatus.isDisableKey() == false)
			outputVO.setCdKey(telegramService.getCdKey());
		long processTime = System.currentTimeMillis() - startTime;
		outputVO.setCode(code);
		outputVO.setDesc(desc);
		outputVO.setProcessTime(processTime);
		outputVO.setHostName(InetAddress.getLocalHost().getHostName());
		logger.info("hexareceive processTime={} RequestID={}", processTime, (String) MDC.get($REQUEST_ID));
		logger.info("hexareceive gateway out total receive cnt=[{}]", _list_no);
		return outputVO;
	}
	// ----

	/**
	 * @return the telegramService
	 */
	public TelegramService getTelegramService() {
		return telegramService;
	}

	/**
	 * @param telegramService the telegramService to set
	 */
	public void setTelegramService(TelegramService telegramService) {
		this.telegramService = telegramService;
	}

	/**
	 * @return the responseTimeout
	 */
	public int getResponseTimeout() {
		return responseTimeout;
	}

	/**
	 * @param responseTimeout the responseTimeout to set
	 */
	public void setResponseTimeout(int responseTimeout) {
		this.responseTimeout = responseTimeout;
	}

	/**
	 * @return the interval
	 */
	public int getInterval() {
		return interval;
	}

	/**
	 * @param interval the interval to set
	 */
	public void setInterval(int interval) {
		this.interval = interval;
	}

	/**
	 * @return the telegramServicePool
	 */
	public ObjectPool getTelegramServicePool() {
		return telegramServicePool;
	}

	/**
	 * @param telegramServicePool the telegramServicePool to set
	 */
	public void setTelegramServicePool(ObjectPool telegramServicePool) {
		this.telegramServicePool = telegramServicePool;
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

	// 20190121
	// Scott Hong

	/**
	 * @return the fundTelegramServicePool
	 */
	public ObjectPool getFundTelegramServicePool() {
		return fundTelegramServicePool;
	}

	/**
	 * @param fundtelegramServicePool the fundTelegramServicePool to set
	 */
	public void setFundTelegramServicePool(ObjectPool fundTelegramServicePool) {
		this.fundTelegramServicePool = fundTelegramServicePool;
	}
	// -----------------

	/**
	 * @return the fundTelegramService
	 */
	public FundTelegramService getFundTelegramService() {
		return fundTelegramService;
	}

	/**
	 * @param fundTelegramService the fundTelegramService to set
	 */
	public void setFundTelegramService(FundTelegramService fundTelegramService) {
		this.fundTelegramService = fundTelegramService;
	}

	// 20190325
	/**
	 * @return the hexaTelegramServicePool
	 */
	public ObjectPool getHexaTelegramServicePool() {
		return hexaTelegramServicePool;
	}

	/**
	 * @param hexatelegramServicePool the hexaTelegramServicePool to set
	 */
	public void setHexaTelegramServicePool(ObjectPool hexaTelegramServicePool) {
		this.hexaTelegramServicePool = hexaTelegramServicePool;
	}
	// -----------------

	/**
	 * @return the hexaTelegramService
	 */
	public HexaTelegramService getHexaTelegramService() {
		return hexaTelegramService;
	}

	/**
	 * @param hexaTelegramService the hexaTelegramService to set
	 */
	public void setHexaTelegramService(HexaTelegramService hexaTelegramService) {
		this.hexaTelegramService = hexaTelegramService;
	}
	// ----
}
