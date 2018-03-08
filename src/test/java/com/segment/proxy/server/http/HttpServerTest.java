package com.segment.proxy.server.http;

import com.segment.proxy.cache.LRUCacheImpl;
import com.segment.proxy.clients.RedisClient;
import com.segment.proxy.configs.ProxyConfigs;
import com.segment.proxy.helper.ApiTestUtils;
import com.segment.proxy.helper.ApiTestUtils.TestResponse;
import com.segment.proxy.helper.PortFinder;
import com.segment.proxy.server.http.HttpServer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import redis.clients.jedis.Jedis;
import spark.Spark;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(RedisClient.class)
@PowerMockIgnore("javax.management.*")
public class HttpServerTest {

    /**
     * Have to put all tests in 1 method as Spark java does not behave well with multiple servers in same jvm.
     * @throws Exception
     */
    @Test
    public void startServer() throws Exception {
        int port = PortFinder.findRandomOpenPort();

        PowerMockito.mockStatic(Spark.class);
        ProxyConfigs configs = mock(ProxyConfigs.class);
        when(configs.getServerPort()).thenReturn(port);

        //Mock RedisClient
        Jedis jd = mock(Jedis.class);
        PowerMockito.mockStatic(RedisClient.class);
        BDDMockito.given(RedisClient.getRedisClient()).willReturn(jd);
        when(jd.get("foo")).thenReturn("foundfoo");

        HttpServer server = new HttpServer(configs, new LRUCacheImpl(5,5));
        server.startServer();

        //Ensure we wait for Server to start up
        PowerMockito.verifyStatic(VerificationModeFactory.times(1));
        Spark.awaitInitialization();

        //Ensure routes for get /proxy and /* are created
        TestResponse resp = ApiTestUtils.request("GET", "/proxy", "http://localhost:"+port);
        assertEquals(resp.status, 400);

        TestResponse resp1 = ApiTestUtils.request("GET", "/invalid", "http://localhost:"+port);
        assertEquals(resp.status, 400);

        //Ensure we get response success if key found in Redis
        TestResponse resp2 = ApiTestUtils.request("GET", "/proxy?key=foo", "http://localhost:"+port);
        assertEquals(resp2.status, 200);
        assertEquals(resp2.body, "foundfoo");

        // Ensure we get a 'nil' response if key missing in Redis and cache
        TestResponse resp3 = ApiTestUtils.request("GET", "/proxy?key=invalid", "http://localhost:"+port);
        assertEquals(resp3.status, 200);
        assertEquals(resp3.body, "nil");

        // Ensure invalid query parameters requests are handled
        TestResponse resp4 = ApiTestUtils.request("GET", "/proxy?key###", "http://localhost:"+port);
        assertEquals(resp4.status, 200);

        server.stopServer();

    }

}
