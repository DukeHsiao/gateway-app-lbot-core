package com.systex.jbranch.host.landbank;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

/**
 * @author Alex Lin
 * @version 2011/01/20 1:27 PM
 */
public class Header {
// ------------------------------ FIELDS ------------------------------

    public static final int LENGTH_POSITION_START = 3;
    public static final int LENGTH_POSITION_END = 5;
    public static final int STATUS_POSITION = 10;
    private static final byte[] HEADER_TEMPLATE = new byte[]{
            0x0f, 0x0f, 0x0f,
            0x00, 0x00, 0x12, 0x01,
            'X', 'M', 'T',
            0x5b, 0x0f
    };

    private byte[] header;
    private byte status;
    private int length;
    private String hexStatus;

// --------------------------- CONSTRUCTORS ---------------------------

    public Header() {
        this(ArrayUtils.clone(HEADER_TEMPLATE));
    }

    public Header(byte[] header) {
        this.header = header;
        this.setStatus(header[STATUS_POSITION]);
        this.length = NumberUtils.toInt(Hex.encodeHexString(ArrayUtils.subarray(header, LENGTH_POSITION_START, LENGTH_POSITION_END + 1)));
    }

    public void setStatus(byte status) {
        this.status = status;
        this.header[STATUS_POSITION] = status;
        this.hexStatus = Hex.encodeHexString(new byte[]{this.status});
    }

// -------------------------- OTHER METHODS --------------------------

    public void setLength(int length) throws DecoderException {
        if (this.length != length) {
            this.setLength(Hex.decodeHex(StringUtils.leftPad(String.valueOf(length), 6, "0").toCharArray()));
        }
    }

    public void setLength(byte[] length) {
        System.arraycopy(length, 0, this.header, LENGTH_POSITION_START, 3);
    }

// --------------------- GETTER / SETTER METHODS ---------------------

    public byte[] getHeader() {
        return this.header;
    }

    public String getHexStatus() {
        return this.hexStatus;
    }

    public int getLength() {
        return this.length;
    }

    public byte getStatus() {
        return this.status;
    }

// ------------------------ CANONICAL METHODS ------------------------

    @Override
    public String toString() {
        return "com.systex.jbranch.host.landbank.Header{" +
                "header=" + this.toHexString() +
                ", hexStatus=" + this.hexStatus +
                ", length=" + this.length +
                '}';
    }

    public String toHexString() {
        return Hex.encodeHexString(this.header);
    }
}
