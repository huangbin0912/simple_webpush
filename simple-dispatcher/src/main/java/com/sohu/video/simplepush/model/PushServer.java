/*
 * Copyright (c) 2012 Sohu. All Rights Reserved
 */
package com.sohu.video.simplepush.model;

import java.util.List;

/**
 * <p>
 * Description:
 * </p>
 * 
 * @author jiannanfan
 * @version 1.0
 * @Date 2013-10-25
 */
public class PushServer {
    private String topic;

    private List<String> urls;

    private Integer group;

    public PushServer(String topic, List<String> urls, Integer group) {
        super();
        this.topic = topic;
        this.urls = urls;
        this.group = group;
    }

    public PushServer() {
        super();
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public List<String> getUrls() {
        return urls;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }

    public Integer getGroup() {
        return group;
    }

    public void setGroup(Integer group) {
        this.group = group;
    }

    @Override
    public String toString() {
        return "PushServer [topic=" + topic + ", urls=" + urls + ", group=" + group + "]";
    }

}
