package com.segment.proxy.server.http;

import com.segment.proxy.cache.Cache;
import com.segment.proxy.configs.ProxyConfigs;
import com.segment.proxy.server.commons.RequestHandler;
import com.segment.proxy.server.commons.ServerResponse;
import com.segment.proxy.server.commons.ServerResponseHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.*;

import static spark.Spark.*;

/**
 * HTTP Server class that is used to start/stop the HTTP proxy server.
 * Uses SparkJava framework (http://sparkjava.com/) for setting up the web server with minimal boilerplate code.
 */
public class HttpServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpServer.class);

    private ProxyConfigs configs;
    private Cache cache;

    /**
     * Creates new HTTP Server with the configs and the Cache to use
     * @param configs The [[ProxyConfigs]] provided at command line
     * @param cache The [[Cache]] implementation
     */
    public HttpServer(ProxyConfigs configs, Cache cache) {
        this.configs = configs;
        this.cache = cache;
    }

    /**
     * Starts the HTTP server, creates the routes to service and waits until initialization completes.
     */
    public void startServer() {
        createRoutes();
        awaitInitialization();
    }

    /**
     * Creates the routes that will be serviced by the server.
     * /proxy route is used to issue the GET request for the key: eg valid request - /proxy?key=foo
     * All other requests not starting with /proxy will be replied with a 400 code
     */
    private void createRoutes() { //Easy to add more routes
        port(this.configs.getServerPort());
        threadPool(configs.getThreadCount());

        get("/proxy", (req, res) -> {
            RequestHandler handler = new RequestHandler(cache);
            ServerResponse response = handler.serviceRequest(getKeyFromRequest(req));
            res.status(response.getCode());
            return response.getMsg();
        });
        get("/*", (req, res) -> {
            LOGGER.error("Invalid request url. Use /proxy to issue request");
            res.status(ServerResponseHelper.BAD_REQUEST_CODE);
            return ServerResponseHelper.FAILURE_MSG;
        });
    }

    /**
     * helper to get the key from the request. Requests are of the form: /proxy?key=foo
     * @param req the Request object to fetch the key from
     * @return The retrieved key
     */
    private String getKeyFromRequest(Request req) {
        String key = "";
        try {
            if(req == null)
                return null;
            else {
                return req.queryParamOrDefault("key", null);
            }
        } catch(Exception e) {
            LOGGER.error("Error retrieving Key from Request : "+e.getMessage());
            return null;
        }
    }

    /**
     * Stops the server
     */
    public void stopServer() {
        stop();
    }
}