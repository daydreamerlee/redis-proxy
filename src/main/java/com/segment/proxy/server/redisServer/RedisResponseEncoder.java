package com.segment.proxy.server.redisServer;

import com.segment.proxy.server.commons.ServerResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Writes the message to the output [[ByteBuf]] to respond to the client request.
 * Output follows the Redis Protocol.
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
