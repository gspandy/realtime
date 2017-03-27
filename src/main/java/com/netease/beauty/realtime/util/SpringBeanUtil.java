package com.netease.beauty.realtime.util;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class SpringBeanUtil implements ApplicationContextAware, DisposableBean {

	private static final Object LOCK = new Object();
	
	private static ApplicationContext applicationContext;
	
	@Override
	public void destroy() throws Exception {
		applicationContext = null;
	}
	
	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		synchronized (LOCK) {
			SpringBeanUtil.applicationContext = context;
		}
	}

	public static <T> T getBean(String name, Class<T> clazz) {
		if (applicationContext == null) {
			return null;
		}
		return (T) applicationContext.getBean(name, clazz);
	}
}
