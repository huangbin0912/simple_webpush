/*
 * Copyright (c) 2012 Sohu. All Rights Reserved
 */
package com.sohu.video.simplepush.message;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.MessageConverter;

/**
 * <p>
 * Description: 
 * </p>
 * @author jiannanfan
 * @version 1.0
 * @Date 2013-10-30
 */
public class MessageConverterImpl implements MessageConverter {

    @Override
    public String fromMessage(Message message) throws JMSException, MessageConversionException {
        TextMessage msg = (TextMessage) message;
        return msg.getText();
    }

    @Override
    public Message toMessage(Object obj, Session session) throws JMSException, MessageConversionException {
        TextMessage msg = session.createTextMessage();
//        msg.setText((String)obj);
        return msg;
    }

}

    