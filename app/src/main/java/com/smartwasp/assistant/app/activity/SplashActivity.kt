package com.smartwasp.assistant.app.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.view.WindowInsets
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import com.orhanobut.logger.Logger
import com.smartwasp.assistant.app.R
import com.smartwasp.assistant.app.base.SmartApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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
//        Android P 为 Android 9；Android Q 为 Android 10；Android R 为 Android 11
        val controller = ViewCompat.getWindowInsetsController(window.decorView)
        controller?.also {
            it.hide(WindowInsets.Type.statusBars())
            it.hide(WindowInsets.Type.navigationBars())
        }?: kotlin.run {
            val decorView: View = window.decorView
            var uiOptions = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
            decorView.systemUiVisibility = uiOptions
        }
        setContentView(R.layout.activity_splash)
        GlobalScope.launch(Dispatchers.IO) {
            doWork()
            GlobalScope.launch(Dispatchers.Main) {
                toMain()
            }
        }
    }

    //做耗时动作
    private val doWork = {
        SystemClock.sleep(2000)
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