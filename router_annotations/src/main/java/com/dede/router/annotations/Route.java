package com.dede.router.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by hsh on 2019-06-03 15:20
 * 路由定义注解
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface Route {

    String[] route();

    boolean parserParams() default true;

    boolean typeCase() default true;
}
