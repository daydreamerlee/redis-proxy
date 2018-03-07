package com.segment.proxy.server;

import com.segment.proxy.cache.Cache;
import com.segment.proxy.cache.CacheRecord;
import com.segment.proxy.clients.RedisClient;
import com.segment.proxy.configs.ProxyConfigs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import spark.*;
import spark.Response;


import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.stop;

/**
 * Created by umehta on 3/2/18.
 */
public class HttpServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpServer.class);

    private ProxyConfigs configs;
    private Cache cache;

    public HttpServer(ProxyConfigs configs, Cache cache) {
        this.configs = configs;
        this.cache = cache;
    }

    public void startServer() {
        createRoutes();
    }

    private void createRoutes() { //Easy to add more routes
        port(this.configs.getServerPort());

        get("/proxy", (req, res) -> {
            RequestHandler handler = new RequestHandler(cache);
            ServerResponse response = handler.serviceRequest(getKeyFromRequest(req));
            res.status(response.getCode());
            return response.getMsg();
        });
//        get("/*", (req, res) -> {
//            res.status(ServerResponseHelper.BAD_REQUEST_CODE);
//            return ServerResponseHelper.BAD_REQUEST_MSG;
//        });
    }

    private String getKeyFromRequest(Request req) {
        String key = "";
        try {
            if(req == null)
                return null;
            else {
                return req.queryParamOrDefault("key", null);
            }
        } catch(Exception e) {
            LOGGER.error("Error retrieving Key from Request : "+e.getMessage());
            return null;
        }
    }
}