/*
 * Copyright (c) 2012 Sohu. All Rights Reserved
 */
package message;

import com.sohu.video.simplepush.message.PushMessage;
import com.sohu.video.simplepush.util.JsonUtil;

/**
 * <p>
 * Description:
 * </p>
 * 
 * @author jiannanfan
 * @version 1.0
 * @Date 2013-10-31
 */
public class Test {
    public static void main(String[] args) {
        PushMessage m = new PushMessage();
        m.setTopic("a-topic");
        m.setSessionid("123");
        m.setInfo("我说@#:abc");
        String s = JsonUtil.toJson(m);
        
        PushMessage t = JsonUtil.fromJson(s, PushMessage.class);
        System.out.println(t.getTopic());
        System.out.println(t.getSessionid());
        System.out.println(t.getInfo());
        
        
//        String message = "{\"topic\":\"b-topic\",\"info\":\"helloworld\"}";
        
        String message = "{\"topic\":\"b-topic\",\"info\":{\"name\":\"hello\",\"age\":11,\"ids\":[111,222,333]}}";
        
        String message2 = "{\"topic\":\"b-topic\",\"info\":{\"name\":\"hello\",\"age\":11,\"ls\":{\"a\":111,\"b\":\"bbb\"}}}";
        
        long start = System.currentTimeMillis();
        PushMessage mess = JsonUtil.fromJson(message, PushMessage.class);
        PushMessage mess2 = JsonUtil.fromJson(message2, PushMessage.class);
        System.out.println(System.currentTimeMillis() - start);
        System.out.println(mess);
        System.out.println(mess2);
    }
}
