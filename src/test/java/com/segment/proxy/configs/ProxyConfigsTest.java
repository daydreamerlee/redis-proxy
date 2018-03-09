package com.segment.proxy.configs;

import com.beust.jcommander.ParameterException;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by umehta on 3/5/18.
 */
public class ProxyConfigsTest {
    @Test
    public void parse() throws Exception {
        String[] args = {"-a=localhost","-p=6380","-e=100","-t=http","-w=9020"};
        ProxyConfigs configs = new ProxyConfigs();
        configs.parse(args);
        assertNotNull("Config object should not be null", configs);
        assertEquals("Config should be set correctly. Should override defaults", configs.getRedisUrl(), "localhost");
        assertNotEquals("Config should not be set to default if command line arg provided", configs.getRedisPort(), 6379);
        assertEquals("Default config should be used if config is not provided", configs.getThreadCount(), 20);
    }

    @Test(expected = ParameterException.class)
    public void testParseException() {
        String[] args = {"-invalid=localhost","-p=6380","-e=100","-t=http","-w=9020"};
        ProxyConfigs configs = new ProxyConfigs().parse(args);
    }

    @Test(expected = ParameterException.class)
    public void testParseException1() {
        //Invalid negative port should throw exception
        String[] args = {"-a=localhost","-p=6380","-e=100","-t=http","-w=-9020"};
        ProxyConfigs configs = new ProxyConfigs().parse(args);
    }

    @Test
    public void getRedisUrl() throws Exception {
        ProxyConfigs configs = new ProxyConfigs();
        assertEquals("It should return the default url", configs.getRedisUrl(), "localhost");

        String[] args = {"-a=myHost","-p=6380","-e=100","-t=http","-w=9020"};
        configs.parse(args);
        assertEquals("It should return the set url", configs.getRedisUrl(), "myHost");
    }

    @Test
    public void getRedisPort() throws Exception {
        ProxyConfigs configs = new ProxyConfigs();
        assertEquals("It should return the default port", configs.getRedisPort(), 6379);

        String[] args = {"-a=myHost","-p=6380","-e=100","-t=http","-w=9020"};
        configs.parse(args);
        assertEquals("It should return the set port of Redis server", configs.getRedisPort(), 6380);
    }

    @Test
    public void getCacheExpiration() throws Exception {
        ProxyConfigs configs = new ProxyConfigs();
        assertEquals("It should return the default expiration", configs.getCacheExpiration(), 120);

        String[] args = {"-a=myHost","-p=6380","-e=100","-t=http","-w=9020"};
        configs.parse(args);
        assertEquals("It should return the set expiration", configs.getCacheExpiration(), 100);
    }

    @Test
    public void getCacheSize() throws Exception {
        ProxyConfigs configs = new ProxyConfigs();
        assertEquals("It should return the default cache size", configs.getCacheSize(), 100);

        String[] args = {"-a=myHost","-p=6380","-c=500","-t=http","-w=9020"};
        configs.parse(args);
        assertEquals("It should return the set cache size", configs.getCacheSize(), 500);
    }

    @Test
    public void getServerPort() throws Exception {
        ProxyConfigs configs = new ProxyConfigs();
        assertEquals("It should return the default server port", configs.getServerPort(), 8080);

        String[] args = {"-a=myHost","-p=6380","-e=100","-t=http","-w=9020"};
        configs.parse(args);
        assertEquals("It should return the set server port", configs.getServerPort(), 9020);

    }

    @Test
    public void getThreadCount() throws Exception {
        ProxyConfigs configs = new ProxyConfigs();
        assertEquals("It should return the default thread count", configs.getThreadCount(), 20);

        String[] args = {"-a=myHost","-p=6380","-n=40","-t=http","-w=9020"};
        configs.parse(args);
        assertEquals("It should return the set thread count", configs.getThreadCount(), 40);
    }

    @Test
    public void getServerType() throws Exception {
        ProxyConfigs configs = new ProxyConfigs();
        assertEquals("It should return the default server type", configs.getServerType(), "http");

        String[] args = {"-a=myHost","-p=6380","-e=100","-t=redisAPI","-w=9020"};
        configs.parse(args);
        assertEquals("It should return the set server type", configs.getServerType(), "redisAPI");
    }

    @Test
    public void configString() throws Exception {
        ProxyConfigs configs = new ProxyConfigs();
        String expectedString = "Redis Address : localhost\n" +
                "Redis Port : 6379\n" +
                "Cache Expiration Time (seconds) : 120\n" +
                "Cache capacity : 100\n" +
                "Proxy Port : 8080\n" +
                "Num of Threads : 20\n" +
                "Server Type: http";
        assertEquals("It should return the correct config string", configs.configString(), expectedString);
    }

}