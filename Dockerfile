FROM openjdk:8-jdk
WORKDIR /

ADD target/redis-proxy-1.0-SNAPSHOT-uber.jar redis-proxy.jar

ENTRYPOINT ["java", "-cp", "redis-proxy.jar", "com.segment.proxy.App"]
CMD [-h] 
