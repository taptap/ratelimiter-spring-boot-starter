package com.taptap.ratelimiter.exception;

import com.taptap.ratelimiter.model.Mode;

/**
 * @author kl (http://kailing.pub)
 * @since 2021/3/17
 */
public class RateLimitException extends RuntimeException {

    private final long extra;
    private final Mode mode;

    public RateLimitException(String message, long extra, Mode mode) {
        super(message);
        this.extra = extra;
        this.mode = mode;
    }

    public long getExtra() {
        return extra;
    }

    public Mode getMode() {
        return mode;
    }
}
