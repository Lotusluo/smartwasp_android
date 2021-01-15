package com.smartwasp.assistant.app.viewModel

import android.app.Application
import android.webkit.WebView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.iflytek.home.sdk.IFlyHome
import com.iflytek.home.sdk.callback.AuthResultCallback
import com.orhanobut.logger.Logger
import com.smartwasp.assistant.app.base.BaseViewModel
import com.smartwasp.assistant.app.bean.test.TimeBean
import com.smartwasp.assistant.app.bean.test.TimeData
import com.smartwasp.assistant.app.util.IFLYOS
import com.smartwasp.assistant.app.util.RetrofitManager
import com.smartwasp.assistant.app.util.retrofit
import kotlinx.android.synthetic.main.activity_web_view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Created by luotao on 2021/1/8 12:01
 * E-Mail Address：gtkrockets@163.com
 */
class WebViewViewModel(application: Application):BaseViewModel(application) {
    /**
     * 打开搜全页
     */
    fun openAuthorizePage(webView:WebView,url:String):LiveData<String>{
        val authorizeData = MutableLiveData<String>()
        IFlyHome.openAuthorizePage(webView, url,object: AuthResultCallback {
            override fun onFailed(params: String) {
                authorizeData.postValue(IFLYOS.ERROR)
            }
            override fun onSuccess() {
                authorizeData.postValue(IFLYOS.OK)
            }
        })
        return authorizeData
    }
}