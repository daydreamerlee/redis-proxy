package com.segment.proxy.clients;

/**
 * Created by umehta on 3/4/18.
 */
public class RedisException extends Exception {

    public RedisException (String msg){
        super(msg);
    }
}
