package com.systex.jbranch.host.landbank;

import org.apache.commons.codec.binary.Hex;

/**
 * @author Alex Lin
 * @version 2011/01/20 1:28 PM
 */
public class Payload {
// ------------------------------ FIELDS ------------------------------

    private byte[] payload;

// --------------------------- CONSTRUCTORS ---------------------------

    public Payload() {
    }

    public Payload(byte[] payload) {
        this.payload = payload;
    }

// --------------------- GETTER / SETTER METHODS ---------------------

    public byte[] getPayload() {
        return this.payload;
    }

    public void setPayload(byte... payload) {
        this.payload = payload;
    }

// ------------------------ CANONICAL METHODS ------------------------

    @Override
    public String toString() {
        return "com.systex.jbranch.host.landbank.Payload{" +
                "payload=" + this.toHexString() +
                '}';
    }

    public String toHexString() {
        return Hex.encodeHexString(this.payload);
    }
}
