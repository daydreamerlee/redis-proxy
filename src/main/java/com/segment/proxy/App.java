package com.segment.proxy;

import com.beust.jcommander.JCommander;
import com.segment.proxy.cache.Cache;
import com.segment.proxy.cache.CacheRecord;
import com.segment.proxy.cache.LRUCacheImpl;
import com.segment.proxy.clients.RedisClient;
import com.segment.proxy.configs.ProxyConfigs;
import com.segment.proxy.redisServer.RedisApiServer;
import com.segment.proxy.server.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import static spark.Spark.*;

/**
 * Created by umehta on 3/2/18.
 */
public class App {

    private static Logger LOGGER = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws Exception {
        //Parse Command Line args
        ProxyConfigs configs = new ProxyConfigs().parse(args);
        LOGGER.info(configs.toString());

        //Set up jedis Resource Pool
        //RedisClient.intitializeClient(configs);

        //Initialize LRU cache
        //Cache<String, CacheRecord<String>> cache = new LRUCacheImpl<String, String>(configs.getCacheSize(), configs.getCacheExpiration());

        //Start HttpServer
//        HttpServer server = new HttpServer(configs, cache);
//        server.startServer();

        //Start Redis API Server
        RedisApiServer redisApiServer = new RedisApiServer(configs.getWebServerPort(), configs.getThreadCount());
        redisApiServer.run();
    }
}
