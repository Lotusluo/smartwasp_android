package com.smartwasp.assistant.app.util

import android.graphics.Color
import android.os.Build
import android.view.*
import com.orhanobut.logger.Logger


/**
 * Created by luotao on 2021/1/7 18:25
 * E-Mail Address：gtkrockets@163.com
 */
object ScreenUtil {

    /**
     * 显示或者隐藏当前Window的导航栏
     */
    fun navigationGo(window: Window,bool:Boolean) {
        Logger.e("navigationGo:${Build.VERSION.SDK_INT}")
        //隐藏虚拟按键，并且全屏
        when (Build.VERSION.SDK_INT) {
            in 1 ..10 -> return
            in 11..18 -> {
                val v: View = window.decorView
                v.systemUiVisibility = if (bool) View.GONE else View.VISIBLE
            }
            in 19..29 -> {
                val decorView: View = window.decorView
                var uiOptions = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
                if (!bool) uiOptions = View.SCREEN_STATE_OFF
                decorView.systemUiVisibility = uiOptions
                window.setFlags(
                        WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN
                )
                window.statusBarColor = Color.TRANSPARENT

//                if (Build.VERSION.SDK_INT >= 21) {
//                    val decorView = window.decorView
//                    val option = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
//                    decorView.systemUiVisibility = option
//
//                }
            }
            else -> {
                window.setDecorFitsSystemWindows(false)
                window.insetsController?.let {
                    if(bool)
                        it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                    else
                        it.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                    it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            }
        }
    }
}