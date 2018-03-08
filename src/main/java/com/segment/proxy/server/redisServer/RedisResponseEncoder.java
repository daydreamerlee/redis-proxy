package com.segment.proxy.server.redisServer;

import com.segment.proxy.server.commons.ServerResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by umehta on 3/4/18.
 */
public class RedisResponseEncoder extends MessageToByteEncoder<ServerResponse> {
    private static Logger LOGGER = LoggerFactory.getLogger(RedisResponseEncoder.class);

    @Override
    public void encode(ChannelHandlerContext ctx, ServerResponse msg, ByteBuf out) throws Exception {
        msg.write(out);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        //Can do more exception handling
        LOGGER.info(cause.getLocalizedMessage());
        ctx.close();
    }
}
