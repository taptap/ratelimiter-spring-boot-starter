package com.taptap.ratelimiter.model;

/**
 * @author kl (http://kailing.pub)
 * @since 2022/8/23
 */
public enum Mode {
    /**
     * 时间窗口模式
     */
    TIME_WINDOW,
    /**
     * 令牌桶模式
     */
    TOKEN_BUCKET

}
