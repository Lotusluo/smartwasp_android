package com.smartwasp.assistant.app.viewModel

import android.app.Application
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.iflytek.home.sdk.IFlyHome
import com.iflytek.home.sdk.callback.ResponseCallback
import com.orhanobut.logger.Logger
import com.smartwasp.assistant.app.base.BaseViewModel
import com.smartwasp.assistant.app.base.SmartApp
import com.smartwasp.assistant.app.bean.AuthCheckBean
import com.smartwasp.assistant.app.bean.BindDevices
import com.smartwasp.assistant.app.bean.DeviceBean
import com.smartwasp.assistant.app.bean.FindBean
import com.smartwasp.assistant.app.util.AppExecutors
import com.smartwasp.assistant.app.util.IFLYOS
import retrofit2.Call
import retrofit2.Response

/**
 * Created by luotao on 2021/2/22 11:44
 * E-Mail Address：gtkrockets@163.com
 */
class ApBindModel(application: Application): BaseViewModel(application) {

    //轮询结果
    val askDeferred = MutableLiveData<String>()
    //授权码
    private lateinit var authCode:String

    /**
     * 轮询设备状态
     * @param authCode 设备信息
     * @param delayInSeconds 轮询时间
     */
    fun askDevAuth(authCode:String,delayInSeconds:Long = 1){
        this.authCode = authCode
        cancelAskDevStatus()
        AppExecutors.get().mainThread().executeDelay(askDevAuthJob,delayInSeconds * 1000)
    }

    /**
     * 取消轮询设备状态
     */
    private fun cancelAskDevStatus(owner: LifecycleOwner? = null){
        AppExecutors.get().mainThread().removeCallbacks(askDevAuthJob)
        owner?.let {
            askDeferred.removeObservers(it)
        }
    }

    override fun onCleared() {
        super.onCleared()
        cancelAskDevStatus()
    }

    /**
     * 询问设备状态动作
     */
    private var askDevAuthJob = Runnable {
        askDevAuth(authCode)
        IFlyHome.checkAuthCodeState(authCode, object : ResponseCallback {
            override fun onResponse(response: Response<String>) {
                if(response.isSuccessful){
                    Logger.e(response.body().toString())
                    val authCheckCode = Gson().fromJson<AuthCheckBean>(response.body(), object: TypeToken<AuthCheckBean>(){}.type)
                    if(authCheckCode.code == "0000"){
                        cancelAskDevStatus()
                        askDeferred.postValue(IFLYOS.OK)
                    }
                }
            }
            override fun onFailure(call: Call<String>, t: Throwable) {}
        })
    }
}