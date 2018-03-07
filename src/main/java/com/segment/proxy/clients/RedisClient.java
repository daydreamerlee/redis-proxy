package com.segment.proxy.clients;

import com.segment.proxy.configs.ProxyConfigs;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Created by umehta on 3/2/18.
 */
public class RedisClient {
    private static String host;
    private static int port;
    private static int size;

    private static volatile JedisPool pool;

    public static void intitializeClient(ProxyConfigs configs) {
        host = configs.getRedisUrl();
        port = configs.getRedisPort();
        size = configs.getThreadCount(); //Make Resource pool same as number of threads
    }

    //Lazy threadsafe intialization
    public static Jedis getRedisClient (){
        if(pool == null){
            synchronized (RedisClient.class) {
                if(pool == null) {
                    JedisPoolConfig config = setPoolConfig();
                    pool = new JedisPool(config, host, port);
                    System.out.println("Created pool : "+host); //TODO: change to logger
                }
            }
        }
        return pool.getResource();
    }


    private static JedisPoolConfig setPoolConfig() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();

        //Set max connections to be equal to the number of threads so that
        //each thread gets its own instance and does not have to wait
        poolConfig.setMaxTotal(size);
        poolConfig.setMaxIdle(size);

        poolConfig.setBlockWhenExhausted(true);

        //Configure timeouts - Make it configurable
        //Max time to wait to obtain pool resource
        poolConfig.setMaxWaitMillis(60000);
        poolConfig.setMinEvictableIdleTimeMillis(30000);  //Release held resources if idle

        return poolConfig;
    }

    public static void closePool() {
        if(pool != null) {
            synchronized (RedisClient.class) {
                if(pool != null) {
                    pool.close();
                    pool = null;
                }
            }
        }

    }
}
