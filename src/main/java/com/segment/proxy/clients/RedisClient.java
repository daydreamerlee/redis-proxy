package com.segment.proxy.clients;

import com.segment.proxy.configs.ProxyConfigs;
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
    private static JedisPoolConfig poolConfig;

    public static void intitializeClient(ProxyConfigs configs) {
        host = configs.getRedisUrl();
        port = configs.getRedisPort();
        size = configs.getThreadCount(); //Make Resource pool same as number of threads
    }

    //Lazy threadsafe intialization
    public static JedisPool getPool (){
        if(pool == null){
            synchronized (RedisClient.class) {
                if(pool == null) {
                    setPoolConfig();
                    pool = new JedisPool(poolConfig, host);
                    System.out.println("Created pool"); //TODO: change to logger
                }
            }
        }
        return pool;
    }


    private static void setPoolConfig() {
        poolConfig = new JedisPoolConfig();

        //Set max connections to be equal to the number of threads so that
        //each thread gets its own instance and does not have to wait
        poolConfig.setMaxTotal(size);
        poolConfig.setMaxIdle(size);

        poolConfig.setBlockWhenExhausted(true);

        //Configure timeouts
        //Max time to wait to obtain pool resource
        poolConfig.setMaxWaitMillis(60000);

    }

    public static void closePool() {
        if(pool != null)
            pool.close();
    }
}
