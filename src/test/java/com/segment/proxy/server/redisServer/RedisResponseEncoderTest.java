package com.segment.proxy.server.redisServer;

import com.segment.proxy.server.commons.ServerResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.CharsetUtil;
import org.eclipse.jetty.io.ssl.ALPNProcessor;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by umehta on 3/8/18.
 */
public class RedisResponseEncoderTest {

    @Test
    public void testRedisResponseEncoder() throws  Exception {
        EmbeddedChannel channel = new EmbeddedChannel(new RedisResponseEncoder());

        //Test for key not found or invalid request
        ServerResponse resp = new ServerResponse("nil", 200);
        channel.writeOutbound(resp);
        ByteBuf buf = channel.readOutbound();
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);

        assertEquals("$-1\r\n", new String(bytes));
        channel.flush();

        //Test for key found in Redis/Cache
        ServerResponse resp1 = new ServerResponse("foo", 200);
        channel.writeOutbound(resp1);
        ByteBuf buf1 = channel.readOutbound();
        byte[] bytes1 = new byte[buf1.readableBytes()];
        buf1.readBytes(bytes1);

        assertEquals("$3\r\nfoo\r\n", new String(bytes1));

    }
}