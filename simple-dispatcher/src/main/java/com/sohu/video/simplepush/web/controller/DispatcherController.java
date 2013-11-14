/*
 * Copyright (c) 2012 Sohu. All Rights Reserved
 */
package com.sohu.video.simplepush.web.controller;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sohu.video.simplepush.model.PushServer;
import com.sohu.video.simplepush.service.PushServerService;
import com.sohu.video.simplepush.util.JsonUtil;

/**
 * <p>
 * Description:
 * </p>
 * 
 * @author jiannanfan
 * @version 1.0
 * @Date 2013-10-25
 */
@Controller
@RequestMapping(value = "/simplepush/d")
public class DispatcherController {

    private final Log log = LogFactory.getLog(getClass());

    @Resource
    private PushServerService pushServerService;

    @RequestMapping(value = "/q/{topic}/{callback}.do", method = RequestMethod.GET)
    public @ResponseBody
    String queryServers(@PathVariable("topic") String topic, @PathVariable("callback") String callback) {
        if (topic == null || "".equals(topic.trim()) || callback == null || "".equals(callback.trim())) {
            return "wrong args";
        }
        PushServer server = pushServerService.queryServers(topic);
        return server == null ? "empty" : callback + "(" + JsonUtil.toJson(server) + ")";
        // return test(topic, callback);
    }

    // private String test(String topic, String callback) {
    // PushServer server = new PushServer();
    // server.setTopic(topic);
    // server.setGroup(5);
    // List<String> urls = new ArrayList<String>();
    // urls.add("http://10.10.12.123:8080");
    // urls.add("http://10.10.12.123:8081");
    // server.setUrls(urls);
    //
    // return callback + "(" + JsonUtil.toJson(server) + ")";
    // }
    //

}
