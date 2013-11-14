/**
 * 
 */
package com.sohu.video.simplepush.util;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;

import com.sohu.video.simplepush.WebPusher;

/**
 * @author erichuang
 *
 */
public class WebPushUtil {
	
	/**
	 * 获取Client IP : 此方法能够穿透squid 和 proxy
	 * 
	 * @param request
	 * @return .
	 */
	public static String getClientIpAddress(HttpServletRequest request) {
		String ip = request.getHeader("dian-remote");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("x-forwarded-for");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		if (ip.indexOf(",") > 0)
			ip = ip.substring(0, ip.indexOf(","));
		return ip;
	}
	
	public static String getIpAddressByUrl(String url){
		URL _url;
		try {
			_url = new URL(url);
			return _url.getHost();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 获取webpush topic
	 * @param channelid
	 * @return
	 */
	public static String getWebPushTopic(String channelid){
		if(channelid.length() <= WebPusher.SUBSCRIPTION_PREFIX_START){
			return null;
		}
		if(channelid.charAt(channelid.length() - 1) != '/'){
			channelid = channelid + "/";
		}
		try {
			channelid = channelid.substring(WebPusher.SUBSCRIPTION_PREFIX_START);
			return channelid.substring(0, channelid.indexOf('/'));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

}
