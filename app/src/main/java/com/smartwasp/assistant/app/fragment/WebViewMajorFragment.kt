package com.smartwasp.assistant.app.fragment

import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.databinding.ViewDataBinding
import com.iflytek.home.sdk.IFlyHome
import com.iflytek.home.sdk.callback.IFlyHomeCallback
import com.orhanobut.logger.Logger
import com.smartwasp.assistant.app.base.BaseViewModel
import com.smartwasp.assistant.app.util.IFLYOS
import kotlinx.android.synthetic.main.activity_web_view.webview

/**
 * Created by luotao on 2021/1/23 17:09
 * E-Mail Address：gtkrockets@163.com
 */
abstract class WebViewMajorFragment<
        VM: BaseViewModel,
        BD: ViewDataBinding>: MainChildFragment<VM, BD>() {

    //注册的webView
    protected var webViewTag:String? = null

    /**
     * 注册webView
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        webViewTag = onWebRegister()
    }

    /**
     * 注册webview
     */
    private fun onWebRegister():String{
        val webTag = arguments?.getString(IFLYOS.EXTRA_TAG)
        //注册registerWebView与openWebPage是在View中使用IFlyHomeSDK的地方，其余均在VM中
        return IFlyHome.register(webview, object : IFlyHomeCallback() {
            override fun updateHeaderColor(color: String) {}
            override fun updateTitle(title: String) {}
            /**
             * 页面中需要打开新页面时回调
             */
            override fun openNewPage(tag: String, params: String?) {
                openNewPage1(tag,params)
            }
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
        },webTag)
    }

    /**
     * 打开新页面
     * @param tag
     * @param params
     */
    protected open fun openNewPage1(tag: String, params: String?) {

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