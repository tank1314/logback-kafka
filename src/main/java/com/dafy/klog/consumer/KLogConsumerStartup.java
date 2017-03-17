package com.dafy.klog.consumer;

import ch.qos.logback.classic.PatternLayout;
import com.dafy.klog.logback.KLogConverter;

import java.io.InputStream;
import java.util.Properties;

/**
 * Created by Administrator on 2016/3/31.
 */
public class KLogConsumerStartup {
    static {
        PatternLayout.defaultConverterMap.put("sn",KLogConverter.ServiceNameConvert.class.getName());
        PatternLayout.defaultConverterMap.put("addr", KLogConverter.AddressConvert.class.getName());
        PatternLayout.defaultConverterMap.put("pid",KLogConverter.PidConvert.class.getName());
    }
    public static void main(String[] args) throws Exception{
        String propConfig="klog-consumer.properties";
        Properties props=new Properties();
        InputStream in = KLogConsumerStartup.class.getClassLoader().getResourceAsStream(propConfig);
        props.load(in);
        KLogConsumerController invoker=new KLogConsumerController(props);
        invoker.start();
    }

}
