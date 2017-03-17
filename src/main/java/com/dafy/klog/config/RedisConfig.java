package com.dafy.klog.config;

import redis.clients.jedis.JedisPoolConfig;

/**
 * Created by Caedmon on 2017/3/16.
 */
public class RedisConfig {
    public JedisPoolConfig jedisPoolConfig;
    public String host;
    public int port;
    public int database;
}
