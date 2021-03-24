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

    private LuaScript(){}
    private static final Logger log = LoggerFactory.getLogger(LuaScript.class);
    private static final String RATE_LIMITER_FILE_PATH = "META-INF/ratelimiter-spring-boot-starter-rateLimit.lua";
    private static String rateLimiterScript;

    static {
        InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(RATE_LIMITER_FILE_PATH);
        try {
            rateLimiterScript =  StreamUtils.copyToString(in, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("ratelimiter-spring-boot-starter Initialization failure",e);
        }
    }

    public static String getRateLimiterScript() {
        return rateLimiterScript;
    }
}
