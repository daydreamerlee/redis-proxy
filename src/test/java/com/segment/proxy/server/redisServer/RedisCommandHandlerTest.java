package com.segment.proxy.server.redisServer;

import com.segment.proxy.cache.LRUCacheImpl;
import com.segment.proxy.clients.RedisClient;
import com.segment.proxy.server.commons.ServerResponse;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.eclipse.jetty.io.ssl.ALPNProcessor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import redis.clients.jedis.Jedis;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(RedisClient.class)
@PowerMockIgnore("javax.management.*")
public class RedisCommandHandlerTest {

    @Test
    public void testCommandHandler() throws Exception {
        EmbeddedChannel channel = new EmbeddedChannel(new RedisCommandDecoder());
        channel.pipeline().addLast(new RedisCommandHandler(new LRUCacheImpl<>(5,5)));

        //Mock RedisClient
        Jedis jd = mock(Jedis.class);
        PowerMockito.mockStatic(RedisClient.class);
        BDDMockito.given(RedisClient.getRedisClient()).willReturn(jd);
        when(jd.get("foo")).thenReturn("foundfoo");
        when(jd.get("bar")).thenReturn(null);

        //Key found in Redis
        String msg = "*2\r\n$3\r\nGET\r\n$3\r\nfoo\r\n";
        channel.writeInbound(Unpooled.wrappedBuffer(msg.getBytes()));
        ServerResponse resp = channel.readOutbound();
        assertEquals("foundfoo", resp.getMsg());

        //Key Not found in Redis
        String msg1 = "*2\r\n$3\r\nGET\r\n$3\r\nbar\r\n";
        channel.writeInbound(Unpooled.wrappedBuffer(msg1.getBytes()));
        ServerResponse resp1 = channel.readOutbound();
        assertEquals("nil", resp1.getMsg());

        //Invalid request - Valid request but Unsupported in current impl
        String msg2 = "*2\r\n$3\r\nSET\r\n$3\r\nbar\r\n";
        channel.writeInbound(Unpooled.wrappedBuffer(msg2.getBytes()));
        ServerResponse resp2 = channel.readOutbound();
        assertEquals("Channel should return nil for unsupported request", resp2.getMsg(), "nil");

        String msg3 = "*2\r\n$4\r\nLLEN\r\n$6\r\nmylist\r\n";
        channel.writeInbound(Unpooled.wrappedBuffer(msg3.getBytes()));
        ServerResponse resp3 = channel.readOutbound();
        assertEquals("Channel should return nil for unsupported request", resp3.getMsg(), "nil");
    }
}