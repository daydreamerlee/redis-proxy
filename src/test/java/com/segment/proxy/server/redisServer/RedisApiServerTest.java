package com.segment.proxy.server.redisServer;

import com.segment.proxy.cache.LRUCacheImpl;
import com.segment.proxy.clients.RedisClient;
import com.segment.proxy.server.commons.ServerResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Before;
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
public class RedisApiServerTest {

    @Test
    public void testEncodeDecode() throws Exception {
        EmbeddedChannel ch = new EmbeddedChannel(new RedisCommandDecoder(), new RedisCommandHandler(new LRUCacheImpl<>(5,5)), new RedisResponseEncoder() );

        //Mock RedisClient
        Jedis jd = mock(Jedis.class);
        PowerMockito.mockStatic(RedisClient.class);
        BDDMockito.given(RedisClient.getRedisClient()).willReturn(jd);
        when(jd.get("foo")).thenReturn("foundfoo");
        when(jd.get("bar")).thenReturn(null);

        //Test for key found in Redis
        String msg = "*2\r\n$3\r\nGET\r\n$3\r\nfoo\r\n";
        ch.writeInbound(Unpooled.wrappedBuffer(msg.getBytes()));

        ServerResponse resp = ch.readOutbound();
        ch.writeOutbound(resp);

        ByteBuf buf = ch.readOutbound();
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);

        assertEquals("$8\r\nfoundfoo\r\n", new String(bytes));
        ch.flush();

        //Test for key Not found in Redis
        String msg1 = "*2\r\n$3\r\nGET\r\n$3\r\nbar\r\n";
        ch.writeInbound(Unpooled.wrappedBuffer(msg1.getBytes()));

        ServerResponse resp1 = ch.readOutbound();
        ch.writeOutbound(resp1);

        ByteBuf buf1 = ch.readOutbound();
        byte[] bytes1 = new byte[buf1.readableBytes()];
        buf1.readBytes(bytes1);

        assertEquals("$-1\r\n", new String(bytes1));
    }

    @Test
    public void testEncodeDecodeInvalidRequests() throws Exception {
        EmbeddedChannel ch = new EmbeddedChannel(new RedisCommandDecoder(), new RedisCommandHandler(new LRUCacheImpl<>(5,5)), new RedisResponseEncoder() );

        //Mock RedisClient
        Jedis jd = mock(Jedis.class);
        PowerMockito.mockStatic(RedisClient.class);
        BDDMockito.given(RedisClient.getRedisClient()).willReturn(jd);
        when(jd.get("foo")).thenReturn("foundfoo");
        when(jd.get("bar")).thenReturn(null);

        //Test for unsupported requests
        String msg1 = "*2\r\n$3\r\nSET\r\n$3\r\nbar\r\n";
        ch.writeInbound(Unpooled.wrappedBuffer(msg1.getBytes()));

        ServerResponse resp1 = ch.readOutbound();
        ch.writeOutbound(resp1);

        ByteBuf buf1 = ch.readOutbound();
        byte[] bytes1 = new byte[buf1.readableBytes()];
        buf1.readBytes(bytes1);

        assertEquals("$-1\r\n", new String(bytes1));

        //Test for unsupported requests - 2
        String msg2 = "*2\r\n$4\r\nLLEN\r\n$6\r\nmylist\r\n";
        ch.writeInbound(Unpooled.wrappedBuffer(msg2.getBytes()));

        ServerResponse resp2 = ch.readOutbound();
        ch.writeOutbound(resp1);

        ByteBuf buf2 = ch.readOutbound();
        byte[] bytes2 = new byte[buf2.readableBytes()];
        buf2.readBytes(bytes2);

        assertEquals("$-1\r\n", new String(bytes2));
    }

    @Test
    public void testEncodeDecodeMalformedRequests() throws Exception {
        EmbeddedChannel ch = new EmbeddedChannel(new RedisCommandDecoder(), new RedisCommandHandler(new LRUCacheImpl<>(5,5)), new RedisResponseEncoder() );

        //Mock RedisClient
        Jedis jd = mock(Jedis.class);
        PowerMockito.mockStatic(RedisClient.class);
        BDDMockito.given(RedisClient.getRedisClient()).willReturn(jd);
        when(jd.get("foo")).thenReturn("foundfoo");
        when(jd.get("bar")).thenReturn(null);

        String msg1 = "*2\r\n$100\r\nSET\r\n$3\r\nbar\r\n";
        ch.writeInbound(Unpooled.wrappedBuffer(msg1.getBytes()));

        ServerResponse resp1 = ch.readOutbound();
        ch.writeOutbound(resp1);

        ByteBuf buf1 = ch.readOutbound();
        assertNull("Channel will be closed and request will be dropped", buf1);
    }
}