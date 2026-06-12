package com.example.it211project.aspect;

import com.example.it211project.annotation.LogExecutionTime;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

/**
 * FR-11 (AF1): Ghi log thời gian thực hiện cho tất cả các chức năng
 * Sử dụng AOP để tách biệt code logging khỏi business logic
 */
@Aspect
@Component
@Slf4j
public class LoggingAspect {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * Pointcut cho tất cả các method trong package service
     */
    @Pointcut("execution(* com.example.it211project.service.impl.*.*(..))")
    public void serviceMethods() {}

    /**
     * Pointcut cho các method được đánh dấu bởi @LogExecutionTime
     */
    @Pointcut("@annotation(com.example.it211project.annotation.LogExecutionTime)")
    public void annotatedMethods() {}

    /**
     * Pointcut cho các method trong controller
     */
    @Pointcut("execution(* com.example.it211project.controller.*.*(..))")
    public void controllerMethods() {}

    /**
     * Around advice - Ghi log thời gian thực thi cho service methods
     */
    @Around("serviceMethods() || annotatedMethods() || controllerMethods()")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {

        long startTime = System.currentTimeMillis();
        String startTimeStr = LocalDateTime.now().format(FORMATTER);

        // Lấy thông tin method
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        // Log trước khi thực thi
        log.info("╔══════════════════════════════════════════════════════════════════╗");
        log.info("║ [START] Method: {}.{}", className, methodName);
        log.info("║ [START] Time: {}", startTimeStr);
        log.info("║ [START] Arguments: {}", args.length > 0 ? Arrays.toString(args) : "[]");
        log.info("╚══════════════════════════════════════════════════════════════════╝");

        Object result = null;
        Exception exception = null;

        try {
            // Thực thi method gốc
            result = joinPoint.proceed();
            return result;

        } catch (Exception ex) {
            exception = ex;
            throw ex;

        } finally {
            // Log sau khi thực thi (cả thành công và thất bại)
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            String endTimeStr = LocalDateTime.now().format(FORMATTER);

            log.info("╔══════════════════════════════════════════════════════════════════╗");
            log.info("║ [END]   Method: {}.{}", className, methodName);
            log.info("║ [END]   Start Time: {}", startTimeStr);
            log.info("║ [END]   End Time: {}", endTimeStr);
            log.info("║ [END]   Execution Time: {} ms", executionTime);

            if (exception != null) {
                log.error("║ [END]   Status: FAILED");
                log.error("║ [END]   Exception: {}", exception.getClass().getSimpleName());
                log.error("║ [END]   Message: {}", exception.getMessage());
            } else {
                log.info("║ [END]   Status: SUCCESS");
                if (result != null) {
                    String resultStr = result.toString();
                    if (resultStr.length() > 200) {
                        resultStr = resultStr.substring(0, 200) + "...";
                    }
                    log.info("║ [END]   Result: {}", resultStr);
                }
            }
            log.info("╚══════════════════════════════════════════════════════════════════╝");
        }
    }
}