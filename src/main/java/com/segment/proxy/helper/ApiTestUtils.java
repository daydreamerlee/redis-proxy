package com.segment.proxy.helper;

import com.segment.proxy.server.commons.ServerResponseHelper;
import spark.utils.IOUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Helper to issue HTTP requests to test the HTTP Server
 */
public class ApiTestUtils {

    /**
     * Issues HTTP request to the server provided in the configs
     * @param method GET/POST request
     * @param path the path of the request : Eg /path?key=value
     * @param connectionURL The server connection URL : Eg localhost:8080
     * @return
     */
    public static TestResponse request(String method, String path, String connectionURL) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(connectionURL + path);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            connection.setDoOutput(true);
            connection.connect();
            String body = IOUtils.toString(connection.getInputStream());
            return new TestResponse(connection.getResponseCode(), body);
        } catch (IOException e) {
            return new TestResponse(ServerResponseHelper.BAD_REQUEST_CODE, null);
        }
    }

    public static class TestResponse {

        public final String body;
        public final int status;

        public TestResponse(int status, String body) {
            this.status = status;
            this.body = body;
        }
    }
}