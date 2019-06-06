# Router
A Android Route Project


#### How to use

 1.app init
    
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
            .addCompont(Compont(SecondActivity::class.java, "/test"))
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
            
 2.open page
    
    Router.open(context)
            .url("/second?data=我是通过解析url得到的参数")
            .callback {
                val result = it.getStringExtra("result_second") ?: ""
                tv_data.text = result
            }
            .go()

