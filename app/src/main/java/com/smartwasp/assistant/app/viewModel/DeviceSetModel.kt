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
import com.smartwasp.assistant.app.util.IFLYOS
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Response

/**
 * Created by luotao on 2021/1/11 15:44
 * E-Mail Address：gtkrockets@163.com
 */
class DeviceSetModel(application: Application):BaseViewModel(application) {
    /**
     * 更新设备别名
     * @param deviceId 设备标识符
     * @param alias 设备别名
     */
    fun updateAlias(deviceId:String,alias:String):LiveData<String>{
        val aliasData = MutableLiveData<String>()
        IFlyHome.updateDeviceInfo(deviceId, alias, null, null, null, object:ResponseCallback{
            override fun onFailure(call: Call<String>, t: Throwable) {
                aliasData.postValue(IFLYOS.ERROR)
            }
            override fun onResponse(response: Response<String>) {
                if(response.isSuccessful){
                    aliasData.postValue(IFLYOS.OK)
                }else{
                    aliasData.postValue(IFLYOS.ERROR)
                }
            }
        })
        return aliasData
    }

    /**
     * 更新设备持续交互能力
     * @param deviceId 设备标识符
     * @param isLongWake 设备别名
     */
    fun updateLongWake(deviceId:String,isLongWake:Boolean):LiveData<String>{
        val longWakeData = MutableLiveData<String>()
        IFlyHome.updateDeviceInfo(deviceId, null, null, isLongWake, null, object:ResponseCallback{
            override fun onFailure(call: Call<String>, t: Throwable) {
                longWakeData.postValue(IFLYOS.ERROR)
            }
            override fun onResponse(response: Response<String>) {
                Logger.e(response.body().toString())
                if(response.isSuccessful){
                    longWakeData.postValue(IFLYOS.OK)
                }else{
                    longWakeData.postValue(IFLYOS.ERROR)
                }
            }
        })
        return longWakeData
    }

    /**
     * 解除绑定
     * @param deviceId 设备标识符
     */
    fun unBind(deviceId:String):LiveData<String>{
        val unBindData = MutableLiveData<String>()
        IFlyHome.deleteUserDevice(deviceId, object:ResponseCallback{
            override fun onFailure(call: Call<String>, t: Throwable) {
                unBindData.postValue(IFLYOS.ERROR)
            }
            override fun onResponse(response: Response<String>) {
                if(response.isSuccessful){
                    unBindData.postValue(IFLYOS.OK)
                }else{
                    unBindData.postValue(IFLYOS.ERROR)
                }
            }
        })
        return unBindData
    }

    /**
     * 获取一次某一个设备的信息
     * @param deviceID 设备ID
     */
    fun askDevStatus(deviceID:String):MutableLiveData<Result<DeviceBean>>{
        val devStatusData = MutableLiveData<Result<DeviceBean>>()
        IFlyHome.getDeviceDetail(deviceID,object :ResponseCallback{
            override fun onFailure(call: Call<String>, t: Throwable) {
                devStatusData.postValue(Result.failure(Throwable("Err")))
            }
            override fun onResponse(response: Response<String>) {
                if(response.isSuccessful){
                    val deviceInfoBean = Gson().fromJson<DeviceBean>(response.body(), object: TypeToken<DeviceBean>(){}.type)
                    devStatusData.postValue(Result.success(deviceInfoBean))
                }else{
                    devStatusData.postValue(Result.failure(Throwable("empty")))
                }
            }
        })
        return devStatusData
    }
}