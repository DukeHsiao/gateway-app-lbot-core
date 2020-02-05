package com.systex.jbranch.host.util;

import java.nio.charset.StandardCharsets;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.ArrayUtils;

public class TelegramKeyUtil {
	
//	private Logger logger = Logger.getLogger(TelegramKeyUtil.class);
	
	private int keyOffset = 0;
	private int keyLength = 15;
	
	public String getTelegramKey(byte[] bytes){
	    if (bytes == null) {
            return "";
        }
	    if (bytes.length < keyLength) {
            return "";
        }
	    
		bytes = ArrayUtils.subarray(bytes, keyOffset, keyOffset + keyLength);
		return Hex.encodeHexString(bytes).replace("f", "");
	}
    //20190316
	// for map telegramkey
	public String getTelegramKey2(byte[] bytes){
	    if (bytes == null) {
            return "";
        }
	    if (bytes.length < keyLength) {
            return "";
        }
	    bytes = ArrayUtils.subarray(bytes, keyOffset, keyOffset + keyLength);
		return new String(bytes, StandardCharsets.UTF_8);
	}
	//----

	/**
	 * @return the keyOffset
	 */
	public int getKeyOffset() {
		return keyOffset;
	}

	/**
	 * @param keyOffset the keyOffset to set
	 */
	public void setKeyOffset(int keyOffset) {
		this.keyOffset = keyOffset;
	}

	/**
	 * @return the keyLength
	 */
	public int getKeyLength() {
		return keyLength;
	}

	/**
	 * @param keyLength the keyLength to set
	 */
	public void setKeyLength(int keyLength) {
		this.keyLength = keyLength;
	}
	
	
//    public static String calcTitaTelegramKey(byte[] requestBytes) {
//    	if(requestBytes.length <= 15){
//    		String key = Hex.encodeHexString(requestBytes);
//    		logger.debug("tita key=" + key);
//    		return key;
//    	}
//		int startIdx = 0;
//		int length = 0;
//		if(requestBytes[18] == -95){//為~
//			length = 15;
//		}else{
//			length = 8;
//		}
//		requestBytes = ArrayUtils.subarray(requestBytes, startIdx, startIdx + length);
//		String key = Hex.encodeHexString(requestBytes);
//		logger.debug("tita key=" + key);
//		return key;
//	}
//    
//	public static String calcTotaTelegramKey(byte[] payLoad) {
//		int startIdx = 0;
//		int length = 0;
//		if(payLoad.length >= 68 && payLoad[68] == -95){//為~
//			length = 15;
//		}else{
//			length = 8;
//		}
//		payLoad = ArrayUtils.subarray(payLoad, startIdx, startIdx + length);
//		String key = Hex.encodeHexString(payLoad);
//		logger.debug("tota key=" + key);
//		return key;
//	}
}
