package com.smartwasp.assistant.app.fragment

import android.util.Log
import android.view.View
import com.smartwasp.assistant.app.R
import com.smartwasp.assistant.app.base.BaseDialogFragment
import com.smartwasp.assistant.app.base.SmartApp
import kotlinx.android.synthetic.main.fragment_sensitive_dialog.*

class SensitiveTipDialog private constructor():BaseDialogFragment(){

    //布局文件
    override val layoutResID:Int = R.layout.fragment_sensitive_dialog
    //样式数据
    override val styleResID:Int = R.style.CommonWindowStyle_SensitiveDialog

    companion object{
        const val TAG = "SensitiveTipDialog"
        /**
         * 静态构建方法
         */
        fun newsInstance():SensitiveTipDialog{
            return SensitiveTipDialog()
        }
    }

    /**
     * 初始化UI
     */
    override fun initUI(){
        noBtn.setOnClickListener {
            activity?.finish()
        }

        yesBtn.setOnClickListener {
            dismissAllowingStateLoss()
        }

    }
}