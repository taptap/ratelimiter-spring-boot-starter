package com.taptap.ratelimiter.configuration;

import com.taptap.ratelimiter.core.RateLimitAspectHandler;
import com.taptap.ratelimiter.core.BizKeyProvider;
import com.taptap.ratelimiter.core.RateLimiterService;
import com.taptap.ratelimiter.web.RateLimitExceptionHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.util.ClassUtils;

/**
 * @author kl (http://kailing.pub)
 * @since 2021/3/16
 */
@Configuration
@ConditionalOnProperty(prefix = RateLimiterProperties.PREFIX, name = "enabled", havingValue = "true")
@AutoConfigureAfter(RedisAutoConfiguration.class)
@EnableConfigurationProperties(RateLimiterProperties.class)
@Import({RateLimitAspectHandler.class, RateLimitExceptionHandler.class})
public class RateLimiterAutoConfiguration {

    private final RateLimiterProperties limiterProperties;

    public RateLimiterAutoConfiguration(RateLimiterProperties limiterProperties) {
        this.limiterProperties = limiterProperties;
    }

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean
    RedissonClient redisson() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Config config = new Config();
        if(limiterProperties.getRedisClusterServer()!=null){
            config.useClusterServers().setPassword(limiterProperties.getRedisPassword())
                    .addNodeAddress(limiterProperties.getRedisClusterServer().getNodeAddresses());
        }else {
            config.useSingleServer().setAddress(limiterProperties.getRedisAddress())
                    .setDatabase(limiterProperties.getRedisDatabase())
                    .setPassword(limiterProperties.getRedisPassword());
        }
        Codec codec=(Codec) ClassUtils.forName(limiterProperties.getCodec(),ClassUtils.getDefaultClassLoader()).newInstance();
        config.setCodec(codec);
        config.setEventLoopGroup(new NioEventLoopGroup());
        return Redisson.create(config);
    }

    @Bean
    public RateLimiterService rateLimiterInfoProvider(){
        return new RateLimiterService();
    }

    @Bean
    public BizKeyProvider bizKeyProvider(){
        return new BizKeyProvider();
    }

}
