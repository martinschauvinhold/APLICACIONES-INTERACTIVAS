package com.uade.tpo.demo.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {

    @Around("execution(* com.uade.tpo.demo.controllers..*(..)) || execution(* com.uade.tpo.demo.service..*(..))")
    public Object log(ProceedingJoinPoint joinPoint) throws Throwable {
        Logger logger = LoggerFactory.getLogger(joinPoint.getTarget().getClass());
        String method = joinPoint.getSignature().getName();

        logger.info("Entering {} with args: {}", method, Arrays.toString(joinPoint.getArgs()));

        try {
            Object result = joinPoint.proceed();
            logger.info("Exiting {} with result: {}", method, result);
            return result;
        } catch (Exception e) {
            logger.error("Exception in {} with args: {} — {}: {}", method, Arrays.toString(joinPoint.getArgs()), e.getClass().getSimpleName(), e.getMessage());
            throw e;
        }
    }
}
