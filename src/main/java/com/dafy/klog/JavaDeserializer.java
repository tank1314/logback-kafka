package com.dafy.klog;

import org.apache.kafka.common.serialization.Deserializer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Map;

/**
 * Created by Administrator on 2016/4/1.
 */
public class JavaDeserializer implements Deserializer<Object>{
    public JavaDeserializer() {
        super();
    }

    @Override
    public void configure(Map<String, ?> map, boolean b) {

    }

    @Override
    public Object deserialize(String s, byte[] bytes) {

        try {
            ByteArrayInputStream bis=new ByteArrayInputStream(bytes);
            ObjectInputStream ois=new ObjectInputStream(bis);
            return ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void close() {

    }
}
