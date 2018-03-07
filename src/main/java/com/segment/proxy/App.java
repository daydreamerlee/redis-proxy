package com.segment.proxy;

import com.beust.jcommander.ParameterException;
import com.segment.proxy.cache.Cache;
import com.segment.proxy.cache.CacheRecord;
import com.segment.proxy.cache.LRUCacheImpl;
import com.segment.proxy.clients.RedisClient;
import com.segment.proxy.configs.ProxyConfigs;
import com.segment.proxy.redisServer.RedisApiServer;
import com.segment.proxy.server.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class App {

    private static Logger LOGGER = LoggerFactory.getLogger(App.class);

    public static void main(String[] args)  {
        try {
            //Parse Command Line args
            ProxyConfigs configs = new ProxyConfigs().parse(args);
            LOGGER.info(configs.configString());

            //Set up jedis Resource Pool
            RedisClient.intitializeClient(configs);
            startServer(configs);
        } catch (ParameterException e) {
              LOGGER.error("Invalid Arguments. See usage :  --help ");
        } catch (Exception e) {
            LOGGER.error("Exception in Proxy Server. See logs for details : "+e.getMessage());
        } finally {
            RedisClient.closePool();
        }
    }

    private static void startServer(ProxyConfigs configs) throws Exception {
        //Initialize LRU cache
        Cache<String, CacheRecord<String>> cache = new LRUCacheImpl<>(configs.getCacheSize(), configs.getCacheExpiration());
        String serverType = configs.getServerType().toLowerCase();

        if(serverType.equals("http")) {
            //Start HttpServer
            HttpServer server = new HttpServer(configs, cache);
            server.startServer();
        } else if (serverType.equals("redisapi")){
            //Start Redis API Server
            RedisApiServer redisApiServer = new RedisApiServer(configs.getServerPort(), configs.getThreadCount(), cache);
            redisApiServer.startServer();
        } else {
            LOGGER.error("Invalid server type. Valid types are : http/redisApi");
        }
    }
}
