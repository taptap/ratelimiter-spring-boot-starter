package com.taptap.ratelimiter.core;

import com.taptap.ratelimiter.model.LuaScript;
import com.taptap.ratelimiter.model.Result;
import com.taptap.ratelimiter.model.Rule;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.LongCodec;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * @author kl (http://kailing.pub)
 * @since 2022/8/24
 */
@Component
public class TokenBucketRateLimiter implements RateLimiter {

    private final RScript rScript;

    public TokenBucketRateLimiter(RedissonClient client) {
        this.rScript = client.getScript(LongCodec.INSTANCE);
    }

    @Override
    public Result isAllowed(Rule rule) {
        List<Object> keys = getKeys(rule.getKey());
        String script = LuaScript.getTokenBucketRateLimiterScript();
        List<Long> results = rScript.eval(RScript.Mode.READ_WRITE, script, RScript.ReturnType.MULTI, keys, rule.getRate(), rule.getBucketCapacity(), rule.getRequestedTokens());
        boolean isAllowed = results.get(0) == 1L;
        long newTokens = results.get(1);

        return new Result(isAllowed, newTokens);
    }

    static List<Object> getKeys(String key) {
        String prefix = "request_rate_limiter.{" + key;
        String tokenKey = prefix + "}.tokens";
        String timestampKey = prefix + "}.timestamp";
        return Arrays.asList(tokenKey, timestampKey);
    }
}

