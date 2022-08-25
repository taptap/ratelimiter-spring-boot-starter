package com.taptap.ratelimiter.model;

/**
 * Created by kl on 2017/12/29.
 * Content : 限流器规则信息
 */
public class Rule {

    private String key;
    private int rate;
    private int rateInterval;
    private Mode mode;
    private int bucketCapacity;
    private int requestedTokens;
    private String fallbackFunction;

    public Rule(String key, int rate, int rateInterval, Mode mode, int bucketCapacity, int requestedTokens) {
        this.key = key;
        this.rate = rate;
        this.rateInterval = rateInterval;
        this.mode = mode;
        this.bucketCapacity = bucketCapacity;
        this.requestedTokens = requestedTokens;
    }

    public Rule(String key, int rate, Mode mode) {
        this.key = key;
        this.rate = rate;
        this.mode = mode;
    }

    public Rule(Mode mode) {
        this.mode = mode;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public int getRateInterval() {
        return rateInterval;
    }

    public void setRateInterval(int rateInterval) {
        this.rateInterval = rateInterval;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public int getBucketCapacity() {
        return bucketCapacity;
    }

    public void setBucketCapacity(int bucketCapacity) {
        this.bucketCapacity = bucketCapacity;
    }

    public int getRequestedTokens() {
        return requestedTokens;
    }

    public void setRequestedTokens(int requestedTokens) {
        this.requestedTokens = requestedTokens;
    }

    public String getFallbackFunction() {
        return fallbackFunction;
    }

    public void setFallbackFunction(String fallbackFunction) {
        this.fallbackFunction = fallbackFunction;
    }
}
