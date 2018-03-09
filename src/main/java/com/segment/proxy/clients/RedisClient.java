package com.segment.proxy.clients;

import com.segment.proxy.configs.ProxyConfigs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Initializes connection to Redis and set up a Resource pool of clients to reduce connection set up time when serving requests.
 * The number of these resources are initialized to be the same as the number of threads in the Proxy Server. This way each thread would
 * get one client and would not have to wait for another thread to complete.
 */
public class RedisClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisClient.class);
    private static String host;
    private static int port;
    private static int size;

    private static volatile JedisPool pool;

    /**
     *  Initializes the configs that are used to connect to the Redis Server
     * @param configs The configs provided to the Proxy
     */
    public static void intitializeClient(ProxyConfigs configs) {
        host = configs.getRedisUrl();
        port = configs.getRedisPort();
        size = configs.getThreadCount(); //Make Resource pool same as number of threads
    }

    /**
     * Return a [[Jedis]] client that is connected to the Redis Server.
     * Lazy threadsafe intialization of the Redis pool.
     * @return [[Jedis]] client
     */
    public static Jedis getRedisClient (){
        if(pool == null){
            synchronized (RedisClient.class) {
                if(pool == null) {
                    JedisPoolConfig config = setPoolConfig();
                    pool = new JedisPool(config, host, port);
                    LOGGER.info("Created Redis Connection pool : "+host +":"+port); //TODO: change to logger
                }
            }
        }
        return pool.getResource(); //Redis Pool is threadsafe so need not be in the synchronized block
    }


    /**
     * Sets the configs requires for the Jedis pool.
     * @return [[JedisPoolConfig]] that is used by the [[JedisPool]] to initialize
     */
    private static JedisPoolConfig setPoolConfig() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();

        //Set max connections to be equal to the number of threads so that
        //each thread gets its own instance and does not have to wait
        poolConfig.setMaxTotal(size);
        poolConfig.setMaxIdle(size);

        poolConfig.setBlockWhenExhausted(true);

        //Could be configurable
        //Max time to wait to obtain pool resource
        poolConfig.setMaxWaitMillis(60000); //Wait 60 seconds for client before timing out
        poolConfig.setMinEvictableIdleTimeMillis(30000);  //Release held resources if idle for 30 seconds

        return poolConfig;
    }

    /**
     * Closes the Resource pool
     */
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
