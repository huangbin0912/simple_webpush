/**
 * 
 */
package com.sohu.video.simplepush.message;

import java.util.Map;

import org.eclipse.jetty.util.ajax.JSON;
import org.eclipse.jetty.util.ajax.JSON.Output;

/**
 * @author erichuang
 * 
 */
public class PushMessage implements JSON.Convertible {

    private Object info;

    private String topic;

    private String sessionid;

    public PushMessage() {
        super();
    }

    public PushMessage(Object info) {
        super();
        this.info = info;
    }

    public PushMessage(Object info, String topic, String sessionid) {
        super();
        this.info = info;
        this.topic = topic;
        this.sessionid = sessionid;
    }

    @Override
    public void toJSON(Output out) {
        // TODO Auto-generated method stub
        out.add("msg", info);
    }

    @Override
    public void fromJSON(Map object) {
        // TODO Auto-generated method stub

    }

    @Override
    public String toString() {
        return "PushMessage [info=" + info + "]";
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getSessionid() {
        return sessionid;
    }

    public void setSessionid(String sessionid) {
        this.sessionid = sessionid;
    }

    public Object getInfo() {
        return info;
    }

    public void setInfo(Object info) {
        this.info = info;
    }

}
