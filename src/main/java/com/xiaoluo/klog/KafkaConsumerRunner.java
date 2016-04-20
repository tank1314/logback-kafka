package com.xiaoluo.klog;

import ch.qos.logback.classic.LoggerContext;
import com.xl.tool.redis.RedisKit;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Caedmon on 2016/4/19.
 */
public class KafkaConsumerRunner implements Runnable{
    private Consumer<String,Object> consumer;
    private static final Map<String,KafkaConsumerAppender> appenderCache=new HashMap<>();
    private Properties properties;
    private LoggerContext context;
    private volatile boolean started;
    private AtomicBoolean closed=new AtomicBoolean(false);
    public KafkaConsumerRunner(Properties properties,LoggerContext context){
        this.properties=properties;
        this.consumer=new KafkaConsumer(properties);
        this.context=context;
    }
    @Override
    public void run() {
        if(isStarted()){
           return;
        }
        String topicName=properties.getProperty("logback.topic.name");
        String fileNamePattern=properties.getProperty("logback.file.name.pattern");
        String logPattern=properties.getProperty("logback.log.pattern");
        String consumerGroupId=properties.getProperty("group.id");
        TopicPartition topicPartition=new TopicPartition(topicName,0);
        consumer.assign(Arrays.asList(topicPartition));
        started=true;
        //读取offset
        Long offset=RedisKit.get(consumerGroupId);
        if(offset==null){
           offset=0L;
        }
        try{
            consumer.seek(topicPartition,offset);
            while (!closed.get()) {
                ConsumerRecords<String, Object> records = consumer.poll(100);
                for (ConsumerRecord<String, Object> record : records) {
                    if(record.offset()<=offset){
                        continue;
                    }
                    KLogEvent event = (KLogEvent) record.value();
                    String appenderName=event.getServiceName()+"-"+event.getAddress();
                    KafkaConsumerAppender appender=appenderCache.get(appenderName);
                    if(appender==null) {
                        appender=new KafkaConsumerAppender(context,appenderName,event.getServiceName(),
                                event.getAddress(),fileNamePattern,
                                logPattern);
                        appenderCache.put(appenderName, appender);
                        appender.start();
                    }
                    appender.doAppend(event);
                    consumer.commitAsync();
                    RedisKit.set(consumerGroupId,record.offset());
                }
            }
        }catch (WakeupException e){
            if(!closed.get()){
                throw e;
            }
        }finally {
            consumer.close();
        }
    }
    public boolean isStarted(){
        return started;
    }
    public void shutdown(){
        closed.set(true);
        consumer.wakeup();
    }
}
