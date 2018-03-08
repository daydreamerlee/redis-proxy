package com.segment.proxy.server.common;

import com.segment.proxy.server.commons.ServerResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;
import org.junit.Test;

import static io.netty.util.ReferenceCountUtil.releaseLater;
import static org.junit.Assert.*;

/**
 * Created by umehta on 3/7/18.
 */
public class ServerResponseTest {
    @Test
    public void getMsg() throws Exception {
        ServerResponse resp = new ServerResponse("foo", 200);
        assertEquals(resp.getMsg(), "foo");
        assertNotEquals(resp.getMsg(), "bar");
    }

    @Test
    public void getCode() throws Exception {
        ServerResponse resp = new ServerResponse("foo", 400);
        assertEquals(resp.getCode(), 400);
        assertNotEquals(resp.getCode(), 200);
    }

    @Test
    public void write() throws Exception {
        ServerResponse resp = new ServerResponse("foo", 200);
        String expectedString = "$3\r\nfoo\r\n";
        ByteBuf buf = releaseLater(Unpooled.buffer(16));
        resp.write(buf);

        byte[] bytes = new byte[buf.readableBytes()];
        buf.getBytes(buf.readerIndex(), bytes);

        String received = new String(bytes);

        assertEquals(expectedString, received);
    }

    @Test
    public void write1() throws Exception {
        ServerResponse resp = new ServerResponse("nil", 200);
        String expectedString = "$-1\r\n";
        ByteBuf buf = releaseLater(Unpooled.buffer(16));
        resp.write(buf);

        byte[] bytes = new byte[buf.readableBytes()];
        buf.getBytes(buf.readerIndex(), bytes);

        String received = new String(bytes);

        assertEquals(expectedString, received);
    }

}