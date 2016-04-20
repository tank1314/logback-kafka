package com.xiaoluo.klog;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.joran.JoranConfigurator;
import com.xl.tool.redis.JedisResource;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.*;

/**
 * Created by Administrator on 2016/3/31.
 */
public class KlogConsumerStartup {
    public static void main(String[] args) throws Exception{
        PatternLayout.defaultConverterMap.put("serviceName",KafkaLogConverter.ServiceNameConvert.class.getName());
        PatternLayout.defaultConverterMap.put("address", KafkaLogConverter.AddressConvert.class.getName());
        String propConfig="logback-consumer.properties";
        String logbackConfig="logback-consumer.xml";
        if(args.length>=1){
            propConfig=args[0];
        }
        if(args.length>=2){
            logbackConfig=args[1];
        }
        Properties props=new Properties();
        InputStream in = KlogConsumerStartup.class.getClassLoader().getResourceAsStream(propConfig);
        props.load(in);
        LoggerContext context=initLogback(logbackConfig);
        initRedis(props);
        KafkaConsumerRunner executor=new KafkaConsumerRunner(props,context);
        executor.run();
    }
    public static void initRedis(Properties properties){
        JedisResource.getInstance().init(properties);
    }
    public static LoggerContext initLogback(String configFile) throws Exception{
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        JoranConfigurator configurator = new JoranConfigurator();
        context.reset();
        configurator.setContext(context);
        InputStream in = KlogConsumerStartup.class.getClassLoader().getResourceAsStream(configFile);
        configurator.doConfigure(in);
        return context;
    }
}
