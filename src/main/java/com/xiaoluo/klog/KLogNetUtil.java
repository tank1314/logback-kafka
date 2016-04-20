package com.xiaoluo.klog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by Administrator on 2016/4/1.
 */
public class KLogNetUtil {
    private static final Logger log= LoggerFactory.getLogger(KLogNetUtil.class);
    private static String localhost=null;
    public static String getLocalHost() throws SocketException {
        if(localhost!=null){
            return localhost;
        }
        for (Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces(); interfaces.hasMoreElements();) {
            NetworkInterface networkInterface = interfaces.nextElement();
            if (networkInterface.isLoopback()|| !networkInterface.isUp()) {
                continue;
            }
            Enumeration<InetAddress> enumeration = networkInterface.getInetAddresses();
            while (enumeration.hasMoreElements()) {
                InetAddress inetAddress=enumeration.nextElement();
                if(inetAddress instanceof Inet4Address){
                    String hostAddress=inetAddress.getHostAddress();
                    if(!hostAddress.contains("localhost")&&!hostAddress.contains("127.0.0.1")){
                        log.debug("Get local host {}",hostAddress);
                        localhost=hostAddress;
                        return hostAddress;
                    }
                }


            }
        }
        return null;
    }
}
