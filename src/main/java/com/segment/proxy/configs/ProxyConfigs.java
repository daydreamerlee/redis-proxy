package com.segment.proxy.configs;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.validators.PositiveInteger;

/**
 * Created by umehta on 3/2/18.
 */

@Parameters(separators = "=")
public class ProxyConfigs {

    @Parameter(names = { "-a", "--address" }, description = "Backing Redis address", required = false)
    private String redisUrl = "localhost";

    @Parameter(names = {"-p", "--redis-port" }, description = "Web Server Port", required = false, validateWith = PositiveInteger.class)
    private Integer redisPort = 6379;

    @Parameter(names = { "-e", "--expiry" }, description = "Cache Expiration Time in Seconds", required = false)
    private Integer expiry = 120;

    @Parameter(names = {"-c", "--capacity"}, description = "Cache Capacity", required = false, validateWith = PositiveInteger.class)
    private Integer capacity = 100;

    @Parameter(names = {"-w", "--web-port" }, description = "Web Server Port", required = false, validateWith = PositiveInteger.class)
    private Integer port = 8080;

    @Parameter(names = {"-t", "--threads" }, description = "Number of threads to serve requests", required = false, validateWith = PositiveInteger.class)
    private Integer threads = 20;

    @Parameter(names = { "-h", "--help" }, description = "Print help information and exit")
    private boolean showHelp = false;


    public ProxyConfigs parse(String[] args) {
        JCommander jc = new JCommander(this);
        try{
            jc.parse(args);
            if(this.showHelp){
                jc.usage();
                System.exit(0);  //Display usage and exit
            }
        } catch (ParameterException e) {
        System.err.println(e.getMessage()); //TODO: Replace with logger
        jc.usage();
        System.exit(1);
    }
        return this;
    }

    public String getRedisUrl() {
        return redisUrl;
    }

    public int getRedisPort() {
        return redisPort;
    }

    public int getCacheExpiration() {
        return expiry;
    }

    public int getCacheSize() {
        return capacity;
    }

    public int getWebServerPort() {
        return port;
    }

    public int getThreadCount() {
        return threads;
    }

    public String toString() {
        return "Redis Address : "+redisUrl +
                "\nRedis Port : "+redisPort +
                "\nCache Expiration Time (seconds) : "+expiry +
                "\nCache capacity : "+capacity +
                "\nProxy Port : "+port +
                "\nNum of Threads : "+threads;
    }

}
