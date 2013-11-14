/**
 * 
 */
package com.sohu.video.simplepush.message;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.sohu.video.simplepush.WebPusher;
import com.sohu.video.simplepush.util.JsonUtil;
import com.sohu.video.simplepush.util.WebPushUtil;

/**
 * @author erichuang
 *
 */
@Service("synchronizer")
public class ConfigurerSynchronizer implements Runnable{
	
	@Autowired
    @Qualifier("pushConfigService")
    private WebPushConfigurerService pushConfigService;
	
	@Autowired
    @Qualifier("webPusher")
	private WebPusher simplepush;
	
	private Map<String, PushServer> settings = new HashMap<String, PushServer>();
	
	private final Log log = LogFactory.getLog(ConfigurerSynchronizer.class);
	
	private ConfigurerListener configurerListener;
	
	private Set<String> localAddresses = new HashSet<String>();
	
	volatile boolean active = true;
	
	public ConfigurerSynchronizer(){
		try {
			Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
			while(netInterfaces.hasMoreElements()){
				NetworkInterface ni = netInterfaces.nextElement();
				Enumeration<InetAddress> addresses = ni.getInetAddresses();
				while(addresses.hasMoreElements()){
					String ipaddress = addresses.nextElement().getHostAddress();
					if (ipaddress.indexOf(":") == -1) {						
						localAddresses.add(ipaddress);
						log.info("---find server ip address :" + ipaddress);
					}
				}
			}
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public ConfigurerListener getConfigurerListener() {
		return configurerListener;
	}

	public void setConfigurerListener(ConfigurerListener configurerListener) {
		this.configurerListener = configurerListener;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		log.info("ConfigurerSynchronizer start!");
		
		while(active){
			
			try{				
				synchronize();
			} catch (Exception e){
				
			}
			
			// sleep 
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
            	log.error("", e);
            }
		}
		
		log.info("ConfigurerSynchronizer exit!");
		
	}
	
	private void synchronize(){
		log.info("synchronize start");
		List<String> newTopics = pushConfigService.lrange("simplepush-topic", 0, -1);
		Map<String, PushServer> newSettings = new HashMap<String, PushServer>();
		if(newTopics != null){
			for(String topic : newTopics){
				try {
					String setting = pushConfigService.get(topic);
					if(StringUtils.isNotEmpty(setting)){							
						PushServer server = JsonUtil.fromJson(setting, PushServer.class);
						if(server != null){
							newSettings.put(topic, server);
						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			compire(newTopics, newSettings);
		}
		log.info("synchronize stop");
	}
	
	
	
	private void compire(List<String> newTopics, Map<String, PushServer> newSettings){
		
		//check add
		List<PushServer> addPushServers = addPushServers(newTopics, newSettings);
		
		//check remove
		List<PushServer> removePushServers = removePushServers(newTopics, newSettings);
		
		//check update
		List<PushServer> updatePushServers = updatePushServers(newTopics, newSettings);
		
		fireAddPushServerEvent(addPushServers);
		
		fireRemovePushServerEvent(removePushServers);
		
		fireUpdatePushServerEvent(updatePushServers);
		
	}
	
	private List<PushServer> addPushServers(List<String> newTopics, Map<String, PushServer> newSettings){
		List<PushServer> servers = new ArrayList<PushServer>();
		for(String topic : newTopics){
			if(simplepush.getTopicMap().contains(topic)){
				continue;
			}
			//topic not found
			PushServer server = newSettings.get(topic);
			if(server != null){
				List<String> urls = server.getUrls();
				if(urls != null && urls.size() > 0){
					for(String url : urls){
						String ipaddress = WebPushUtil.getIpAddressByUrl(url);
						if(localAddresses.contains(ipaddress)){
							//find local server ip
							String res = simplepush.getTopicMap().putIfAbsent(topic, topic);
							if(res == null){
								servers.add(server);
							}
							settings.put(topic, server);
						}
					}
				}
			}else{
				log.warn(topic + " config is not found, addPushServers method.");
			}
		}
		return servers;
	
	}
	
	private List<PushServer> removePushServers(List<String> newTopics, Map<String, PushServer> newSettings){
		List<PushServer> servers = new ArrayList<PushServer>();
		 
		for(String topic : simplepush.getTopics()){
			if(newTopics.contains(topic)){
				//ip not found
				PushServer nserver = newSettings.get(topic);
				if(nserver != null){
					List<String> urls = nserver.getUrls();
					if(urls != null && urls.size() > 0){
						boolean retain = false;
						for(String url : urls){
							String ipaddress = WebPushUtil.getIpAddressByUrl(url);
							if(localAddresses.contains(ipaddress)){
								retain = true;
								break;
							}
						}
						if(!retain){
							_doRemove(servers, topic, nserver);
						}
					}
				}
				continue;
			}
			//topic not found
			PushServer server = settings.get(topic);
			if(server != null){
				_doRemove(servers, topic, server);
			}else{
				log.warn(topic + " config is not found, removePushServers method.");
			}
		}
		return servers;
	}
	
	private void _doRemove(List<PushServer> servers, String topic, PushServer server){
		servers.add(server);
		simplepush.getTopics().remove(topic);
		settings.remove(topic);
	}
	
	private List<PushServer> updatePushServers(List<String> newTopics, Map<String, PushServer> newSettings){
		List<PushServer> servers = new ArrayList<PushServer>();
		for(String topic : newTopics){
			if(simplepush.getTopics().contains(topic)){
				PushServer newserver = newSettings.get(topic);
				PushServer oldserver = settings.get(topic);
				if(newserver != null){
					// group changed
					if(oldserver == null || !oldserver.getGroup().equals(newserver.getGroup())){
						servers.add(newserver);
						settings.put(topic, newserver);
					}
				}else{
					log.warn(topic + " config is not found, updatePushServers method.");
				}
			}
		}
		return servers;
	}
	
	private void fireAddPushServerEvent(List<PushServer> addPushServers){
		if(configurerListener != null){
			for(PushServer server : addPushServers){
				configurerListener.pushServerAdded(server);
			}
		}
	}
	
	private void fireRemovePushServerEvent(List<PushServer> removePushServers){
		if(configurerListener != null){
			for(PushServer server : removePushServers){
				configurerListener.pushServerRemoved(server);
			}
		}
	}
	
	private void fireUpdatePushServerEvent(List<PushServer> updatePushServers){
		if(configurerListener != null){
			for(PushServer server : updatePushServers){
				configurerListener.pushServerUpdated(server);
			}
		}		
	}
	
	interface ConfigurerListener{
		
		void pushServerAdded(PushServer server);
		
		
		void pushServerRemoved(PushServer server);
		
		
		void pushServerUpdated(PushServer server);
		
	}

}
