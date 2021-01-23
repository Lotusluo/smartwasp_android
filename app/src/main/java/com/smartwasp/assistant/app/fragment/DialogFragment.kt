package com.smartwasp.assistant.app.fragment

import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.iflytek.home.sdk.IFlyHome
import com.iflytek.home.sdk.callback.IFlyHomeCallback
import com.orhanobut.logger.Logger
import com.smartwasp.assistant.app.R
import com.smartwasp.assistant.app.base.addFragmentByTag
import com.smartwasp.assistant.app.bean.ItemBean
import com.smartwasp.assistant.app.databinding.FragmentDialogBinding
import com.smartwasp.assistant.app.viewModel.DialogModel
import kotlinx.android.synthetic.main.activity_web_view.*
import kotlinx.android.synthetic.main.activity_web_view.webview
import kotlinx.android.synthetic.main.fragment_dialog.*

/**
 * Created by luotao on 2021/1/15 13:39
 * E-Mail Address：gtkrockets@163.com
 */
class DialogFragment private constructor():MainChildFragment<DialogModel,FragmentDialogBinding>() {

    companion object{
        fun newsInstance():DialogFragment{
            return DialogFragment()
        }
    }

    //布局文件
    override val layoutResID:Int = R.layout.fragment_dialog
    //注册的webView
    private var webViewTag:String? = null

    /**
     * 注册webView
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //注册registerWebView与openWebPage是在View中使用IFlyHomeSDK的地方，其余均在VM中
        webViewTag = IFlyHome.register(webview, object : IFlyHomeCallback() {
            override fun updateHeaderColor(color: String) {}
            override fun updateTitle(title: String) {}
            /**
             * 页面中需要打开新页面时回调
             */
            override fun openNewPage(tag: String, params: String?) {}
            override fun closePage() { }
            override fun getWebViewClient(): WebViewClient? {
                return object : WebViewClient() {
                    /**
                     * 加载页面错误
                     */
                    override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                        super.onReceivedError(view, request, error)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            Logger.e("onReceivedError:${error?.description}")
                        }
                    }
                }
            }
        })
    }

    /**
     * 通知选择的设备变更
     */
    override fun notifyCurDeviceChanged(){
        super.notifyCurDeviceChanged()
        if(isResumed){
            currentDevice?.let {
                webViewTag?.let {webView->
                    val params = HashMap<String, String>()
                    params["deviceId"] = it.device_id
                    IFlyHome.openWebPage(webView, IFlyHome.DIALOGUE, params).toString()
                }
                webview.visibility = View.VISIBLE
            }?: kotlin.run {
                //todo 没有deviceID
                webview.visibility = View.GONE
            }
        }
    }

    /**
     * 是否隐藏中
     */
    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        webViewTag?.let {
            if(hidden){
                IFlyHome.pauseWebView(it)
            }else{
                IFlyHome.resumeWebView(it)
            }
        }
    }

    /**
     * 恢复
     */
    override fun onResume() {
        super.onResume()
        webViewTag?.let {
            IFlyHome.resumeWebView(it)
        }
    }

    /**
     * 暂停
     */
    override fun onPause() {
        super.onPause()
        webViewTag?.let {
            IFlyHome.pauseWebView(it)
        }
    }

    /**
     * 销毁
     */
    override fun onDestroyView() {
        webview.destroy()
        super.onDestroyView()
    }
}