package com.smartwasp.assistant.app.base

import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.smartwasp.assistant.app.R

/**
 * Created by luotao on 2021/7/15 11:44
 * E-Mail Address：gtkrockets@163.com
 */
abstract class BaseDialogFragment:DialogFragment(), DialogInterface.OnKeyListener {
    //布局文件
    protected open val layoutResID:Int = 0
    //样式数据
    protected open val styleResID:Int = R.style.CommonWindowStyle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //设置弹出框基本样式
        setStyle(STYLE_NO_FRAME, styleResID)
    }

    /**
     * 复写呈现,避免闪退
     */
    override fun show(manager: FragmentManager, tag: String?) {
        try{
            manager.beginTransaction().remove(this).commit()
            super.show(manager, tag)
        }catch (e: Exception) {
            println(e.message)
        }
    }

    /**
     * 添加返回按钮监听
     */
    override fun onKey(dialog: DialogInterface?, keyCode: Int, event: KeyEvent?): Boolean {
        if (isCancelable && keyCode == KeyEvent.KEYCODE_BACK && event!!.action == KeyEvent.ACTION_UP) {
            dismissAllowingStateLoss()
            return true
        }
        return false
    }

    /**
     * 构建可视化控件
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if(layoutResID <= 0){
            return super.onCreateView(inflater, container, savedInstanceState)
        }
        val uiView = inflater.inflate(layoutResID, container, false)
        dialog?.setOnKeyListener(this)
        dialog?.setCanceledOnTouchOutside(false)
        return uiView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    /**
     * 视图可以获取焦点
     */
    override fun onResume() {
        val window = dialog?.window
        val windowParam = window?.attributes
        windowParam?.width = WindowManager.LayoutParams.WRAP_CONTENT
        windowParam?.height = WindowManager.LayoutParams.WRAP_CONTENT
        window?.attributes = windowParam
        super.onResume()
    }

    /**
     * 初始化界面
     */
    protected open fun initUI(){

    }
}