package com.taptap.ratelimiter.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author kl (http://kailing.pub)
 * @since 2021/3/16
 */
@ConfigurationProperties(prefix = RateLimiterProperties.PREFIX)
public class RateLimiterProperties {

    public static final String PREFIX = "spring.ratelimiter";
    //redisson
    private String redisAddress;
    private String redisPassword;
    private int redisDatabase = 1;
    private ClusterServer redisClusterServer;

    private int statusCode = 429;
    private String responseBody = "{\"code\":429,\"msg\":\"Too Many Requests\"}";

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public String getRedisAddress() {
        return redisAddress;
    }

    public void setRedisAddress(String redisAddress) {
        this.redisAddress = redisAddress;
    }

    public String getRedisPassword() {
        return redisPassword;
    }

    public void setRedisPassword(String redisPassword) {
        this.redisPassword = redisPassword;
    }

    public int getRedisDatabase() {
        return redisDatabase;
    }

    public void setRedisDatabase(int redisDatabase) {
        this.redisDatabase = redisDatabase;
    }

    public ClusterServer getRedisClusterServer() {
        return redisClusterServer;
    }

    public void setRedisClusterServer(ClusterServer redisClusterServer) {
        this.redisClusterServer = redisClusterServer;
    }

    public static class ClusterServer{

        private String[] nodeAddresses;

        public String[] getNodeAddresses() {
            return nodeAddresses;
        }

        public void setNodeAddresses(String[] nodeAddresses) {
            this.nodeAddresses = nodeAddresses;
        }
    }
}
