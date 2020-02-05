package com.systex.jbranch.host.landbank;


import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.ArrayUtils;

/**
 * @author Alex Lin
 * @version 2011/01/19 2:33 PM
 */
public class HostMessage {
// ------------------------------ FIELDS ------------------------------

    private static final int HEADER_LENGTH = 12;
    private Header header;
    private Payload payload;
    private byte[] source;

// --------------------------- CONSTRUCTORS ---------------------------

    public HostMessage(byte[] source) {
        this.header = new Header(ArrayUtils.subarray(source, 0, HEADER_LENGTH));
        if (this.header.getLength() > HEADER_LENGTH) {
            this.payload = new Payload(ArrayUtils.subarray(source, HEADER_LENGTH, this.header.getLength()));
        }
        this.source = ArrayUtils.subarray(source, 0, this.header.getLength());
    }

    public HostMessage(Header header, Payload payload) {
        this.header = header;
        this.payload = payload;
        this.source = ArrayUtils.addAll(header.getHeader(), payload.getPayload());
    }

// -------------------------- OTHER METHODS --------------------------

    public boolean matchStatus(byte... statuses) {
        return ArrayUtils.contains(statuses, this.header.getStatus());
    }

    public String toHexString() {
        return Hex.encodeHexString(this.source);
    }

// --------------------- GETTER / SETTER METHODS ---------------------

    public Header getHeader() {
        return this.header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public Payload getPayload() {
        return this.payload;
    }

    public void setPayload(Payload payload) {
        this.payload = payload;
    }

    public byte[] getSource() {
        return this.source;
    }

// ------------------------ CANONICAL METHODS ------------------------

    @Override
    public String toString() {
        return "com.systex.jbranch.host.landbank.HostMessage{" +
                "header=" + header +
                ", payload=" + payload +
                '}';
    }
}
