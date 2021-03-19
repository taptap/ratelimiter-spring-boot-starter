package com.taptap.ratelimiter;

import com.taptap.ratelimiter.annotation.RateLimit;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author kl (http://kailing.pub)
 * @since 2021/3/17
 */
@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/get")
    @RateLimit(rate = 2, rateInterval = "10s",fallbackFunction = "getFallback")
    public String get(String name) {
        return "get";
    }

    @GetMapping("/get2")
    @RateLimit(rate = 2, rateInterval = "10s")
    public String get2() {
        return "get";
    }

    @PostMapping("/hello")
    @RateLimit(rate = 5, rateInterval = "10s",keys = {"#user.name","user.id"})
    public String hello(@RequestBody User user) {
        return "hello";
    }

    public String getFallback(String name){
        return "命中了" + name;
    }

    public String keyFunction(String name) {
        return "keyFunction";
    }
}
