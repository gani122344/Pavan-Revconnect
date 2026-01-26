package org.example.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {
    private static final Logger logger =
            LoggerFactory.getLogger(LoggingAspect.class);

    @Before("execution(* org.example.service.*.*(..))")
    public void beforeService(JoinPoint jp){
        logger.info("Service started: " + jp.getSignature().getName());
    }

    @AfterReturning("execution(* org.example.service.*.*(..))")
    public void afterService(JoinPoint jp){
        logger.info("Service finished: " + jp.getSignature().getName());
    }

    @Before("execution(* org.example.dao.*.*(..))")
    public void beforeDao(JoinPoint jp){
        logger.info("DAO called: " + jp.getSignature().getName());
    }
}
