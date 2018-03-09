package com.segment.proxy.configs;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.validators.PositiveInteger;

/**
 * Class for the configs to be used by the Proxy Server. Parses the command line args to get the value of the fields. Uses default values if not provided on the command line.
 */
@Parameters(separators = "=")
public class ProxyConfigs {

    @Parameter(names = { "-a", "--address" }, description = "Backing Redis address", required = false)
    private String redisUrl = "localhost";

    @Parameter(names = {"-p", "--redis-port" }, description = "Backing Redis Port", required = false, validateWith = PositiveInteger.class)
    private Integer redisPort = 6379;

    @Parameter(names = { "-e", "--expiry" }, description = "Cache Expiration Time in Seconds", required = false)
    private Integer expiry = 120;

    @Parameter(names = {"-c", "--capacity"}, description = "Cache Capacity", required = false, validateWith = PositiveInteger.class)
    private Integer capacity = 100;

    @Parameter(names = { "-t", "--server-type" }, description = "http/redisAPI", required = false)
    private String serverType = "http";

    @Parameter(names = {"-w", "--server-port" }, description = "HTTP/RedisAPI Server Port", required = false, validateWith = PositiveInteger.class)
    private Integer port = 8080;

    @Parameter(names = {"-n", "--num-threads" }, description = "Number of threads to serve requests", required = false, validateWith = PositiveInteger.class)
    private Integer threads = 20;

    @Parameter(names = { "-h", "--help" }, description = "Print help information and exit")
    private boolean showHelp = false;


    /**
     * Parses the command line arguments and returns a [[ProxyConfig]] object with the set fields.
     * @param args The command line args
     * @return The [[ProxyConfig]] object
     */
    public ProxyConfigs parse(String[] args) {
        JCommander jc = new JCommander(this);
        try{
            jc.parse(args);
            if(this.showHelp){
                jc.usage();
                System.exit(0);  //Display usage and exit
            }
        } catch (ParameterException e) {
            jc.usage();
            throw  e;
        }
        return this;
    }

    /**
     * @return URL of the Redis Server
     */
    public String getRedisUrl() {
        return redisUrl;
    }

    /**
     * @return Port of the Redis Server
     */
    public int getRedisPort() {
        return redisPort;
    }

    /**
     * @return Cache entry expiration time
     */
    public int getCacheExpiration() {
        return expiry;
    }

    /**
     * @return Size of the LRU cache
     */
    public int getCacheSize() {
        return capacity;
    }

    /**
     * @return Port to start the Server on
     */
    public int getServerPort() {
        return port;
    }

    /**
     * @return the number of threads to process the request.
     */
    public int getThreadCount() {
        return threads;
    }

    /**
     * @return Type of the server - http or RedisApi server
     */
    public String getServerType() {return serverType; }

    /**
     * @return Stringified version of the Config object
     */
    public String configString() {
        return "Redis Address : "+redisUrl +
                "\nRedis Port : "+redisPort +
                "\nCache Expiration Time (seconds) : "+expiry +
                "\nCache capacity : "+capacity +
                "\nProxy Port : "+port +
                "\nNum of Threads : "+threads +
                "\nServer Type: "+serverType;
    }

}
