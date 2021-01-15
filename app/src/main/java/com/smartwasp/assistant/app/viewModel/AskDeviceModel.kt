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
open class AskDeviceModel(application: Application):BaseViewModel(application) {

    //轮询结果
    private val askDeferred = MutableLiveData<Result<DeviceBean>>()
    //是否在轮询
    private var isAskingDeferred:Boolean = false
    //在轮询中的设备
    private var mDevice:DeviceBean? = null

    /**
     * 轮询设备状态
     * @param device 设备信息
     * @param delayInSeconds 轮询时间
     */
    fun askDevStatus(device:DeviceBean,delayInSeconds:Long = 20):MutableLiveData<Result<DeviceBean>>?{
        cancelAskDevStatus()
        if(device.isHeader())
            return null
        mDevice = device
        AppExecutors.get().mainThread().executeDelay(askDevStatusJob,delayInSeconds * 1000)
        return askDeferred
    }

    /**
     * 取消轮询设备状态
     */
    fun cancelAskDevStatus(owner: LifecycleOwner? = null){
        AppExecutors.get().mainThread().removeCallbacks(askDevStatusJob)
        isAskingDeferred = false
        mDevice = null
        owner?.let {
            askDeferred.removeObservers(it)
        }
    }

    /**
     * 询问设备状态动作
     */
    private var askDevStatusJob = Runnable {
        mDevice?.let {
            askDevStatus(it)
        }
        if(isAskingDeferred)
            return@Runnable
        if(this is MainViewModel && SmartApp.DOS_MINE_FRAGMENT_SHOWN)
           return@Runnable
        Logger.e("askDevStatusJob:${this}")
        isAskingDeferred = true
        IFlyHome.getDeviceDetail(mDevice!!.device_id,object :ResponseCallback{
            override fun onFailure(call: Call<String>, t: Throwable) {
                isAskingDeferred = false
                askDeferred.postValue(Result.failure(Throwable("Err")))
            }
            override fun onResponse(response: Response<String>) {
                isAskingDeferred = false
                if(response.isSuccessful){
                    mDevice?.let {
                        val deviceInfoBean = Gson().fromJson<DeviceBean>(response.body(), object: TypeToken<DeviceBean>(){}.type)
                        deviceInfoBean.position = mDevice!!.position
                        askDeferred.postValue(Result.success(deviceInfoBean))
                    } ?: askDeferred.postValue(Result.failure(Throwable("empty")))
                }else{
                    askDeferred.postValue(Result.failure(Throwable("empty")))
                }
            }
        })
    }
}