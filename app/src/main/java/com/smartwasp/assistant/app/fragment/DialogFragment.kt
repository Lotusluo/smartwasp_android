package com.smartwasp.assistant.app.fragment


import com.iflytek.home.sdk.IFlyHome
import com.smartwasp.assistant.app.R
import com.smartwasp.assistant.app.base.BaseViewModel
import com.smartwasp.assistant.app.base.SmartApp
import com.smartwasp.assistant.app.databinding.FragmentDialogBinding

/**
 * Created by luotao on 2021/1/15 13:39
 * E-Mail Address：gtkrockets@163.com
 */
class DialogFragment private constructor():WebViewMajorFragment<BaseViewModel,FragmentDialogBinding>() {

    companion object{
        fun newsInstance():DialogFragment{
            return DialogFragment()
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
            SmartApp.activity?.currentDevice?.let {
                val params = HashMap<String, String>()
                params["deviceId"] = it.device_id
                IFlyHome.openWebPage(webView, IFlyHome.DIALOGUE, params).toString()
            } ?: kotlin.run {

            }
        }
    }
}