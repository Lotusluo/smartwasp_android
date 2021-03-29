package com.smartwasp.assistant.app.util

import android.app.Activity
import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.orhanobut.logger.Logger
import com.smartwasp.assistant.app.R
import java.util.*

/**
 * Created by luotao on 2021/1/9 09:43
 * E-Mail Address：gtkrockets@163.com
 */
object LoadingUtil {

    private var loading:AlertDialog? = null

    /**
     * 创建一个全局的加载显示控件
     * @param activity loading的上下文
     * @param cancelListener 弹出框消失监听器
     * @param cancelable 是否能够取消
     * @return 返回对话框码
     */
    fun create(activity:Activity,cancelListener:(()->Unit)? = null,cancelable:Boolean = false){
        dismiss()
        val mLoading = AlertDialog
                .Builder(activity,R.style.CommonWindowStyle)
                .setView(R.layout.layout_loading)
                .setCancelable(cancelable)
                .create()
        mLoading.show()
        mLoading.setOnCancelListener {
            dismiss()
            cancelListener?.let { it() }
        }
        loading = mLoading
//        flag = UUID.randomUUID().hashCode()
    }

    /**
     * 销毁加载框
     */
    fun dismiss(){
        loading?.let {
            it.dismiss()
        }
    }

    private var mToast:Toast? = null
    fun showToast(context:Application,text:String,duration: Int = Toast.LENGTH_SHORT){
        mToast?.let {
            it.cancel()
        }
        mToast = Toast.makeText(context,text,duration)
        mToast!!.show()
    }


    fun openDialog(context: Context,resID:Int){
        AlertDialog.Builder(context)
                .setTitle(R.string.tip)
                .setMessage(resID)
                .setPositiveButton(android.R.string.ok,null)
                .show()
    }
}