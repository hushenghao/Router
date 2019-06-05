package com.dede.router.util

import com.dede.router.Compont
import com.dede.router.Router

/**
 * Created by hsh on 2019-06-05 14:33
 */
object RouterInjectHelper {

    fun inject(routerInit: Router.RouterInit) {
        var compontList: List<*>? = null
        try {
            val clazz = Class.forName("com.dede.router.Router_Inject")
            val field = clazz.getField("compontList")
            val componts = field.get(null)
            if (componts is List<*>) {
                compontList = componts
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (compontList == null) {
            return
        }

        compontList.filter { it is Compont }
            .forEach {
                routerInit.addCompont(it as Compont)
            }
    }
}
