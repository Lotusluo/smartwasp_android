package com.smartwasp.assistant.app.base

import android.app.Application
import android.content.Intent
import android.os.Process
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.iflytek.home.sdk.IFlyHome
import com.iflytek.home.sdk.push.OsPushService
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.smartwasp.assistant.app.bean.DeviceBean
import com.smartwasp.assistant.app.bean.MusicStateBean
import com.smartwasp.assistant.app.bean.StatusBean
import com.smartwasp.assistant.app.bean.UserBean
import com.smartwasp.assistant.app.util.ConfigUtils
import com.smartwasp.assistant.app.util.NetWorkUtil
import com.smartwasp.assistant.app.util.SerializableUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.jessyan.autosize.AutoSize
import java.io.InputStream
import kotlin.system.exitProcess

/**
 * Created by luotao on 2021/1/7 15:35
 * E-Mail Address：gtkrockets@163.com
 */
class SmartApp : Application() {
    companion object{
        lateinit var app:SmartApp
            private set

        //登录的用户数据
        var userBean:UserBean? = null
        //是否已经登录
        fun isLogin():Boolean{
            return null != userBean
        }

        fun finish(cmd:Int = 0){
            Process.killProcess(Process.myPid())
            exitProcess(cmd)
        }

        fun restart() {
            app.packageManager.getLaunchIntentForPackage(app.packageName)?.let {
                it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                app.startActivity(it)
                finish(0)
            }
        }
        //首页是否需要刷新绑定的设备
        var NEED_MAIN_REFRESH_DEVICES:Boolean = true
        //是否需要刷新绑定的设备的详细信息
        var NEED_REFRESH_DEVICES_DETAIL:Boolean = true
        //“我的页面”是否呈交互态
        var DOS_MINE_FRAGMENT_SHOWN:Boolean = false

        /**
         * 离在线与媒体消息订阅回调
         */
        val subscribeCallback = object : OsPushService.SubscribeCallback() {

            /**
             * 音乐播放状态改变
             * @param deviceId
             * @param message
             */
            override fun onMediaStateMessage(deviceId: String, message: String) {
                super.onMediaStateMessage(deviceId, message)
                if(!message.isNullOrEmpty()){
                    try {
                        val stateBeans = Gson().fromJson<StatusBean<MusicStateBean>>(message, object: TypeToken<StatusBean<MusicStateBean>>(){}.type)
                        stateBeans.data.device_id = deviceId
                        mediaStateObservers.forEach {
                            it.postValue(stateBeans)
                        }
                    }catch (e:Throwable){
                    }
                }
            }

            /**
             * 设备离在线状态改变
             * @param userId
             * @param message
             */
            override fun onDeviceStateMessage(userId: String, message: String) {
                super.onDeviceStateMessage(userId, message)
                if(!message.isNullOrEmpty()){
                    try {
                        val stateBeans = Gson().fromJson<StatusBean<DeviceBean>>(message, object: TypeToken<StatusBean<DeviceBean>>(){}.type)
                        devStateObservers.forEach {
                            it.postValue(stateBeans)
                        }
                    }catch (e:Throwable){

                    }
                }
            }
        }
        //设备状态观察者列表
        private val devStateObservers:MutableList<MutableLiveData<StatusBean<DeviceBean>>> = mutableListOf()
        //媒体状态列表
        private val mediaStateObservers:MutableList<MutableLiveData<StatusBean<MusicStateBean>>> = mutableListOf()
        /**
         * 注册设备状态观察
         * @param liveData
         */
        fun addDevStateObserver(liveData: MutableLiveData<StatusBean<DeviceBean>>){
            if(devStateObservers.contains(liveData))
                return
            devStateObservers.add(liveData)
        }

        /**
         * 移除设备状态观察
         * @param liveData
         */
        fun removeDevObserver(liveData: MutableLiveData<StatusBean<DeviceBean>>){
            devStateObservers.remove(liveData)
        }

        /**
         * 注册媒体状态观察
         * @param liveData
         */
        fun addMediaObserver(liveData: MutableLiveData<StatusBean<MusicStateBean>>){
            if(mediaStateObservers.contains(liveData))
                return
            mediaStateObservers.add(liveData)
        }

        /**
         * 移除媒体状态观察
         * @param liveData
         */
        fun removeMediaObserver(liveData: MutableLiveData<StatusBean<MusicStateBean>>){
            mediaStateObservers.remove(liveData)
        }
    }

    /**
     * 应用入口
     */
    override fun onCreate() {
        super.onCreate()
        app = this
        //讯飞初始化
        IFlyHome.init(this, "28e49106-5d37-45fd-8ac8-c8d1f21356f5", IFlyHome.LoginWay.STANDARD)
        //设置支持的https协议
        System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2,SSLv3")
        //今日头条适配方案
        AutoSize.initCompatMultiProcess(this)
        Glide.get(app)
                .registry
                .replace(GlideUrl::class.java,
                        InputStream::class.java,
                        OkHttpUrlLoader.Factory(NetWorkUtil.getOkHttpsSSLOkHttpClient().build()))
        //Logger
        Logger.addLogAdapter(AndroidLogAdapter())
        //初始化配置
        ConfigUtils.init(app)
        //恢复用户ID
        GlobalScope.launch(Dispatchers.IO) {
            val keyUserId = ConfigUtils.getString(ConfigUtils.KEY_USER_ID,null)
            keyUserId?.let {
                userBean = SerializableUtils.readObject(it) as UserBean?
                Logger.d("userBean:$userBean")
            }
        }
    }
}