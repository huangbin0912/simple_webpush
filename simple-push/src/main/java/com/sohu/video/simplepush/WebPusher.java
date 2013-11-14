/**
 * Copyright (c) 2013 Sohu. All Rights Reserved
 */
package com.sohu.video.simplepush;

import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cometd.annotation.Configure;
import org.cometd.annotation.Service;
import org.cometd.annotation.Session;
import org.cometd.bayeux.client.ClientSessionChannel;
import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.bayeux.server.ConfigurableServerChannel;
import org.cometd.bayeux.server.ServerChannel;
import org.cometd.bayeux.server.ServerSession;
import org.cometd.server.authorizer.GrantAuthorizer;
import org.cometd.server.filter.DataFilterMessageListener;
import org.cometd.server.filter.NoMarkupFilter;
import org.cometd.server.transport.HttpTransport;

import com.sohu.video.simplepush.message.PushMessage;
import com.sohu.video.simplepush.util.WebPushUtil;

/**
 * @author erichuang
 *
 */
@javax.inject.Named // Tells Spring that this is a bean
@javax.inject.Singleton // Tells Spring that this is a singleton
@Service("simplepush")
public class WebPusher {
	
	//web push subscription prefix
	public static final String WEBPUSH_SUBSCRIPTION_PREFIX = "/simplepush/";
	
	public static final int SUBSCRIPTION_PREFIX_START = WEBPUSH_SUBSCRIPTION_PREFIX.length();
	
	private final Log sessionlog = LogFactory.getLog("webpush.sessionlog");
	
	private final Log log = LogFactory.getLog(WebPusher.class);
	
	private BayeuxServer bayeuxServer;
	
	@Session
    private ServerSession serverSession;
    
    private boolean avoidInjectionTwice;
	
	
	// all topic channels
    private final ConcurrentHashMap<String, String> topics = new ConcurrentHashMap<String, String>();
    
    // sessionid --> channelid
    private final ConcurrentHashMap<String, String> sessionChannelMapping = new ConcurrentHashMap<String, String>();
	
	@Inject
    public void setBayeux(BayeuxServer bayeux) {
		
		if(avoidInjectionTwice){
			return;
		}
		
		log.debug("call setBayeux in WebPusher");
		
		this.bayeuxServer = bayeux;
		
		bayeuxServer.addListener(new BayeuxServer.ChannelListener() {
			
			@Override
			public void configureChannel(ConfigurableServerChannel channel) {
				// TODO Auto-generated method stub
				if(channel.getId().equals("/simplepush") || channel.getId().equals("/simplepush/*")){
					return;
				}
				
				String topic = WebPushUtil.getWebPushTopic(channel.getId());
				if(topic != null && topics.contains(topic)){
					channel.setPersistent(true);
					sessionlog.info("accept configureChannel:" + channel.getId());
				}else{
//					ServerChannel serverChannel = (ServerChannel)channel;
//					serverChannel.remove();
					sessionlog.info("not accept configureChannel:" + channel.getId());
				}
				
			}
			
			@Override
			public void channelRemoved(String channelId) {
				// TODO Auto-generated method stub
				String topic = WebPushUtil.getWebPushTopic(channelId);
				if(topic != null){
					topics.remove(topic);				
				}
				sessionlog.info("channelRemoved:" + channelId);
			}
			
			@Override
			public void channelAdded(ServerChannel channel) {
				// TODO Auto-generated method stub
				sessionlog.info("channelAdded:" + channel.getId());
			}
		});
		
		bayeuxServer.addListener(new BayeuxServer.SubscriptionListener() {
			
			@Override
			public void unsubscribed(ServerSession session, ServerChannel channel) {
				// TODO Auto-generated method stub
				String logmsg = "unsubscribed-session:" + session.getId() + ",channel:" + channel.getId() + ",ip:" + getClientIpAddress() + ",time:" + System.currentTimeMillis();
                sessionlog.info(logmsg);
                sessionChannelMapping.remove(session.getId(), channel.getId());
			}
			
			@Override
			public void subscribed(ServerSession session, ServerChannel channel) {
				// TODO Auto-generated method stub
				String logmsg = "subscribed-session:" + session.getId() + ",channel:" + channel.getId() + ",ip:" + getClientIpAddress() + ",time:" + System.currentTimeMillis();
                sessionlog.info(logmsg);
                sessionChannelMapping.put(session.getId(), channel.getId());
			}
		});
	
		avoidInjectionTwice = true;
	}
	
	@PostConstruct
	public void start(){
		log.info("WebPusher start!");
	}
	
	public void send2Topic(String channelId, Queue<PushMessage> messages){
		String topic = WebPushUtil.getWebPushTopic(channelId);
		if (!topics.contains(topic)) {
            log.error("cometChannel:" + channelId + " does not exist.");
        } else{
        	 try {
				ClientSessionChannel sessionChannel = serverSession.getLocalSession().getChannel(channelId);
				sessionChannel.publish(messages);
				log.info("send2Topic msg " + messages +" to " + channelId);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				log.error("send2Topic error  msg " + messages +" to " + channelId, e);
			}
        }
		
	}
	
	public void send2Session(PushMessage message){
		String channel;
		if ((channel = sessionChannelMapping.get(message.getSessionid())) != null) {
            try {
                ServerSession client = bayeuxServer.getSession(message.getSessionid());
                if (client != null) {
                    client.deliver(serverSession, channel, new PushMessage[]{message}, null);
                    log.info("send2Session msg " + message +" to " + message.getSessionid());
                }
            } catch (Exception e) {
                log.error("send2Session error msg " + message +" to " + message.getSessionid(), e);
            }
        }
	}
	
	public Set<String> getTopics() {
		return topics.keySet();
	}
	
	public ConcurrentHashMap<String, String> getTopicMap(){
		return topics;
	}

	@Configure({"/simplepush","/simplepush/*"})
    protected void configureChannel(ConfigurableServerChannel channel) {
        DataFilterMessageListener noMarkup = new DataFilterMessageListener(new NoMarkupFilter());
        channel.addListener(noMarkup);
        channel.setPersistent(true);
        sessionlog.info("accept configureChannel:" + channel.getId());
        channel.addAuthorizer(GrantAuthorizer.GRANT_ALL);
    }

	private String getClientIpAddress(){
		HttpTransport transport = (HttpTransport)bayeuxServer.getCurrentTransport();
        return (transport == null ? "" : WebPushUtil.getClientIpAddress(transport.getCurrentRequest()));
	}
	
	public static void main(String[] args) {
		String s = "/simplepush/demo/1";
		System.out.println(s.substring(SUBSCRIPTION_PREFIX_START, s.lastIndexOf('/')));

	}

}
