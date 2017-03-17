package com.dafy.klog;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

/**
 * Created by Caedmon on 2017/3/2.
 */
public class ProducerServer {
    private static final Logger log=LoggerFactory.getLogger(ProducerServer.class);
    public static void main(String[] args) throws Exception{
            LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
            JoranConfigurator configurator = new JoranConfigurator();
            context.reset();
            configurator.setContext(context);
            String server="C";
            InputStream in = ProducerServer.class.getClassLoader().getResourceAsStream("producer-"+server+".xml");
            configurator.doConfigure(in);
            int i=0;
            while (true){
                Thread.sleep(10);
                log.info(server+" Log "+(++i));
            }

    }
}
