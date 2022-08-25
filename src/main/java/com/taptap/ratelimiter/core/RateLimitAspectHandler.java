package com.taptap.ratelimiter.core;

import com.taptap.ratelimiter.annotation.RateLimit;
import com.taptap.ratelimiter.exception.RateLimitException;
import com.taptap.ratelimiter.model.Result;
import com.taptap.ratelimiter.model.Rule;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

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
    private final RuleProvider ruleProvider;

    public RateLimitAspectHandler(RateLimiterService lockInfoProvider, RuleProvider ruleProvider) {
        this.rateLimiterService = lockInfoProvider;
        this.ruleProvider = ruleProvider;
    }

    @Around(value = "@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        Rule rule = ruleProvider.getRateLimiterRule(joinPoint, rateLimit);

        Result result = rateLimiterService.isAllowed(rule);
        boolean allowed = result.isAllow();
        if (!allowed) {
            logger.info("Trigger current limiting,key:{}", rule.getKey());
            if (StringUtils.hasLength(rule.getFallbackFunction())) {
                return ruleProvider.executeFunction(rule.getFallbackFunction(), joinPoint);
            }
            long extra = result.getExtra();
            throw new RateLimitException("Too Many Requests", extra, rule.getMode());
        }
        return joinPoint.proceed();
    }


}
