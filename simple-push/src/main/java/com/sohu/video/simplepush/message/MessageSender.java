/**
 * 
 */
package com.sohu.video.simplepush.message;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.sohu.video.simplepush.WebPusher;

/**
 * @author erichuang
 *
 */
@Service("messageSender")
public class MessageSender {
	
	private final Log log = LogFactory.getLog(MessageSender.class);
	
	private final Map<String, TopicSender> workers = new HashMap<String, TopicSender>();
	
	@Autowired
    @Qualifier("synchronizer")
    private ConfigurerSynchronizer synchronizer;
	
	@Autowired
    @Qualifier("webPusher")
	private WebPusher simplepush;
	
	@Autowired
    @Qualifier("messageExecutor")
	private MessageConsumer consumer;
	
	static final int MIN_INTERVAL = 1000;
	
	private int senderInterval = MIN_INTERVAL;
	
	private final Thread.UncaughtExceptionHandler defaultExceptionHandler = new Thread.UncaughtExceptionHandler() {
		
		@Override
		public void uncaughtException(Thread t, Throwable e) {
			// TODO Auto-generated method stub
			log.error("Thread[" + t.getName() + "] exception . ", e);
		}
	};

	
	@PostConstruct
	void init(){
		
		int _interval = getSenderInterval();
		
		if(_interval > MIN_INTERVAL){
			senderInterval = _interval;
		}
		
		log.info("senderInterval is " + senderInterval);
		
		synchronizer.setConfigurerListener(new ConfigurerSynchronizer.ConfigurerListener(){

			@Override
			public void pushServerAdded(PushServer server) {
				// TODO Auto-generated method stub
				TopicSender sender = new TopicSender(server.getTopic(), server.getGroup(), senderInterval);
				log.info("------------add " + sender);
				workers.put(server.getTopic(), sender);
				Thread runner = new Thread(sender, "TopicSender_" + server.getTopic());
				runner.setUncaughtExceptionHandler(defaultExceptionHandler);
				runner.start();
			}

			@Override
			public void pushServerRemoved(PushServer server) {
				// TODO Auto-generated method stub
				TopicSender sender = workers.get(server.getTopic());
				log.info("------------remove " + sender);
				if(sender != null){
					sender.shutdown();
				}
				consumer.getMsg(server.getTopic());
				log.info("------------clear messages of " + server.getTopic());
			}

			@Override
			public void pushServerUpdated(PushServer server) {
				// TODO Auto-generated method stub
				TopicSender sender = new TopicSender(server.getTopic(), server.getGroup(), senderInterval);
				TopicSender oldsender = workers.get(server.getTopic());
				if(oldsender != null){
					oldsender.shutdown();
				}
				log.info("------------update old:" + oldsender + ", new:" + sender);
				Thread runner = new Thread(sender, "TopicSender_" + server.getTopic());
				runner.setUncaughtExceptionHandler(defaultExceptionHandler);
				runner.start();

			}
			
		});
		
		Thread synchronizeRunner = new Thread(synchronizer, "synchronizeRunner");
		synchronizeRunner.setUncaughtExceptionHandler(defaultExceptionHandler);
		synchronizeRunner.start();
	}
	
	private int getSenderInterval(){
		try {
			Properties props = new Properties();
			InputStream input = MessageSender.class.getClassLoader().getResourceAsStream("push.properties");
			props.load(input);
			input.close();
			return Integer.parseInt(props.getProperty("sender.interval"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.warn("getSenderInterval error.", e);
			return 0;
		}
	}
	
	
	class TopicSender implements Runnable{
		
		final int interval;
		
		final String topic;

        final String channelid;

        final int group;

        volatile boolean active = true;
        
        public TopicSender(String topic, int group, int interval){
            this.topic = topic;
            this.channelid = WebPusher.WEBPUSH_SUBSCRIPTION_PREFIX + topic;
            this.group = group;
            this.interval = interval > MIN_INTERVAL ? interval : MIN_INTERVAL;
        }

		@Override
		public void run() {
			// TODO Auto-generated method stub
			log.info("Thread " + Thread.currentThread().getName() + " start!");
			while(active){
				Queue<PushMessage> messages = consumer.getMsg(topic);
				for(int i = 1; i <= group; i++){
					try {
						String channel = channelid + "/" + i;
						if(messages != null && messages.size() > 0){							
							simplepush.send2Topic(channel, messages);
						}
		                Thread.sleep(interval);
		            } catch (Exception e) {
		                // TODO Auto-generated catch block
		            	log.error("[error] send message to topic:" + topic, e);
		            }
				}
				
			}
			log.info("Thread " + Thread.currentThread().getName() + " exit!");
		}
		
		void shutdown(){
            active = false;
        }

		@Override
		public String toString() {
			return "TopicSender [interval=" + interval + ", topic=" + topic
					+ ", num=" + group + "]";
		}
		
		
		
	}

}
