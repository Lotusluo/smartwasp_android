package com.smartwasp.assistant.app.activity

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.view.WindowInsets
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.orhanobut.logger.Logger
import com.smartwasp.assistant.app.R
import com.smartwasp.assistant.app.base.SmartApp
import com.smartwasp.assistant.app.util.RetrofitManager
import com.smartwasp.assistant.app.util.StatusBarUtil
import kotlinx.android.synthetic.main.activity_prev_bind.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by luotao on 2021/1/7 15:20
 * E-Mail Address：gtkrockets@163.com
 * APP快速启动页
 */
class SplashActivity : AppCompatActivity() {

    private val manual:AtomicBoolean = AtomicBoolean(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        Android P 为 Android 9 28；Android Q 为 Android 10；Android R 为 Android 11
        val controller = ViewCompat.getWindowInsetsController(window.decorView)
        setContentView(R.layout.activity_splash)
        controller?.also {
            it.hide(WindowInsetsCompat.Type.statusBars())
            it.hide(WindowInsetsCompat.Type.navigationBars())
        }?: kotlin.run {
            val decorView: View = window.decorView
            var uiOptions =
                    (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
            decorView.systemUiVisibility = uiOptions
        }
        GlobalScope.launch(Dispatchers.IO) {
            doWork()
//            GlobalScope.launch(Dispatchers.Main) {
//                SystemClock.sleep(1000)
//                toMain()
//            }
            suspend {
                withContext(Dispatchers.Main) {
                    SystemClock.sleep(1000)
                    toMain()
                }
            }.invoke()
        }
    }

    //做耗时动作
    private val doWork = {
        //检查更新
        try{
            val response = RetrofitManager.get().retrofitApiService?.update()?.execute()
            response?.let {
                if(it.isSuccessful && null != it.body() && it.body()!!.errCode == 0){
                    SmartApp.updateBean = it.body()!!.data
                }
            }
        }catch (e:Throwable){}


    }

    //进入主界面
    private val toMain = {
        manual.set(false)
        startActivity(Intent(this,MainActivity::class.java))
    }

    /**
     * 销毁
     */
    override fun onStop() {
        super.onStop()
        finish()
        if(manual.get()){
            SmartApp.finish(0)
        }
    }
}