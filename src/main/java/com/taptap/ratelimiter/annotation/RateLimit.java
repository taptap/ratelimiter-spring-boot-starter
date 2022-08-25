package com.taptap.ratelimiter.annotation;

import com.taptap.ratelimiter.model.Mode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author kl (http://kailing.pub)
 * @since 2021/3/16
 */
@Target(value = {ElementType.METHOD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface RateLimit {

    //===================== 公共参数 ============================

    Mode mode() default Mode.TIME_WINDOW;
    /**
     * 时间窗口模式表示每个时间窗口内的请求数量
     * 令牌桶模式表示每秒的令牌生产数量
     * @return rate
     */
    int rate();

    /**
     * 获取key
     * @return keys
     */
    String [] keys() default {};

    /**
     * 限流后的自定义回退后的拒绝逻辑
     * @return fallback
     */
    String fallbackFunction() default "";

    /**
     * 自定义业务 key 的 Function
     * @return key
     */
    String customKeyFunction() default "";

    /**
     * 时间窗口流量数量表达式
     * @return rateExpression
     */
    String rateExpression() default "";

    //===================== 时间窗口模式参数 ============================

    /**
     * 时间窗口，最小单位秒，如 2s，2h , 2d ,默认 1s
     * @return rateInterval
     */
    String rateInterval() default "1s";

    //===================== 令牌桶模式参数 ============================

    /**
     * 令牌桶容量,默认为1,这个限制了瞬时最大并发数量
     * @return bucketCapacity
     */
    int bucketCapacity() default 1;

    /**
     * 令牌桶容量表达式
     * @return bucketCapacityExpression
     */
    String bucketCapacityExpression() default "";

    /**
     * 每次获取多少令牌，默认为1。一般不用设置，除非你知道你在做什么
     * @return requestedTokens
     */
    int requestedTokens() default 1;

}
