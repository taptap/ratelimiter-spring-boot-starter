package com.taptap.ratelimiter.model;

/**
 * Created by kl on 2017/12/29.
 * Content :锁基本信息
 */
public class RateLimiterInfo {

    private String key;
    private long rate;
    private long rateInterval;

    public RateLimiterInfo(String key, long rate, long rateInterval) {
        this.key = key;
        this.rate = rate;
        this.rateInterval = rateInterval;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public long getRate() {
        return rate;
    }

    public void setRate(long rate) {
        this.rate = rate;
    }

    public long getRateInterval() {
        return rateInterval;
    }

    public void setRateInterval(long rateInterval) {
        this.rateInterval = rateInterval;
    }

}
