package com.dafy.klog.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.spi.PreSerializationTransformer;
import com.dafy.klog.logback.KLogEvent;

import java.io.Serializable;

/**
 * Created by Caedmon on 2016/4/1.
 */
public class KLogSerializationTransformer  implements PreSerializationTransformer<ILoggingEvent> {
    @Override
    public Serializable transform(ILoggingEvent event) {
        if(event==null) return null;
        if(event instanceof LoggingEvent) {
            return KLogEvent.build(event);
        }
        else if(event instanceof KLogEvent) return (KLogEvent)event;
        else throw new IllegalArgumentException("Unsupported type "+event.getClass().getName());
    }
}
