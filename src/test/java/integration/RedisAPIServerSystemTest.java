package integration;

import com.segment.proxy.cache.Cache;
import com.segment.proxy.cache.CacheRecord;
import com.segment.proxy.cache.LRUCacheImpl;
import com.segment.proxy.clients.RedisClient;
import com.segment.proxy.configs.ProxyConfigs;
import com.segment.proxy.helper.PortFinder;
import com.segment.proxy.server.http.HttpServer;
import com.segment.proxy.server.redisServer.RedisApiServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.embedded.RedisServer;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by umehta on 3/8/18.
 */
public class RedisAPIServerSystemTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisAPIServerSystemTest.class);
    RedisApiServer server = null;
    String serverUrl = "";
    Cache<String, CacheRecord<String>> cache = null;
    ProxyConfigs configs = null;
    private static Thread thread;

    @Before
    public void setup() throws Exception {
        thread = new Thread(() -> {
            try {
                //Get Configs
                configs = getConfigs();
                LOGGER.info(configs.configString());

                //Create cache
                cache = new LRUCacheImpl<>(configs.getCacheSize(), configs.getCacheExpiration());

                //Start Embedded Redis Server
                RedisServer redisServer = new RedisServer(configs.getRedisPort());
                redisServer.start();
                addDummyEntriesToRedis(configs);
                RedisClient.intitializeClient(configs);

                //Start Server
                serverUrl = "http://localhost:" + configs.getServerPort();
                server = new RedisApiServer(configs.getServerPort(), configs.getThreadCount(), cache);
                server.startServer();
            }catch(InterruptedException ex) {
                LOGGER.info("Shutting down server");
            } catch (Exception ex) {
                LOGGER.error("Could not start Server for tests", ex);
            }
        });
        thread.start();
        // make sure server gets off the ground
        Thread.sleep(5000);
    }

    @Test
    public void testSystem() throws Exception {
        Socket client = new Socket("localhost", configs.getServerPort());
        OutputStream clientOut = client.getOutputStream();
        InputStream clientIn = client.getInputStream();

        //Test 1 - valid input present in Redis
        String msg = "*2\r\n$3\r\nGET\r\n$3\r\nfoo\r\n";
        String expected = "$6\r\nfooval\r\n";
        clientOut.write(msg.getBytes());
        clientOut.flush();

        byte[] value = new byte[expected.length()]; //remove all crlf
        clientIn.read(value);
        assertEquals(new String(value), expected);


        //Test 2 - valid input absent in Redis
        String msg1 = "*2\r\n$3\r\nGET\r\n$3\r\ninv\r\n";
        String expected1 = "$-1\r\n";
        clientOut.write(msg1.getBytes());
        clientOut.flush();

        byte[] value1 = new byte[expected1.length()]; //remove all crlf
        clientIn.read(value1);
        assertEquals(new String(value1), expected1);

        //Test 2 - unsupported request
        String msg2 = "*2\r\n$3\r\nSET\r\n$3\r\ninv\r\n";
        String expected2 = "$-1\r\n";
        clientOut.write(msg2.getBytes());
        clientOut.flush();

        byte[] value2 = new byte[expected2.length()]; //remove all crlf
        clientIn.read(value2);
        assertEquals(new String(value2), expected2);
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

    @After
    public void tearDown() throws Exception {
        Thread.sleep(1000);

        thread.interrupt();
        while (thread.isAlive()) {
            Thread.sleep(250);
        }
    }
}
