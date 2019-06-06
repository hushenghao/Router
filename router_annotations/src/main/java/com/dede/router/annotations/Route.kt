package com.dede.router.annotations

/**
 * Created by hsh on 2019-06-03 15:20
 * 路由定义注解
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Route(val route: Array<String>, val parserParams: Boolean = true, val typeCase: Boolean = true)
