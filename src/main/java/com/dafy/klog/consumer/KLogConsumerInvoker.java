package com.dafy.klog.consumer;

import ch.qos.logback.classic.LoggerContext;
import com.dafy.klog.logback.KLogEvent;
import com.dafy.klog.offset.OffsetRecorder;
import com.dafy.klog.offset.RedisOffsetRecorder;
import com.dafy.klog.config.OffsetConfig;
import com.dafy.klog.config.RedisConfig;
import com.google.common.base.Strings;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Caedmon on 2017/3/15.
 */
public class KLogConsumerInvoker implements Runnable{
    private Properties properties;
    private int partition;
    private KafkaConsumer<String,Object> kafkaConsumer;
    private OffsetRecorder offsetRecorder;
    private AtomicBoolean closed=new AtomicBoolean(false);
    private LoggerContext context;
    private static final Logger log= LoggerFactory.getLogger(KLogConsumerInvoker.class);
    /**
     * 不同appender的缓存
     * */
    private static final Map<String,KLogConsumerAppender> appenderCache=new ConcurrentHashMap<>();
    public KLogConsumerInvoker(Properties properties, LoggerContext context, int partition){
        this.properties=properties;
        this.context=context;
        this.partition=partition;
        this.kafkaConsumer=new KafkaConsumer(properties);
        this.offsetRecorder=buildOffsetRecorder(properties);
    }
    public OffsetRecorder buildOffsetRecorder(Properties properties){
        RedisConfig redisConfig=new RedisConfig();
        JedisPoolConfig jedisPoolConfig=new JedisPoolConfig();
        jedisPoolConfig.setMinIdle(2);
        jedisPoolConfig.setMaxIdle(5);
        jedisPoolConfig.setTestOnReturn(true);
        jedisPoolConfig.setTestOnCreate(true);
        jedisPoolConfig.setTestOnBorrow(true);
        jedisPoolConfig.setMaxTotal(5);
        redisConfig.jedisPoolConfig=jedisPoolConfig;
        redisConfig.host=properties.getProperty("redis.host");
        redisConfig.port=Integer.parseInt(properties.getProperty("redis.port"));
        String topicName=properties.getProperty("kafka.topic.name");
        String groupId=properties.getProperty("kafka.group.id");
        OffsetConfig offsetConfig=new OffsetConfig();
        offsetConfig.groupId=groupId;
        offsetConfig.topicName=topicName;
        offsetConfig.partition=partition;
        OffsetRecorder recorder=new RedisOffsetRecorder(redisConfig,offsetConfig);
        return recorder;
    }
    @Override
    public void run() {
        String topicName=properties.getProperty("kafka.topic.name");
        String pullIntervalValue=properties.getProperty("kafka.pull.interval");
        long pullInterval=100;
        if(!Strings.isNullOrEmpty(pullIntervalValue)){
            pullInterval=Long.valueOf(pullIntervalValue);
        }
        TopicPartition topicPartition=new TopicPartition(topicName,partition);
        kafkaConsumer.assign(Arrays.asList(topicPartition));
        //读取offset
        Long offset=offsetRecorder.getOffset();
        try{
            kafkaConsumer.seek(topicPartition,offset);
            while (!closed.get()) {
                //从kafka pull 消息
                ConsumerRecords<String, Object> records = kafkaConsumer.poll(pullInterval);
                long lastRecordOffset=offset;
                for (ConsumerRecord<String, Object> record : records) {
                    if(record.offset()<=offset){
                        continue;
                    }
                    KLogEvent event = (KLogEvent) record.value();
                    KLogConsumerAppender appender=getAppender(event);
                    appender.doAppend(event);
                    lastRecordOffset=record.offset();
                }
                if(!records.isEmpty()){
                    kafkaConsumer.commitAsync(new OffsetCommitCallback() {
                        @Override
                        public void onComplete(Map<TopicPartition, OffsetAndMetadata> offsets, Exception exception) {
                            if(exception!=null){
                                exception.printStackTrace();
                            }
                        }
                    });
                }

                if(lastRecordOffset>offset){
                    offsetRecorder.setOffset(lastRecordOffset);
                }
            }
        }catch (Throwable e){
            if(!closed.get()){
                log.error("pull log error",e);
            }
        }finally {
            kafkaConsumer.close();
        }
    }
    /**
     * 获取Appender
     * */
    private KLogConsumerAppender getAppender(KLogEvent event){
        String fileNamePattern=this.properties.getProperty("logback.fileName.pattern");
        String logPattern=this.properties.getProperty("logback.log.pattern");
        String appenderName=event.getServiceName()+"-"+event.getAddress();
        KLogConsumerAppender appender=appenderCache.get(appenderName);
        if(appender==null) {
            appender=new KLogConsumerAppender(context,appenderName,event.getServiceName(),
                    event.getAddress(),fileNamePattern,
                    logPattern);
            appenderCache.put(appenderName, appender);
            appender.start();
        }
        return appender;
    }

}
