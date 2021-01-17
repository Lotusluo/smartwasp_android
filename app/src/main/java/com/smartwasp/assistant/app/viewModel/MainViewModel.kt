package com.smartwasp.assistant.app.viewModel

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.iflytek.home.sdk.IFlyHome
import com.iflytek.home.sdk.callback.IFlyHomeLoginCallback
import com.iflytek.home.sdk.callback.ResponseCallback
import com.orhanobut.logger.Logger
import com.smartwasp.assistant.app.base.BaseViewModel
import com.smartwasp.assistant.app.base.SmartApp
import com.smartwasp.assistant.app.bean.DeviceBean
import com.smartwasp.assistant.app.bean.UserBean
import com.smartwasp.assistant.app.bean.test.BindDevices
import com.smartwasp.assistant.app.bean.test.TimeBean
import com.smartwasp.assistant.app.bean.test.TimeData
import com.smartwasp.assistant.app.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Response

/**
 * Created by luotao on 2021/1/8 12:01
 * E-Mail Address：gtkrockets@163.com
 */
class MainViewModel(application: Application):AskDeviceModel(application) {
    //获取网络时间校准
    fun timeCheck():LiveData<TimeBean<TimeData>> {
        val timeCheckData = MutableLiveData<TimeBean<TimeData>>()
        //获取原始数据
        retrofit<TimeBean<TimeData>> {
            api = RetrofitManager.get().retrofitApiService?.launch()
            onSuccess {
                timeCheckData.postValue(it)
            }
        }
        return timeCheckData
    }

    //请求讯飞登录
    fun iflyoslogin():LiveData<String>{
        val iflyosloginData = MutableLiveData<String>()
        IFlyHome.openLogin(object : IFlyHomeLoginCallback {
            //登录失败
            override fun onLoginFailed(type: Int, error: Throwable?) {
                Logger.e("type:$type,${error?.message}")
                iflyosloginData.postValue(IFLYOS.ERROR)
            }
            //登录成功
            override fun onLoginSuccess() {
                iflyosloginData.postValue(IFLYOS.OK)
            }
            //返回登录的地址
            override fun openNewPage(tag: String): Boolean {
                iflyosloginData.postValue("${IFLYOS.EXTRA}:$tag")
                return false
            }
        })
        return iflyosloginData
    }

    /**
     * 获取用户唯一识别符
     */
    fun getUserBean():LiveData<String>{
        val userIdData = MutableLiveData<String>()
        IFlyHome.getUserInfo(object : ResponseCallback{
            //获取用户信息失败
            override fun onFailure(call: Call<String>, t: Throwable) {
                userIdData.postValue(IFLYOS.ERROR)
            }
            override fun onResponse(response: Response<String>) {
                //成功获取用户信息
                if(response.isSuccessful){
                    val userBean = Gson().fromJson<UserBean>(response.body(), object:TypeToken<UserBean>(){}.type)
                    SmartApp.userBean = userBean
                    launch(Dispatchers.IO) {
                        ConfigUtils.putString(ConfigUtils.KEY_USER_ID, SerializableUtils.writeObject(userBean))
                        Logger.e("写入userID!")
                    }
                    userIdData.postValue(IFLYOS.OK)
                }else{
                    userIdData.postValue(IFLYOS.ERROR)
                }
            }
        })
        return userIdData
    }

    /**
     * 获取账号绑定的设备(第一个为添加设备)
     */
    fun getBindDevices():LiveData<Result<BindDevices>>{
        //todo 全部使用IFlyHome处理result
        val bindDevicesData = MutableLiveData<Result<BindDevices>>()
        val result = IFlyHome.getUserDevices(object :ResponseCallback{
            override fun onFailure(call: Call<String>, t: Throwable) {
                bindDevicesData.postValue(Result.failure(Throwable("Err")))
            }
            override fun onResponse(response: Response<String>) {
                if(response.isSuccessful){
                    //todo Gson转换捕捉错误
                    val deviceBeans = Gson().fromJson<BindDevices>(response.body(), object: TypeToken<BindDevices>(){}.type)
                    bindDevicesData.postValue(Result.success(deviceBeans))
                }else{
                    bindDevicesData.postValue(Result.failure(Throwable("empty")))
                }
            }
        })
        return bindDevicesData
    }
}