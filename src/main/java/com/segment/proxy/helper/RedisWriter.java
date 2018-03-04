package com.segment.proxy.helper;

import com.segment.proxy.clients.RedisClient;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * Created by umehta on 3/3/18.
 */
public class RedisWriter {
    public static void main(String[] args) {

        Jedis js = new Jedis("localhost");
        String[] keys = {"foo", "bar", "xyz", "abc", "world", "yes"};
        String[] values = {"fooval", "barval", "xyzval", "abcval", "worldval", "no"};

        for(int i = 0; i < keys.length; i ++){
            js.set(keys[i], values[i]);

        }

        js.close();
    }
}
