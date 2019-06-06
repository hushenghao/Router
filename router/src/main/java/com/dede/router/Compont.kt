package com.dede.router

import android.app.Activity
import android.content.Context
import android.os.Bundle
import com.dede.router.intercept.Intercept

/**
 * Created by hsh on 2019-06-03 14:43
 * 路由页面
 */
open class Compont : Intercept {

    internal val targetClass: Class<out Activity>?// target activity class

    internal var buildParams: Boolean = true
    internal var typeCase: Boolean = true
    internal var intercept: Intercept? = null
    internal var urls: Array<out String?>? = null// Compont添加成功后urls会重新赋值为null

    constructor(clazz: Class<out Activity>?, vararg urls: String?) {
        this.targetClass = clazz
        this.urls = urls
    }

    override fun onIntercept(context: Context, url: String, bundle: Bundle): Boolean {
        return intercept?.onIntercept(context, url, bundle) ?: false
    }


    class Builder(vararg urls: String?) {

        private var parserParams: Boolean = true
        private var typeCase: Boolean = true
        private var targetClass: Class<out Activity>? = null
        private var intercept: Intercept? = null
        private var urls: Array<out String?>? = null

        init {
            this.urls = urls
        }

        fun parserParams(buildParams: Boolean): Builder {
            this.parserParams = buildParams
            return this
        }

        fun typeCase(typeCase: Boolean): Builder {
            this.typeCase = typeCase
            return this
        }

        fun target(targetClass: Class<out Activity>): Builder {
            this.targetClass = targetClass
            return this
        }

        fun intercept(intercept: Intercept): Builder {
            this.intercept = intercept
            return this
        }

        fun build(): Compont {
            return Compont(this.targetClass, *urls ?: emptyArray()).apply {
                intercept = this@Builder.intercept
            }
        }
    }
}