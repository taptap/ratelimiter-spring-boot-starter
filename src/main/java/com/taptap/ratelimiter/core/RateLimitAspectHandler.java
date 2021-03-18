package com.taptap.ratelimiter.core;

import com.taptap.ratelimiter.annotation.RateLimit;
import com.taptap.ratelimiter.exception.RateLimitException;
import com.taptap.ratelimiter.model.LuaScript;
import com.taptap.ratelimiter.model.RateLimiterInfo;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kl on 2017/12/29.
 * Content : 切面拦截处理器
 */
@Aspect
@Component
@Order(0)
public class RateLimitAspectHandler {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitAspectHandler.class);

    private final RateLimiterService rateLimiterService;
    private final RScript rScript;

    public RateLimitAspectHandler(RedissonClient client, RateLimiterService lockInfoProvider) {
        this.rateLimiterService = lockInfoProvider;
        this.rScript = client.getScript();
    }

    @Around(value = "@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        RateLimiterInfo limiterInfo = rateLimiterService.getRateLimiterInfo(joinPoint, rateLimit);

        List<Object> keys = new ArrayList<>();
        keys.add(limiterInfo.getKey());
        keys.add(limiterInfo.getRate());
        keys.add(limiterInfo.getRateInterval());

        if (rScript.eval(RScript.Mode.READ_WRITE, LuaScript.RATE_LIMITER, RScript.ReturnType.BOOLEAN, keys)) {
            logger.info("Trigger current limiting,key:{}", limiterInfo.getKey());
            if (StringUtils.hasLength(rateLimit.fallbackFunction())) {
                return rateLimiterService.executeFunction(rateLimit.fallbackFunction(), joinPoint);
            }

            long retryAfter = rScript.eval(RScript.Mode.READ_ONLY, LuaScript.TTL, RScript.ReturnType.INTEGER, keys);
            throw new RateLimitException("Too Many Requests", retryAfter);
        }
        return joinPoint.proceed();
    }


}
