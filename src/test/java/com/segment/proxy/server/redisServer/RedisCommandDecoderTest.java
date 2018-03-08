package com.segment.proxy.server.redisServer;

import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by umehta on 3/7/18.
 */
public class RedisCommandDecoderTest {
    @Test
    public void testRedisCommandDecoder1() {
        EmbeddedChannel channel = new EmbeddedChannel(new RedisCommandDecoder());
        String msg = "*2\r\n$3\r\nGET\r\n$3\r\nfoo\r\n";
        channel.writeInbound(Unpooled.wrappedBuffer(msg.getBytes()));
        byte[][] myObject = channel.readInbound();

        assertEquals("GET", new String(myObject[0]));
        assertEquals("foo", new String(myObject[1]));
    }

    @Test
    public void testRedisCommandDecoder2() {
        EmbeddedChannel channel = new EmbeddedChannel(new RedisCommandDecoder());
        String msg = "*2\r\n$3\r\nEXTRALEN\r\n$3\r\nfoo\r\n";
        channel.writeInbound(Unpooled.wrappedBuffer(msg.getBytes()));
        byte[][] myObject = channel.readInbound();
        //Malformed requests should close the channel in the exception handling part
        assertNull(myObject);
    }
}