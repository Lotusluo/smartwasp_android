//package com.smartwasp.assistant.app.util
//
//import android.graphics.Bitmap
//import android.net.http.SslError
//import android.view.ViewGroup
//import android.webkit.SslErrorHandler
//import android.webkit.WebResourceError
//import android.webkit.WebResourceRequest
//import android.webkit.WebView
//import android.widget.LinearLayout
//import androidx.fragment.app.Fragment
//import androidx.fragment.app.FragmentActivity
//import androidx.lifecycle.Lifecycle
//import androidx.lifecycle.LifecycleObserver
//import androidx.lifecycle.OnLifecycleEvent
//import com.just.agentweb.AgentWeb
//import com.just.agentweb.WebViewClient
//import com.orhanobut.logger.Logger
//import com.smartwasp.assistant.app.R
//import com.smartwasp.assistant.app.base.SmartApp
//
///**
// * Created by luotao on 2021/1/9 13:33
// * E-Mail Address：gtkrockets@163.com
// */
//class WebViewHelper<HOST> private constructor(var containerID:ViewGroup): LifecycleObserver {
//    private var mPreAgentWeb: AgentWeb.PreAgentWeb?= null
//    private var mPage: AgentWeb?= null
//    companion object{
//        /**
//         * 生成绑定 FragmentActivity|与Fragment 的WebViewHelper
//         */
//        fun<HOST> create(host:HOST,_containerID:ViewGroup):WebViewHelper<HOST>{
//            val mWebViewHelper = WebViewHelper<HOST>(_containerID)
//            mWebViewHelper.init(host,_containerID)
//            return mWebViewHelper
//        }
//    }
//
//    /**
//     * 初始化mAgentWeb
//     * @param host webView宿主
//     * @param _containerID webView容器
//     */
//    private fun init(host: HOST,_containerID:ViewGroup){
//        val builder : AgentWeb.AgentBuilder = when(host){
//            is Fragment ->{
//                AgentWeb.with(host)
//            }else ->{
//                AgentWeb.with(host as FragmentActivity)
//            }
//        }
//
//        mPreAgentWeb = builder
//            .setAgentWebParent(_containerID, LinearLayout.LayoutParams(-1, -1))
//            .useDefaultIndicator(SmartApp.app.resources.getColor(R.color.smartwasp_yellow))
//            .setWebViewClient(mWebViewClient)
//            .createAgentWeb()
//            .ready()
//    }
//
//    /**
//     * 加载地址
//     * @param url 加载的地址
//     */
//    fun load(url:String){
//        mPage?.webLifeCycle?.onDestroy()
//        mPage = mPreAgentWeb?.go(url)
//        Logger.e(url)
//    }
//
//    /**
//     * 浏览器监听器
//     */
//    private val mWebViewClient = object:WebViewClient(){
//        private val timerMap =
//            HashMap<String, Long>()
//
//        /**
//         * 网页开始加载
//         */
//        override fun onPageStarted(view: WebView?, url: String, favicon: Bitmap?) {
//            super.onPageStarted(view, url, favicon)
//            Logger.e("onPageStarted:${url}")
//            timerMap[url] = System.nanoTime()
//        }
//
//        /**
//         * 网页加载完毕
//         */
//        override fun onPageFinished(view: WebView?, url: String?) {
//            super.onPageFinished(view, url)
//            Logger.e("onPageFinished:${url}")
//        }
//
//        /**
//         * 处理网页加载失败回调
//         */
//        override fun onReceivedError(
//            view: WebView?,
//            request: WebResourceRequest?,
//            error: WebResourceError?
//        ) {
//            Logger.e("onReceivedError:${error.toString()}")
//            super.onReceivedError(view, request, error)
//        }
//
//        /**
//         * SSL安全校验失败
//         */
//        override fun onReceivedSslError(
//            view: WebView?,
//            handler: SslErrorHandler?,
//            error: SslError?
//        ) {
//            handler?.proceed()
//            super.onReceivedSslError(view, handler, error)
//        }
//    }
//
//    /**
//     * 交互状态
//     */
//    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
//    fun onResume(){
//        mPage?.webLifeCycle?.onResume()
//    }
//
//    /**
//     * 停止
//     */
//    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
//    fun onPause(){
//        mPage?.webLifeCycle?.onPause()
//    }
//
//    /**
//     * 销毁
//     */
//    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
//    fun onDestroy(){
//        mPage?.webLifeCycle?.onDestroy()
//        mPage = null
//        mPreAgentWeb = null
//    }
//}