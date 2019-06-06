package com.dede.router_demo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.dede.router.Router
import com.dede.router.annotations.Route
import kotlinx.android.synthetic.main.activity_main.*

@Route(route = ["/main", "/home"])
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val data = intent.getStringExtra("data") ?: ""
        tv_data.text = data
    }

    fun jump(v: View) {
        Router.open(this)
            .url("/second?data=我是通过解析url得到的参数")
            .callback {
                val result = it.getStringExtra("result_second") ?: ""
                tv_data.text = result
            }
            .go()
    }

    override fun finish() {
        setResult(
            Activity.RESULT_OK, Intent()
                .putExtra("result_main", "这是Main返回的数据")
        )
        super.finish()
    }
}
