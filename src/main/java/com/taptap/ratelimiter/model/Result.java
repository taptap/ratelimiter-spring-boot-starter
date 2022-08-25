package com.taptap.ratelimiter.model;

/**
 * @author kl (http://kailing.pub)
 * @since 2022/8/23
 */
public class Result {
    private boolean isAllow;
    private Long extra;

    public Result(boolean isAllow, Long extra) {
        this.isAllow = isAllow;
        this.extra = extra;
    }

    public boolean isAllow() {
        return isAllow;
    }

    public void setAllow(boolean allow) {
        isAllow = allow;
    }

    public Long getExtra() {
        return extra;
    }

    public void setExtra(Long extra) {
        this.extra = extra;
    }
}
