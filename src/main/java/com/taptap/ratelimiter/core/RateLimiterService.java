package com.taptap.ratelimiter.core;

import com.taptap.ratelimiter.model.Mode;
import com.taptap.ratelimiter.model.Result;
import com.taptap.ratelimiter.model.Rule;
import org.redisson.api.RedissonClient;

import java.util.HashMap;
import java.util.Map;

/**
 * @author kl (http://kailing.pub)
 * @since 2021/3/16
 */
public class RateLimiterService {

    private static final Map<Mode, RateLimiter> RATE_LIMITER_FACTORY = new HashMap<>();

    public RateLimiterService(RedissonClient redissonClient) {
        RATE_LIMITER_FACTORY.put(Mode.TIME_WINDOW, new TimeWindowRateLimiter(redissonClient));
        RATE_LIMITER_FACTORY.put(Mode.TOKEN_BUCKET, new TokenBucketRateLimiter(redissonClient));
    }

    public Result isAllowed(Rule rule) {
        RateLimiter rateLimiter = RATE_LIMITER_FACTORY.get(rule.getMode());
        return rateLimiter.isAllowed(rule);
    }



}
