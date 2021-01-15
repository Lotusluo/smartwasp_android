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
import com.smartwasp.assistant.app.bean.DeviceBean
import com.smartwasp.assistant.app.bean.test.BindDevices
import com.smartwasp.assistant.app.util.AppExecutors
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Response

/**
 * Created by luotao on 2021/1/11 15:44
 * E-Mail Address：gtkrockets@163.com
 */
class MineModel(application: Application):AskDeviceModel(application) {
    /**
     * 获取账号绑定的设备(第一个为添加设备)
     */
    fun getBindDevices():LiveData<Result<BindDevices>> {
        //todo 全部使用IFlyHome处理result
        val bindDevicesData = MutableLiveData<Result<BindDevices>>()
        IFlyHome.getUserDevices(object : ResponseCallback {
            override fun onFailure(call: Call<String>, t: Throwable) {
                bindDevicesData.postValue(Result.failure(Throwable("Err")))
            }

            override fun onResponse(response: Response<String>) {
                if (response.isSuccessful) {
                    val deviceBeans = Gson().fromJson<BindDevices>(response.body(), object : TypeToken<BindDevices>() {}.type)
                    bindDevicesData.postValue(Result.success(deviceBeans))
                } else {
                    bindDevicesData.postValue(Result.failure(Throwable("empty")))
                }
            }
        })
        return bindDevicesData
    }
}