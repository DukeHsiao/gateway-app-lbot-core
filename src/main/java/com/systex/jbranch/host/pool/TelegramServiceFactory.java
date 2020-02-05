package com.systex.jbranch.host.pool;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.pool.PoolableObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.systex.jbranch.host.context.Context;
import com.systex.jbranch.host.landbank.TelegramService;

public class TelegramServiceFactory implements PoolableObjectFactory{

    private String serverAddress;
    private int serverPort;
    private String localAddress;
    private String localPortExpression;
    private List<Integer> localPortList = new ArrayList<Integer>();
	
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    
    public void init() throws Exception{
    	calcExpressionReange();
    	logger.info("localPortList" + localPortList.toString());
    }
    
    public static void main(String[] args) throws Exception {
    	TelegramServiceFactory factory = new TelegramServiceFactory();
    	factory.setLocalPortExpression("3101-3120");
    	factory.init();
	}

	private void calcExpressionReange() {
		String[] portArr = localPortExpression.split(",");
    	for (int i = 0; i < portArr.length; i++) {
    		String tempPort = portArr[i].trim();
    		int idx = tempPort.indexOf("-");
			if(idx == -1){
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
    
	@Override
	public Object makeObject() throws Exception {

		if(localPortList.size() == 0){
			throw new Exception("Active數量不可大於localPort數量");
		}

		try {
			int localPort = localPortList.remove(0);

			while(Context.getContext() == null){
				logger.info("waitint for Context initialize...");
				Thread.sleep(1000L);
			}

			TelegramService telegramService = Context.getBean("telegramService", TelegramService.class);
			telegramService.setServerAddress(this.serverAddress);
			telegramService.setServerPort(this.serverPort);
			telegramService.setLocalAddress(this.localAddress);
			telegramService.setLocalPort(localPort);
			telegramService.init();
			return telegramService;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	@Override
	public void destroyObject(Object obj) throws Exception {
		TelegramService ts = (TelegramService) obj;
		ts.close();
		localPortList.add(ts.getLocalPort());
		ts = null;
	}

	@Override
	public boolean validateObject(Object obj) {
		TelegramService ts = (TelegramService) obj;
		return ts.validate();
	}

	@Override
	public void activateObject(Object obj) throws Exception {
		TelegramService services = (TelegramService) obj;
		services.putMDC();
	}

	@Override
	public void passivateObject(Object obj) throws Exception {
		//尚無需求需要實作
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

}

