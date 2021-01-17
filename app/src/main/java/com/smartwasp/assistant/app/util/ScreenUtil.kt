package com.smartwasp.assistant.app.util

import android.app.Activity
import android.content.Context
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
     * @param window
     * @param bool 是否隐藏
     */
    fun navigationGo(window: Window,bool:Boolean) {
        when (Build.VERSION.SDK_INT) {
            in Build.VERSION_CODES.KITKAT..Build.VERSION_CODES.Q -> {
                val decorView: View = window.decorView
                var uiOptions = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
                if (!bool) uiOptions = View.SCREEN_STATE_OFF
                decorView.systemUiVisibility = uiOptions
            }
            in Build.VERSION_CODES.R .. Int.MAX_VALUE-> {
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