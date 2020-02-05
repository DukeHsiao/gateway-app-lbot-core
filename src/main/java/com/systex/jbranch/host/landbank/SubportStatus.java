package com.systex.jbranch.host.landbank;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

@Repository
public class SubportStatus {
	
	private Map<Integer, Boolean> subportStatus = new ConcurrentHashMap();
	private Map<Integer, Boolean> keyStatus = new ConcurrentHashMap();
	private boolean disableCrypt = false;
    private boolean disableSecurity = false;
    //20181105 by Scott Hong
    private boolean disableKey = false;
	//
	public void setAlive(int subport, boolean isAlive){
		subportStatus.put(subport, isAlive);
	}
	
	public boolean getAlive(int subport){
		return subportStatus.get(subport);
	}
	
	public boolean hasAlive(){
		return subportStatus.containsValue(true);
	}
	
	public void setKeyStatus(int subport, boolean isNomal){
		keyStatus.put(subport, isNomal);
	}
	
	public boolean hasKeyNomal(){
		return keyStatus.containsValue(true);
	}
	
	public boolean getKeyStatus(int subport){
		return keyStatus.get(subport);
	}

	/**
	 * @return the disableCrypt
	 */
	public boolean isDisableCrypt() {
		return disableCrypt;
	}

	/**
	 * @param disableCrypt the disableCrypt to set
	 */
	public void setDisableCrypt(boolean disableCrypt) {
		this.disableCrypt = disableCrypt;
	}

	/**
	 * @return the disableSecurity
	 */
	public boolean isDisableSecurity() {
		return disableSecurity;
	}

	/**
	 * @param disableSecurity the disableSecurity to set
	 */
	public void setDisableSecurity(boolean disableSecurity) {
		this.disableSecurity = disableSecurity;
	}

    //20181105
	/**
	 * @return the isDisableKey
	 */

	public boolean isDisableKey() {
		return disableKey;
	}

	/**
	 * @param setDisableKey the setDisableKe to set
	 */

	public void setDisableKey(boolean disableKey) {
		this.disableKey = disableKey;
	}
	
	
}
