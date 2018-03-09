package com.segment.proxy.server.redisServer;

import com.segment.proxy.cache.Cache;
import com.segment.proxy.cache.CacheRecord;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;

/**
 * Sets up a Redis API server that services clients interfacing through the Redis Protocol
 * This implementation only supports GET requests.
 * Uses netty framework to set up the server. Requires more boilerplate code than the HTTP server and it is slightly complex. - https://netty.io/wiki/user-guide-for-4.x.html
 *
 *
 * Eg interaction:
 * Request from client:
 * *2\r\n$3\r\nGET\r\n$3\r\nfoo\r\n
 *
 * Response:
 * If found in cache or Redis:
 * $6\r\nfooval\r\n
 *
 * If not found or exception during handling request:
 * $-1\r\n
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
        EventLoopGroup bossGroup = new NioEventLoopGroup(); // Use default number of threads to receive requests. Boss Group hands over the request to the workers to handle
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        DefaultEventExecutorGroup group = new DefaultEventExecutorGroup(this.threads); //Set configured thread count to the executor group that would be servicing the requests

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) { //Adds the command decoder, handler and the response encoder to the pipeline.
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(new RedisCommandDecoder());
                            ch.pipeline().addLast(new RedisResponseEncoder());
                            ch.pipeline().addLast(group, new RedisCommandHandler(cache));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .localAddress(port);


            // Bind and start to accept incoming connections.
            ChannelFuture f = b.bind().sync();

            // Wait until the server socket is closed.
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

}
