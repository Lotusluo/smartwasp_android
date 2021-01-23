package com.smartwasp.assistant.app.fragment


import com.iflytek.home.sdk.IFlyHome
import com.smartwasp.assistant.app.R
import com.smartwasp.assistant.app.base.addFragmentByTag
import com.smartwasp.assistant.app.databinding.FragmentDialogBinding
import com.smartwasp.assistant.app.viewModel.DialogModel


/**
 * Created by luotao on 2021/1/23 17:07
 * E-Mail Address：gtkrockets@163.com
 */
class SkillFragment private constructor(): WebViewMajorFragment<DialogModel,FragmentDialogBinding>() {

    companion object{
        fun newsInstance():SkillFragment{
            return SkillFragment()
        }
    }

    //布局文件
    override val layoutResID:Int = R.layout.fragment_dialog

    /**
     * 通知选择的设备变更
     */
    override fun notifyCurDeviceChanged() {
        super.notifyCurDeviceChanged()
        webViewTag?.let { webView ->
            currentDevice?.let {
                val params = HashMap<String, String>()
                params["deviceId"] = it.device_id
                IFlyHome.openWebPage(webView, IFlyHome.SKILLS, params).toString()
            } ?: kotlin.run {

            }
        }
    }

    /**
     * 打开新页面
     * @param tag
     * @param params
     */
    override fun openNewPage1(tag: String, params: String?) {
        addFragmentByTag(R.id.container,WebViewMinorFragment.newsInstance(tag = tag))
    }
}