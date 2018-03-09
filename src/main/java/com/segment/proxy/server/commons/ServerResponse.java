package com.segment.proxy.server.commons;

import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;

/**
 * Stores the Response of the server for requests issued to the Proxy.
 */
public class ServerResponse {
    private String msg;
    private int code;

    //Required for the Redis API server
    private final byte START_MARKER = '$';
    private final byte[] CRLF = {'\r', '\n'};
    private final byte[] NOT_FOUND_MSG = {'-', '1'};

    /**
     * Creates new [[ServerResponse]] object with the given msg and status.
     * Status is not used by the Redis API server
     * @param msg The Response Msg for the given Request
     * @param code The status code of the response.
     */
    public ServerResponse(String msg, int code) {
        this.msg = msg;
        this.code = code;
    }

    /**
     * Creates new [[ServerResponse]] object with the given msg. Status is set to success by default.
     * @param msg The Response Msg for the given Request
     */
    public ServerResponse(String msg) {
        this(msg, ServerResponseHelper.SUCCESS_CODE);
    }

    /**
     * @return the response text stored by the object
     */
    public String getMsg() { return this.msg; }

    /**
     * @return the status of the response stored by the object
     */
    public int getCode() { return this.code; }

    /**
     * This method is used by the RedisAPI server to write the message to the [[ByteBuf]] OutputStream that is sent to the client.
     * @param out [[ByteBuf]] OutputStream to write the message to.
     */
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
