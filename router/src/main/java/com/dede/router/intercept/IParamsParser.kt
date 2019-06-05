package com.dede.router.intercept

import android.os.Bundle

/**
 * Created by hsh on 2019-06-05 09:59
 * 路由参数解析器接口
 */
interface IParamsParser {

    /**
     * 解析路由后面的参数，返回Bundle并传递到目标Activity
     * @param url 路由链接
     * @param caseType 是否需要转换参数类型，String -> int/float/boolean...
     */
    fun parserParams(url: String, caseType: Boolean): Bundle

}