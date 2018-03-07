package com.segment.proxy.redisServer;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by umehta on 3/4/18.
 */
public class RedisResponseEncoder extends MessageToByteEncoder<String> {
    private static Logger LOGGER = LoggerFactory.getLogger(RedisResponseEncoder.class);
    private final char START_MARKER = '$';
    private final byte[] CRLF = {'\r', '\n'};
    private final byte[] NOT_FOUND_MSG = {'-', '1'};

    @Override
    public void encode(ChannelHandlerContext ctx, String msg, ByteBuf out) throws Exception {
        byte[] response = msg.getBytes();
        String len = msg.length() + "";

        out.writeChar(START_MARKER);
        if(msg.equals("nil")) {
            out.writeBytes(NOT_FOUND_MSG);
            out.writeBytes(CRLF);
        }else {
            out.writeBytes(len.getBytes());
            out.writeBytes(CRLF);
            out.writeBytes(msg.getBytes());
            out.writeBytes(CRLF);
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {

        LOGGER.info(cause.getLocalizedMessage());
        //do more exception handling
        ctx.close();
    }
}
