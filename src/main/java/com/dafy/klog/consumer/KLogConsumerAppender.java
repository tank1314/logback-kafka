package com.dafy.klog.consumer;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;

/**
 * Created by Caedmon on 2016/4/18.
 */
public class KLogConsumerAppender extends RollingFileAppender {
    private String serviceName;
    private String address;
    private String fileNamePattern;
    private String logPattern;
    private String appenderName;
    public KLogConsumerAppender(LoggerContext context, String appenderName, String serviceName,
                                String address, String fileNamePattern, String logPattern){
        this.appenderName=appenderName;
        this.serviceName=serviceName;
        this.address=address;
        this.fileNamePattern=fileNamePattern;
        this.logPattern=logPattern;
        this.context=context;
    }
    public void start(){
        if(isStarted()){
            return;
        }
        setName(appenderName);
        setFile("logs/"+serviceName+"/"+serviceName+"-"+address+".log");
        TimeBasedRollingPolicy policy=new TimeBasedRollingPolicy<>();
        policy.setFileNamePattern(fileNamePattern);
        policy.setParent(this);
        policy.setContext(context);
        policy.start();
        this.setRollingPolicy(policy);
        PatternLayoutEncoder encoder=new PatternLayoutEncoder();
        encoder.setContext(context);
        setEncoder(encoder);
        encoder.setPattern(logPattern);
        encoder.start();
        super.start();
    }
}
