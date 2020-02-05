package com.systex.jbranch.host.landbank;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

public class LogUtils {
	
	public static String listToString(List<String> list){
		StringBuffer sb = new StringBuffer();
		for (String str : list) {
			sb.append(str + "\r\n");
		}
		return sb.toString();
	}
	
	public static List<String> toLog(byte[] bytes, boolean isMask, String encoding) throws UnsupportedEncodingException{
		List<String> result = new ArrayList<String>();
		int displaySize = 8;
		int len = bytes.length / displaySize;
		len = bytes.length % displaySize == 0 ? len : len + 1;
		len++;//多run一圈，將最後一次的byte array放入result，並顯示文字
		boolean isIn = false;
		byte[] firstTempBytes = null;
		byte[] lastTempBytes = null;
		StringBuffer row = new StringBuffer();
		boolean nextRowFirstChinese = false;
		for (int i = 0; i < len; i++) {

			int offset = i * displaySize;
			int endsite = offset + displaySize;
			if(endsite > bytes.length){
				endsite = bytes.length;
			}

			if(isIn){
				lastTempBytes = ArrayUtils.subarray(bytes, offset, endsite);
				row.append(" ");
				for (byte b : lastTempBytes) {
					row.append(toHex(b) + " ");
				}
				row.append(" ");
				byte[] tempAll = ArrayUtils.addAll(firstTempBytes, lastTempBytes);

				String tempStr = row.toString();
				if(tempStr.length() < 50){
					row = new StringBuffer();
					row.append(StringUtils.rightPad(tempStr, 50, " "));
				}
				for (int j = 0; j < tempAll.length; j++) {
					byte b = tempAll[j];
					if(nextRowFirstChinese){
						if(isMask){
							row.append(".");								
						}else{
							row.append(new String(new byte[]{b}, encoding));
//							row.append("?");
						}
						nextRowFirstChinese = false;
					}else if((b & 0xff) <= 0x7f){
						row.append(new String(new byte[]{b}));
						nextRowFirstChinese = false;
					}else{
						if(j == tempAll.length - 1){
							row.append(new String(new byte[]{b}, encoding));
//							row.append("?");
							nextRowFirstChinese = true;
						}else{
							if(isMask){
								row.append("..");
								j++;
							}else{
								row.append(new String(new byte[]{b, tempAll[++j]}, encoding));
							}
							nextRowFirstChinese = false;
						}
					}
				}
				result.add(row.toString());
				row = new StringBuffer();
			}else{
				firstTempBytes = ArrayUtils.subarray(bytes, offset, endsite);
				for (byte b : firstTempBytes) {
					row.append(toHex(b) + " ");
				}
			}
			isIn = !isIn;
		}
		
		return result;
	}
	
	private static String toHex(byte b){
		String temp = "00" + Integer.toHexString(b & 0xff);
		return temp.substring(temp.length() - 2);
	}
	
	public static void main(String[] args) throws IOException {
		byte[] bytes = FileUtils.readFileToByteArray(new File("D:\\tita.txt"));

		long start = System.currentTimeMillis();
		List result = toLog(bytes, false, "MS950");
		long end = System.currentTimeMillis();
		for (int i = 0; i < result.size(); i++) {
			System.out.println(result.get(i));
		}
		System.out.println("time:" + (end - start) / 1000.0 );
	}
}
