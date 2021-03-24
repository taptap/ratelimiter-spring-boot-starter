package com.taptap.ratelimiter.web;

import com.taptap.ratelimiter.configuration.RateLimiterProperties;
import com.taptap.ratelimiter.exception.RateLimitException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author kl (http://kailing.pub)
 * @since 2021/3/17
 */
@ControllerAdvice
@ConditionalOnProperty(prefix = RateLimiterProperties.PREFIX, name = "exceptionHandler.enable", havingValue = "true", matchIfMissing = true)
public class RateLimitExceptionHandler {

    private final RateLimiterProperties limiterProperties;

    public RateLimitExceptionHandler(RateLimiterProperties limiterProperties) {
        this.limiterProperties = limiterProperties;
    }

    @ExceptionHandler(value = RateLimitException.class)
    @ResponseBody
    public ResponseEntity<String> exceptionHandler(RateLimitException e) {

        return ResponseEntity.status(limiterProperties.getStatusCode())
                .header(HttpHeaders.RETRY_AFTER, String.valueOf(e.getRetryAfter()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(limiterProperties.getResponseBody());
    }
}
