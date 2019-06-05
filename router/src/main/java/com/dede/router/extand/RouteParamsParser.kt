package com.dede.router.extand

import android.os.Bundle
import com.dede.router.intercept.IParamsParser
import kotlin.collections.forEach
import kotlin.collections.map as map1

/**
 * Created by hsh on 2019-06-05 09:52
 * 路由参数解析器
 */
class RouteParamsParser : IParamsParser {

    /**
     * find url params ,xxx?a=b&c=d&e=1
     */
    override fun parserParams(url: String, caseType: Boolean): Bundle {
        val bundle = Bundle()
        if (!url.contains("?"))
            return bundle
        val split = url.split("?")
        if (split.size < 2) {
            return bundle
        }

        (1 until split.size)
            .map1 { split[it] }
            .forEach {
                it.split("&").forEach inn@{
                    if (!it.contains("="))
                        return@inn
                    val param = it.split("=")
                    val key = param[0]
                    if (key.isEmpty()) return@inn
                    if (param.size < 2) {
                        bundle.putString(key, "")
                    } else {
                        putBundle(bundle, key, param[1], caseType)
                    }
                }
            }

        return bundle
    }

    /**
     * string to other type，add to bundle
     */
    private fun putBundle(bundle: Bundle, key: String, value: String, caseType: Boolean) {
        if (caseType) {
            if (value.equals("true", true) || value.equals("false", true)) {
                bundle.putBoolean(key, value.toBoolean())
                return
            }

            try {
                val i = value.toInt()
                bundle.putInt(key, i)
                return
            } catch (e: NumberFormatException) {
            }
            // todo if should add other type method
            try {
                val f = value.toFloat()
                bundle.putFloat(key, f)
                return
            } catch (e: NumberFormatException) {
            }
        }

        bundle.putString(key, value)
    }
}