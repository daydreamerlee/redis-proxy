package com.segment.proxy.server;

/**
 * Created by umehta on 3/3/18.
 */
public class ServerResponseHelper {
    public final static String SUCCESS = "SUCCESS";
    public final static int SUCCESS_CODE = 200;

    public final static String FAILURE = "FAILURE";
    public final static String FAILURE_MSG = "Error retrieving Key";
    public final static int FAILURE_CODE = 500;

    public final static String NOT_FOUND_MSG = "nil";
    public final static int NOT_FOUND_CODE = 404;

    public final static String BAD_REQUEST_MSG = "Bad Request";
    public final static int BAD_REQUEST_CODE = 400;


}
