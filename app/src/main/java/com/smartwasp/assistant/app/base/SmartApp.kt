package com.smartwasp.assistant.app.base

import android.app.Application
import android.os.Process
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.iflytek.home.sdk.IFlyHome
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
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
        lateinit var app:Application
            private set

        var userBean:UserBean? = null

        fun finish(cmd:Int = 0){
            Process.killProcess(Process.myPid())
            exitProcess(cmd)
        }
        //首页是否需要刷新绑定的设备
        var NEED_MAIN_REFRESH_DEVICES:Boolean = true
        //是否需要刷新绑定的设备的详细信息
        var NEED_REFRESH_DEVICES_DETAIL:Boolean = true
        //“我的页面”是否呈交互态
        var DOS_MINE_FRAGMENT_SHOWN:Boolean = false
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
            }
        }
    }
}