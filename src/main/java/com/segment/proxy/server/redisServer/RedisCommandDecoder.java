package com.segment.proxy.server.redisServer;

import com.segment.proxy.clients.RedisException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Created by umehta on 3/4/18.
 */
public class RedisCommandDecoder extends ReplayingDecoder<Void> {
    private static Logger LOGGER = LoggerFactory.getLogger(RedisCommandDecoder.class);
    private final char CR = '\r';
    private final char LF = '\n';
    private byte[][] bytes;
    private int arguments = 0;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception  { // (2)
        if (bytes != null) {
            int numArgs = bytes.length;
            for (int i = arguments; i < numArgs; i++) {
                if (in.readByte() == '$') {
                    long l = readLong(in);
                    if (l > Integer.MAX_VALUE) {
                        throw new IllegalArgumentException("Java only supports arrays up to " + Integer.MAX_VALUE + " in size");
                    }
                    int size = (int) l;
                    bytes[i] = new byte[size];
                    in.readBytes(bytes[i]);
                    if (in.bytesBefore((byte) '\r') != 0) {
                        throw new RedisException("Argument doesn't end in CRLF");
                    }
                    in.skipBytes(2);
                    arguments++;
                    checkpoint();
                } else {
                    throw new IOException("Unexpected character");
                }
            }
            try {
                out.add(bytes);
            } finally {
                bytes = null;
                arguments = 0;
            }
        }else if (in.readByte() == '*') {
            long l = readLong(in);
            if (l > Integer.MAX_VALUE) {
                throw new IllegalArgumentException("Java only supports arrays up to " + Integer.MAX_VALUE + " in size");
            }
            int numArgs = (int) l;
            if (numArgs < 0) {
                throw new IllegalArgumentException("Invalid size: " + numArgs);
            }
            bytes = new byte[numArgs][];
            checkpoint();
            decode(ctx, in, out);
        }
    }

    private long readLong(ByteBuf is) throws IOException {
        long size = 0;
        int sign = 1;
        int read = is.readByte();
        if (read == '-') {
            read = is.readByte();
            sign = -1;
        }
        do {
            if (read == CR) {
                if (is.readByte() == LF) {
                    break;
                }
            }
            int value = read - '0';
            if (value >= 0 && value < 10) {
                size *= 10;
                size += value;
            } else {
                throw new IOException("Invalid character in integer");
            }
            read = is.readByte();
        } while (true);
        return size * sign;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        LOGGER.error(cause.getLocalizedMessage());
        ctx.close();
    }
}

