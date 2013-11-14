/*
 * Copyright (c) 2012 Sohu. All Rights Reserved
 */
package com.sohu.video.simplepush.message;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.sohu.video.simplepush.WebPusher;
import com.sohu.video.simplepush.util.JsonUtil;

/**
 * <p>
 * Description:
 * </p>
 * 
 * @author jiannanfan
 * @version 1.0
 * @Date 2013-10-30
 */
public class MessageConsumer {
    private Log log = LogFactory.getLog(getClass());

    private Lock lock = new ReentrantLock();

    private Map<String, Queue<PushMessage>> msgMap = new HashMap<String, Queue<PushMessage>>();
    
    @Autowired
    @Qualifier("webPusher")
	private WebPusher simplepush;

    public void execute(String msg) {
    	log.info("receive>>>>" + msg);
        PushMessage message = JsonUtil.fromJson(msg, PushMessage.class);
        if (message == null) {
            log.warn("fromJson error - " + msg);
            return;
        }
        if(simplepush.getTopics().contains(message.getTopic())){        	
        	String sessionid = message.getSessionid();
        	if (sessionid == null || "".equals(sessionid.trim())) {
        		//one to many
        		addMsg(message);
        	} else {
        		//one to one
        		simplepush.send2Session(message);
        	}
        }
    }

    public void addMsg(PushMessage message) {
        lock.lock();
        try {
            String topic = message.getTopic();
            if (msgMap.containsKey(topic)) {
                Queue<PushMessage> queue = msgMap.get(topic);
                queue.add(message);

            } else {
                Queue<PushMessage> queue = new LinkedList<PushMessage>();
                queue.add(message);
                msgMap.put(topic, queue);
            }
        } finally {
            lock.unlock();
        }
    }

    public Map<String, Queue<PushMessage>> getMsg() {
        lock.lock();
        try {
            Map<String, Queue<PushMessage>> map = msgMap;
            msgMap = new HashMap<String, Queue<PushMessage>>(map.size());
            return map;
        } finally {
            lock.unlock();
        }
    }

    public Queue<PushMessage> getMsg(String topic) {
        lock.lock();
        try {
            if (msgMap.containsKey(topic)) {
                Queue<PushMessage> queue = msgMap.get(topic);
                msgMap.remove(topic);
                return queue;
            } else {
                return null;
            }
        } finally {
            lock.unlock();
        }
    }
}
