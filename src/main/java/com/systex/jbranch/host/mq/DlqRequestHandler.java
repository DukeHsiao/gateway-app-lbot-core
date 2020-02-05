package com.systex.jbranch.host.mq;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.systex.jbranch.platform.host.transform.JMSGatewayInputVO;
import com.systex.jbranch.platform.host.transform.JMSGatewayOutputVO;

public class DlqRequestHandler {

	private final Logger logger = LoggerFactory.getLogger(DlqRequestHandler.class);
	
	public void handle(Object handle) throws Exception {
		if(handle instanceof JMSGatewayOutputVO){
			JMSGatewayOutputVO outputVO = (JMSGatewayOutputVO) handle;
			String msg = "JMSGatewayOutputVO [requestID=" + outputVO.getRequestID() + ", processTime="
			+ outputVO.getProcessTime() + ", cdKey=" + Arrays.toString(outputVO.getCdKey()) + ", code="
			+ outputVO.getCode() + ", desc=" + outputVO.getDesc() + ", content=" + outputVO.getContent()
			+ ", hostName=" + outputVO.getHostName() + "]";
			logger.warn(msg);
		}else if(handle instanceof JMSGatewayInputVO){
			JMSGatewayInputVO inputVO = (JMSGatewayInputVO) handle;
			String msg = "JMSGatewayInputVO [requestID=" + inputVO.getRequestID() + ", content="
			+ Arrays.toString(inputVO.getContent()) + "]";
			logger.warn(msg);
		}else if(handle != null){
			logger.warn(handle.toString());
		}else{
			logger.warn("message is null");
		}
	}
}