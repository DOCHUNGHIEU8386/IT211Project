package com.example.it211project.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation để đánh dấu các method cần ghi log thời gian thực thi.
 * Sử dụng với AOP Aspect.
 *
 * Ví dụ:
 * @LogExecutionTime
 * public UserResponse register(RegisterRequest request) { ... }
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogExecutionTime {
}