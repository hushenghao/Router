package com.dede.router.intercept

import android.content.Context
import android.os.Bundle

/**
 * Created by hsh on 2019-06-05 11:22
 * 路由拦截器
 */
interface Intercept {

    /**
     * 路由拦截
     * @return true 拦截路由，自定义处理  false 默认
     */
    fun onIntercept(context: Context, url: String, bundle: Bundle): Boolean
}