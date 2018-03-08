package com.segment.proxy.server.redisServer;

import com.segment.proxy.cache.Cache;
import com.segment.proxy.cache.CacheRecord;
import com.segment.proxy.server.commons.RequestHandler;
import com.segment.proxy.server.commons.ServerResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by umehta on 3/4/18.
 */
public class RedisCommandHandler extends SimpleChannelInboundHandler<byte[][]> {
    private static Logger LOGGER = LoggerFactory.getLogger(RedisCommandHandler.class);
    private final String getString = "get";
    private Cache<String, CacheRecord<String>> cache;

    public RedisCommandHandler(Cache<String, CacheRecord<String>> cache) {
        this.cache = cache;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, byte[][] msg) throws Exception {
        //2d byte array here contains the command and the key to retrieve
        //Check if the message byte array has only 2 elements.
        if(msg.length != 2)
            throw new IllegalArgumentException("Invalid number of arguments. Current implementation only supports GET request");

        String command  = new String(msg[0]);
        String key = new String(msg[1]);

        if(!command.toLowerCase().equals(getString))
            throw new UnsupportedOperationException("Current implementation only supports GET requests");

        //Fetch key from redis/cache
        RequestHandler handler = new RequestHandler(cache);
        ServerResponse resp = handler.serviceRequest(key);

        ctx.write(resp);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {

        LOGGER.error(cause.getLocalizedMessage());
        //do more exception handling
        ctx.write(new ServerResponse("nil", -1));
    }
}
