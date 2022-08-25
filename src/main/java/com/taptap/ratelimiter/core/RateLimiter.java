package com.taptap.ratelimiter.core;

import com.taptap.ratelimiter.model.Result;
import com.taptap.ratelimiter.model.Rule;

/**
 * @author kl (http://kailing.pub)
 * @since 2022/8/23
 */
public interface RateLimiter {

    Result isAllowed(Rule rule);
}
