package com.segment.proxy.server;

import com.segment.proxy.cache.Cache;
import com.segment.proxy.cache.CacheRecord;
import com.segment.proxy.clients.RedisClient;
import com.segment.proxy.clients.RedisException;
import org.eclipse.jetty.io.ssl.ALPNProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import spark.Request;

/**
 * Created by umehta on 3/4/18.
 */
public class RequestHandler {
    private Cache cache;
    private static Logger LOGGER = LoggerFactory.getLogger(RequestHandler.class);

    public RequestHandler(Cache cache) {
        this.cache = cache;
    }

    public ServerResponse serviceRequest(String req) {
        Jedis jd = null;
        ServerResponse resp = null;
        try {

            if(req == null){
                LOGGER.debug("Bad Request. Returning 400");
                resp = new ServerResponse(ServerResponseHelper.BAD_REQUEST_MSG, ServerResponseHelper.BAD_REQUEST_CODE);
                return resp;
            }

            // Check if it exists in local cache
            CacheRecord value = getFromCache(req);

            if(!(value == null)) {
                LOGGER.debug("Key found in Cache : "+req);
                resp = new ServerResponse((String)value.getValue(), ServerResponseHelper.SUCCESS_CODE);
            }else {
                String redisValue = getFromRedis(req);
                resp = new ServerResponse(redisValue, ServerResponseHelper.SUCCESS_CODE);
            }

        } catch (RedisException re) {
            LOGGER.debug(re.getMessage());
            resp = new ServerResponse(ServerResponseHelper.FAILURE_MSG, ServerResponseHelper.FAILURE_CODE);
        }
        catch (Exception e) {
            LOGGER.error("Error Servicing request : "+e.getMessage());
            resp = new ServerResponse(ServerResponseHelper.FAILURE_MSG, ServerResponseHelper.FAILURE_CODE);
        }
        return resp;
    }


    private CacheRecord getFromCache(String key) {
        return (CacheRecord)cache.get(key);
    }


    private String getFromRedis(String key) throws RedisException {
        Jedis jd = null;
        String redisValue = "";
        try{
            // If not get it from Redis
            LOGGER.debug("Cache Miss. Fetching key "+key+ "from Redis");
            jd = RedisClient.getRedisClient();
            redisValue = jd.get(key);

            if (redisValue == null) {
                LOGGER.debug("Key not found in Redis : "+key);
                redisValue = ServerResponseHelper.NOT_FOUND_MSG;
            }
            else {
                LOGGER.debug("Key found in Redis : "+key);
                cache.set(key, new CacheRecord<>(redisValue, System.currentTimeMillis()));
            }
        } catch (Exception e) {
            throw new RedisException("Error retrieving key from Redis : "+key);
        } finally {
            LOGGER.debug("closing jd client");
            if(jd != null)
                jd.close();
        }
        return redisValue;
    }
}
