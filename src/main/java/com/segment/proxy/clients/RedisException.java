package com.segment.proxy.clients;

/**
 * User defined exception to be thrown when interacting with Redis.
 */
public class RedisException extends Exception {

    public RedisException (String msg){
        super(msg);
    }
}
