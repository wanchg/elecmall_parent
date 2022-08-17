package com.atguigu.gmall.common.cache;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)  //只在方法中生效
@Retention(RetentionPolicy.RUNTIME)  //RUNTIME在字节码和运行时生效,CLASS在字节码时生效,SOURCE在源文件时生效
public @interface GmallCache {
    //注解的属性
    String prefix() default "";
}
