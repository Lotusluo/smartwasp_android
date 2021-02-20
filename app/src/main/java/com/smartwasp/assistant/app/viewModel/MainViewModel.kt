package com.smartwasp.assistant.app.viewModel

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.SystemClock
import androidx.core.util.TimeUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.iflytek.home.sdk.IFlyHome
import com.iflytek.home.sdk.callback.IFlyHomeLoginCallback
import com.iflytek.home.sdk.callback.ResponseCallback
import com.iflytek.home.sdk.push.OsPushService
import com.orhanobut.logger.Logger
import com.smartwasp.assistant.app.activity.MainActivity
import com.smartwasp.assistant.app.base.SmartApp
import com.smartwasp.assistant.app.bean.*
import com.smartwasp.assistant.app.bean.test.TimeBean
import com.smartwasp.assistant.app.bean.test.TimeData
import com.smartwasp.assistant.app.service.PushService
import com.smartwasp.assistant.app.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Response
import java.util.concurrent.TimeUnit

/**
 * Created by luotao on 2021/1/8 12:01
 * E-Mail Address：gtkrockets@163.com
 */
class MainViewModel(application: Application):AskDeviceModel(application) {
    //请求讯飞登录
    fun iflyoslogin():LiveData<String>{
        val iflyosloginData = MutableLiveData<String>()
        val result = IFlyHome.openLogin(object : IFlyHomeLoginCallback {
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
        if(result != IFlyHome.RESULT_OK){
            iflyosloginData.postValue(IFLYOS.ERROR)
        }
        return iflyosloginData
    }

    /**
     * 获取用户唯一识别符
     */
    fun getUserBean():LiveData<String>{
        val userIdData = MutableLiveData<String>()
        val result = IFlyHome.getUserInfo(object : ResponseCallback{
            //获取用户信息失败
            override fun onFailure(call: Call<String>, t: Throwable) {
                userIdData.postValue(IFLYOS.ERROR)
            }
            override fun onResponse(response: Response<String>) {
                //成功获取用户信息
                if(response.isSuccessful){
                    try {
                        val userBean = Gson().fromJson<UserBean>(response.body(), object:TypeToken<UserBean>(){}.type)
                        SmartApp.userBean = userBean
                        AppExecutors.get().diskIO().execute {
                            ConfigUtils.putString(ConfigUtils.KEY_USER_ID, SerializableUtils.writeObject(userBean))
                            userIdData.postValue(IFLYOS.OK)
                        }
                    }catch (e:Throwable){
                        userIdData.postValue(IFLYOS.ERROR)
                    }
                }else{
                    userIdData.postValue(IFLYOS.ERROR)
                }
            }
        })
        if(result != IFlyHome.RESULT_OK){
            userIdData.postValue(IFLYOS.ERROR)
        }
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
                    try {
                        val deviceBeans = Gson().fromJson<BindDevices>(response.body(), object: TypeToken<BindDevices>(){}.type)
                        bindDevicesData.postValue(Result.success(deviceBeans))
                    }catch (e:Throwable){
                        bindDevicesData.postValue(Result.failure(Throwable("Err")))
                    }
                }else{
                    bindDevicesData.postValue(Result.failure(Throwable("empty")))
                }
            }
        })
        if(result != IFlyHome.RESULT_OK){
            bindDevicesData.postValue(Result.failure(Throwable("Err")))
        }
        return bindDevicesData
    }

    /**
     * 获取当前设备的媒体状态
     * @param deviceID
     */
    fun getMediaStatus(deviceID:String):LiveData<Result<MusicStateBean>>{
        //todo 全部使用IFlyHome处理result
        val musicStateBeanData = MutableLiveData<Result<MusicStateBean>>()
        val result = IFlyHome.getMusicControlState(deviceID,object :ResponseCallback{
            override fun onFailure(call: Call<String>, t: Throwable) {
                musicStateBeanData.postValue(Result.failure(Throwable("Err")))
            }
            override fun onResponse(response: Response<String>) {
                if(response.isSuccessful){
                    try {
                        val deviceBeans = Gson().fromJson<MusicStateBean>(response.body(), object: TypeToken<MusicStateBean>(){}.type)
                        musicStateBeanData.postValue(Result.success(deviceBeans))
                    }catch (e:Throwable){
                        musicStateBeanData.postValue(Result.failure(Throwable("Err")))
                    }
                }else{
                    musicStateBeanData.postValue(Result.failure(Throwable("empty")))
                }
            }
        })
        if(result != IFlyHome.RESULT_OK){
            musicStateBeanData.postValue(Result.failure(Throwable("Err")))
        }
        return musicStateBeanData
    }


    /**
     * 初始化订阅消息binder
     * @param activity
     */
    fun initBinder(activity: MainActivity){
        service?.let {
            subscribeDeviceStatus()
        } ?: kotlin.run {
            val intent = Intent(activity, PushService::class.java)
            activity.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    /**
     * 清除binder
     */
    fun clearBinder(activity: MainActivity){
        try {
            activity.unbindService(serviceConnection)
        }catch (e :Throwable){}
        service = null
    }

    @Volatile
    private var service: PushService? = null
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            Logger.d("onServiceDisconnected")
            service?.let { service ->
                service.removeSubscribeCallback(SmartApp.subscribeCallback)
            }
            service = null
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Logger.d("onServiceConnected")
            if (service is PushService.ServiceBinder) {
                val pushService = service.getService()
                pushService.addSubscribeCallback(SmartApp.subscribeCallback)
                this@MainViewModel.service = pushService
                subscribeDeviceStatus()
            }
        }
    }

    /**
     * 订阅设备状态
     */
    private fun subscribeDeviceStatus(){
        service ?: return
        service?.let {pushService->
            val subscribeUsers = pushService.getSubscribedDeviceStateUsers()
            SmartApp.userBean?.user_id?.let {
                if(!subscribeUsers.contains(it)){
                    //开启该用户的设备状态订阅
                    pushService.subscribeUserDeviceState(it)
                }
            }
        }

    }

    /**
     * 订阅当前设备的媒体状态
     * @param deviceID
     */
    fun subscribeMediaStatus(deviceID: String){
        service?.let {
            exeSubscribeMediaState(deviceID)
        }?: kotlin.run {
            AppExecutors.get().diskIO().execute {
                val startLooperTime = System.nanoTime()
                while (null == service){
                    SystemClock.sleep(200)
                    val nowLooperTime = System.nanoTime()
                    if(TimeUnit.NANOSECONDS.toMillis(nowLooperTime - startLooperTime) >= 4000){
                        //超时退出
                        return@execute
                    }
                }
                AppExecutors.get().mainThread().execute {
                    service?.let {
                        exeSubscribeMediaState(deviceID)
                    }
                }
            }
        }
    }

    private inline fun exeSubscribeMediaState(deviceID: String){
        val devices = service!!.getSubscribedMediaStateDevices()
        devices.forEach {
            if(it != deviceID){
                service!!.unsubscribeDeviceMediaState(it)
            }
        }
        if(!devices.contains(deviceID)){
            //开启设备媒体状态订阅
            service!!.subscribeDeviceMediaState(deviceID)
        }
    }
}