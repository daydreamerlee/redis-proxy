package com.segment.proxy.redisServer;

import com.segment.proxy.cache.Cache;
import com.segment.proxy.cache.CacheRecord;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;

/**
 * Created by umehta on 3/4/18.
 */
public class RedisApiServer {
    private int port;
    private int threads;
    private Cache<String, CacheRecord<String>> cache;

    public RedisApiServer(int port, int threads, Cache<String, CacheRecord<String>> cache) {
        this.port = port;
        this.threads = threads;
        this.cache = cache;
    }

    public void startServer() throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup(); // (1)
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        DefaultEventExecutorGroup group = new DefaultEventExecutorGroup(this.threads);

        try {
            ServerBootstrap b = new ServerBootstrap(); // (2)
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class) // (3)
                    .childHandler(new ChannelInitializer<SocketChannel>() { // (4)
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(new RedisCommandDecoder());
                            ch.pipeline().addLast(new RedisResponseEncoder());
                            ch.pipeline().addLast(group, new RedisCommandHandler(cache));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)          // (5)
                    .childOption(ChannelOption.TCP_NODELAY, true) // (6)
                    .localAddress(port);


            // Bind and start to accept incoming connections.
            ChannelFuture f = b.bind().sync(); // (7)

            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

}
