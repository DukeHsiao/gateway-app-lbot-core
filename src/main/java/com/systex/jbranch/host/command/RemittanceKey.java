package com.systex.jbranch.host.command;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.systex.jbranch.host.landbank.KeyStore;
import com.systex.jbranch.platform.common.errHandle.JBranchException;

public class RemittanceKey {

    private static Logger logger = LoggerFactory.getLogger(RemittanceKey.class);
	
	public static byte[] readRemittanceKey() throws JBranchException{
		  try{
				byte[] remittKey;
				File keyFolder;
			  	String path = System.getProperty(KeyStore.GATEWAY_KEY_FOLDER);
		        keyFolder = new File(path);
		        if (logger.isDebugEnabled()) {
		        	logger.debug("loading remitt Key");
		        }
		        File remittKeyFile = getRemittKeyFile(keyFolder);
		        
		        if (remittKeyFile.exists()) {
		            remittKey = FileUtils.readFileToByteArray(remittKeyFile);
		            if (logger.isDebugEnabled()) {
		                logger.debug("loaded Remitt Key from file");
		            }
			        return remittKey;
		        }
		        else {
		            logger.warn("Remitt Key not exist");
		            return null;
		        }	       
  
	        }catch(Exception e)    {
	        	throw new JBranchException(e.getMessage(), e);
	        }
	}
	private static File getRemittKeyFile(File keyFolder) {
		  return new File(keyFolder, "RemitteKey.key");
	}
}
