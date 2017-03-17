package com.dafy.klog;

import org.apache.kafka.common.serialization.Serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;

/**
 * Created by Administrator on 2016/4/1.
 */
public class JavaSerializer implements Serializer<Object> {
    public JavaSerializer() {
        super();
    }

    @Override
    public void configure(Map<String, ?> map, boolean b) {

    }

    @Override
    public void close() {

    }

    @Override
    public byte[] serialize(String s, Object serializable) {
        ByteArrayOutputStream bos=new ByteArrayOutputStream();
        try {
            ObjectOutputStream ex = new ObjectOutputStream(bos);
            ex.writeObject(serializable);
            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
