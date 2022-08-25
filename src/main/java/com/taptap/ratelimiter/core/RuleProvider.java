package com.taptap.ratelimiter.core;

import com.taptap.ratelimiter.annotation.RateLimit;
import com.taptap.ratelimiter.annotation.RateLimitKey;
import com.taptap.ratelimiter.exception.ExecuteFunctionException;
import com.taptap.ratelimiter.model.Rule;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.convert.DurationStyle;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author kl (http://kailing.pub)
 * @since 2021/3/16
 */
public class RuleProvider implements BeanFactoryAware {


    private static final Logger logger = LoggerFactory.getLogger(RuleProvider.class);

    private final ParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();
    private static final TemplateParserContext PARSER_CONTEXT = new TemplateParserContext();
    private final ExpressionParser parser = new SpelExpressionParser();
    private final StandardEvaluationContext evaluationContext = new StandardEvaluationContext();
    private BeanFactory beanFactory;

    public String getKeyName(JoinPoint joinPoint, RateLimit rateLimit) {
        Method method = getMethod(joinPoint);
        List<String> definitionKeys = getSpelDefinitionKey(rateLimit.keys(), method, joinPoint.getArgs());
        List<String> keyList = new ArrayList<>(definitionKeys);
        List<String> parameterKeys = getParameterKey(method.getParameters(), joinPoint.getArgs());
        keyList.addAll(parameterKeys);
        return StringUtils.collectionToDelimitedString(keyList,"","-","");
    }

    public int getRate(RateLimit rateLimit){
        if (StringUtils.hasText(rateLimit.rateExpression())) {
            String value = parser.parseExpression(resolve(rateLimit.rateExpression()), PARSER_CONTEXT)
                    .getValue(String.class);
            if (value != null) {
                return Integer.parseInt(value);
            }
        }
        return rateLimit.rate();
    }

    public int getBucketCapacity(RateLimit rateLimit){
        if (StringUtils.hasText(rateLimit.bucketCapacityExpression())) {
            String value = parser.parseExpression(resolve(rateLimit.bucketCapacityExpression()), PARSER_CONTEXT)
                    .getValue(String.class);
            if (value != null) {
                return Integer.parseInt(value);
            }
        }
        return rateLimit.bucketCapacity();
    }

    private Method getMethod(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        if (method.getDeclaringClass().isInterface()) {
            try {
                method = joinPoint.getTarget().getClass().getDeclaredMethod(signature.getName(),
                        method.getParameterTypes());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return method;
    }

    @SuppressWarnings("ConstantConditions")
    private List<String> getSpelDefinitionKey(String[] definitionKeys, Method method, Object[] parameterValues) {
        List<String> definitionKeyList = new ArrayList<>();
        for (String definitionKey : definitionKeys) {
            if (!ObjectUtils.isEmpty(definitionKey)) {
                EvaluationContext context = new MethodBasedEvaluationContext(null, method, parameterValues, nameDiscoverer);
                Object objKey = parser.parseExpression(definitionKey).getValue(context);
                definitionKeyList.add(ObjectUtils.nullSafeToString(objKey));
            }
        }
        return definitionKeyList;
    }

    private List<String> getParameterKey(Parameter[] parameters, Object[] parameterValues) {
        List<String> parameterKey = new ArrayList<>();
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].getAnnotation(RateLimitKey.class) != null) {
                RateLimitKey keyAnnotation = parameters[i].getAnnotation(RateLimitKey.class);
                if (keyAnnotation.value().isEmpty()) {
                    Object parameterValue = parameterValues[i];
                    parameterKey.add(ObjectUtils.nullSafeToString(parameterValue));
                } else {
                    StandardEvaluationContext context = new StandardEvaluationContext(parameterValues[i]);
                    Object key = parser.parseExpression(keyAnnotation.value()).getValue(context);
                    parameterKey.add(ObjectUtils.nullSafeToString(key));
                }
            }
        }
        return parameterKey;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void setBeanFactory(BeanFactory beanFactory){
        this.beanFactory = beanFactory;
        this.evaluationContext.setBeanResolver(new BeanFactoryResolver(beanFactory));
    }

    private String resolve(String value) {
        return ((ConfigurableBeanFactory) this.beanFactory).resolveEmbeddedValue(value);
    }

    /**
     * 获取基础的限流 key
     */
    private String getKey(MethodSignature signature) {
        return String.format("%s.%s", signature.getDeclaringTypeName(), signature.getMethod().getName());

    }

    Rule getRateLimiterRule(JoinPoint joinPoint, RateLimit rateLimit) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String businessKeyName = this.getKeyName(joinPoint, rateLimit);
        String rateLimitKey = this.getKey(signature) + businessKeyName;
        if (StringUtils.hasLength(rateLimit.customKeyFunction())) {
            try {
                rateLimitKey = this.getKey(signature) + this.executeFunction(rateLimit.customKeyFunction(), joinPoint).toString();
            } catch (Throwable throwable) {
                logger.info("Gets the custom Key exception and degrades it to the default Key:{}", rateLimit, throwable);
            }
        }
        int rate = this.getRate(rateLimit);
        int bucketCapacity = this.getBucketCapacity(rateLimit);
        long rateInterval = DurationStyle.detectAndParse(rateLimit.rateInterval()).getSeconds();

        Rule rule = new Rule(rateLimitKey, rate, rateLimit.mode());
        rule.setRateInterval(Long.valueOf(rateInterval).intValue());
        rule.setFallbackFunction(rateLimit.fallbackFunction());
        rule.setRequestedTokens(rateLimit.requestedTokens());
        rule.setBucketCapacity(bucketCapacity);

        return rule;
    }

    /**
     * 执行自定义函数
     */
    public Object executeFunction(String fallbackName, JoinPoint joinPoint) throws Throwable {
        // prepare invocation context
        Method currentMethod = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Object target = joinPoint.getTarget();
        Method handleMethod;
        try {
            handleMethod = joinPoint.getTarget().getClass().getDeclaredMethod(fallbackName, currentMethod.getParameterTypes());
            handleMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Illegal annotation param customLockTimeoutStrategy", e);
        }
        Object[] args = joinPoint.getArgs();

        // invoke
        Object res;
        try {
            res = handleMethod.invoke(target, args);
        } catch (IllegalAccessException e) {
            throw new ExecuteFunctionException("Fail to invoke custom lock timeout handler: " + fallbackName, e);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }

        return res;
    }

}
