package com.dafy.klog.consumer;

import ch.qos.logback.classic.PatternLayout;
import com.dafy.klog.logback.KafkaLogConverter;

import java.io.InputStream;
import java.util.Properties;

/**
 * Created by Administrator on 2016/3/31.
 */
public class KlogConsumerStartup {
    static {
        PatternLayout.defaultConverterMap.put("sn",KafkaLogConverter.ServiceNameConvert.class.getName());
        PatternLayout.defaultConverterMap.put("addr", KafkaLogConverter.AddressConvert.class.getName());
        PatternLayout.defaultConverterMap.put("pid",KafkaLogConverter.PidConvert.class.getName());
    }
    public static void main(String[] args) throws Exception{
        String propConfig="klog-consumer.properties";
        Properties props=new Properties();
        InputStream in = KlogConsumerStartup.class.getClassLoader().getResourceAsStream(propConfig);
        props.load(in);
        ConsumerController invoker=new ConsumerController(props);
        invoker.start();
    }

}
