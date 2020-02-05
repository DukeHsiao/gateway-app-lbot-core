package com.systex.jbranch.host.utlsysf;

import org.apache.commons.codec.binary.Hex;


public class Utlsysf {
    static {
    	System.loadLibrary("utlsysf");
    }

    public native int UTLEnableCryptoData(int sess, byte[] source, int srclen, byte[] destination, int[] destlen);
    public native int UTLEncryptBlock(int sess, byte[] source, int srclen, byte[] destination, int[] destlen);
    public native int UTLDecryptBlock(int sess, byte[] source, int srclen, byte[] destination, int[] destlen, int[] xmt);

    public int utlenablecryptodata(int sess, byte[] source,int srclen,byte[] dest, int[] destlen)
    {
        return UTLEnableCryptoData(sess, source, srclen, dest, destlen);
    }

    public int utlencryptblock(int sess, byte[] source,int srclen,byte[] dest, int[] destlen)
    {
        return UTLEncryptBlock(sess, source, srclen, dest, destlen);
    }

    public int utldecryptblock(int sess, byte[] source,int srclen,byte[] dest, int[] destlen,int[] xmt)
    {
        return UTLDecryptBlock(sess, source, srclen, dest, destlen, xmt);
    }

    public static void main(String[] args) {
	try {
           int code=0;
           int sess;
           int srclen = 0;
           int[] destlen = new int[2];
           int[] xmt = new int[2];
           byte[] abdestbuf;
           byte[] endestbuf = new byte[1024];
           byte[] dedestbuf = new byte[1024];

           byte[] ENABLE_TEMPLATE = new byte[]{
               0x0f,0x0f,0x0f,0x00,0x00,0x21,0x01,'X','M','T',0x5b,0x0f,
               0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20
           };

           byte[] ENCRYPT_TEMPLATE = new byte[]{
               0x0f,0x0f,0x0f,0x00,0x00,0x58,0x01,0x00,0x00,0x01,0x0f,0x00,
               '1','2','3','4','5','6','7','8','9','A','B','C'
           };

           Utlsysf  jnisysf = new Utlsysf();

	   byte[] srcByteArr = ENABLE_TEMPLATE;
           sess = 0;
	   srclen =  srcByteArr.length;
	   abdestbuf = new byte[srclen];
           code = jnisysf.utlenablecryptodata(sess,srcByteArr,srclen,abdestbuf,destlen);
           System.out.println("code = "+code+" deslen = "+destlen[0]);
//           System.out.println("destbuf = "+new String(abdestbuf));
           System.out.println("Hex.encodeHexString[" + Hex.encodeHexString(abdestbuf));

	   byte[] ensrcByteArr = ENCRYPT_TEMPLATE;
	   srclen =  ensrcByteArr.length;
	   endestbuf = new byte[srclen];
           sess = 1;
           code = jnisysf.utlencryptblock(sess,ensrcByteArr,srclen,endestbuf,destlen);
           System.out.println("encrypt code = "+code+" deslen = "+destlen[0]);
//           System.out.println("encrypt destbuf = "+new String(endestbuf));
           System.out.println("Hex.encodeHexString[" + Hex.encodeHexString(endestbuf));

	   srclen =  endestbuf.length;
	   dedestbuf = new byte[srclen];
           sess = 1;
           System.out.println("decrypt srclen = "+srclen);
           code = jnisysf.utldecryptblock(sess,endestbuf,srclen,dedestbuf,destlen,xmt);
           System.out.println("decrypt code = "+code+" deslen = "+destlen[0]);
//           System.out.println("decrypt destbuf = "+new String(dedestbuf));
           System.out.println("Hex.encodeHexString[" + Hex.encodeHexString(dedestbuf));
        } catch (Exception e) {
	   // TODO Auto-generated catch block
	   e.printStackTrace();
	}
    }

}
