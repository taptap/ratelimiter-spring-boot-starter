package com.taptap.ratelimiter.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author kl (http://kailing.pub)
 * @since 2021/3/18
 */
public final class LuaScript {

    private LuaScript() {
    }

    private static final Logger log = LoggerFactory.getLogger(LuaScript.class);
    private static final String timeWindowRateLimiterScript;
    private static final String tokenBucketRateLimiterScript;


    static {
        timeWindowRateLimiterScript = getRateLimiterScript("META-INF/timeWindow-rateLimit.lua");
        tokenBucketRateLimiterScript = getRateLimiterScript("META-INF/tokenBucket-rateLimit.lua");
    }

    private static String getRateLimiterScript(String scriptFileName) {
        InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(scriptFileName);
        try {
            return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("tokenBucket-rateLimit.lua Initialization failure", e);
            throw new RuntimeException(e);
        }
    }

    public static String getTimeWindowRateLimiterScript() {
        return timeWindowRateLimiterScript;
    }

    public static String getTokenBucketRateLimiterScript() {
        return tokenBucketRateLimiterScript;
    }
}
