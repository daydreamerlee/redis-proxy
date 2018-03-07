package com.segment.proxy.server;

import com.segment.proxy.cache.LRUCacheImpl;
import com.segment.proxy.configs.ProxyConfigs;
import com.segment.proxy.helper.ApiTestUtils;
import com.segment.proxy.helper.ApiTestUtils.TestResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;

import static com.segment.proxy.helper.ApiTestUtils.request;
import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static spark.Spark.awaitInitialization;
import static spark.Spark.stop;

/**
 * Created by umehta on 3/5/18.
 */
public class HttpServerTest {
    HttpServer server = null;
    int port = 10050;
    @Before
    public void setUp() throws Exception {
        ProxyConfigs configs = new ProxyConfigs(); //Use default configs
        ProxyConfigs spyConfigs = spy(configs);
        doReturn("localhost").when(spyConfigs).getRedisUrl();
        doReturn(port).when(spyConfigs).getServerPort();
        server = new HttpServer(spyConfigs, new LRUCacheImpl<String, String>(5,-1));  //5 capacity, infinite ttl
        server.startServer();
        awaitInitialization();

        //Start Embedded Redis
    }

    @Test
    public void startServer() throws Exception {
        String testUrl = "/proxy?";

        //TestResponse res = ApiTestUtils.request("GET", testUrl, "http://localhost:"+port);
        //assertEquals(400, res.status);

    }

    @After
    public void tearDown() throws Exception {
        stop();
    }

    public void addRandomValues(Jedis jd) {
        jd.set("foo", "fooval");
        jd.set("bar", "barval");
    }

}