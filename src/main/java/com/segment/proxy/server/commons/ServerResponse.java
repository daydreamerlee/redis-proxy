package com.segment.proxy.server.commons;

import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;

/**
 * Created by umehta on 3/4/18.
 */
public class ServerResponse {
    private String msg;
    private int code;

    //Required for the Redis API server
    private final byte START_MARKER = '$';
    private final byte[] CRLF = {'\r', '\n'};
    private final byte[] NOT_FOUND_MSG = {'-', '1'};

    public ServerResponse(String msg, int code) {
        this.msg = msg;
        this.code = code;
    }

    public String getMsg() { return this.msg; }
    public int getCode() { return this.code; }

    public void write(ByteBuf out) {
        String len = msg.length()+"";

        out.writeByte(START_MARKER);
        if(msg.equals("nil")) {
            out.writeBytes(NOT_FOUND_MSG);
            out.writeBytes(CRLF);
        }else {
            out.writeBytes(len.getBytes());
            out.writeBytes(CRLF);
            out.writeBytes(msg.getBytes(CharsetUtil.US_ASCII));
            out.writeBytes(CRLF);
        }
    }
}
