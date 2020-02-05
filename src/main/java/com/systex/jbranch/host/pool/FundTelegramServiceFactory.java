package com.systex.jbranch.host.pool;
/**
 * @author 1800389 Scott Hong 2019/1/10 Main Lead starting program form Funding
 *         connect to AS/400
 *
 */

import org.apache.commons.pool.PoolableObjectFactory;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.systex.jbranch.host.context.Context;
import com.systex.jbranch.host.landbank.FundTelegramService;
public class FundTelegramServiceFactory implements PoolableObjectFactory {

	private String serverAddress;
	private int serverPort;
	private String localAddress;
	private String localPortExpression;
	//20190402
	private int keepAliveTime; // 1/1000 second
	//----
	//20190527
	private String resetSessIfRevTimeout = "";
	//----

	private List<Integer> localPortList = new ArrayList<Integer>();

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public void init() throws Exception {
		calcExpressionReange();
//		logger.info("FundTelegramServiceFactory localPortList" + localPortList.toString());
        //20190503 modify for log by Scott Hong
		logger.info("localPortList" + localPortList.toString());
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		FundTelegramServiceFactory factory = new FundTelegramServiceFactory();
		factory.setLocalPortExpression("20001-20002");
		factory.init();
	}

	private void calcExpressionReange() {
		String[] portArr = localPortExpression.split(",");
		for (int i = 0; i < portArr.length; i++) {
			String tempPort = portArr[i].trim();
			int idx = tempPort.indexOf("-");
			if (idx == -1) {
				localPortList.add(Integer.parseInt(tempPort));
				continue;
			}

			String[] portReange = tempPort.split("-", 2);
			int startPort = Integer.parseInt(portReange[0].trim());
			int endPort = Integer.parseInt(portReange[1].trim());
			for (int j = startPort; j <= endPort; j++) {
				localPortList.add(j);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.commons.pool.PoolableObjectFactory#makeObject()
	 */
	@Override
	public Object makeObject() throws Exception {
//logger.info("makeObject start localPortList.size=[{}]",localPortList.size());

		if (localPortList.size() == 0) {
			throw new Exception("Active數量不可大於localPort數量");
		}
//logger.info("makeObject start 2");

		try {
			int localPort = localPortList.remove(0);
			while (Context.getContext() == null) {
				logger.info("waiting for Context initialize...");

				Thread.sleep(1000L);
			}
			FundTelegramService fundTelegramService = Context.getBean("fundTelegramService", FundTelegramService.class);
			fundTelegramService.setServerAddress(this.serverAddress);
			fundTelegramService.setServerPort(this.serverPort);
			fundTelegramService.setLocalAddress(this.localAddress);
			fundTelegramService.setLocalPort(localPort);
			//20190402
			fundTelegramService.setKeepAliveTime(keepAliveTime);
			//----
			//20190527
			fundTelegramService.setResetSessIfRevTimeout(resetSessIfRevTimeout);
			//----
			fundTelegramService.init();
			return fundTelegramService;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.commons.pool.PoolableObjectFactory#destroyObject(java.lang.Object)
	 */
	@Override
	public void destroyObject(Object obj) throws Exception {
		FundTelegramService ts = (FundTelegramService) obj;

		ts.close();
		localPortList.add(ts.getLocalPort());
		ts = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.commons.pool.PoolableObjectFactory#validateObject(java.lang.
	 * Object)
	 */
	@Override
	public boolean validateObject(Object obj) {
		FundTelegramService ts = (FundTelegramService) obj;
		return ts.validate();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.commons.pool.PoolableObjectFactory#activateObject(java.lang.
	 * Object)
	 */
	@Override
	public void activateObject(Object obj) throws Exception {
		FundTelegramService services = (FundTelegramService) obj;
		services.putMDC();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.commons.pool.PoolableObjectFactory#passivateObject(java.lang.
	 * Object)
	 */
	@Override
	public void passivateObject(Object obj) throws Exception {
		// TODO Auto-generated method stub

	}

	/**
	 * @return the serverAddress
	 */
	public String getServerAddress() {
		return serverAddress;
	}

	/**
	 * @param serverAddress the serverAddress to set
	 */
	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
	}

	/**
	 * @return the serverPort
	 */
	public int getServerPort() {
		return serverPort;
	}

	/**
	 * @param serverPort the serverPort to set
	 */
	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	/**
	 * @return the localAddress
	 */
	public String getLocalAddress() {
		return localAddress;
	}

	/**
	 * @param localAddress the localAddress to set
	 */
	public void setLocalAddress(String localAddress) {
		this.localAddress = localAddress;
	}

	/**
	 * @return the localPortExpression
	 */
	public String getLocalPortExpression() {
		return localPortExpression;
	}

	/**
	 * @param localPortExpression the localPortExpression to set
	 */
	public void setLocalPortExpression(String localPortExpression) {
		this.localPortExpression = localPortExpression;
	}

	/**
	 * @return the keepAliveTime
	 */
	public int getkeepAliveTime() {
		return keepAliveTime;
	}

	/**
	 * @param keepAliveTime the keepAliveTime to set
	 */
	public void setkeepAliveTime(int keepAliveTime) {
		this.keepAliveTime = keepAliveTime;
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
	
	//----

}
