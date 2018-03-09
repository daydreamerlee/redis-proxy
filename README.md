# redis-proxy
## Overview
The Redis Proxy is a transparent service that adds Caching feature on top of Redis. Results of GET requests made to Redis via the Proxy are cached and subsequent requests are handled by the cache in the proxy instead. 

## High Level Architecture Overview
### LRU Cache
The Least Recently Used(LRU) cache used by the proxy is created with an initial max capacity and time to live(ttl) for each entry. The Cache is implemented using a `java.util.LinkedHashMap<K,V>` which provides a `HashMap` implementation with predictable iteration order. <br />
Requests for keys not stored in Cache are forwarded to the Redis Server and the result is stored in the Cache so the next request for the same key is served by the Cache. The Cache is implemented using a `java.util.LinkedHashMap<K,V>` which provides a `HashMap` implementation with predictable iteration order. If the key is found in the Cache, then it is removed and added again so that it is at the head of the list of keys maintained by the `HashMap`. This way, its ensured that recently accessed keys are not evicted as per the LRU policy.<br />
*TTL* : The Map also has a eviction thread that checks for the `lastAccessed` timestamp for each entry and evicts the entries older than the configured TTL. The eviction thread is only started if the configured TTL is greater than 0 seconds.<br />
*Synchronization* : The cache is thread safe with the critical sections enclosed in `synchronized` blocks. This ensures that only 1 thread can Read/Write the cache at a time and avoid race conditions. Another option to ensure thread safety is to make the `HashMap` volatile but that is not an option here since the `get`, `set` are not atomic operations both involving get-update-set transactions. 

### Request Handler
The `Request Handler` component is the main component that handles each request that the server receives. It receives a `String key` and returns a `ServerResponse` based on the status of the `GET` operation. The flow of handling the request is as follows:<br />
1. First check if the request is valid and follows the requiremement of the server. If not, return a Bad Request message to the client.<br />
2. If it is a valid request, then it first attempts to get the key from the local Cache and returns if its a hit.<br />
3. If its a Cache miss, it tries to fetch the key from Redis and returns the result. `nil` is returned if key is absent in Redis as well.<br />
4. If key is found in Redis, the Cache is updated with the key so that subsequent requests can be handled by the cache.<br />

### Server
This proxy can be started using 2 different servers based on the protocol  - `HTTP` or `Redis Protocol`. The type of the server to use is provided as a runtime argument.
#### HTTP Web Service
The HTTP web service is provided by a [Spark Java](http://sparkjava.com/) based web framework. The framework makes it easy to setup a web service without a lot of boiler plate code. The server starts at the port provided at start up or uses the default `8080` port. The service accepts `HTTP GET` requests having the following path:
```
GET /proxy?key=foo
```
Here `foo` can be replaced by any key that we want to fetch from the Proxy Service. All other requests not following this standard would receive a `400` response from the server. Any server side errors while servicing a valid request would result in a `500` code being sent back. The server starts  a pre configured number of worker threads that would provide the functionality to serve requests concurrently.

#### Redis API Server
The Proxy can also be started as a Redis API server that can interact with a client using the Redis Protocol. The current implementation only supports `GET` requests from the client and all other requests would not be serviced. The server starts at the provided port (defaults to 8080) and uses [netty](https://netty.io/) to set up this protocol server. This server too starts a pre configured number of worker threads that would provide the functionality to serve requests concurrently. 
Eg Client Server interaction:
```
Request from client:
*2\r\n$3\r\nGET\r\n$3\r\nfoo\r\n

Response:
If key is found in Cache or Redis:
$6\r\nfooval\r\n

If key not found or exception during handling request:
$-1\r\n
```
This implementation could be improved by making it more modular to support more types of requests. Current implementation satisfies the current scope.

### Multi Threading support
The proxy supports multiple clients to connect concurrently and can serve N requests at a given time. The value N is set at runtime as one of the command line arguments. Both servers start up N worker threads to serve the requests. The `LRU Cache` is thread safe so having multiple threads access it at the same time would not lead to race conditions. Saying so, the cache would be the bottleneck in the system as only one thread can access it at a time.


## Algorithmic Complexity of Cache
Time:
1. GET - O(1)
2. SET - O(1) 
3. CONTAINS - O(1)
4. Key Eviction - O(N) - goes through all the keys to find the `lastAccessed` timestamp.
<br />All operations on the underlying `LinkedHashMap` are in O(1) time complexity the cache follows the same.

Space:
1. O(N) - where N is the size of the cache. 
Requires additional memory to store the pointers for the Doubly Linked List implementation.

## Build and Test
1. Clone the gihub repo and `cd` into the repo:
```
git clone https://github.com/uditmehta27/redis-proxy.git
cd redis-proxy
```
2. Run the tests
```
make test
```
3. Build the package
```
make package
```
4. Build the docker image:
```
docker build -t redis-proxy-develop .
```
## Run application
All commands are from the project root
### Using Docker
To print usage:
```
docker run redis-proxy-develop --help
```
To start HTTP Server at port 11500, cache capacity 100, TTL to 2 minutes and processing threads to 10
```
export PORT=11500
export REDIS_HOST=localhost
docker run -p $PORT:$PORT redis-proxy-develop -a=$REDIS_HOST -w=$PORT -c=100 -e=120 -n=10

Send HTTP requests : GET /proxy?key=foo
```
To start Redis API Server at port 11500.
```
export PORT=11500
export REDIS_HOST=localhost
docker run -p $PORT:$PORT redis-proxy-develop -a=$REDIS_HOST -w=$PORT -t=redisApi

Send request using Redis protocol: *2\r\n$3\r\nGET\r\n$3\r\nfoo\r\n
```
The Project also includes a `docker-compose.yml` file that helps to deploy the app along with a Redis docker container. You can change the command line configs from the `command` key in the file.
```
docker compose up
```
### As standalone Java Application
```
export PORT=11500
export REDIS_HOST=localhost
java -cp target/redis-proxy-1.0-SNAPSHOT-uber.jar com.segment.proxy.App -a=$REDIS_HOST -c=5 -w=11500
```

## Time Spent
1. Familiarizing with Docker: 3-4 hours as it was the first time I would be using Docker to deploy my own application and not using a pre built docker image.
2. Base Implementation - 4-5 hours to write the basic code. Code was non modular, no support for multi threading, only HTTP server support, no testing
3. Getting code production ready - 4-5 hours. Includes modularized code, support for multi threading, basic unit tests
4. Testing - 3-4 hours to writes all unit and integration tests
5. Redis API Server - 5-6 hours to write the server implementation. Most of the time was used in understanding Netty and writing unit tests.
6. Documentation - 2 hours for comments and writing README.

## Unsupported features
1. All features are supported. The Redis API server could be made more modular and fancy to support more types of requests.
2. Better testing for Redis API server - possible improvement
3. Better configuration capability - timeouts for threads and redis client
