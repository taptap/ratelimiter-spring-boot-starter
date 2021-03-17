package com.taptap.ratelimiter.core;

import com.taptap.ratelimiter.annotation.RateLimit;
import com.taptap.ratelimiter.exception.RateLimitException;
import com.taptap.ratelimiter.model.RateLimiterInfo;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * Created by kl on 2017/12/29.
 * Content : 切面拦截处理器
 */
@Aspect
@Component
@Order(0)
public class RateLimitAspectHandler {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitAspectHandler.class);

    private final RedissonClient client;
    private final RateLimiterService rateLimiterService;

    public RateLimitAspectHandler(RedissonClient client, RateLimiterService lockInfoProvider) {
        this.client = client;
        this.rateLimiterService = lockInfoProvider;
    }

    @Around(value = "@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        RateLimiterInfo limiterInfo = rateLimiterService.getRateLimiterInfo(joinPoint, rateLimit);
        RAtomicLong rAtomicLong = client.getAtomicLong(limiterInfo.getKey());
        if (!rAtomicLong.isExists()) {
            rAtomicLong.incrementAndGet();
            while (true){
                if (rAtomicLong.expire(limiterInfo.getRateInterval(), TimeUnit.SECONDS)) break;
            }

        } else if (rAtomicLong.incrementAndGet() > limiterInfo.getRate()) {
            logger.info("Trigger current limiting,key:{}", limiterInfo.getKey());
            if (StringUtils.hasLength(rateLimit.fallbackFunction())) {
                return rateLimiterService.executeFunction(rateLimit.fallbackFunction(), joinPoint);
            }
            long retryAfter = rAtomicLong.remainTimeToLive();
            throw new RateLimitException("Too Many Requests", retryAfter);
        }
        return joinPoint.proceed();
    }


}
