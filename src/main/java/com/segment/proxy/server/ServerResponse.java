package com.segment.proxy.server;

/**
 * Created by umehta on 3/4/18.
 */
public class ServerResponse {

    private String msg;
    private int code;

    public ServerResponse(String msg, int code) {
        this.msg = msg;
        this.code = code;
    }

    public String getMsg() { return this.msg; }
    public int getCode() { return this.code; }
}
