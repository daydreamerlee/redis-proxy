package com.segment.proxy.redisServer;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

/**
 * Created by umehta on 3/4/18.
 */
public class RedisCommandDecoder extends ReplayingDecoder<Void> {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)  { // (2)
        try {
            while (in.isReadable()) { // (1)
                in.readBytes(2);
                System.out.print((char) in.readByte());
                System.out.flush();
                checkpoint();
            }
        } finally {
            //in.release(); // (2)
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }
}


