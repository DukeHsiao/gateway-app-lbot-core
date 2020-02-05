package com.systex.jbranch.host.landbank;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Alex Lin
 * @version 2011/01/20 3:26 PM
 */
public class KeyStore {
    public static final String GATEWAY_KEY_FOLDER = "gateway.key.folder";
// ------------------------------ FIELDS ------------------------------

    private File keyFolder;
    private byte[] macKey;
    private byte[] cdKey;
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private String subFolder;
	private byte[] emptyKey = {0x00, 0x00, 0x00 ,0x00 ,0x00 ,0x00 ,0x00 ,0x00 };

    public KeyStore() throws IOException {
    	this("");
    }

    public KeyStore(String subFolder) throws IOException {
    	this.subFolder = subFolder;
    	loadKeys();
	}

	public void loadKeys() throws IOException {
        String path = System.getProperty(GATEWAY_KEY_FOLDER);
        keyFolder = new File(path);
        if (logger.isDebugEnabled()) {
            logger.debug("loading Initial Key");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("loading MAC Key");
        }
        File macKeyFile = getMACKeyFile();
        if (macKeyFile.exists() == false) {
//        	throw new IOException("can't found [" + macKeyFile.getAbsolutePath() + "]");
        	macKeyFile.getParentFile().mkdirs();
        	FileUtils.writeByteArrayToFile(macKeyFile, emptyKey);
        }
        macKey = FileUtils.readFileToByteArray(macKeyFile);
        if (logger.isDebugEnabled()) {
            logger.debug("loaded MAC Key from file");
        }
        
        if (logger.isDebugEnabled()) {
            logger.debug("loading CD Key");
        }
        File cdKeyFile = getCDKeyFile();
        if (cdKeyFile.exists() == false) {
        	cdKeyFile.getParentFile().mkdirs();
        	FileUtils.writeByteArrayToFile(cdKeyFile, emptyKey);
        }

        cdKey = FileUtils.readFileToByteArray(cdKeyFile);
        if (logger.isDebugEnabled()) {
            logger.debug("loaded CD Key from file");
        }
        
    }

// --------------------- GETTER / SETTER METHODS ---------------------

    public byte[] getCdKey() {
        return cdKey;
    }

    public void setCdKey(byte[] cdKey) throws IOException {
    	if(cdKey.length != 8){
    		cdKey = emptyKey;
    	}
        FileUtils.writeByteArrayToFile(getCDKeyFile(), cdKey);
        this.cdKey = cdKey;
    }

    public byte[] getInitialKey() {
    	File initialKeyFile = getInitialKeyFile();
    	byte[] initialKey = null;
        if (initialKeyFile.exists()) {
            try {
				initialKey = FileUtils.readFileToByteArray(initialKeyFile);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
            if (logger.isDebugEnabled()) {
                logger.info("loaded Initial Key from file");
            }
        } else {
            logger.warn("Initial Key not exist");
        }
        return initialKey;
    }

    public void setInitialKey(byte[] initialKey) throws IOException {
    	if(initialKey.length != 8){
    		initialKey = emptyKey;
    	}
        FileUtils.writeByteArrayToFile(getInitialKeyFile(), initialKey);
    }

    public byte[] getMacKey() {
        return macKey;
    }

    public void setMacKey(byte[] macKey) throws IOException {
    	if(macKey.length != 8){
    		macKey = emptyKey;
    	}
        FileUtils.writeByteArrayToFile(getMACKeyFile(), macKey);
        this.macKey = macKey;
    }

    private File getCDKeyFile() {
        return new File(keyFolder + "/" + subFolder, "CDKey.key");
    }

    private File getMACKeyFile() {
        return new File(keyFolder + "/" + subFolder, "MACKey.key");
    }

    private File getInitialKeyFile() {
        return new File(keyFolder, "InitialKey.key");
    }
}
