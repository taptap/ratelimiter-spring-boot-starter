package com.taptap.ratelimiter.web;

import com.taptap.ratelimiter.configuration.RateLimiterProperties;
import com.taptap.ratelimiter.exception.RateLimitException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;

/**
 * @author kl (http://kailing.pub)
 * @since 2021/3/17
 */
@ControllerAdvice
@ConditionalOnProperty(prefix = RateLimiterProperties.PREFIX, name = "exceptionHandler.enable", havingValue = "true",matchIfMissing = true)
public class RateLimitExceptionHandler {

    private final  RateLimiterProperties limiterProperties;

    public RateLimitExceptionHandler(RateLimiterProperties limiterProperties) {
        this.limiterProperties = limiterProperties;
    }

    @ExceptionHandler(value = RateLimitException.class)
    @ResponseBody
    public String exceptionHandler(HttpServletResponse response, RateLimitException e){
        response.setStatus(limiterProperties.getStatusCode());
        response.setHeader("Retry-After", String.valueOf(e.getRetryAfter()));
        return limiterProperties.getResponseBody();
    }
}
