package com.smartwasp.assistant.app.util

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

/**
 * Created by luotao on 2021/1/30 11:07
 * E-Mail Address：gtkrockets@163.com
 */
object  ServiceUtil {
    /**
     * 隐藏键盘
     * @param view
     */
    fun hintKbTwo(view: View) {
        view.requestFocus()
        val token = view.windowToken
        if (token != null) {
            val im = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(token, 0)
        }
    }
}