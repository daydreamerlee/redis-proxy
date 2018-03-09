package com.segment.proxy;

import com.beust.jcommander.ParameterException;
import com.segment.proxy.cache.Cache;
import com.segment.proxy.cache.CacheRecord;
import com.segment.proxy.cache.LRUCacheImpl;
import com.segment.proxy.clients.RedisClient;
import com.segment.proxy.configs.ProxyConfigs;
import com.segment.proxy.server.redisServer.RedisApiServer;
import com.segment.proxy.server.http.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main Application that parses the command line arguments and starts the proxy server.
 *
 * Usage :
 * java -cp target/redis-proxy-1.0-SNAPSHOT-uber.jar com.segment.proxy.App --help
         Usage: <main class> [options]
         Options:
         -a, --address
         Backing Redis address
         Default: localhost
         -c, --capacity
         Cache Capacity
         Default: 100
         -e, --expiry
         Cache Expiration Time in Seconds
         Default: 120
         -h, --help
         Print help information and exit
         Default: false
         -n, --num-threads
         Number of threads to serve requests
         Default: 20
         -p, --redis-port
         Backing Redis Port
         Default: 6379
         -w, --server-port
         HTTP/RedisAPI Server Port
         Default: 8080
         -t, --server-type
         http/redisAPI
         Default: http
 */
public class App {
    private static Logger LOGGER = LoggerFactory.getLogger(App.class);
    private static final String HTTP_SERVER = "http";
    private static final String REDIS_SERVER = "redisapi";

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
            System.exit(1);
        } finally {
            RedisClient.closePool();
        }
    }

    /**
     * Starts the server based on the provided argument. Defaults to http server.
     * @param configs The configs to be used by the Proxy
     * @throws Exception
     */
    private static void startServer(ProxyConfigs configs) throws Exception  {
        String serverType = configs.getServerType().toLowerCase();
        Cache<String, CacheRecord<String>> cache = null;
        if(serverType.equals(HTTP_SERVER)) {
            //Start HttpServer
            //Initialize LRU cache only if server is valid. Would avoid starting eviction thread in the cache
            cache = new LRUCacheImpl<>(configs.getCacheSize(), configs.getCacheExpiration());

            HttpServer server = new HttpServer(configs, cache);
            server.startServer();
        } else if (serverType.equals(REDIS_SERVER)){
            //Start Redis API Server
            cache = new LRUCacheImpl<>(configs.getCacheSize(), configs.getCacheExpiration());

            RedisApiServer redisApiServer = new RedisApiServer(configs.getServerPort(), configs.getThreadCount(), cache);
            redisApiServer.startServer();
        } else {
            throw new IllegalArgumentException("Invalid server type. Valid types are : http/redisApi");
        }
    }
}
