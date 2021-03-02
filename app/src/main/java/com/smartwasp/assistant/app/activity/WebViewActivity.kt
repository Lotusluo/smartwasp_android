package com.smartwasp.assistant.app.activity

import android.app.Activity
import android.content.Intent
import android.content.res.TypedArray
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.iflytek.home.sdk.IFlyHome
import com.iflytek.home.sdk.callback.IFlyHomeCallback
import com.kyleduo.switchbutton.VoicePlayingIcon
import com.orhanobut.logger.Logger
import com.smartwasp.assistant.app.R
import com.smartwasp.assistant.app.base.BaseActivity
import com.smartwasp.assistant.app.base.SmartApp
import com.smartwasp.assistant.app.bean.DeviceBean
import com.smartwasp.assistant.app.bean.MusicStateBean
import com.smartwasp.assistant.app.bean.StatusBean
import com.smartwasp.assistant.app.databinding.ActivityWebViewBinding
import com.smartwasp.assistant.app.fragment.MainChildFragment
import com.smartwasp.assistant.app.util.AppExecutors
import com.smartwasp.assistant.app.util.IFLYOS
import com.smartwasp.assistant.app.util.LoadingUtil
import com.smartwasp.assistant.app.util.StatusBarUtil
import com.smartwasp.assistant.app.viewModel.WebViewViewModel
import kotlinx.android.synthetic.main.activity_music.*
import kotlinx.android.synthetic.main.activity_web_view.*
import kotlinx.android.synthetic.main.activity_web_view.toolbar
import kotlinx.android.synthetic.main.fragment_music_item.*
import kotlinx.android.synthetic.main.fragment_music_item.bezelImageView
import kotlinx.android.synthetic.main.layout_device_header.*
import kotlinx.android.synthetic.main.layout_toolbar_device.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

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

    //媒体状态
    private val mediaStateObserver: MutableLiveData<StatusBean<MusicStateBean>> = MutableLiveData()
    //当前的媒体状态
    private var currentMedia:StatusBean<MusicStateBean>? = null
        set(value) {
            field = value
            notifyMediaChanged()
        }

    /**
     * 设备媒体状态改变
     */
    private fun notifyMediaChanged(){
        val iconMedia = findViewById<VoicePlayingIcon>(R.id.media_icon)
        iconMedia?.let {music->
            currentMedia?.data?.music_player?.let {
                if (it.playing)music.start()
                else music.stop()
            }?: kotlin.run {
                music.stop()
            }
        }
    }

    /**
     * 初始化观察者
     */
    private fun initObserver(){
        SmartApp.addMediaObserver(mediaStateObserver)
        mediaStateObserver.observe(this, Observer {state->
            currentMedia?.let {
                if(it.data.device_id == state.data.device_id){
                    if(it.timestamp.toLong() < state.timestamp.toLong()){
                        currentMedia = state
                    }
                }
            }?: kotlin.run {
                currentMedia = state
            }
        })
    }

    /**
     * 讯飞交互页Activity
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


//        webview.settings.javaScriptEnabled = true
//        webview.settings.blockNetworkImage = false
//        webview.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        mType = intent.getStringExtra(IFLYOS.EXTRA_TYPE)
        //检测是否直接有跳转的tag
        val webTag = intent.getStringExtra(IFLYOS.EXTRA_TAG)
        //注册registerWebView与openWebPage是在View中使用IFlyHomeSDK的地方，其余均在VM中
        webViewTag = IFlyHome.register(webview, object : IFlyHomeCallback() {
            override fun updateHeaderColor(color: String) {
                window.statusBarColor =
                if(toolbar.visibility == View.GONE) {
                    Color.parseColor(color)
                } else {
                    val attribute = intArrayOf(R.attr.toolbarColor)
                    val array: TypedArray = theme.obtainStyledAttributes(attribute)
                    val color = array.getColor(0,Color.CYAN)
                    array.recycle()
                    color
                }
            }
            override fun updateTitle(title: String) {
                setTittle(title)
            }
            /**
             * 页面中需要打开新页面时回调
             */
            override fun openNewPage(tag: String, params: String?) {
                startActivity(Intent(this@WebViewActivity, WebViewActivity::class.java).apply {
                    putExtra(IFLYOS.EXTRA_TAG,tag)
                    putExtra(IFLYOS.EXTRA_TYPE,IFLYOS.TYPE_PAGE)
                })
                //内容账号页可能存在更改设备状态参数,设置刷新TAG
                intent.getStringExtra(IFLYOS.EXTRA_PAGE)?.let {
                    if(it == IFlyHome.ACCOUNTS){
                        SmartApp.NEED_MAIN_REFRESH_DEVICES = true
                    }
                }
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

        //设置toolbar可见与否
        toolbar.visibility = when(mType){
            IFLYOS.TYPE_LOGIN->{
                View.GONE
            }
            else->{
                //可能存在的媒体状态
                currentMedia = SmartApp.activity?.mediaState
                initObserver()
                View.VISIBLE
            }
        }

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
                setNavigator(R.drawable.ic_close)
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
        if(deviceID.isNullOrEmpty()){
            IFlyHome.openWebPage(webViewTag, page)
        }else{
            val params = HashMap<String, String>()
            params["deviceId"] = deviceID
            IFlyHome.openWebPage(webViewTag, page, params).toString()
        }
    }

    /**
     * 拦截退出
     */
    override fun interceptLeftButton():Boolean{
        if(webview?.canGoBack()!!){
            webview.goBack()
            return true
        }
        return super.interceptLeftButton()
    }

    /**
     * 按钮点击
     * @param v
     */
    override fun onButtonClick(v: View){
        super.onButtonClick(v)
        when(v.id){
            R.id.media_icon->{
                //跳转音乐播放界面
                SmartApp.activity?.turnToMusic()
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
        notifyMediaChanged()
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
     * 停止
     */
    override fun onStop() {
        super.onStop()
        val iconMedia = findViewById<VoicePlayingIcon>(R.id.media_icon)
        iconMedia?.stop()
    }

    /**
     * 销毁
     */
    override fun onDestroy() {
        webview.destroy()
        super.onDestroy()
        SmartApp.removeMediaObserver(mediaStateObserver)
        if(mType == IFLYOS.TYPE_LOGIN && !IFlyHome.isLogin()){
            //登录界面退出时还未登录，则杀掉应用
            SmartApp.finish(0)
        }
    }
}