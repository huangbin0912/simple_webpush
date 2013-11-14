/*
 * Copyright (c) 2012 Sohu. All Rights Reserved
 */
package com.sohu.video.simplepush.service;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import com.sohu.video.simplepush.init.PushServerCache;
import com.sohu.video.simplepush.model.PushServer;

/**
 * <p>
 * Description: 
 * </p>
 * @author jiannanfan
 * @version 1.0
 * @Date 2013-10-25
 */
@Service
public class PushServerService {
    private final Log log = LogFactory.getLog(getClass());
    
    @Resource
    private PushServerCache pushServerCache;
    
    public PushServer queryServers(String topic) {
        if (topic == null || "".equals(topic.trim())) {
            return null;
        }
        log.info("queryServers-" + topic);
        return pushServerCache.fetchPushServer(topic);
    }
    
}

    