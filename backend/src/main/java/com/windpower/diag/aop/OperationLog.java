package com.windpower.diag.aop;

import org.noear.solon.annotation.Around;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Around(OperationLogAspect.class)
public @interface OperationLog {
    String module() default "";
    String operation() default "";
}
