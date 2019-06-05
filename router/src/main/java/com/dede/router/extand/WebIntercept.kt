package com.dede.router.extand

import android.content.Context
import android.os.Bundle
import com.dede.router.intercept.IGlobalIntercept

/**
 * Created by hsh on 2019-06-05 10:43
 */
abstract class WebIntercept : IGlobalIntercept {
    override fun onIntercept(context: Context, url: String, bundle: Bundle): Boolean {
        if (url.startsWith("http")) {
            return true
        }
        return false
    }

    abstract fun openWebView(context: Context, url: String, bundle: Bundle)

    override fun onUndefined(context: Context, url: String, bundle: Bundle) {
    }
}