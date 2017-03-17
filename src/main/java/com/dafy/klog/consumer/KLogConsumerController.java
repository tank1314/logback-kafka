package com.dafy.klog.consumer;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Caedmon on 2016/4/19.
 */
public class KLogConsumerController {
    private Properties properties;
    private LoggerContext context;
    private  ExecutorService executorService;
    private volatile boolean started;
    public KLogConsumerController(Properties properties) throws Exception{
        this.properties=properties;
        initContext();
    }
    public LoggerContext initContext() throws Exception{
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        JoranConfigurator configurator = new JoranConfigurator();
        context.reset();
        configurator.setContext(context);
        String configPath=properties.getProperty("logback.config.path");
        InputStream in = this.getClass().getClassLoader().getResourceAsStream(configPath);
        configurator.doConfigure(in);
        this.context=context;
        return context;
    }
    public void start() {
        if(isStarted()){
           return;
        }
        String partitionValue=properties.getProperty("kafka.consumer.partition");
        int partition=1;
        if(partitionValue!=null){
            partition=Integer.parseInt(partitionValue);
        }
        executorService=Executors.newFixedThreadPool(partition);
        for(int i=0;i<partition;i++){
            KLogConsumerInvoker consumer=new KLogConsumerInvoker(properties,context,i);
            executorService.execute(consumer);
        }
        this.started=true;

    }
    public boolean isStarted(){
        return started;
    }

}
