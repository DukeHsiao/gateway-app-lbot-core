package com.systex.jbranch.host.util;

import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.dom4j.io.OutputFormat;

/*
 * 2019/3/21
 * Scott Hong
 */

public class Dom4jtool {

	private static Element root;
	private static List<String> name = new ArrayList<String>();
	private Document document;
	private String codeSet = "UTF-8"; // default for UTF-8 code set
	private static SimpleDateFormat formatOutput = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.sssZ");

	public Dom4jtool() {
	}

	public static void main(String args[]) {
		Dom4jtool dj = new Dom4jtool();
//    URLConnectionUtil urlc = new URLConnectionUtil() ;
		// URL
//        String urlStr = "http://tw.news.yahoo.com/rss/business/rss.xml";

//    Map<String,Object> map = new HashMap<String,Object>(); 
//    InputStream res = null ;

		try {
			//
//            res = URLConnectionUtil.doGet(urlStr, map);

			//
//            SAXReader reader = new SAXReader();
//            document = reader.read(res);
//      Document chkdocument;
//      String filename = "RFXConfig.xml";
//        String filename = "test.xml";
//      String filename = "jss.xml";
//      document = loadXMLFile("jss.xml"); 
//      chkdocument = dj.loadXMLFile("jss.xml"); 
//      chkdocument = dj.loadXMLFile(filename); 
//        dj.loadXMLFile("." + File.separator + filename); 
			// XML Root Node
//      root = chkdocument.getRootElement();
			// Root Node NAME
////      System.out.println( root.getName() );
////      System.out.println(  );

			// XML DATA
//      printXMLTree();

			// print data => /rss/channel/item/title
//            name = getAllDataByPath("/rss/channel/item/title");
//      name = getAllDataByPath("/JSS_CONFIG/UNIT/DTS_SERVICE");
//      getAllDataByPath("/JSS_CONFIG/UNIT/DTS_SERVICE/IMPLEMENT");
//      getAllDataBySubPath(document, "/JSS_CONFIG/UNIT", "DTS_SERVICE/IMPLEMENT");
//      getAllDataBySubPath(document, "/JSS_CONFIG/UNIT", "DTS_SERVICE/FTP/PASSWORD");
//      getAllDataBySubPath(document, "/JSS_CONFIG/UNIT", "DTS_SERVICE/IMPLEMENT[text()='toptools.app.service.dts.FtpRateHandler']");
//      getAllDataBySubPath(document, "/JSS_CONFIG/UNIT", "DTS_SERVICE/IMPLEMENT[text()='toptools.app.service.dts.FtpRateHandleo']");
//      dj.loadXMLData("<?xml version=\"1.0\" encoding=\"UTF-8\"?><SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:esb=\"http://www.ibm.com.tw/esb\" xmlns:tw=\"http://www.tcbbank.com.tw/esb/tw\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><SOAP-ENV:Header></SOAP-ENV:Header><SOAP-ENV:Body><esb:MsgRq><Header><ClientId>FXDL</ClientId><ClientDt>2017-03-08T11:11:11.234+11:00</ClientDt><TxnId>FXDL500</TxnId></Header><SvcRq xsi:type=\"tw:FXDL500SvcRqType\"><header><UnitId>091</UnitId><ServerDate>20170308</ServerDate><ServerTime>111111</ServerTime><MI_REQ_RESP_ID>DL500</MI_REQ_RESP_ID><MI_CMD_TYPE></MI_CMD_TYPE><MI_F01></MI_F01><MI_F02></MI_F02><MI_F03>FXDL500</MI_F03><MI_F04></MI_F04></header><Record><MI_F05>20871948</MI_F05><MI_F06>USD</MI_F06><MI_F07>P220463660</MI_F07></Record></SvcRq></esb:MsgRq></SOAP-ENV:Body></SOAP-ENV:Envelope>");

//      if (dj.chkAllDataBySubPath("/", "mi"))
//        System.out.println("get Data");
//      else        
//        System.out.println("no Data");

//////
			// if (dj.chkAllDataBySubPath("/mi", "header/MI_F03")) {
			// System.out.println("get Data CHID=" + dj.getDataBySubPath("/mi",
			// "header/MI_F03", 0, "").substring(0, 4));
//      } else        
//        System.out.println("no Data");

//      System.out.println("===>" + dj.generateXMLString(true));
//      dj.deleteNodeBySubPath("/", "mi");
//      System.out.println("2 ===>" + dj.getAllDataStringBySubPath("/mi", "header"));
//      System.out.println("3 ===>" + dj.getAllDataStringBySubPath("/mi", "record"));

//      System.out.println("2 ===>" + dj.generateXMLString("header", true));
//      System.out.println("3 ===>" + dj.generateXMLString("Record", true));
			/*
			 * if (dj.chkAllDataBySubPath("/JSS_CONFIG/UNIT",
			 * "DTS_SERVICE/IMPLEMENT[text()='toptools.app.service.dts.FtpRateHandler']"))
			 * System.out.println("get Data"); else System.out.println("no Data"); if
			 * (dj.chkAllDataBySubPath("/JSS_CONFIG/UNIT",
			 * "DTS_SERVICE/IMPLEMENT[text()='toptools.app.service.dts.FtpRateHandleo']"))
			 * System.out.println("get Data"); else System.out.println("no Data"); // if
			 * (dj.chkAllDataBySubPath(chkdocument, "/JSS_CONFIG/UNIT",
			 * "DTS_SERVICE[@ID='FTP Rate File Handler']/FTP/USER")) //
			 * System.out.println("get Data"); // else // System.out.println("no Data"); if
			 * (dj.chkAllDataBySubPath("/JSS_CONFIG/UNIT",
			 * "DTS_SERVICE[@ID='FTP Rate File Handler']/FTP/USER")) {
			 * System.out.println("get Data user=" + dj.getDataBySubPath("/JSS_CONFIG/UNIT",
			 * "DTS_SERVICE[@ID='FTP Rate File Handler']/FTP/USER", 0, "")); } else
			 * System.out.println("no Data"); if (dj.chkAllDataBySubPath("/JSS_CONFIG/UNIT",
			 * "DTS_SERVICE[@ID='FTP Rate File Handler']/FTP/PASSWORD")) {
			 * System.out.println("get Data pass=" + dj.getDataBySubPath("/JSS_CONFIG/UNIT",
			 * "DTS_SERVICE[@ID='FTP Rate File Handler']/FTP/PASSWORD", 0, ""));
			 * dj.setDataBySubPath("/JSS_CONFIG/UNIT",
			 * "DTS_SERVICE[@ID='FTP Rate File Handler']/FTP/PASSWORD", 0, "", "11223333");
			 * } else System.out.println("no Data"); if
			 * (dj.chkAllDataBySubPath("/JSS_CONFIG/UNIT",
			 * "DTS_SERVICE[@ID='FTP Rate File Handler']/FTP/PASSWORD")) {
			 * System.out.println("2 get Data pass=" +
			 * dj.getDataBySubPath("/JSS_CONFIG/UNIT",
			 * "DTS_SERVICE[@ID='FTP Rate File Handler']/FTP/PASSWORD", 0, "")); } else
			 * System.out.println("2 no Data");
			 * 
			 * if (dj.chkAllDataBySubPath("/preferences/root/node/node/node/node/node",
			 * "node[@name='SYS']/map/entry[@key='FTP_TXTDBUUSER']")) {
			 * System.out.println("get Data DBU user=" +
			 * dj.getDataBySubPath("/preferences/root/node/node/node/node/node",
			 * "node[@name='SYS']/map/entry[@key='FTP_TXTDBUUSER']", 1, "value")); } else
			 * System.out.println("no Data"); if
			 * (dj.chkAllDataBySubPath("/preferences/root/node/node/node/node/node",
			 * "node[@name='SYS']/map/entry[@key='FTP_TXTDBUPASSWORD']")) {
			 * System.out.println("get Data DBU pass=" +
			 * dj.getDataBySubPath("/preferences/root/node/node/node/node/node",
			 * "node[@name='SYS']/map/entry[@key='FTP_TXTDBUPASSWORD']", 1, "value")); }
			 * else System.out.println("no Data"); if
			 * (dj.chkAllDataBySubPath("/preferences/root/node/node/node/node/node",
			 * "node[@name='SYS']/map/entry[@key='FTP_TXTOBUUSER']")) {
			 * System.out.println("get Data OBU user=" +
			 * dj.getDataBySubPath("/preferences/root/node/node/node/node/node",
			 * "node[@name='SYS']/map/entry[@key='FTP_TXTOBUUSER']", 1, "value")); } else
			 * System.out.println("no Data"); if
			 * (dj.chkAllDataBySubPath("/preferences/root/node/node/node/node/node",
			 * "node[@name='SYS']/map/entry[@key='FTP_TXTOBUPASSWORD']")) {
			 * System.out.println("get Data OBU pass=" +
			 * dj.getDataBySubPath("/preferences/root/node/node/node/node/node",
			 * "node[@name='SYS']/map/entry[@key='FTP_TXTOBUPASSWORD']", 1, "value"));
			 * dj.setDataBySubPath("/preferences/root/node/node/node/node/node",
			 * "node[@name='SYS']/map/entry[@key='FTP_TXTOBUPASSWORD']", 1, "value",
			 * "9999999"); } else System.out.println("no Data"); if
			 * (dj.chkAllDataBySubPath("/preferences/root/node/node/node/node/node",
			 * "node[@name='SYS']/map/entry[@key='FTP_TXTOBUPASSWORD']")) {
			 * System.out.println("2 get Data OBU pass=" +
			 * dj.getDataBySubPath("/preferences/root/node/node/node/node/node",
			 * "node[@name='SYS']/map/entry[@key='FTP_TXTOBUPASSWORD']", 1, "value")); }
			 * else System.out.println("2 no Data"); if (dj.chkAllDataBySubPath("/",
			 * "SOAP-ENV:Envelope")) System.out.println("get Data"); else
			 * System.out.println("no Data"); if
			 * (dj.chkAllDataBySubPath("/SOAP-ENV:Envelope",
			 * "SOAP-ENV:Body/esb:MsgRq/Header/ClientId")) System.out.println("get Data");
			 * else System.out.println("no Data"); // dj.loadXMLData(
			 * "<mi><header><UnitId>091</UnitId><ServerDate>20170308</ServerDate><ServerTime>111111</ServerTime><MI_REQ_RESP_ID>DL500</MI_REQ_RESP_ID><MI_CMD_TYPE></MI_CMD_TYPE><MI_F01></MI_F01><MI_F02></MI_F02><MI_F03>FXDL500</MI_F03><MI_F04></MI_F04></header><Record><MI_F05>20871948</MI_F05><MI_F06>USD</MI_F06><MI_F07>P220463660</MI_F07></Record></mi>"
			 * ); // dj.loadXMLData(
			 * "<mi><header><UnitId>091</UnitId><ServerDate>20170308</ServerDate><ServerTime>111111</ServerTime><MI_REQ_RESP_ID>DL500</MI_REQ_RESP_ID><MI_CMD_TYPE></MI_CMD_TYPE><MI_F01></MI_F01><MI_F02></MI_F02><MI_F03>FXDL500</MI_F03><MI_F04></MI_F04></header><Record><MI_F05>20871948</MI_F05><MI_F06>USD</MI_F06><MI_F07>P220463660</MI_F07></Record></mi>"
			 * ); if (dj.chkAllDataBySubPath("/", "mi")) System.out.println("get Data");
			 * else System.out.println("no Data");
			 */
//      dj.loadXMLData("<?xml version=\"1.0\" encoding=\"UTF-8\"?><SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:esb=\"http://www.ibm.com.tw/esb\" xmlns:tw=\"http://www.tcbbank.com.tw/esb/tw\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><SOAP-ENV:Header></SOAP-ENV:Header><SOAP-ENV:Body><esb:MsgRq><Header><ClientId>FXDL</ClientId><ClientDt>2017-03-08T11:11:11.234+11:00</ClientDt><TxnId>FXDL500</TxnId></Header><SvcRq xsi:type=\"tw:FXDL500SvcRqType\"><header><UnitId>091</UnitId><ServerDate>20170308</ServerDate><ServerTime>111111</ServerTime><MI_REQ_RESP_ID>DL500</MI_REQ_RESP_ID><MI_CMD_TYPE></MI_CMD_TYPE><MI_F01></MI_F01><MI_F02></MI_F02><MI_F03>FXDL500</MI_F03><MI_F04></MI_F04></header><Record><MI_F05>20871948</MI_F05><MI_F06>USD</MI_F06><MI_F07>P220463660</MI_F07></Record></SvcRq></esb:MsgRq></SOAP-ENV:Body></SOAP-ENV:Envelope>");
//      if (dj.chkAllDataBySubPath("/SOAP-ENV:Envelope", "SOAP-ENV:Body/esb:MsgRq/Header/ClientId"))
//        System.out.println("get Data");
//      else        
//        System.out.println("no Data");
//      if (dj.chkAllDataBySubPath("/SOAP-ENV:Envelope", "SOAP-ENV:Body/esb:MsgRq/SvcRq"))
//        System.out.println("get Data");
//      else        
//        System.out.println("no Data");
///////
//      System.out.println(dj.getAllDataStringBySubPath("/SOAP-ENV:Envelope", "SOAP-ENV:Body/esb:MsgRq/SvcRq/header/*"));
			// System.out.println(dj.getAllDataStringBySubPath("/SOAP-ENV:Envelope",
			// "SOAP-ENV:Body/esb:MsgRq/SvcRq/Record/*"));
//////
			/*
			 * dj.
			 * loadXMLData("<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:esb=\"http://www.ibm.com.tw/esb\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:fx=\"http://www.tcbbank.com.tw/esb/fx\"><SOAP-ENV:Header></SOAP-ENV:Header><SOAP-ENV:Body><esb:MsgRs><Header><ClientId>FXDL</ClientId><ClientDt>2017-03-15T05:44:32.032+0800</ClientDt><TxnId>FXDL951</TxnId><Status><SystemId>ESB</SystemId><StatusCode>0</StatusCode><Severity>INFO</Severity><StatusDesc></StatusDesc></Status></Header><SvcRs xsi:type=\"fx:FXDL951SvcRsType\"><Next></Next></SvcRs></esb:MsgRs></SOAP-ENV:Body></SOAP-ENV:Envelope>"
			 * ); System.out.println("==>" +
			 * dj.getDataBySubPath("/SOAP-ENV:Envelope/SOAP-ENV:Body/esb:MsgRs/Header",
			 * "TxnId", 0, "")); System.out.println("==>" +
			 * dj.getAllDataStringBySubPath("/SOAP-ENV:Envelope/SOAP-ENV:Body/esb:MsgRs",
			 * "SvcRs/*")); dj.
			 * loadXMLData("<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:esb=\"http://www.ibm.com.tw/esb\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:fx=\"http://www.tcbbank.com.tw/esb/fx\"><SOAP-ENV:Header></SOAP-ENV:Header><SOAP-ENV:Body><esb:MsgRs><Header><ClientId>FXDL</ClientId><ClientDt>2017-03-16T10:28:08.008+0800</ClientDt><TxnId>FXDL500</TxnId><Status><SystemId>FX</SystemId><StatusCode>FX01</StatusCode><Severity>ERROR</Severity><StatusDesc>嚙箭嚙踝蕭嚙踝蕭嚙編嚙箭嚙踝蕭嚙編嚙瘩嚙褕歹蕭</StatusDesc></Status></Header><SvcRs><MI_F01></MI_F01><MI_F02></MI_F02><MI_F03>FXDL500</MI_F03><MI_F04></MI_F04></SvcRs></esb:MsgRs></SOAP-ENV:Body></SOAP-ENV:Envelope>"
			 * );
			 * 
			 * System.out.println("==>" +
			 * dj.getDataBySubPath("/SOAP-ENV:Envelope/SOAP-ENV:Body/esb:MsgRs/Header",
			 * "Status/StatusDesc", 0, "")); System.out.println("==>" +
			 * dj.getDataBySubPath("/SOAP-ENV:Envelope/SOAP-ENV:Body/esb:MsgRs/Header",
			 * "TxnId", 0, "")); System.out.println("==>" +
			 * dj.getAllDataStringBySubPath("/SOAP-ENV:Envelope/SOAP-ENV:Body/esb:MsgRs",
			 * "SvcRs/*")); System.out.println("==>" +
			 * dj.chkNodeNumByPath("/SOAP-ENV:Envelope/SOAP-ENV:Body/esb:MsgRs/SvcRs/*"));
			 * String target =
			 * dj.getNodeNameByOrder("/SOAP-ENV:Envelope/SOAP-ENV:Body/esb:MsgRs/SvcRs/*",
			 * 3); System.out.println("==>" + target); System.out.println("==>" +
			 * dj.getDataBySubPath("/SOAP-ENV:Envelope/SOAP-ENV:Body/esb:MsgRs/SvcRs",
			 * target, 0, "")); target =
			 * dj.getNodeNameByOrder("/SOAP-ENV:Envelope/SOAP-ENV:Body/esb:MsgRs/SvcRs/*",
			 * 4); System.out.println("==>" + target); System.out.println("==>" +
			 * dj.getDataBySubPath("/SOAP-ENV:Envelope/SOAP-ENV:Body/esb:MsgRs/SvcRs",
			 * target, 0, "")); dj.deleteNodeBySubPath(
			 * "/SOAP-ENV:Envelope/SOAP-ENV:Body/esb:MsgRs/SvcRs/MI_F03", "");
			 * dj.deleteNodeBySubPath(
			 * "/SOAP-ENV:Envelope/SOAP-ENV:Body/esb:MsgRs/SvcRs/MI_F04", "");
			 */
			String target = "";
//      System.out.println("==>" + dj.getAllDataStringBySubPath("/SOAP-ENV:Envelope/SOAP-ENV:Body/esb:MsgRs", "SvcRs/*"));
			dj.loadXMLData(
					"<mi version = \"1.0\"><header><field name =\"UserName\" value=\"\" /><field name =\"UserId\" value=\"12635008\" /><field name =\"UserEmail\" value=\"\" /><field name =\"ServerDate\" value=\"20170406\" /><field name =\"ServerTime\" value=\"112628\" /><field name =\"MI_REQ_RESP_ID\" value=\"\" /><field name =\"MI_CMD_TYPE\" value=\"\" /><field name =\"MI_F01\" value=\"\" /><field name =\"MI_F02\" value=\"\" /><field name =\"MI_F03\" value=\"FXDL960\" /><field name =\"MI_F04\" value=\"1\" /><field name =\"MI_F05\" value=\"2\" /><field name =\"MI_F06\" value=\"3\" /><field name =\"MI_F07\" value=\"4\" /><field name =\"MI_F08\" value=\"5\" /></header></mi>");
			System.out.println("==>" + dj.getAllDataStringBySubPath("/mi", "header/*"));
			int n = dj.chkNodeNumByPath("/mi/header/*");
			System.out.println("total n==>" + n);
			target = dj.getNodeNameByOrder("/mi/header/*", n);
			System.out.println(n + " ==>" + target);
			ArrayList<String> listn = new ArrayList<String>();
			ArrayList<String> listt = new ArrayList<String>();
			HashMap[] attrym = new java.util.HashMap[n];

			String clientId = "", trnid = "";
			for (int i = 1; i <= n; i++) {
				attrym[i - 1] = new java.util.HashMap();
				System.out.println(i + "--> name [" + dj.getAttributeByOrder("/mi/header/*", i, "name") + "] ==> value["
						+ dj.getAttributeByOrder("/mi/header/*", i, "value") + "]");
				listn.add(dj.getAttributeByOrder("/mi/header/*", i, "name"));
				if (i == 10) {
					trnid = dj.getAttributeByOrder("/mi/header/*", i, "value");
					clientId = trnid.substring(0, 4);
				}
				listt.add(dj.getAttributeByOrder("/mi/header/*", i, "value"));
				attrym[i - 1].put("name", dj.getAttributeByOrder("/mi/header/*", i, "name"));
				attrym[i - 1].put("value", dj.getAttributeByOrder("/mi/header/*", i, "value"));
			}

			String sndbuf = String.format(
					"<?xml version=\"1.0\" encoding=\"UTF-8\"?><SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:esb=\"http://www.ibm.com.tw/esb\" xmlns:tw=\"http://www.tcbbank.com.tw/esb/tw\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><SOAP-ENV:Header></SOAP-ENV:Header><SOAP-ENV:Body><esb:MsgRq><Header><ClientId>%s</ClientId><ClientDt>%s</ClientDt><TxnId>%s</TxnId></Header><SvcRq xsi:type=\"tw:%sSvcRqType\"></SvcRq></esb:MsgRq></SOAP-ENV:Body></SOAP-ENV:Envelope>",
					clientId, formatOutput.format(new Date()), trnid, trnid);

			dj.loadXMLData(sndbuf);

			dj.addSubNodes("/SOAP-ENV:Envelope/SOAP-ENV:Body/esb:MsgRq/SvcRq", "TWHeader", listn, listt, attrym);
//      dj.addSubNodes("/SOAP-ENV:Envelope/SOAP-ENV:Body/esb:MsgRq/SvcRq", "TWHeader", listn, listt, null);
			sndbuf = dj.generateXMLString(false);
			System.out.println(sndbuf);

//      dj.loadXMLData("<?xml version=\"1.0\" encoding=\"Big5\"?><SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:esb=\"http://www.ibm.com.tw/esb\" xmlns:tw=\"http://www.tcbbank.com.tw/esb/tw\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><SOAP-ENV:Header /><SOAP-ENV:Body><esb:MsgRs><Header><ClientId>FXDL</ClientId><ClientDt>2017-04-07T03:53:56.056+0800</ClientDt><TxnId>FXDL960</TxnId><Status><SystemId>FXDL</SystemId><StatusCode>0</StatusCode><Severity>INFO</Severity><StatusDesc /></Status></Header><SvcRs xsi:type=\"tw:FXDL960SvcRsType\"><TWHeader><MI_REQ_RESP_ID>EAIMQT01</MI_REQ_RESP_ID><MI_CMD_TYPE>d064e6582001030c</MI_CMD_TYPE><MI_F01>20170407</MI_F01><MI_F02>15535620</MI_F02><MI_F03>55645526</MI_F03><MI_F04>FXDL960</MI_F04><MI_F05 /><MI_F06>E024</MI_F06><MI_F07>Failed to commit data!  caused by:FILEDLP 嚙緝嚙踝蕭峇G嚙箠嚙踝蕭嚙複會嚙瞋嚙瘢嚙稻嚙瘠   Message ID.:E024</MI_F07><MI_F08>FXDL960</MI_F08></TWHeader></SvcRs></esb:MsgRs></SOAP-ENV:Body></SOAP-ENV:Envelope>");
//      System.out.println("TWHeader ==>" + dj.getAllDataStringBySubPath("/SOAP-ENV:Envelope", "SOAP-ENV:Body/esb:MsgRs/SvcRs/TWHeader/*"));
//      System.out.println("TWBody ==>" + dj.getAllDataStringBySubPath("/SOAP-ENV:Envelope", "SOAP-ENV:Body/esb:MsgRs/SvcRs/TWBody/*"));
			String ss = "<?xml version=\"1.0\" encoding=\"Big5\"?><SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:esb=\"http://www.ibm.com.tw/esb\" xmlns:tw=\"http://www.tcbbank.com.tw/esb/tw\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><SOAP-ENV:Header /><SOAP-ENV:Body><esb:MsgRs><Header><ClientId>FXDL</ClientId><ClientDt>2017-04-07T03:53:56.056+0800</ClientDt><TxnId>FXDL960</TxnId><Status><SystemId>FXDL</SystemId><StatusCode>0</StatusCode><Severity>INFO</Severity><StatusDesc /></Status></Header><SvcRs xsi:type=\"tw:FXDL960SvcRsType\"><TWHeader><MI_REQ_RESP_ID>EAIMQT01</MI_REQ_RESP_ID><MI_CMD_TYPE>d064e6582001030c</MI_CMD_TYPE><MI_F01>20170407</MI_F01><MI_F02>15535620</MI_F02><MI_F03>55645526</MI_F03><MI_F04>FXDL960</MI_F04><MI_F05 /><MI_F06>E024</MI_F06><MI_F07>Failed to commit data!  caused by:FILEDLP 嚙緝嚙踝蕭峇G嚙箠嚙踝蕭嚙複會嚙瞋嚙瘢嚙稻嚙瘠   Message ID.:E024</MI_F07><MI_F08>FXDL960</MI_F08></TWHeader></SvcRs></esb:MsgRs></SOAP-ENV:Body></SOAP-ENV:Envelope>";
			System.out.println("[" + dj.TransferOutgoingMessage(ss.getBytes(), ss.getBytes().length) + "]");
			// System.out.println("==>" + dj.getAllDataStringBySubPath("/mi", "header/*"));

//      sndbuf = dj.generateXMLString(false);
//      System.out.println(sndbuf);
//    String sndbuf = String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?><SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:esb=\"http://www.ibm.com.tw/esb\" xmlns:tw=\"http://www.tcbbank.com.tw/esb/tw\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><SOAP-ENV:Header></SOAP-ENV:Header><SOAP-ENV:Body><esb:MsgRq><Header><ClientId>%s</ClientId><ClientDt>%s</ClientDt><TxnId>%s</TxnId></Header><SvcRq xsi:type=\"tw:%sSvcRqType\"><TWHeader>%s</TWHeader></SvcRq></esb:MsgRq></SOAP-ENV:Body></SOAP-ENV:Envelope>", clientId, formatOutput.format(new Date()), trnid, trnid, sndhd);

			// dj.loadXMLData("<?xml version=\"1.0\" encoding=\"UTF-8\"?><SOAP-ENV:Envelope
			// xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"
			// xmlns:esb=\"http://www.ibm.com.tw/esb\"
			// xmlns:tw=\"http://www.tcbbank.com.tw/esb/tw\"
			// xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><SOAP-ENV:Header></SOAP-ENV:Header><SOAP-ENV:Body><esb:MsgRq><Header><ClientId>FXDL</ClientId><ClientDt>2017-03-08T11:11:11.234+11:00</ClientDt><TxnId>FXDL500</TxnId></Header><SvcRq
			// xsi:type=\"tw:FXDL500SvcRqType\"><TWHeader><UnitId>091</UnitId><ServerDate>20170308</ServerDate><ServerTime>111111</ServerTime><MI_REQ_RESP_ID>DL500</MI_REQ_RESP_ID><MI_CMD_TYPE></MI_CMD_TYPE><MI_F01></MI_F01><MI_F02></MI_F02><MI_F03>FXDL500</MI_F03><MI_F04></MI_F04></TWHeader><TWBody
			// xsi:type=\"tw:FXDL500RqType\"><MI_F05>20871948</MI_F05><MI_F06>USD</MI_F06><MI_F07>P220463660</MI_F07></TWBody></SvcRq></esb:MsgRq></SOAP-ENV:Body></SOAP-ENV:Envelope>");
//      System.out.println("1==>" + dj.getDataBySubPath("/SOAP-ENV:Envelope/SOAP-ENV:Body/esb:MsgRq/Header", "TxnId", 0, ""));
//      System.out.println("==>" + dj.getAllDataStringBySubPath("/SOAP-ENV:Envelope/SOAP-ENV:Body/esb:MsgRq", "SvcRq/*"));

//d      if (dj.chkAllDataBySubPath("/mi", "header/MI_F03"))
//        System.out.println("get Data");
//      else        
//        System.out.println("no Data");
//     filename = "test.xml";
//     dj.generateXMLFile("." + File.separator + "modify-" + filename);
			String v0021_tita = "<RqXMLData>   <Header>                <ClientDtTm>20190320182438</ClientDtTm>         <ClientID>10.2.45.17</ClientID>         <FrnIP>10.1.5.146</FrnIP>               <FrnName>NB</FrnName>           <FrnMsgID>077455961351</FrnMsgID>               <SvcCode>NONE</SvcCode>         <SvcType>FxConAcctInqRq</SvcType>               <Encoding>Big5</Encoding>               <Language>zh_TW</Language>      </Header>       <AuthData>              <CustPermId/>               <CustLoginId>983940</CustLoginId>          <CustLoginType>1</CustLoginType>                <CustPswd/>               <SignonRole>Agent</SignonRole>          <AuthenticationRule/>       </AuthData>     <ATMAuthData>           <BankNo/>               <CardNo/>               <TrCode/>               <FrnCode/>              <FrnChkCode/>           <TrDate/>               <RCPTIdTSAC/>           <CardMemo/>             <Auth/> </ATMAuthData>  <Text>          <CustPermId>A134***146</CustPermId>             <AcctIdFrom>983004145941</AcctIdFrom>   </Text></RqXMLData>";
			dj.loadXMLData(v0021_tita);
                        System.out.println("FrnMsgID=[" + dj.getDataBySubPath("/RqXMLData/Header", "FrnMsgID", 0, "") + "]");
		} catch (Exception e) {
		}
	}

	private String TransferOutgoingMessage(byte[] origmsg, int len) {
		String rtnbuf = "";
		Dom4jtool dj = new Dom4jtool();
		String nodename = "";

		dj.loadXMLData(new String(origmsg, 0, len));
//      log.debug("header ==>" + dj.getAllDataStringBySubPath("/SOAP-ENV:Envelope/SOAP-ENV:Body/esb:MsgRs/SvcRs", "TWHeader"));
		System.err.println("header ==>"
				+ dj.getAllDataStringBySubPath("/SOAP-ENV:Envelope/SOAP-ENV:Body/esb:MsgRs/SvcRs", "TWHeader"));
		int n = dj.chkNodeNumByPath("/SOAP-ENV:Envelope/SOAP-ENV:Body/esb:MsgRs/SvcRs/TWHeader/*");
//      log.debug("total TWHeader Field number==>" + n);
		System.err.println("total TWHeader Field number==>" + n);
		ArrayList<String> listn = new ArrayList<String>();
		HashMap[] attrym = new java.util.HashMap[n];
		String sf6 = "";
		for (int i = 1; i <= n; i++) {
			attrym[i - 1] = new java.util.HashMap();
			nodename = dj.getNodeNameByOrder("/SOAP-ENV:Envelope/SOAP-ENV:Body/esb:MsgRs/SvcRs/TWHeader/*", i);
//        log.debug(i + "--> name [" + nodename + "] ==> value[" + dj.getDataBySubPath("/SOAP-ENV:Envelope/SOAP-ENV:Body/esb:MsgRs/SvcRs/TWHeader", nodename, 0, "") + "]");
			System.err.println(i + "--> name [" + nodename + "] ==> value["
					+ dj.getDataBySubPath("/SOAP-ENV:Envelope/SOAP-ENV:Body/esb:MsgRs/SvcRs/TWHeader", nodename, 0, "")
					+ "]");
			listn.add(nodename);
			attrym[i - 1].put("name", nodename);
			if (nodename.trim().equals("MI_F06"))
				sf6 = dj.getDataBySubPath("/SOAP-ENV:Envelope/SOAP-ENV:Body/esb:MsgRs/SvcRs/TWHeader", nodename, 0, "");
			if (nodename.trim().equals("MI_F07")) {
				String ss = dj.getDataBySubPath("/SOAP-ENV:Envelope/SOAP-ENV:Body/esb:MsgRs/SvcRs/TWHeader", nodename,
						0, "");
				if (ss.length() > 40 && !sf6.trim().equals("0000"))
					attrym[i - 1].put("value", sf6);
			} else
				attrym[i - 1].put("value", dj.getDataBySubPath(
						"/SOAP-ENV:Envelope/SOAP-ENV:Body/esb:MsgRs/SvcRs/TWHeader", nodename, 0, ""));
		}
		dj.loadXMLData("<mi version='1.0'></mi>");
		dj.addSubNodes("/mi", "header", listn, null, attrym);
		String header = dj.generateXMLString(true);
		System.err.println("header ==>" + header);
		dj.loadXMLData(new String(origmsg, 0, len));
//      log.debug("body ==>" + dj.getAllDataStringBySubPath("/SOAP-ENV:Envelope/SOAP-ENV:Body/esb:MsgRs/SvcRs", "TWBody"));
		System.err.println("body ==>"
				+ dj.getAllDataStringBySubPath("/SOAP-ENV:Envelope/SOAP-ENV:Body/esb:MsgRs/SvcRs", "TWBody"));
		n = dj.chkNodeNumByPath("/SOAP-ENV:Envelope/SOAP-ENV:Body/esb:MsgRs/SvcRs/TWTWBody/*");
//      log.debug("total TWTWBody Field number==>" + n);
		System.err.println("total TWTWBody Field number==>" + n);
		listn = new ArrayList<String>();
		attrym = new java.util.HashMap[n];
		for (int i = 1; i <= n; i++) {
			attrym[i - 1] = new java.util.HashMap();
			nodename = dj.getNodeNameByOrder("/SOAP-ENV:Envelope/SOAP-ENV:Body/esb:MsgRs/SvcRs/TWTWBody/*", i);
//        log.debug(i + "--> name [" + nodename + "] ==> value[" + dj.getDataBySubPath("/SOAP-ENV:Envelope/SOAP-ENV:Body/esb:MsgRs/SvcRs/TWTWBody", nodename, 0, "") + "]");
			System.err.println(i + "--> name [" + nodename + "] ==> value["
					+ dj.getDataBySubPath("/SOAP-ENV:Envelope/SOAP-ENV:Body/esb:MsgRs/SvcRs/TWTWBody", nodename, 0, "")
					+ "]");
			listn.add(nodename);
			attrym[i - 1].put("name", nodename);
			attrym[i - 1].put("value", dj
					.getDataBySubPath("/SOAP-ENV:Envelope/SOAP-ENV:Body/esb:MsgRs/SvcRs/TWTWBody/*", nodename, 0, ""));
		}
		dj.loadXMLData(header);
		dj.addSubNodes("/mi", "record", listn, null, attrym);
		rtnbuf = dj.generateXMLString(true);
		listn = null;
		attrym = null;
		return rtnbuf;
	}

	/** print all xml data */
	public void printXMLTree() {
		printElement(root, 0);
		return;
	}

	private void printElement(Element element, int level) {
		// print
		for (int i = 0; i < level; i++) {
			System.out.print("\t");
		}
		System.out.print("<" + element.getQualifiedName() + ">");
		//
		List attributes = element.attributes();
		for (int i = 0; i < attributes.size(); i++) {
			Attribute a = ((Attribute) attributes.get(i));
			System.out.print(" (Attr:\"" + a.getName() + "\"==" + a.getValue() + ")");
		}
		System.out.println(" " + element.getTextTrim());
		Iterator iter = element.elementIterator();
		while (iter.hasNext()) {
			Element sub = (Element) iter.next();
			printElement(sub, level + 1);
		}
		return;
	}

	/**  */
	public Document loadXMLData(String message) {
		this.document = null;
		try {
			this.document = DocumentHelper.parseText(message);
			if (this.document.getXMLEncoding() != null)
				this.codeSet = this.document.getXMLEncoding();
//      System.out.println(":" + this.codeSet);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return this.document;
	}

	/**  */
	public Document loadXMLFile(String filename) {
//    Document document = null;
		this.document = null;
		try {
			SAXReader saxReader = new SAXReader();
			this.document = saxReader.read(new File(filename));
			if (this.document.getXMLEncoding() != null)
				this.codeSet = this.document.getXMLEncoding();
//      System.out.println(filename + ":" + this.codeSet);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return this.document;
	}

	/** */
	public void generateXMLFile(String filename, Document document) {
		try {
			// OutputFormat.createPrettyPrint();
			// System.getProperty("line.separator") for XML file format
			//
			// OutputFormat.createCompactFormat(); for Telegram type
			//
			OutputFormat formats = OutputFormat.createPrettyPrint();
			formats.setLineSeparator(System.getProperty("line.separator"));
			this.document.setXMLEncoding(this.codeSet);
			formats.setEncoding(this.codeSet);
			XMLWriter output = new XMLWriter(new FileWriter(new File(filename)), formats);
			output.write(document);
			output.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void generateXMLFile(String filename) {
		try {
			// OutputFormat.createPrettyPrint();
			// System.getProperty("line.separator") for XML file format
			//
			// OutputFormat.createCompactFormat(); for Telegram type
			//
			OutputFormat formats = OutputFormat.createPrettyPrint();
			formats.setLineSeparator(System.getProperty("line.separator"));
			XMLWriter output = new XMLWriter(new FileWriter(new File(filename)), formats);
			this.document.setXMLEncoding(this.codeSet);
			formats.setEncoding(this.codeSet);
			output.write(this.document);
			output.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public String generateXMLString(boolean _compact) {
		String rtn = "";
		try {
			// OutputFormat.createPrettyPrint();
			// System.getProperty("line.separator") for XML file format
			//
			// OutputFormat.createCompactFormat(); for Telegram type
			//
			StringWriter out = new StringWriter();
			OutputFormat formats = null;
			if (_compact)
				formats = OutputFormat.createCompactFormat();
			else {
				formats = OutputFormat.createPrettyPrint();
				formats.setLineSeparator(System.getProperty("line.separator"));
			}
			XMLWriter output = new XMLWriter(out, formats);
			this.document.setXMLEncoding(this.codeSet);
			formats.setEncoding(this.codeSet);
			output.write(this.document);
			output.close();
			out.flush();
			rtn = out.toString();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return rtn;
	}

	public String generateXMLString(String node, boolean _compact) {
		String rtn = "";
		try {
			// OutputFormat.createPrettyPrint();
			// System.getProperty("line.separator") for XML file format
			//
			// OutputFormat.createCompactFormat(); for Telegram type
			//
			StringWriter out = new StringWriter();
			OutputFormat formats = null;
			if (_compact)
				formats = OutputFormat.createCompactFormat();
			else {
				formats = OutputFormat.createPrettyPrint();
				formats.setLineSeparator(System.getProperty("line.separator"));
			}
			XMLWriter output = new XMLWriter(out, formats);
			this.document.setXMLEncoding(this.codeSet);
			formats.setEncoding(this.codeSet);
			Element rootElm = this.document.getRootElement();
			Element memberElm = rootElm.element(node);
			output.write(memberElm);
			output.close();
			out.flush();
			rtn = out.toString();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return rtn;
	}

	public String generateXMLString(Element memberElm, boolean _compact) {
		String rtn = "";
		try {
			StringWriter out = new StringWriter();
			OutputFormat formats = null;
			if (_compact)
				formats = OutputFormat.createCompactFormat();
			else {
				formats = OutputFormat.createPrettyPrint();
				formats.setLineSeparator(System.getProperty("line.separator"));
			}
			XMLWriter output = new XMLWriter(out, formats);
			output.write(memberElm);
			output.close();
			out.flush();
			rtn = out.toString();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return rtn;

	}

	/**
	 * Path Data example: String path="/rss/channel/item/title"
	 * 
	 * @param path
	 *//*
		 * public List<String> getAllDataByPath( String path ){ List<String> data = new
		 * ArrayList<String>(); Iterator it = document.selectNodes( path ).iterator();
		 * while(it.hasNext()) { Element ele = (Element)it.next(); printElement(ele, 0);
		 * } return data; }
		 */
	/**
	 * Path Data example: String path="/rss/channel/item/title"
	 * 
	 * @param path
	 */
	public void getAllDataByPath(String path) {
		Iterator<?> it = this.document.selectNodes(path).iterator();
		while (it.hasNext()) {
			Element ele = (Element) it.next();
			printElement(ele, 0);
		}
	}

	public void getAllDataBySubPath(Document doc, String path, String subpath) {
		Node subroot = doc.selectSingleNode(path);
//      printElement((Element )subroot, 0);
		if (subroot == null)
			return;
		Iterator<?> it = subroot.selectNodes(subpath).iterator();
		if (it == null)
			return;
		while (it.hasNext()) {
			Element ele = (Element) it.next();
			if (ele != null) {
				printElement(ele, 0);
			} else
				break;
		}
	}

	public void getAllDataBySubPath(String path, String subpath) {
		Node subroot = this.document.selectSingleNode(path);
//      printElement((Element )subroot, 0);
		if (subroot == null)
			return;
		Iterator<?> it = subroot.selectNodes(subpath).iterator();
		if (it == null)
			return;
		while (it.hasNext()) {
			Element ele = (Element) it.next();
			if (ele != null) {
				printElement(ele, 0);
			} else
				break;
		}
	}

	public boolean chkAllDataBySubPath(Document doc, String path, String subpath) {
		boolean rtn = false;
		Node subroot = doc.selectSingleNode(path);
//      printElement((Element )subroot, 0);
		if (subroot == null)
			return rtn;
		Iterator<?> it = subroot.selectNodes(subpath).iterator();
		if (it == null)
			return rtn;
		while (it.hasNext()) {
			Element ele = (Element) it.next();
			if (ele != null) {
				printElement(ele, 0);
				rtn = true;
			} else
				break;
		}
		return rtn;
	}

	public boolean chkAllDataBySubPath(String path, String subpath) {
		boolean rtn = false;
		Node subroot = this.document.selectSingleNode(path);
//      printElement((Element )subroot, 0);
		if (subroot == null)
			return rtn;
		Iterator<?> it = subroot.selectNodes(subpath).iterator();
		if (it == null)
			return rtn;
		while (it.hasNext()) {
			Element ele = (Element) it.next();
			if (ele != null) {
				printElement(ele, 0);
				rtn = true;
			} else
				break;
		}
		return rtn;
	}

	public String getAllDataStringBySubPath(String path, String subpath) {
		String rtn = "";
		Node subroot = this.document.selectSingleNode(path);
		if (subroot == null)
			return rtn;
		Iterator<?> it = subroot.selectNodes(subpath).iterator();
		if (it == null)
			return rtn;
		while (it.hasNext()) {
			Element ele = (Element) it.next();
			if (ele != null) {
				rtn = rtn + generateXMLString(ele, true);
			} else
				break;
		}
		return rtn;
	}

	public String getDataBySubPath(Document doc, String path, String subpath, int type, String target) {
		// type 0 get Text target ignore
		// 1 get Atrribute value target == attribute id
		String rtn = "";
		Node subroot = doc.selectSingleNode(path);
//      printElement((Element )subroot, 0);
		if (subroot == null)
			return rtn;
		Iterator<?> it = subroot.selectNodes(subpath).iterator();
		if (it == null)
			return rtn;
		while (it.hasNext()) {
			Element ele = (Element) it.next();
			if (ele != null) {
				rtn = (type == 0 ? ele.getTextTrim() : ele.attributeValue(target));
			} else
				break;
		}
		return rtn;
	}

	public String getDataBySubPath(String path, String subpath, int type, String target) {
		// type 0 get Text target ignore
		// 1 get Atrribute value target == attribute id
		String rtn = "";
		Node subroot = this.document.selectSingleNode(path);
//      printElement((Element )subroot, 0);
		if (subroot == null)
			return rtn;
		Iterator<?> it = subroot.selectNodes(subpath).iterator();
		if (it == null)
			return rtn;
		while (it.hasNext()) {
			Element ele = (Element) it.next();
			if (ele != null) {
				rtn = (type == 0 ? ele.getTextTrim() : ele.attributeValue(target));
			} else
				break;
		}
		return rtn;
	}

	public boolean setDataBySubPath(Document doc, String path, String subpath, int type, String target,
			String setvalue) {
		// 0 set text target ignore
		boolean rtn = false;
		Node subroot = doc.selectSingleNode(path);
//      printElement((Element )subroot, 0);
		if (subroot == null)
			return rtn;
		Iterator<?> it = subroot.selectNodes(subpath).iterator();
		if (it == null)
			return rtn;
		while (it.hasNext()) {
			Element ele = (Element) it.next();
			if (ele != null) {
				if (type == 0)
					ele.setText(setvalue);
				else
					ele.addAttribute(target, setvalue);
				rtn = true;
			} else
				break;
		}
		return rtn;
	}

	public boolean setDataBySubPath(String path, String subpath, int type, String target, String setvalue) {
		// 0 set text target ignore
		boolean rtn = false;
		Node subroot = this.document.selectSingleNode(path);
//      printElement((Element )subroot, 0);
		if (subroot == null)
			return rtn;
		Iterator<?> it = subroot.selectNodes(subpath).iterator();
		if (it == null)
			return rtn;
		while (it.hasNext()) {
			Element ele = (Element) it.next();
			if (ele != null) {
				if (type == 0)
					ele.setText(setvalue);
				else
					ele.addAttribute(target, setvalue);
				rtn = true;
			} else
				break;
		}
		return rtn;
	}

	public boolean deleteNodeBySubPath(String path, String subpath) {
		boolean rtn = false;
		Node subroot = this.document.selectSingleNode(path);
//      printElement((Element )subroot, 0);
		if (subroot == null)
			return rtn;
		subroot.detach();
		rtn = true;
		return rtn;
	}

	public boolean deleteNodeKeepSubPath(String path) {
		boolean rtn = false;
		String subPath = path + "/*";
		List<?> list = this.document.selectNodes(subPath);
		if (list == null)
			return rtn;
		Node subroot = this.document.selectSingleNode(path);
		if (subroot == null)
			return rtn;
		Element parmentElement = subroot.getParent();
		subroot.detach();
		for (Iterator<?> iter = list.iterator(); iter.hasNext();) {
			Element saveElement = (Element) iter.next();
			Element newSubElement = parmentElement.addElement(saveElement.getName());
			newSubElement.setText(saveElement.getTextTrim());
		}
		rtn = true;
		return rtn;
	}

	public boolean changeNodeKeepSubPath(String path, String chgname) {
		boolean rtn = false;
		String subPath = path + "/*";
		List<?> list = this.document.selectNodes(subPath);
		if (list == null)
			return rtn;
		Node subroot = this.document.selectSingleNode(path);
		if (subroot == null)
			return rtn;
		Element parmentElement = subroot.getParent();
		subroot.detach();
		Element newParmentElement = parmentElement.addElement(chgname);
		for (Iterator<?> iter = list.iterator(); iter.hasNext();) {
			Element saveElement = (Element) iter.next();
			Element newSubElement = newParmentElement.addElement(saveElement.getName());
			newSubElement.setText(saveElement.getTextTrim());
		}
		rtn = true;
		return rtn;
	}

	public boolean addSubNodes(String path, String subnodename, ArrayList<String> alistn, ArrayList<String> alistt,
			HashMap[] aryattrm) {
		boolean rtn = false;
		Node subroot = this.document.selectSingleNode(path);
		if (subroot == null || alistn == null)
			return rtn;
//    System.err.println("---- from addSubNodes " + subnodename);
		Element elem = (Element) subroot;
		Element newSubElement = elem.addElement(subnodename);
		for (int i = 0; i < alistn.size(); i++) {
			Element newGroupElement = newSubElement.addElement(alistn.get(i));
			if (alistt != null && alistt.get(i).trim().length() > 0) {
				newGroupElement.setText(alistt.get(i));
			}
			if (aryattrm != null && aryattrm[i] != null && aryattrm[i].size() > 0) {
				for (Object key : aryattrm[i].keySet()) {
//          System.err.println("---- from addSubNodes " + (String)aryattrm[i].get(key));
					newGroupElement.addAttribute((String) key, (String) aryattrm[i].get(key));
				}
			}
		}
		rtn = true;
		return rtn;
	}

	public int chkNodeNumByPath(String path) {
		int rtn = 0;
		List<?> list = this.document.selectNodes(path);
		if (list == null)
			return rtn;
		Element element = null;
		;
		for (Iterator<?> iter = list.iterator(); iter.hasNext();) {
			element = (Element) iter.next();
			rtn += 1;
		}
		return rtn;
	}

	// order from 1
	public String getNodeNameByOrder(String path, int order) {
		String rtn = "";
		List<?> list = this.document.selectNodes(path);
		if (list == null)
			return rtn;
		Element element = null;
		int idx = 0;
		for (Iterator<?> iter = list.iterator(); iter.hasNext();) {
			element = (Element) iter.next();
			idx += 1;
			if (order == idx)
				return element.getName();
		}
		return rtn;
	}

	public String getAttributeByOrder(String path, int order, String attr) {
		String rtn = "";
		List<?> list = this.document.selectNodes(path);
		if (list == null)
			return rtn;
		Element element = null;
		int idx = 0;
		for (Iterator<?> iter = list.iterator(); iter.hasNext();) {
			element = (Element) iter.next();
			idx += 1;
			if (order == idx)
				return element.attributeValue(attr);
		}
		return rtn;

	}

}
