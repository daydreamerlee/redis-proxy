package com.segment.proxy.helper;

import com.segment.proxy.clients.RedisClient;
import com.segment.proxy.configs.ProxyConfigs;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * Created by umehta on 3/3/18.
 */
public class RedisWriter {
    public static void main(String[] args) throws Exception {

//        Jedis js = new Jedis("localhost");
//        String[] keys = {"foo", "bar", "xyz", "abc", "world", "yes"};
//        String[] values = {"fooval", "barval", "xyzval", "abcval", "worldval", "no"};
//
//        for(int i = 0; i < keys.length; i ++){
//            js.set(keys[i], values[i]);
//
//        }
//
//        js.close();
//        RedisClient.intitializeClient(new ProxyConfigs());
//        JedisPool pool = RedisClient.getPool();
//        Jedis jd = pool.getResource();
//        System.out.println(pool.getNumIdle());
//        jd.close();;
//        pool.close();;
//        RedisServer server = RedisServer.newRedisServer();  // bind to a random port
//        server.start();
//        String host = server.getHost();
//        int port = server.getBindPort();
//
////        Jedis jd = new Jedis(host, port);
//
//        //assertEquals("Number of active pool connections should equal set value", 1, RedisClient.getPool().getNumActive());
//        jd.close();
//        server.stop();
    }
}
