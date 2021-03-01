package com.smartwasp.assistant.app.util

import android.content.Context
import android.os.Looper
import android.os.SystemClock
import androidx.appcompat.app.AlertDialog
import com.orhanobut.logger.Logger
import com.smartwasp.assistant.app.R
import com.smartwasp.assistant.app.base.SmartApp
import me.jessyan.autosize.AutoSizeConfig

/**
 * 闪退
 */
class CrashCollectHandler : Thread.UncaughtExceptionHandler {
    var mContext: Context? = null
    var mDefaultHandler:Thread.UncaughtExceptionHandler ?=null
    
    companion object {
        val instance by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) { CrashCollectHandler() }
        var isCrashOccur = false
    }
    
    fun init(pContext: Context) {
        this.mContext = pContext
        // 获取系统默认的UncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        // 设置该CrashHandler为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this)
    }
    
    //当UncaughtException发生时会转入该函数来处理
    override fun uncaughtException(t: Thread?, e: Throwable?) {
        if (!handleException(e) && mDefaultHandler!=null){
            //如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultHandler?.uncaughtException(t,e)
        }else{
            isCrashOccur = true
            SystemClock.sleep(3000)
            SmartApp.finish(1)
        }
    }

    private fun handleException(ex: Throwable?):Boolean {
        ex ?: return false
        SmartApp.topActivity?.let {
            Thread{
                Looper.prepare()
                AlertDialog.Builder(it)
                        .setTitle("提示")
                        .setMessage("抱歉,程序崩溃了,小黄蜂在线将尽力修复,APP即将重启!")
                        .setPositiveButton(android.R.string.ok,null)
                        .show()
                Looper.loop()
            }.start()
        }
        return true
    }
}