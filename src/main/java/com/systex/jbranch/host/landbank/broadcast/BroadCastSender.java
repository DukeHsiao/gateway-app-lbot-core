package com.systex.jbranch.host.landbank.broadcast;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import com.systex.jbranch.platform.host.transform.JMSGatewayOutputVO;

public class BroadCastSender {

    private final Logger logger = LoggerFactory.getLogger(BroadCastSender.class);

    @Autowired
    private JmsTemplate template = null;

    /**
     * @param vo
     * @throws JMSException
     */
    public void send(final JMSGatewayOutputVO vo) throws JMSException {

            template.send(new MessageCreator() {
                public Message createMessage(Session session) throws JMSException {
                	ObjectMessage message = session.createObjectMessage(vo);
                	logger.info("send broadcast:" + message);
                    return message;
                }
            });
    }
    
    public JmsTemplate getTemplate() {
		return template;
	}

	public void setTemplate(JmsTemplate template) {
		this.template = template;
	}

}