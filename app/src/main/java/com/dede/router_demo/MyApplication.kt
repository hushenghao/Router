package com.dede.router_demo

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import com.dede.router.Compont
import com.dede.router.Router
import com.dede.router.extand.WebIntercept
import com.dede.router.intercept.IGlobalIntercept
import com.dede.router.intercept.Intercept

/**
 * Created by hsh on 2019-06-05 09:41
 */
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Router.init()
            .baseRouter("dede://router.demo/")
            .addGlobalIntercept(object : WebIntercept() {
                override fun openWebView(context: Context, url: String, bundle: Bundle) {
                    // todo jump custom h5 activity
                }
            })
            .addGlobalIntercept(object : IGlobalIntercept {
                override fun onIntercept(context: Context, url: String, bundle: Bundle): Boolean {
                    return false
                }

                override fun onUndefined(context: Context, url: String, bundle: Bundle) {
                    // page undefined
                }
            })
            .addCompont(Compont(SecondActivity::class.java, "/second"))
            .addCompont(
                Compont.Builder("/home")
                    .target(MainActivity::class.java)
                    .intercept(object : Intercept {
                        override fun onIntercept(context: Context, url: String, bundle: Bundle): Boolean {
                            Toast.makeText(context, "/home 被拦截", Toast.LENGTH_SHORT).show()
                            return true
                        }
                    })
                    .build()
            )
            .inject()
    }
}