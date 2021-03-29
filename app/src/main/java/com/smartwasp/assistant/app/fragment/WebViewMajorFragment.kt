package com.smartwasp.assistant.app.fragment

import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.*
import androidx.databinding.ViewDataBinding
import com.iflytek.home.sdk.IFlyHome
import com.iflytek.home.sdk.callback.IFlyHomeCallback
import com.orhanobut.logger.Logger
import com.smartwasp.assistant.app.R
import com.smartwasp.assistant.app.base.BaseViewModel
import com.smartwasp.assistant.app.util.IFLYOS
import kotlinx.android.synthetic.main.activity_web_view.webview
import kotlinx.coroutines.*

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
            override fun updateTitle(title: String) {
                setTittle(title)
            }
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
//                            onReceivedError:net::ERR_NAME_NOT_RESOLVED
                            errorCode = error?.errorCode ?: 0
                            Logger.e("errorCode:$errorCode")
                        }
                    }
                }
            }
        },webTag)
    }
    private var errorCode = 0

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

    private var job:Job? = null
    /**
     * 恢复
     */
    override fun onResume() {
        super.onResume()
        webViewTag?.let {
            IFlyHome.resumeWebView(it)
        }
        job?.cancel()
        if(errorCode != 0){
            webview.reload()
//            job = GlobalScope.launch(Dispatchers.IO) {
//                delay(2000)
//                suspend {
//                    withContext(Dispatchers.Main) {
//                        webview.reload()
//                    }
//                }.invoke()
//            }
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
        job?.cancel()
    }

    /**
     * 销毁
     */
    override fun onDestroyView() {
        webview.destroy()
        super.onDestroyView()
    }
}