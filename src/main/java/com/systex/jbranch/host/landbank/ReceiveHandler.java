package com.systex.jbranch.host.landbank;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class ReceiveHandler {
	
	private Logger logger = LoggerFactory.getLogger(ReceiveHandler.class);
	
	private Map<String, List<byte[]>> telegramReceiveMap = new ConcurrentHashMap<String, List<byte[]>>();
	private Map<String, Object> timeoutKeyMap = new ConcurrentHashMap<String, Object>();
	
	public void put(String key, List<byte[]> content){
		try {
			if(timeoutKeyMap.remove(key) != null){
				logger.info("receive timeout key[{}]", key);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		telegramReceiveMap.put(key, content);
	}
	
	public List<byte[]> remove(String key){
		
		return telegramReceiveMap.remove(key);
	}
	
	public void clear(){
	    //暫不實作clear，保留至每日00:00由crontab stop gateway
//		telegramReceiveMap.clear();
	    logger.info("telegramReceiveMap.clear");
	}

	public void addTimeoutKey(String telegramKey) {
		logger.info("occur timeout key[{}]", telegramKey);
		try {
			timeoutKeyMap.put(telegramKey, "");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
}
