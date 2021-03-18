package com.taptap.ratelimiter.model;

/**
 * @author kl (http://kailing.pub)
 * @since 2021/3/18
 */
public class LuaScript {

    private LuaScript(){}

    public static final String RATE_LIMITER = "local rateLimitKey = KEYS[1];\n" +
            "local rate = tonumber(KEYS[2]);\n" +
            "local rateInterval = tonumber(KEYS[3]);\n" +
            "\n" +
            "local currValue = redis.call('incr', rateLimitKey);\n" +
            "if (currValue == 1) then\n" +
            "    redis.call('expire', rateLimitKey, rateInterval);\n" +
            "    return 0\n" +
            "else\n" +
            "    if (currValue > rate) then\n" +
            "        return 1;\n" +
            "    else\n" +
            "        return 0;\n" +
            "    end\n" +
            "end";

    public static final String TTL ="return redis.call('pttl', KEYS[1])";
}
