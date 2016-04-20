package com.xiaoluo.klog;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * Created by Administrator on 2016/4/15.
 */
public class KafkaLogConverter {
    public static class ServiceNameConvert extends ClassicConverter {
        @Override
        public String convert(ILoggingEvent event) {
            if(event instanceof KLogEvent){
                KLogEvent rpcLogEventVO=(KLogEvent)event;
                return rpcLogEventVO.getServiceName();
            }
            return "";
        }
    }
    public static class AddressConvert extends ClassicConverter{
        @Override
        public String convert(ILoggingEvent event) {
            if(event instanceof KLogEvent){
                KLogEvent rpcLogEventVO=(KLogEvent)event;
                return rpcLogEventVO.getAddress();
            }
            return "";
        }
    }
}
