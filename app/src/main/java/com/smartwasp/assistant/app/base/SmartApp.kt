package com.smartwasp.assistant.app.base

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.webkit.WebView
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import cn.jpush.android.api.JPushInterface
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.iflytek.home.sdk.IFlyHome
import com.iflytek.home.sdk.push.OsPushService
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.smartwasp.assistant.app.BuildConfig
import com.smartwasp.assistant.app.activity.MainActivity
import com.smartwasp.assistant.app.bean.*
import com.smartwasp.assistant.app.util.*
import com.tencent.bugly.crashreport.CrashReport
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.jessyan.autosize.AutoSize
import java.io.InputStream
import java.util.*
import kotlin.system.exitProcess

/**
 * Created by luotao on 2021/1/7 15:35
 * E-Mail Address：gtkrockets@163.com
 */
class SmartApp : Application() {
    companion object{
        //登录的用户数据
        var userBean:UserBean? = null
        //更新版本信息
        var updateBean:UpdateBean? = null
        //是否已经登录
        fun isLogin():Boolean{
            return null != userBean
        }

        fun finish(cmd: Int = 0){
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
                Logger.e("onMediaStateMessage:$message")
                if(!message.isNullOrEmpty()){
                    try {
                        val stateBeans = Gson().fromJson<StatusBean<MusicStateBean>>(message, object : TypeToken<StatusBean<MusicStateBean>>() {}.type)
                        stateBeans.data.device_id = deviceId
                        mediaStateObservers.forEach {
                            it.postValue(stateBeans)
                        }
                    }catch (e: Throwable){ }
                }
            }

            /**
             * 设备离在线状态改变
             * @param userId
             * @param message
             */
            override fun onDeviceStateMessage(userId: String, message: String) {
                super.onDeviceStateMessage(userId, message)
                Logger.e("onDeviceStateMessage:$message")
                if(!message.isNullOrEmpty()){
                    try {
                        val stateBeans = Gson().fromJson<StatusBean<DeviceBean>>(message, object : TypeToken<StatusBean<DeviceBean>>() {}.type)
                        devStateObservers.forEach {
                            it.postValue(stateBeans)
                        }
                    }catch (e: Throwable){

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
        lateinit var app:SmartApp
            private set
        var activity:MainActivity? = null
            private set
        private var mActivityList = Collections.synchronizedList(LinkedList<Activity>())
        var topActivity:Activity? = null
            get() {
                return if(mActivityList.size < 1) null else mActivityList[mActivityList.size - 1]
            }

    }

    /**
     * 应用入口
     */
    override fun onCreate() {
        super.onCreate()
        app = this
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            webViewSetPath(this)
        }
        app.registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                mActivityList.add(activity)
                if (activity is MainActivity) {
                    SmartApp.activity = activity
                }
            }

            override fun onActivityDestroyed(activity: Activity) {
                mActivityList.remove(activity)
                if (activity is MainActivity) {
                    SmartApp.activity = null
                }
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityResumed(activity: Activity) {}
        })
        //将app接入微信
        WXAPIFactory.createWXAPI(this, ConfigUtils.APP_ID).registerApp(ConfigUtils.APP_ID)
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
        //讯飞初始化
        IFlyHome.init(this, "28e49106-5d37-45fd-8ac8-c8d1f21356f5", IFlyHome.LoginWay.STANDARD)

//        //模拟
//        IFlyHome.setCustomToken("jK-vgRVzprcAv7s-nQ6xwbcFK-dSFEmEVDjIiW8fHbLNtd2L0nmHT0Z5Ib2Dr-O9")
//        val userBean = UserBean(false,"135****9417","7c97c06e-f4c1-44ce-b087-ecf2ac2f7b49")
//        SmartApp.userBean = userBean
        //恢复用户ID
        GlobalScope.launch(Dispatchers.IO) {
            val keyUserId = ConfigUtils.getString(ConfigUtils.KEY_USER_ID, null)
            keyUserId?.let {
                userBean = SerializableUtils.readObject(it) as UserBean?
                Logger.d("userBean:$userBean")
            }
        }

        if(!BuildConfig.DEBUG){
//        闪退捕捉
            CrashCollectHandler.instance.init(applicationContext)
            CrashReport.initCrashReport(app, "cc12a03d8c", true)
        }

        //极光推送
        JPushInterface.setDebugMode(BuildConfig.DEBUG)  // 设置开启日志,发布时请关闭日志
        // 初始化 JPush
        JPushInterface.init(this)
        userBean?.let {
            val filter = it.user_id.filterNot { c-> c == '-' }
            JPushInterface.setAlias(this, 100, filter)
        }
    }
    private fun webViewSetPath(context: Context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val processName = getProcessName(context)
            if (null != processName && applicationContext.packageName != processName) {
                WebView.setDataDirectorySuffix(processName)
            }
        }
    }
    private fun getProcessName(context: Context?): String? {
        if (context == null) return null
        val manager: ActivityManager = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (processInfo in manager.runningAppProcesses) {
            if (processInfo.pid === Process.myPid()) {
                return processInfo.processName
            }
        }
        return null
    }

}