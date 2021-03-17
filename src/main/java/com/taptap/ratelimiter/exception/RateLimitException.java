package com.taptap.ratelimiter.exception;

/**
 * @author kl (http://kailing.pub)
 * @since 2021/3/17
 */
public class RateLimitException extends RuntimeException{

    private final long retryAfter;

    public RateLimitException(String message, long retryAfter) {
        super(message);
        this.retryAfter = retryAfter;
    }

    public long getRetryAfter() {
        return retryAfter;
    }
}
