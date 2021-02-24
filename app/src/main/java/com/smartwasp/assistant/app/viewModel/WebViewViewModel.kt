package com.smartwasp.assistant.app.viewModel

import android.app.Application
import android.webkit.WebView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.iflytek.home.sdk.IFlyHome
import com.iflytek.home.sdk.callback.AuthResultCallback
import com.smartwasp.assistant.app.base.BaseViewModel
import com.smartwasp.assistant.app.util.IFLYOS


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
        val result = IFlyHome.openAuthorizePage(webView, url,object: AuthResultCallback {
            override fun onFailed(params: String) {
                authorizeData.postValue(IFLYOS.ERROR)
            }
            override fun onSuccess() {
                authorizeData.postValue(IFLYOS.OK)
            }
        })
        if(result != IFlyHome.RESULT_OK){
            authorizeData.postValue(IFLYOS.ERROR)
        }
        return authorizeData
    }
}