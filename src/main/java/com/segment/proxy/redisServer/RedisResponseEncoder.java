package com.segment.proxy.redisServer;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Created by umehta on 3/4/18.
 */
public class RedisResponseEncoder extends MessageToByteEncoder<String> {
    @Override
    public void encode(ChannelHandlerContext ctx, String msg, ByteBuf out) throws Exception {
        ctx.write(msg);
    }
}
