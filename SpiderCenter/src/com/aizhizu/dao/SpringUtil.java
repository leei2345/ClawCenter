package com.aizhizu.dao;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringUtil
{

    private SpringUtil()
    {
    }

    public static synchronized ApplicationContext getSpringContext()
    {
        if(springContext == null)
            springContext = new ClassPathXmlApplicationContext(CONFIG);
        return springContext;
    }

	private static final String CONFIG = "classpath:applicationContext.xml";
    private static ApplicationContext springContext;
}