package com.dede.router

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.dede.router.extand.RouteParamsParser
import com.dede.router.intercept.IGlobalIntercept
import com.dede.router.intercept.IParamsParser
import com.dede.router.util.RouterInjectHelper
import com.dede.router.util.notNull
import com.gengqiquan.qqresult.QQResult


/**
 * 路由
 * Router.open(context).go(url)
 */
class Router private constructor(private val context: Context) {

    companion object {

        private val TAG = if (BuildConfig.DEBUG) "Router" else ""

        fun open(context: Context): Router {
            return Router(context)
        }

        fun init(): RouterInit {
            return routerInit
        }

        /**
         * 判断是否支持当前路由
         */
        fun support(url: String?): Boolean {
            if (!urlFilter(url)) {
                return false
            }
            return routerInit.find(url!!) != null
        }

        private val routerInit by lazy { RouterInit() }

        /**
         * filter url
         */
        private fun urlFilter(url: String?): Boolean {
            if (url == null || url.isEmpty()) {
                return false
            }
            return true
        }
    }

    /**
     * Router 注册映射表
     */
    class RouterInit internal constructor() {

        private val routerMap = HashMap<String, Compont>()
        // 通用拦截器
        private val globalIntercepts by lazy { ArrayList<IGlobalIntercept>() }
        internal var parser: IParamsParser = RouteParamsParser()
        internal var baseRouter: String? = null

        fun baseRouter(base: String?): RouterInit {
            this.baseRouter = base
            return this
        }

        fun addCompont(url: String, compont: Compont): RouterInit {
            val c = routerMap[url]
            if (c != null) {
                Log.w(TAG, "addCompont: Router url repeated addition :$url")
            }
            routerMap[url] = compont
            return this
        }

        fun addCompont(compont: Compont): RouterInit {
            compont.urls?.forEach {
                if (it.notNull()) {
                    addCompont(it!!, compont)
                }
            }
            compont.urls = null
            return this
        }

        fun addGlobalIntercept(intercept: IGlobalIntercept?): RouterInit {
            if (intercept != null) {
                this.globalIntercepts.add(intercept)
            }
            return this
        }

        fun setParamsParser(parser: IParamsParser?): RouterInit {
            if (parser != null) {
                this.parser = parser
            }
            return this
        }

        /**
         * 注入被注解声明的页面
         */
        fun inject() {
            RouterInjectHelper.inject(this)

            Log.i(TAG, "Router map size : " + routerMap.size)
        }

        internal fun find(url: String): Compont? {
            val indexOf = url.indexOf("?", 0)
            val newUrl = if (indexOf == -1) {
                url
            } else {
                url.substring(0, indexOf)
            }
            return routerMap[newUrl]
        }

        /**
         * 调用通用拦截器，判断是否拦截
         */
        internal fun intercept(context: Context, url: String, bundle: Bundle): Boolean {
            var result = false
            for (intercept in this.globalIntercepts) {
                val r = intercept.onIntercept(context, url, bundle)
                if (!r) {
                    result = r
                }
            }
            return result
        }

        /**
         * 路由没有找到时调用
         */
        internal fun undefined(context: Context, url: String, bundle: Bundle) {
            for (intercept in this.globalIntercepts) {
                intercept.onUndefined(context, url, bundle)
            }
        }

    }

    /** =========================================== */

    private var url: String? = null
    private var parserParams: Boolean = true
    private var typeCase: Boolean = true
    private val bundle = Bundle()
    private var callback: ((Intent) -> Unit)? = null

    fun url(url: String?): Router {
        this.url = url
        return this
    }

    /**
     * 自动解析参数,优先级高于组件定义的
     */
    fun parserParams(auto: Boolean): Router {
        this.parserParams = auto
        return this
    }

    /**
     * 参数类型转换
     */
    fun typeCase(typeCase: Boolean): Router {
        this.typeCase = typeCase
        return this
    }

    fun addParams(bundle: Bundle?): Router {
        if (bundle != null) {
            this.bundle.putAll(bundle)
        }
        return this
    }

    fun addParams(key: String, value: String): Router {
        bundle.putString(key, value)
        return this
    }

    fun addParams(key: String, value: Boolean): Router {
        bundle.putBoolean(key, value)
        return this
    }

    fun addParams(key: String, value: Int): Router {
        bundle.putInt(key, value)
        return this
    }

    fun addParams(key: String, value: Float): Router {
        bundle.putFloat(key, value)
        return this
    }

    /**
     * 路由callback,数据回传
     */
    fun callback(callback: (Intent) -> Unit): Router {
        this.callback = callback
        return this
    }


    fun go(url: String?) {
        url(url).go()
    }

    fun go() {
        if (!urlFilter(this.url)) {
            Log.w(TAG, "Router url is null or empty")
            return
        }
        var url = this.url
        val baseRouter = init().baseRouter
        if (baseRouter.notNull()) {
            if (url!!.startsWith(baseRouter!!)) {
                url = url.substring(baseRouter.length, url.length)
            }
        }

        val comment = init().find(url!!)
        go(comment)
    }

    private fun go(compont: Compont?) {
        val router = init()
        val url = this.url!!

        // 解析参数
        if (parserParams) {
            bundle.putAll(router.parser.parserParams(url, this.typeCase))
        } else {
            if (compont != null && compont.buildParams) {
                bundle.putAll(router.parser.parserParams(url, compont.typeCase))
            }
        }

        // 先走通用拦截器
        if (router.intercept(context, url, bundle)) {// 通用拦截器
            Log.i(TAG, "Router: open with normal onIntercept.  URL:$url")
            return
        }

        if (compont == null) {
            Log.w(TAG, "Router addCompont is onUndefined.  URL:$url")

            router.undefined(context, url, bundle)
            return
        }

        if (compont.onIntercept(context, url, bundle)) {// 当前页面的拦截器
            Log.i(TAG, "Router: open with single onIntercept.  URL:$url")
            return
        }

        if (compont.targetClass == null) {
            Log.w(TAG, "Router addCompont class is null.  URL:$url")
            return
        }

        val intent = Intent(context, compont.targetClass)
            .putExtras(bundle)
        val isAct = context is Activity
        if (!isAct) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val cb = callback != null
        try {
            if (isAct && cb) {
                QQResult.startActivityWith(context, intent)
                    .result {
                        callback?.invoke(it)
                        callback = null
                    }
            } else {
                context.startActivity(intent)
            }
        } catch (e: ClassNotFoundException) {
            Log.e(TAG, "Router: activity no found.  URL:$url")
            e.printStackTrace()
            return
        }

        Log.i(TAG, "Router: open with startActivity")
        if (cb) {
            Log.w(TAG, "Router: context is't Activity instance, ignore callback.  Context:$context")
        }
    }

}
