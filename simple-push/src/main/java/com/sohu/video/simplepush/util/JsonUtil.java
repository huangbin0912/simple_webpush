/*
 * Copyright (c) 2012 Sohu. All Rights Reserved
 */
package com.sohu.video.simplepush.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectReader;

import com.sohu.video.simplepush.message.MessageSender;

/**
 * <p>
 * Description: 
 * </p>
 * @author jiannanfan
 * @version 1.0
 * @Date 2013-10-25
 */
public class JsonUtil {
    
    private static ObjectMapper OBJECTMAPPER;
    
    private static final Log log = LogFactory.getLog(JsonUtil.class);
    
    static {
        OBJECTMAPPER = new ObjectMapper();
    }
    
    public static String toJson(Object bean) {
        try {
            return OBJECTMAPPER.writeValueAsString(bean);
        } catch (Exception e) {
        	log.error("bean:" + bean, e);
            return "toJson error";
        }
    }
    
    public static <T> T fromJson(String json, Class<T> clazz) {
        ObjectReader reader = OBJECTMAPPER.reader(clazz);
        try {
            return (T) reader.readValue(json);
        } catch (Exception e) {
        	log.error("json:" + json, e);
            return null;
        } 
    }
    
}

    