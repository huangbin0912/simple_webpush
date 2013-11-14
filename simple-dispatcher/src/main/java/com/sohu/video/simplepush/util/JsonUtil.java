/*
 * Copyright (c) 2012 Sohu. All Rights Reserved
 */
package com.sohu.video.simplepush.util;

import java.io.IOException;

import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectReader;

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
    
    static {
        OBJECTMAPPER = new ObjectMapper();
    }
    
    public static String toJson(Object bean) {
        try {
            return OBJECTMAPPER.writeValueAsString(bean);
        } catch (Exception e) {
            return "toJson error";
        }
    }
    
    public static <T> T fromJson(String json, Class<T> clazz) {
        ObjectReader reader = OBJECTMAPPER.reader(clazz);
        try {
            return (T) reader.readValue(json);
        } catch (Exception e) {
            return null;
        } 
    }
    
}

    