/**
 * Copyright (c) 2013 Sohu. All Rights Reserved
 */
package com.sohu.video.simplepush;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cometd.annotation.ServerAnnotationProcessor;
import org.cometd.bayeux.server.BayeuxServer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ServletContextAware;

/**
 * @author erichuang
 *
 */
@Component
public class WebPushServerLoader implements DestructionAwareBeanPostProcessor, ServletContextAware, ApplicationContextAware{
	
	private final Log log = LogFactory.getLog(WebPushServerLoader.class);
	
	private BayeuxServer bayeuxServer;
    private ServerAnnotationProcessor processor;
    ApplicationContext applicationContext;

    private void setBayeux(BayeuxServer bayeuxServer){
//    	System.out.println("call setBayeux in WebPushConfigurer");
        this.bayeuxServer = bayeuxServer;
    }
    
    @PostConstruct
    private void init(){
    	log.debug("init bayeuxServer:" + bayeuxServer);
        this.processor = new ServerAnnotationProcessor(bayeuxServer);
    }

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName)
			throws BeansException {
		log.debug("bayeuxServer:" + bayeuxServer);
		processor.processDependencies(bean);
        processor.processConfigurations(bean);
        processor.processCallbacks(bean);
        return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName)
			throws BeansException {
		return bean;
	}

	@Override
	public void setServletContext(ServletContext servletContext) {
		// TODO Auto-generated method stub
		servletContext.setAttribute(BayeuxServer.ATTRIBUTE, bayeuxServer);
	}

	@Override
	public void postProcessBeforeDestruction(Object bean, String beanName)
			throws BeansException {
		// TODO Auto-generated method stub
		processor.deprocessCallbacks(bean);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		// TODO Auto-generated method stub
		this.applicationContext = applicationContext;
		setBayeux((BayeuxServer)applicationContext.getBean("bayeux"));
	}
	

}
