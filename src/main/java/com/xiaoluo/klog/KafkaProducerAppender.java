package com.xiaoluo.klog;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.net.SocketConnector;
import ch.qos.logback.core.spi.PreSerializationTransformer;
import ch.qos.logback.core.util.Duration;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.InterruptException;

import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2016/4/1.
 */
public class KafkaProducerAppender extends AppenderBase<ILoggingEvent> implements Runnable{
    private Producer<String,String> kafkaProducer;
    private int queueSize=1024;
    private String kafkaAddress;
    private String zookeeperAddress;
    private String kafkaTopic;
    private BlockingQueue<ILoggingEvent> queue= new LinkedBlockingDeque(this.queueSize);;
    private Duration eventDelayLimit = new Duration(100L);
    private PreSerializationTransformer transformer=new KLogSerializationTransformer();
    private String serviceName;
    static {
        PatternLayout.defaultConverterMap.put("serviceName",KafkaLogConverter.ServiceNameConvert.class.getName());
        PatternLayout.defaultConverterMap.put("address", KafkaLogConverter.AddressConvert.class.getName());
    }
    @Override
    public void start() {
        if(!this.isStarted()) {
            int errorCount = 0;
            if(kafkaAddress==null) {
                ++errorCount;
                this.addError("No kafka address specify");
            }

            if(this.zookeeperAddress == null) {
                ++errorCount;
                this.addError("No zookeeper address specify");
            }

            if(this.queueSize < 0) {
                ++errorCount;
                this.addError("Queue size must be non-negative");
            }

            if(kafkaTopic==null||kafkaTopic.trim().equals("")){
                ++errorCount;
                this.addError("No kafka topic specify");
            }
            if(errorCount == 0) {
                this.queue=new LinkedBlockingDeque<>(this.queueSize);
                buildKafkaProducer();
            }
            Thread t=new Thread(this);
            t.start();
            super.start();
        }
    }
    private void buildKafkaProducer(){
        Properties props=new Properties();
        System.out.println("KafkaAddress--------------"+kafkaAddress);
        props.put("bootstrap.servers", kafkaAddress);
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "com.xiaoluo.klog.JavaSerializer");
        kafkaProducer=new KafkaProducer(props);
    }
    @Override
    public void run() {
        try{
            while(!Thread.currentThread().isInterrupted()) {
                this.dispatchEvent();
            }
        }catch (InterruptException e){

        }
    }

    public void dispatchEvent(){
        while(true) {
            try{
                ILoggingEvent event = queue.take();
                KLogEvent kLogEvent=(KLogEvent) this.getTransformer().transform(event);
                kLogEvent.setServiceName(this.serviceName);
                if(kLogEvent instanceof KLogEvent){
                    ProducerRecord record = new ProducerRecord(this.kafkaTopic, "", kLogEvent);
                    Future<RecordMetadata> future=kafkaProducer.send(record);
                }
            }catch (Exception e){
                e.printStackTrace();
                this.addInfo(this.kafkaAddress+":"+e);
            }

        }
    }
    @Override
    public void stop() {
        if(this.isStarted()) {
            kafkaProducer.close();
            super.stop();
        }
    }
        @Override
    protected void append(ILoggingEvent event) {
            if(event != null && this.isStarted()) {
                try {
                    boolean e = this.queue.offer(event, this.eventDelayLimit.getMilliseconds(), TimeUnit.MILLISECONDS);
                    if(!e) {
                        this.addInfo("Dropping event due to timeout limit of [" + this.eventDelayLimit + "] milliseconds being exceeded");
                    }
                } catch (InterruptedException var3) {
                    this.addError("Interrupted while appending event to SocketAppender", var3);
                }

            }
    }

    public int getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }

    public String getKafkaAddress() {
        return kafkaAddress;
    }

    public void setKafkaAddress(String kafkaAddress) {
        this.kafkaAddress = kafkaAddress;
    }

    public String getZookeeperAddress() {
        return zookeeperAddress;
    }

    public void setZookeeperAddress(String zookeeperAddress) {
        this.zookeeperAddress = zookeeperAddress;
    }

    public String getKafkaTopic() {
        return kafkaTopic;
    }

    public void setKafkaTopic(String kafkaTopic) {
        this.kafkaTopic = kafkaTopic;
    }

    public BlockingQueue<ILoggingEvent> getQueue() {
        return queue;
    }

    public void setQueue(BlockingQueue<ILoggingEvent> queue) {
        this.queue = queue;
    }

    public Duration getEventDelayLimit() {
        return eventDelayLimit;
    }

    public void setEventDelayLimit(Duration eventDelayLimit) {
        this.eventDelayLimit = eventDelayLimit;
    }

    public PreSerializationTransformer getTransformer() {
        return transformer;
    }

    public void setTransformer(PreSerializationTransformer transformer) {
        this.transformer = transformer;
    }

    public Producer<String, String> getKafkaProducer() {
        return kafkaProducer;
    }

    public void setKafkaProducer(Producer<String, String> kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

}
