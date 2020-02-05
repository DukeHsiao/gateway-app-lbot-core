package com.systex.jbranch.host.context;

import org.springframework.context.ApplicationContext;

public class Context {

	private static ApplicationContext context;

	public static Object getBean(String beanId){
		return context.getBean(beanId);
	}
	
	public static <T> T getBean(Class<T> clazz){
		return context.getBean(clazz);
	}
	
	public static <T> T getBean(String beanId, Class<T> clazz){
		return context.getBean(beanId, clazz);
	}
	
	/**
	 * @return the context
	 */
	public static ApplicationContext getContext() {
		return context;
	}

	/**
	 * @param context the context to set
	 */
	public static void setContext(ApplicationContext c) {
		context = c;
	}
	
	
}
