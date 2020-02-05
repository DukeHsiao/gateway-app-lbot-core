package com.systex.jbranch.host.landbank;

/**
 * 20190114
 * Scott Hong
 *  Fund Telegram spooling main program
 */

import java.net.InetAddress;
import java.util.List;

import org.apache.commons.pool.ObjectPool;
//import org.apache.commons.pool.impl.GenericObjectPool;  

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;

import com.systex.jbranch.host.context.Context;
import com.systex.jbranch.host.server.HostGateway;
import com.systex.jbranch.host.util.TelegramKeyUtil;
import com.systex.jbranch.platform.host.transform.JMSGatewayInputVO;
import com.systex.jbranch.platform.host.transform.JMSGatewayOutputVO;


public class FundTelegramHostGateway implements HostGateway {

	private Logger logger = LoggerFactory.getLogger(FundTelegramHostGateway.class);

	@Autowired
	private TelegramKeyUtil telegramKeyUtil;
	public static final String $REQUEST_ID = "$REQUEST_ID";

	private ObjectPool fundTelegramServicePool;
	private FundTelegramService fundTelegramService;
	private String telegramKey;
	private int responseTimeout = 60 * 1000;// 毫秒
	private int interval = 200;// 毫秒
	private boolean hasAlive = false;
	private boolean hasKeyNomal = false;
	final public static String SCCESS = "0000";
	private long startTime = -1;
	private int listenerPort;
  	private final static int CHANNEL_BUFFER_SIZE = 10;


	@Override
	public void send(Object content) throws Exception {
		startTime = System.currentTimeMillis();
		JMSGatewayInputVO inputVO = (JMSGatewayInputVO) content;
	   String chlSel = new String(inputVO.getContent(), 0, FundTelegramHostGateway.CHANNEL_BUFFER_SIZE);

		logger.info("Fund gateway in");
    	MDC.put($REQUEST_ID, inputVO.getRequestID());
		byte[] actualContentByteary = new byte[inputVO.getContent().length - FundTelegramHostGateway.CHANNEL_BUFFER_SIZE];
    	System.arraycopy(inputVO.getContent(), FundTelegramHostGateway.CHANNEL_BUFFER_SIZE, actualContentByteary, 0,
				inputVO.getContent().length - FundTelegramHostGateway.CHANNEL_BUFFER_SIZE);
    	byte[] requestBytes = actualContentByteary;

		telegramKey = telegramKeyUtil.getTelegramKey(requestBytes);
		logger.info("FundTelegramHostGateway channel[" + chlSel + "] send telegramKey=" + telegramKey);

		SubportStatus subportStatus = Context.getBean(SubportStatus.class);
		logger.info("FundTelegramHostGateway subportStatus=" + subportStatus);
		hasKeyNomal = subportStatus.hasKeyNomal();
		hasAlive = subportStatus.hasAlive();
		logger.info("FundTelegramHostGateway send hasKeyNomal=" + hasKeyNomal + ": hasAlive =" + hasAlive);

		if (hasAlive) {
			try {
				logger.info("FundTelegramHostGateway 1:" + fundTelegramServicePool.getNumActive() + ":" + fundTelegramServicePool.getNumActive());

				fundTelegramService = (FundTelegramService) fundTelegramServicePool.borrowObject();
				logger.info("FundTelegramHostGateway 2");
				listenerPort = fundTelegramService.getLocalPort();

				if (fundTelegramService != null) {
					fundTelegramService.send(requestBytes);
				}
			} finally {
				if (fundTelegramService != null) {
					fundTelegramServicePool.returnObject(fundTelegramService);
					logger.debug("FundTelegramHostGateway borrow after numIdle={}",
							fundTelegramServicePool.getNumIdle() + ",activeIdle=" + fundTelegramServicePool.getNumActive());
				}
			}
		}
	}

	@Override
	public Object receive() throws Exception {
		String code = SCCESS;
		String desc = "";
		List<byte[]> receiveData = null;
		SubportStatus subportStatus = Context.getBean(SubportStatus.class);
		if (hasAlive) {
			long startTime = System.currentTimeMillis();
			ReceiveHandler receiveHandler = Context.getBean(ReceiveHandler.class);
			receiveData = receiveHandler.remove(telegramKey);
			logger.info("FundTelegramHostGateway receive telegramKey=" + telegramKey);
			boolean isWait = true;

			boolean isAlive = true;

			isAlive = subportStatus.getAlive(listenerPort);
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
					logger.warn("FundTelegramHostGateway receive timeout telegramKey=" + telegramKey);
					receiveHandler.addTimeoutKey(telegramKey);
					continue;
				}
				isAlive = subportStatus.getAlive(listenerPort);
			}
			if (isAlive == false) {
				code = "EABG001";
				desc = "電文異常，請與資訊處連管科聯絡，並檢查該筆交易是否成功";
			}
		} else if (hasAlive == false) {
			code = "E002";
			desc = "未與AS/400主機連線";
		}
		if (SCCESS.equals(code) == false) {
			logger.info("FundTelegramHostGateway code[{}], desc=[{}]", code, desc);
		}
		JMSGatewayOutputVO outputVO = new JMSGatewayOutputVO();
		outputVO.setRequestID((String) MDC.get($REQUEST_ID));
		outputVO.setContent((List<byte[]>) receiveData);
		long processTime = System.currentTimeMillis() - startTime;
		outputVO.setCode(code);
		outputVO.setDesc(desc);
		outputVO.setProcessTime(processTime);
		outputVO.setHostName(InetAddress.getLocalHost().getHostName());
		logger.info("FundTelegramHostGateway processTime={}", processTime);
		logger.info("FundTelegramHostGateway gateway out");
		return outputVO;
	}

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
	 * @return the fundTelegramServicePool
	 */
	public ObjectPool getFundTelegramServicePool() {
		return fundTelegramServicePool;
	}

	/**
	 * @param fundTelegramServicePool the fundTelegramServicePool to set
	 */
	public void setFundTelegramServicePool(ObjectPool fundTelegramServicePool) {
		this.fundTelegramServicePool = fundTelegramServicePool;
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
}
