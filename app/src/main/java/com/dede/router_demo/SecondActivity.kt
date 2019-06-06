package com.dede.router_demo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dede.router.Router
import com.dede.router.annotations.Route
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Created by hsh on 2019-06-05 16:25
 */
@Route(route = ["/second"])
class SecondActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bt_jump.text = this.javaClass.simpleName
        val data = intent.getStringExtra("data") ?: ""
        tv_data.text = data

        bt_jump.setOnClickListener {
            Router.open(this)
                .addParams("data", "我是从第二个页面传递的数据")
                .callback {
                    val result = it.getStringExtra("result_main") ?: ""
                    tv_data.text = result
                }
                .go("/home")
        }
    }

    override fun finish() {
        setResult(
            Activity.RESULT_OK, Intent()
                .putExtra("result_second", "这是Second返回的数据！！")
        )
        super.finish()
    }

}