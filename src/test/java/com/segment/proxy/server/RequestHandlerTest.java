package com.segment.proxy.server;

import com.segment.proxy.cache.Cache;
import com.segment.proxy.cache.CacheRecord;
import com.segment.proxy.cache.LRUCacheImpl;
import com.segment.proxy.clients.RedisClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import redis.clients.jedis.Jedis;
import redis.embedded.RedisServer;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(RedisClient.class)
@PowerMockIgnore("javax.management.*")
public class RequestHandlerTest {

    @Test
    public void serviceRequest() throws Exception {
        Cache mockCache = mock(LRUCacheImpl.class);
        RequestHandler handler = new RequestHandler(mockCache);
        Jedis jd = mock(Jedis.class);

        //Test 1 for null requests
        assertEquals("Should handle null requests", handler.serviceRequest(null).getCode(), 400); //invalid request
    }
    @Test
    public void serviceRequest1() throws Exception {
        Cache mockCache = mock(LRUCacheImpl.class);
        RequestHandler handler = new RequestHandler(mockCache);
        Jedis jd = mock(Jedis.class);

        //Test 2 for null clients. Mocking static class
        PowerMockito.mockStatic(RedisClient.class);
        BDDMockito.given(RedisClient.getRedisClient()).willReturn(null); //Checks for connection exceptions returning null clients
        assertEquals("Should handle if null jedis client is returned", handler.serviceRequest("abc").getCode(), 500);
    }

    @Test
    public void serviceRequest2() throws Exception {
        Cache mockCache = mock(LRUCacheImpl.class);
        RequestHandler handler = new RequestHandler(mockCache);
        Jedis jd = mock(Jedis.class);

        //Test 3 for valid Redis client and key found
        PowerMockito.mockStatic(RedisClient.class);
        BDDMockito.given(RedisClient.getRedisClient()).willReturn(jd);
        when(jd.get("foo")).thenReturn("foundfoo"); //found
        when(jd.get("bar")).thenReturn(null); //not found

        ServerResponse resp = handler.serviceRequest("foo");
        assertEquals("Should return key if found in redis", resp.getCode(), 200);
        assertEquals("Should return key if found in redis", resp.getMsg(), "foundfoo");

        //Test 4 for valid client and key not found in Redis
        ServerResponse resp1 = handler.serviceRequest("bar");
        assertEquals("Should return success code even if key not found", resp1.getCode(), 200);
        assertEquals("Should return nil if found not found in redis", resp1.getMsg(), "nil");
    }

    @Test
    public void serviceRequest4() throws Exception {
        Cache<String, CacheRecord<String>> mockCache = mock(LRUCacheImpl.class);
        RequestHandler handler = new RequestHandler(mockCache);
        Jedis jd = mock(Jedis.class);
        PowerMockito.mockStatic(RedisClient.class);
        BDDMockito.given(RedisClient.getRedisClient()).willReturn(jd);

        //Test 5 Verify cache is checked once
        when(mockCache.get("foo")).thenReturn(null); //Cache miss
        handler.serviceRequest("foo");
        verify(mockCache, times(1)).get("foo");

        //Test 6 Verify redis is checked since cache miss
        verify(RedisClient.getRedisClient(), times(1)).get("foo");

        //Test 7 Verify redis is not checked if found in cache
        when(mockCache.get("bar")).thenReturn(new CacheRecord("barval", 1));
        handler.serviceRequest("bar");
        verify(RedisClient.getRedisClient(), times(0)).get("bar");
    }

    @Test
    public void serviceRequest5() throws Exception {
        Cache mockCache = mock(LRUCacheImpl.class);
        RequestHandler handler = new RequestHandler(mockCache);
        Jedis jd = mock(Jedis.class);

        //Test 8  Verify correct response if key found in cache
        when(mockCache.get("bar")).thenReturn(new CacheRecord("barval", 1));
        ServerResponse resp = handler.serviceRequest("bar");
        assertEquals("Should return key if found in redis", resp.getCode(), 200);
        assertEquals("Should return key if found in redis", resp.getMsg(), "barval");
    }

    @Test
    public void serviceRequest6() throws Exception {
        Cache<String, CacheRecord<String>> mockCache = mock(LRUCacheImpl.class);
        RequestHandler handler = new RequestHandler(mockCache);
        Jedis jd = mock(Jedis.class);
        PowerMockito.mockStatic(RedisClient.class);
        BDDMockito.given(RedisClient.getRedisClient()).willReturn(jd);

        //Verify client is closed after handling request
        when(mockCache.get("foo")).thenReturn(null); //cache miss
        when(jd.get("foo")).thenReturn("foundfoo"); //go to redis
        handler.serviceRequest("foo");
        assertFalse(jd.isConnected());
    }
}