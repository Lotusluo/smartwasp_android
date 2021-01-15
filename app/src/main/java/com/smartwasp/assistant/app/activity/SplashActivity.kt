package com.smartwasp.assistant.app.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.smartwasp.assistant.app.R
import com.smartwasp.assistant.app.base.SmartApp
import com.smartwasp.assistant.app.util.ScreenUtil
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
        ScreenUtil.navigationGo(window,true)
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