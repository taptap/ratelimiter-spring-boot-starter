--
-- Created by IntelliJ IDEA.
-- User: kl
-- Date: 2021/3/18
-- Time: 11:17 上午
-- To change this template use File | Settings | File Templates.
-- 限流器 lua 脚本，只是方便编写，实际读取的脚本不在这里
local rateLimitKey = KEYS[1];
local rate = tonumber(KEYS[2]);
local rateInterval = tonumber(KEYS[3]);

local currValue = redis.call('incr', rateLimitKey);
if (currValue == 1) then
    redis.call('expire', rateLimitKey, rateInterval);
    return 0
else
    if (currValue > rate) then
        return 1;
    else
        return 0;
    end
end
