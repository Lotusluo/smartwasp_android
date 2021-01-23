package com.smartwasp.assistant.app.activity

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.*
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import com.iflytek.home.sdk.IFlyHome
import com.iflytek.home.sdk.callback.IFlyHomeCallback
import com.orhanobut.logger.Logger
import com.smartwasp.assistant.app.R
import com.smartwasp.assistant.app.base.BaseActivity
import com.smartwasp.assistant.app.base.SmartApp
import com.smartwasp.assistant.app.databinding.ActivityWebViewBinding
import com.smartwasp.assistant.app.util.IFLYOS
import com.smartwasp.assistant.app.viewModel.WebViewViewModel
import kotlinx.android.synthetic.main.activity_web_view.*

/**
 * Created by luotao on 2021/1/7 15:00
 * E-Mail Address：gtkrockets@163.com
 * 含有WebView的Activity
 */
class WebViewActivity : BaseActivity<WebViewViewModel, ActivityWebViewBinding>() {

    override val layoutResID: Int = R.layout.activity_web_view

    //webView类型
    private var mType:String? = null

    //注册的webView
    private var webViewTag:String? = null

    /**
     * 讯飞交互页Activity
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        webview.settings.javaScriptEnabled = true
        webview.settings.blockNetworkImage = false
        webview.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        mType = intent.getStringExtra(IFLYOS.EXTRA_TYPE)
        //检测是否直接有跳转的tag
        val webTag = intent.getStringExtra(IFLYOS.EXTRA_TAG)
        //注册registerWebView与openWebPage是在View中使用IFlyHomeSDK的地方，其余均在VM中
        webViewTag = IFlyHome.register(webview, object : IFlyHomeCallback() {
            override fun updateHeaderColor(color: String) {
                onUpdateHeader(color)
            }
            override fun updateTitle(title: String) {
                setTittle(title)
            }
            /**
             * 页面中需要打开新页面时回调
             */
            override fun openNewPage(tag: String, params: String?) {
                Logger.d("openNewPage:$tag,$params")
            }
            override fun closePage() { finish() }
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
        }, webTag)

        if(webTag.isNullOrEmpty()){
            //设备ID
            val mayBeDeviceID:String? = intent.getStringExtra(IFLYOS.DEVICE_ID)
            //url地址
            val mayBeUrl:String? = intent.getStringExtra(IFLYOS.EXTRA_URL)
            //讯飞集成页
            val mayBePage:String? = intent.getStringExtra(IFLYOS.EXTRA_PAGE)
            //类型
            val type:String? = intent.getStringExtra(IFLYOS.EXTRA_TYPE)

            //准备跳转到URL地址
            if(!mayBeUrl.isNullOrEmpty()){
                readyToUrl(mayBeUrl,type!!)
                return
            }

            //准备跳转到讯飞集成页
            if(!mayBePage.isNullOrEmpty()){
                readyToDefault(mayBePage,type!!,webViewTag!!,mayBeDeviceID)
                return
            }
        }
    }

    /**
     * 打开网页地址
     * @param url 需要跳转的URL地址
     * @param type 请求类型
     */
    private fun readyToUrl(url:String,type:String){
        when(type){
            IFLYOS.TYPE_BIND->{
                setNavigator(R.mipmap.ic_close)
                mViewModel.openAuthorizePage(webview,url).observe(this, Observer {
                    if(it.startsWith(IFLYOS.OK)){
                        //绑定设备成功
                        setResult(Activity.RESULT_OK)
                        finish()
                    }else{
                        AlertDialog.Builder(this)
                                .setMessage(R.string.bind_devices_err)
                                .setPositiveButton(android.R.string.ok,null)
                                .show()
                    }
                })
            }
            IFLYOS.TYPE_PAGE->{
                webview.loadUrl(url)
            }
        }
    }

    /**
     * 跳转到讯飞集成页
     * @param page 讯飞集成页地址
     * @param type 请求类型
     * @param webViewTag 注册的webView
     * @param deviceID 设备ID
     */
    private fun readyToDefault(page:String,type:String,webViewTag:String,deviceID:String?){
        setNavigator(R.mipmap.ic_close)
        setTittle(getString(
                when(page){
                    IFlyHome.DEVICE_ZONE->{
                        R.string.tip_zone
                    }
                    else->{
                        R.string.page_tip
                    }
                }
        ))
        if(deviceID.isNullOrEmpty()){
            IFlyHome.openWebPage(webViewTag, page)
        }else{
            val params = HashMap<String, String>()
            params["deviceId"] = deviceID
            IFlyHome.openWebPage(webViewTag, page, params).toString()
        }
    }

    /**
     * 更新Toolbar及statusBar颜色
     */
    private fun onUpdateHeader(color: String) {
        if(mType == IFLYOS.TYPE_LOGIN){
            //隐藏toolbar
            toolbar.visibility = View.GONE
            window.statusBarColor = Color.parseColor(color)
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
    override fun onDestroy() {
        webview.destroy()
        super.onDestroy()
        if(mType == IFLYOS.TYPE_LOGIN && null == SmartApp.userBean){
            //登录界面退出时还未登录，则杀掉应用
            SmartApp.finish(0)
        }
    }
}