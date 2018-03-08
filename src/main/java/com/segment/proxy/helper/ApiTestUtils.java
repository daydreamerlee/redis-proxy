package com.segment.proxy.helper;

import com.segment.proxy.server.commons.ServerResponseHelper;
import spark.utils.IOUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by umehta on 3/5/18.
 */
public class ApiTestUtils {
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