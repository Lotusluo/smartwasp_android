package com.smartwasp.assistant.app.fragment

import android.os.Bundle
import android.view.View
import com.smartwasp.assistant.app.R
import com.smartwasp.assistant.app.base.BaseViewModel
import com.smartwasp.assistant.app.base.addFragmentByTag
import com.smartwasp.assistant.app.databinding.FragmentMinorWebBinding
import com.smartwasp.assistant.app.util.IFLYOS

/**
 * Created by luotao on 2021/1/23 17:03
 * E-Mail Address：gtkrockets@163.com
 */
class WebViewMinorFragment private constructor():WebViewMajorFragment<BaseViewModel,FragmentMinorWebBinding>() {

    companion object{
        fun newsInstance(type:String?=null,tag:String):WebViewMinorFragment{
            val webViewMinorFragment = WebViewMinorFragment()
            webViewMinorFragment.arguments = Bundle().apply {
                putString(IFLYOS.EXTRA_TYPE,type)
                putString(IFLYOS.EXTRA_TAG,tag)
            }
            return webViewMinorFragment
        }
    }

    //布局文件
    override val layoutResID:Int = R.layout.fragment_minor_web

    //webView类型
    private var mType:String? = null

    /**
     * 注册webView
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setToolBarIcon(R.drawable.ic_navback,R.color.smartwasp_orange)
        mType = arguments?.getString(IFLYOS.EXTRA_TYPE)
    }

    /**
     * 打开新页面
     * @param tag
     * @param params
     */
    override fun openNewPage1(tag: String, params: String?) {
        addFragmentByTag(R.id.container,newsInstance(tag = tag))
    }
}