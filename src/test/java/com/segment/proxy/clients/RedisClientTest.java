package com.segment.proxy.clients;

import com.segment.proxy.configs.ProxyConfigs;
import com.segment.proxy.helper.PortFinder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.embedded.Redis;
import redis.embedded.RedisServer;

import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

/**
 * Created by umehta on 3/5/18.
 */
public class RedisClientTest {

    @Test
    public void getRedisClient() throws Exception {
        int port = PortFinder.findRandomOpenPort();
        RedisServer server = new RedisServer(port);
        server.start();

        ProxyConfigs configs = new ProxyConfigs();

        ProxyConfigs spyConfigs = spy(configs);
        doReturn("localhost").when(spyConfigs).getRedisUrl();
        doReturn(port).when(spyConfigs).getRedisPort();

        RedisClient.intitializeClient(spyConfigs);
        Jedis jd = RedisClient.getRedisClient();

        assertEquals("Client should be able to set key value", jd.set("foo", "bar"), "OK");
        assertEquals("Client should be able to retrieve set key", jd.get("foo"), "bar");
        assertNull("Client should get null for invalid key", jd.get("invalid"));

        jd.close();
        RedisClient.closePool();
        server.stop();
    }

    @Test
    public void closePool() throws Exception {
        int port = PortFinder.findRandomOpenPort();
        RedisServer server = new RedisServer(port);
        server.start();

        ProxyConfigs configs = new ProxyConfigs();

        ProxyConfigs spyConfigs = spy(configs);
        doReturn("localhost").when(spyConfigs).getRedisUrl();
        doReturn(port).when(spyConfigs).getRedisPort();
        doReturn(1).when(spyConfigs).getThreadCount();

        RedisClient.intitializeClient(spyConfigs);
        Jedis jd = RedisClient.getRedisClient();

        RedisClient.closePool();;

        Jedis jd1 = RedisClient.getRedisClient();

        assertNotEquals(jd1, jd);

        jd.close();
        jd1.close();
        RedisClient.closePool();
        server.stop();
    }


}