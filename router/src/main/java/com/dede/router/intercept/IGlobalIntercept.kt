package com.dede.router.intercept

import android.content.Context
import android.os.Bundle

/**
 * 全局拦截器
 * Created by hsh on 2019-06-03 14:42
 */
interface IGlobalIntercept : Intercept {

    /**
     * 通用拦截器，每跳转一个路由都会调用
     * @return true 拦截路由，自定义处理  false 默认
     */
    override fun onIntercept(context: Context, url: String, bundle: Bundle): Boolean

    /**
     * 路由目标页面未发现
     */
    fun onUndefined(context: Context, url: String, bundle: Bundle)
}