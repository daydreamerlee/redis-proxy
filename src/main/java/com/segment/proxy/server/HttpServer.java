package com.segment.proxy.server;

import com.segment.proxy.cache.Cache;
import com.segment.proxy.cache.CacheRecord;
import com.segment.proxy.clients.RedisClient;
import com.segment.proxy.configs.ProxyConfigs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import spark.*;
import spark.Response;


import static spark.Spark.get;

/**
 * Created by umehta on 3/2/18.
 */
public class HttpServer {
    private static Logger LOGGER = LoggerFactory.getLogger(HttpServer.class);

    private ProxyConfigs configs;
    private JedisPool pool;
    private Cache<String, CacheRecord<String>> cache;

    public HttpServer(ProxyConfigs configs, Cache<String, CacheRecord<String>> cache) {
        this.configs = configs;
        this.cache = cache;
    }

    public void startServer() {
        createRoutes();
    }

    private void createRoutes() { //Easy to add more routes
        get("/proxy", (req, res) -> {
            serviceRequest(req, res);
            return res.body();
        });
    }

    private void serviceRequest(Request request, Response response) {
        Jedis jd = null;
        try {
            // Get key from request
            String key = getKeyFromRequest(request);

            if(key == null){
                LOGGER.debug("Bad Request. Returning 400");
                buildResponse(response, ServerResponse.BAD_REQUEST_MSG, ServerResponse.BAD_REQUEST_CODE);
                return;
            }

            // Check if it exists in local cache
            CacheRecord<String> value = getFromCache(key);

            if(!(value == null)) {
                LOGGER.debug("Key found in Cache : "+key);
                buildResponse(response, value.getValue(), ServerResponse.SUCCESS_CODE);
            }else {
                // If not get it from Redis
                LOGGER.debug("Cache Miss. Fetching key "+key+ "from Redis");
                jd = RedisClient.getPool().getResource();

                String redisValue = getFromRedis(jd, key);
                if (redisValue == null) {
                    LOGGER.debug("Key not found in Redis : "+key);
                    buildResponse(response, ServerResponse.NOT_FOUND_MSG, ServerResponse.NOT_FOUND_CODE);
                }
                else {
                    LOGGER.debug("Key found in Redis : "+key);
                    cache.set(key, new CacheRecord<>(redisValue, System.currentTimeMillis()));
                    buildResponse(response, redisValue, ServerResponse.SUCCESS_CODE);
                }
            }
        }catch (Exception e) {
            LOGGER.error("Error Servicing request : "+e.getMessage());
            buildResponse(response, ServerResponse.FAILURE_MSG, ServerResponse.FAILURE_CODE);
        } finally {
            if(jd != null)
                jd.close();
        }
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

    private CacheRecord<String> getFromCache(String key) {
        return cache.get(key);
    }

    private String getFromRedis(Jedis jd, String key) {
        return jd.get(key);
    }

    private void buildResponse(Response response, String body, int code) {
        response.body(body);
        response.status(code);
    }
}
