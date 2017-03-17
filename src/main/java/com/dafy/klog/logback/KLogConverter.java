package com.dafy.klog.logback;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * Created by Administrator on 2016/4/15.
 */
public class KLogConverter {
    public static class ServiceNameConvert extends ClassicConverter {
        @Override
        public String convert(ILoggingEvent event) {
            if(event instanceof KLogEvent){
                KLogEvent eventVO=(KLogEvent)event;
                return eventVO.getServiceName();
            }
            return "";
        }
    }
    public static class AddressConvert extends ClassicConverter{
        @Override
        public String convert(ILoggingEvent event) {
            if(event instanceof KLogEvent){
                KLogEvent eventVO=(KLogEvent)event;
                return eventVO.getAddress();
            }
            return "";
        }
    }
    public static class PidConvert extends ClassicConverter{
        @Override
        public String convert(ILoggingEvent event) {
            if(event instanceof KLogEvent){
                KLogEvent eventVO=(KLogEvent)event;
                return eventVO.getPid();
            }
            return "";
        }
    }
}
