package integration;

import com.segment.proxy.cache.Cache;
import com.segment.proxy.cache.CacheRecord;
import com.segment.proxy.cache.LRUCacheImpl;
import com.segment.proxy.clients.RedisClient;
import com.segment.proxy.configs.ProxyConfigs;
import com.segment.proxy.helper.ApiTestUtils;
import com.segment.proxy.helper.PortFinder;
import com.segment.proxy.server.http.HttpServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.embedded.RedisServer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * End to End system testing of the HTTP Server without mocks and using embedded Redis.
 */
public class HttpServerSystemTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpServerSystemTest.class);
    HttpServer server = null;
    String serverUrl = "";
    Cache<String, CacheRecord<String>> cache = null;

    @Before
    public void setup() throws Exception {
        //Get Configs
        ProxyConfigs configs = getConfigs();
        LOGGER.info(configs.configString());

        //Create cache
        cache = new LRUCacheImpl<>(configs.getCacheSize(), configs.getCacheExpiration());

        //Start Embedded Redis Server
        RedisServer redisServer = new RedisServer(configs.getRedisPort());
        redisServer.start();
        addDummyEntriesToRedis(configs);
        RedisClient.intitializeClient(configs);

        //Start Server
        serverUrl = "http://localhost:"+configs.getServerPort();
        server = new HttpServer(configs, cache);
        server.startServer();
    }


    @Test
    public void testSystem() throws Exception {
        //Send invalid HTTP Request
        ApiTestUtils.TestResponse resp = ApiTestUtils.request("GET", "/invalid", serverUrl);
        assertEquals(resp.status, 400);

        //Send valid request but cache and Redis miss
        ApiTestUtils.TestResponse resp1 = ApiTestUtils.request("GET", "/proxy?key=not_present", serverUrl);
        assertEquals(resp.status, 400);

        //Send valid request but found in redis
        ApiTestUtils.TestResponse resp2 = ApiTestUtils.request("GET", "/proxy?key=foo", serverUrl);
        assertEquals(resp2.body, "fooval");

        //Ensure Cache now contains the key found in Redis. Ensures "Cached Get" requirement
        assertEquals(cache.getSize(), 1);
        assertEquals(cache.get("foo").getValue(), "fooval");

        //Check LRU works as expected to ensure "LRU Eviction" requirement. Also tests if cache satisfies "Fixed Key Size Requirement"
        ApiTestUtils.TestResponse resp3 = ApiTestUtils.request("GET", "/proxy?key=bar", serverUrl);
        ApiTestUtils.TestResponse resp4 = ApiTestUtils.request("GET", "/proxy?key=xyz", serverUrl);
        ApiTestUtils.TestResponse resp5 = ApiTestUtils.request("GET", "/proxy?key=abc", serverUrl);
        //Now cache should not contain foo key as it is evicted. Size should be 3
        assertEquals(cache.getSize(), 3);
        assertEquals(cache.get("foo"), null);
        assertEquals(cache.get("bar").getValue(), "barval");

        //Check for multithreaded support. Check all responses contain right value
        ApiTestUtils.TestResponse[] responses = new ApiTestUtils.TestResponse[10];
        concurrentRequests(responses);

        for(int i = 0; i < 10; i ++) {
            assertEquals(responses[i].body, "abcval");
        }

        //Check if Key eviction works as expected to satisfy "Global Expiry" Requirement
        Thread.sleep(3000); //sleep for more than ttl time
        assertEquals(cache.getSize(), 0);
        assertEquals(cache.get("bar"), null);
    }

    public void concurrentRequests(ApiTestUtils.TestResponse[] responses) throws Exception {
        ExecutorService exec = Executors.newFixedThreadPool(10);
        for(int i = 0; i < 10; i++) {
            final int index = i;
            exec.submit(new Runnable() {
                public void run() {
                    responses[index] = ApiTestUtils.request("GET", "/proxy?key=abc", serverUrl);
                }
            });
        }
        exec.shutdown();
        exec.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
    }

    @After
    public void tearDown() throws Exception {
        if(server != null)
            server.stopServer();
    }

    public ProxyConfigs getConfigs() throws Exception {
        int port = PortFinder.findRandomOpenPort();
        int redisPort = PortFinder.findRandomOpenPort();
        int expiration = 2; //Record expires in 3 secs
        int capacity = 3; //cache can hold 3 records

        String args[] = {"-w="+port, "-p="+redisPort, "-e="+expiration, "-c="+capacity}; //override 1 default arg
        return new ProxyConfigs().parse(args);
    }

    public void addDummyEntriesToRedis(ProxyConfigs configs) {
        Jedis jd = new Jedis(configs.getRedisUrl(), configs.getRedisPort());
        jd.set("foo", "fooval");
        jd.set("bar", "barval");
        jd.set("abc", "abcval");
        jd.set("xyz", "xyzval");
    }
}
