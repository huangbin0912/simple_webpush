/*
 * Copyright (c) 2012 Sohu. All Rights Reserved
 */
package com.sohu.video.simplepush.init;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sohu.video.simplepush.SubscriptionConfigurerService;
import com.sohu.video.simplepush.model.PushServer;
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
public class PushServerCache {
    private final Log log = LogFactory.getLog(getClass());

    private static final Pattern IP_PATTERN = Pattern.compile("\\d+\\.\\d+\\.\\d+\\.\\d+");

    private SubscriptionConfigurerService subConfigurerService;

    private Map<String, String> configStrMap;

    private Map<String, Queue<PushServer>> serverMap;

    public PushServerCache() {
        
    }
    
    private void init() {
        /*
         * 从redis获取配置并缓存在本地；定时更新本地缓存
         */
        log.info("init start");
        configStrMap = new HashMap<String, String>(1000);

        Map<String, Queue<PushServer>> map = initMap();
        if (map == null) {
            log.warn("#################server map is empty");
            serverMap = new HashMap<String, Queue<PushServer>>(1);
        }
        serverMap = map;
        flushMap();
        log.info("init end");
    }

    private Map<String, Queue<PushServer>> initMap() {
        List<String> topicList = null;
        try {
            topicList = subConfigurerService.lrange("simplepush-topic", 0, -1);
        } catch (Exception e) {
            log.error("get simplepush-topic error ##############", e);
            return null;
        }
        if (topicList == null || topicList.isEmpty()) {
            log.warn("simplepush-topic is empty ##############");
            return null;
        }
        Map<String, Queue<PushServer>> map = new HashMap<String, Queue<PushServer>>(topicList.size());
        for (String topic : topicList) {
            Queue<PushServer> Queue = initPushServer(topic);
            if (Queue != null) {
                map.put(topic, Queue);
            }
        }
        return map.isEmpty() ? null : map;

    }

    private Queue<PushServer> initPushServer(String topic) {
        log.info("initPushServer-" + topic);
        try {
            String temp = subConfigurerService.get(topic);
            if (temp == null || "".equals(temp.trim())) {
                log.warn("this topic's config is empty-" + topic);
                return null;
            }

            if (temp.equals(configStrMap.get(topic))) {
                return serverMap.get(topic);
            } else {
                log.info("this topic's config has changed-" + topic);
                PushServer server = JsonUtil.fromJson(temp, PushServer.class);
                configStrMap.put(topic, temp);
                return forDispatch(server);
            }
        } catch (Exception e) {
            log.error("initPushServer error ##############" + topic, e);
            return null;
        }
    }

    private Queue<PushServer> forDispatch(PushServer server) {
        log.info("forDispatch-" + server.getTopic());
        List<List<String>> sList = seperateByIp(server);
        int size = sList.size();
        log.info("this topic server ip num is-" + size);
        Queue<PushServer> servers = null;
        if (size == 1) {
            servers = disposeSize1(server, sList.get(0));
        } else if (size == 2) {
            servers = disposeSize2(server, sList.get(0), sList.get(1));
        } else {
            servers = disposeSizeN(server, sList);
        }
        return servers;
    }

    private List<List<String>> seperateByIp(PushServer server) {
        log.info("seperateByIp-" + server.getTopic());
        List<String> urls = server.getUrls();
        Map<String, List<String>> map = new HashMap<String, List<String>>(urls.size());
        for (String u : urls) {
            String ip = parseIp(u);
            if (ip != null) {
                if (map.containsKey(ip)) {
                    map.get(ip).add(u);
                } else {
                    List<String> list = new ArrayList<String>();
                    list.add(u);
                    map.put(ip, list);
                }
            }
        }

        if (map.isEmpty()) {
            return null;
        }
        List<List<String>> sList = new ArrayList<List<String>>(map.size());
        sList.addAll(map.values());
        return sList;
    }

    private String parseIp(String line) {
        Matcher mc = IP_PATTERN.matcher(line);
        if (mc.find()) {
            return mc.group();
        } else {
            return null;
        }
    }

    private Queue<PushServer> disposeSize1(PushServer server, List<String> urls) {
        Queue<PushServer> servers = new LinkedList<PushServer>();
        if (urls.size() == 1) {
            servers.add(server);
            return servers;
        }
        for (int i = 0, size = urls.size(); i < size; i++) {
            int k = i + 1;
            servers.add(new PushServer(server.getTopic(), Arrays.asList(urls.get(i), urls.get(k >= size ? 0 : k)), server.getGroup()));
        }
        return servers;
    }

    private Queue<PushServer> disposeSize2(PushServer server, List<String> list1, List<String> list2) {
        Queue<PushServer> servers = new LinkedList<PushServer>();
        if (list1.size() < list2.size()) {
            List<String> temp = list1;
            list1 = list2;
            list2 = temp;
        }

        int size2 = list2.size();
        for (int i = 0, size = list1.size(); i < size; i++) {
            servers.add(new PushServer(server.getTopic(), Arrays.asList(list1.get(i), list2.get(i >= size2 ? 0 : i)), server.getGroup()));
        }

        for (int i = 0; i < size2; i++) {
            servers.add(new PushServer(server.getTopic(), Arrays.asList(list2.get(i), list1.get(i)), server.getGroup()));
        }
        return servers;
    }

    private Queue<PushServer> disposeSizeN(PushServer server, List<List<String>> sList) {
        Queue<PushServer> servers = new LinkedList<PushServer>();
        List<List<List<String>>> list = seperateToSize2(sList);
        for (List<List<String>> temp : list) {
            servers.addAll(disposeSize2(server, temp.get(0), temp.get(1)));
        }
        return servers;
    }

    private List<List<List<String>>> seperateToSize2(List<List<String>> sList) {
        List<List<List<String>>> list = new ArrayList<List<List<String>>>(sList.size());
        for (int i = 0, size = sList.size(); i < size; i++) {
            int k = i + 1;
            List<List<String>> l = new ArrayList<List<String>>(2);
            l.add(sList.get(i));
            l.add(sList.get(k == size ? 0 : k));
            list.add(l);
        }
        return list;
    }

    private void flushMap() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                log.info("flush start");
                Map<String, Queue<PushServer>> map = initMap();
                if (map != null) {
                    serverMap = map;
                }
                log.info("flush end");
            }

        }, 1000 * 60 * 10, 1000 * 60 * 2);
    }

    public PushServer fetchPushServer(String topic) {
        Queue<PushServer> servers = serverMap.get(topic);

        if (servers.size() == 1) {
            return servers.peek();
        }
        synchronized (servers) {
            PushServer s = servers.poll();
            servers.add(s);
            return s;
        }

    }

    public void setSubConfigurerService(SubscriptionConfigurerService subConfigurerService) {
        this.subConfigurerService = subConfigurerService;
    }

}
