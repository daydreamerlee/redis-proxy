package com.segment.proxy.server.commons;

import com.segment.proxy.cache.Cache;
import com.segment.proxy.cache.CacheRecord;
import com.segment.proxy.clients.RedisClient;
import com.segment.proxy.clients.RedisException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

/**
 * Handles the GET request made to the server and returns a [[ServerResponse]] containing the status and the text.
 */
public class RequestHandler {
    private Cache cache;
    private static Logger LOGGER = LoggerFactory.getLogger(RequestHandler.class);

    /**
     * Creates a new RequestHandler with the given cache
     * @param cache Shared synchronized cache object
     */
    public RequestHandler(Cache cache) {
        this.cache = cache;
    }

    /**
     * Serves the request String. Flow for each request is as follow:
     * 1. If request is null then returns a BAD Request Response.
     * 2. If not null, then check if request exists in local cache.
     *      a. If yes, then return the value stored in the Cache.
     *      b. if not, then check for the entry in Redis. Return value if found in Redis or 'nil' if not found.
     * @param req The request to service
     * @return The [[ServerResponse]] response object.
     */
    public ServerResponse serviceRequest(String req) {
        Jedis jd = null;
        ServerResponse resp = null;
        try {

            if(req == null){
                LOGGER.info("Null Request cannot be handled : "+req);
                resp = new ServerResponse(ServerResponseHelper.FAILURE_MSG, ServerResponseHelper.BAD_REQUEST_CODE);
                return resp;
            }

            // Check if it exists in local cache
            CacheRecord value = getFromCache(req);

            if(!(value == null)) {
                LOGGER.debug("Key found in Cache : "+req+". Returning SUCCESS");
                resp = new ServerResponse((String)value.getValue(), ServerResponseHelper.SUCCESS_CODE);
            }else {
                String redisValue = getFromRedis(req);
                resp = new ServerResponse(redisValue, ServerResponseHelper.SUCCESS_CODE);
            }

        } catch (RedisException re) {
            LOGGER.error(re.getMessage());
            resp = new ServerResponse(ServerResponseHelper.FAILURE_MSG, ServerResponseHelper.FAILURE_CODE);
        }
        catch (Exception e) {
            LOGGER.error("Error Servicing request : "+e.getMessage());
            resp = new ServerResponse(ServerResponseHelper.FAILURE_MSG, ServerResponseHelper.FAILURE_CODE);
        }
        return resp;
    }


    /**
     * Get value of given key from Cache
     * @param key Key to look up
     * @return [[CacheRecord]] value found in Cache. Null if not found
     */
    private CacheRecord getFromCache(String key) {
        return (CacheRecord)cache.get(key);
    }


    /**
     * Gets key from Redis on cache miss
     * @param key The key to look up
     * @return The String value in Redis for the key. Return null if not found
     * @throws RedisException
     */
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
                redisValue = ServerResponseHelper.FAILURE_MSG;
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
