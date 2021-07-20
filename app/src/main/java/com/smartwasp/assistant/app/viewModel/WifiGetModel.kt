package com.smartwasp.assistant.app.viewModel

import android.annotation.SuppressLint
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.*
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.os.PatternMatcher
import android.os.SystemClock
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import com.orhanobut.logger.Logger
import com.smartwasp.assistant.app.activity.ApStepActivity
import com.smartwasp.assistant.app.base.BaseViewModel
import com.smartwasp.assistant.app.base.SmartApp
import com.smartwasp.assistant.app.bean.WifiBean
import com.smartwasp.assistant.app.util.AppExecutors
import com.smartwasp.assistant.app.util.NetWorkUtil
import com.smartwasp.assistant.app.util.WifiUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Created by luotao on 2021/1/29 17:42
 * E-Mail Address：gtkrockets@163.com
 */
class WifiGetModel(application: Application):BaseViewModel(application) {
    /**
     * 获取wifi列表list
     * @param context
     */
    fun getWifiList(context: Context):MutableLiveData<Result<List<WifiBean>>>{
        val wifiScanData = MutableLiveData<Result<List<WifiBean>>>()
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiBeanList = mutableListOf<WifiBean>()
        wifiManager.scanResults.forEach {
            Logger.e(it.toString())
            if(it.SSID.isNullOrEmpty()){
                return@forEach
            }
            //wifiManager.calculateSignalLevel todo java.lang.NoSuchMethodError: No virtual method calculateSignalLevel(I)I in class Landroid/net/wifi/WifiManager; or its super classes (declaration of 'android.net.wifi.WifiManager' appears in /system/framework/framework.jar!classes2.dex)
            val wifiBean = WifiBean(it.SSID, it.BSSID, it.capabilities, WifiManager.calculateSignalLevel(it.level, 4))
            wifiBeanList.add(wifiBean)
        }
        wifiScanData.postValue(Result.success(wifiBeanList))
        return wifiScanData
    }

    /**
     * 获取设备的热点
     * @param context
     */
    fun startScan(context: Context){
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
//        The WifiManager.startScan() usage is limited to: - Each foreground app is restricted to 4 scans every 2 minutes.
//        - All background apps combined are restricted to one scan every 30 minutes."
        launch(Dispatchers.IO) {
            SystemClock.sleep(1000)
            wifiManager.startScan()
        }
    }
    var wifiScanData:MutableLiveData<Result<List<WifiBean>>> = MutableLiveData()
        private set
    var autoConnectData:MutableLiveData<Result<String>> = MutableLiveData()
        private set

    /**
     * 监听wifi状态改变
     * @param context
     */
    fun startWifiObserver(context: Context){
        wifiBroadcastReceiver = wifiBroadcastReceiver ?: WifiBroadcastReceiver()
        try {
            SmartApp.app.registerReceiver(wifiBroadcastReceiver, IntentFilter().apply {
                addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
                addAction(WifiManager.ACTION_WIFI_NETWORK_SUGGESTION_POST_CONNECTION)
            })
        }catch (e: Throwable){}
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            return
        }
        //监听网络连接状态
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.requestNetwork(NetworkRequest.Builder().build(), object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                AppExecutors.get().mainThread().executeDelay(Runnable {
                    val bssid = WifiUtils.getConnectedBssid(context)
                    val gateway = NetWorkUtil.getCurrentGateway(context)
                    if (!bssid.isNullOrEmpty() && linking != null && gateway != "127.0.0.1") {
                        if (bssid == linking!!.bssid) {
                            WifiUtils.forgetBut(context, bssid)
                            autoConnectData.postValue(Result.success(bssid))
                            linking = null
                        }
                    }
                }, 1000)
            }
        })
    }

    /**
     * 自动连接wifi
     * @param context
     * @param wifiBean
     * @return 当前正在连接的wifiBean
     */
    @SuppressLint("MissingPermission")
    fun autoConnectWifi(context: Context, wifiBean: WifiBean):WifiBean?{
        if(linking != null){
            //当前有正在连接的mac
            return linking
        }
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if(wifiManager.connectionInfo.bssid == wifiBean.bssid){
            //当前已连接
            return null
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            linking = autoConnectWifiOverQ(context, wifiBean)
            return linking
        }
        //网络配置对象设置无密码连接
        val config = WifiConfiguration().apply {
            allowedAuthAlgorithms.clear()
            allowedGroupCiphers.clear()
            allowedKeyManagement.clear()
            allowedPairwiseCiphers.clear()
            allowedProtocols.clear()
            SSID = "\"" + wifiBean.ssid + "\""
            allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
        }
        //移除之前可能配置过的此wifi
        var netID = -1

        run looper@{
            wifiManager.configuredNetworks.forEach {
                if (it.SSID == "\"" + wifiBean.ssid + "\"") {
                    if(!wifiManager.removeNetwork(it.networkId)){
                        netID = it.networkId
                    }
                    return@looper
                }
            }
        }
        //添加网络ID
        if(netID == -1){
            netID = wifiManager.addNetwork(config)
        }
        //开始联网
        linking = if(wifiManager.enableNetwork(netID, true)){
            wifiBean
        }else{
            null
        }
        return linking
    }

    /**
     * androidQ及以上WIFI连接
     * @param context
     * @param wifiBean
     * @return 当前正在连接的wifiBean
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private fun autoConnectWifiOverQ(context: Context, wifiBean: WifiBean):WifiBean?{

        val specifier = WifiNetworkSpecifier.Builder().run {
            setSsidPattern(PatternMatcher(wifiBean.ssid, PatternMatcher.PATTERN_PREFIX))
            build()
        }

        val request = NetworkRequest.Builder().run {
            addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)
            addCapability(NetworkCapabilities.NET_CAPABILITY_TRUSTED)
            setNetworkSpecifier(specifier)
            build()
        }

//        val suggestionOpen = WifiNetworkSuggestion.Builder().run {
//            setSsid(wifiBean.ssid)
//            build()
//        }
//        val networkSuggestions = listOf(suggestionOpen)
//        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
//        val status = wifiManager.addNetworkSuggestions(networkSuggestions)

        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        try {
            connectivityManager.unregisterNetworkCallback(networkCallbackOverQ)
        }catch (e:IllegalArgumentException){}
        finally {
            connectivityManager.requestNetwork(request, networkCallbackOverQ)
            return wifiBean
        }
    }

//    Android Q联网监听器
    private val networkCallbackOverQ = object: ConnectivityManager.NetworkCallback(){
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            AppExecutors.get().mainThread().executeDelay(Runnable {
                val bssid = WifiUtils.getConnectedBssid(SmartApp.app)
                val gateway = NetWorkUtil.getCurrentGateway(SmartApp.app)
//                Logger.e("ssid:${WifiUtils.getConnectedSsid(SmartApp.app)},bssid:$bssid,linking:$linking,gateway:$gateway")
                if (!bssid.isNullOrEmpty() && linking != null && gateway != "127.0.0.1") {
                    if (bssid == linking!!.bssid) {
                        autoConnectData.postValue(Result.success(bssid))
                        linking = null
                        ApStepActivity.CUR_AP_NETWORK = network

                    }
                }
            }, 1000)
        }

        override fun onUnavailable() {
            super.onUnavailable()
            autoConnectData.postValue(Result.failure(Throwable("err")))
            linking = null
            Logger.e("连接失败")
        }
    }

    /**
     * 移除正在连接的ssid
     */
    @SuppressLint("MissingPermission")
    fun removeLinkingSSID(context: Context):String?{
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
            linking?.let {
                wifiManager.configuredNetworks.forEach {
                    if (it.SSID == "\"" + linking!!.ssid + "\"") {
                        wifiManager.removeNetwork(it.networkId)
                        return@let
                    }
                }
            }
        }
        linking = null
        return wifiManager.connectionInfo.bssid
    }

    //当前正在连接的网络ID
    var linking:WifiBean? = null
        private set
    //wifi监听
    private var wifiBroadcastReceiver:WifiBroadcastReceiver? = null

    /**
     * 清除
     */
    override fun onCleared() {
        super.onCleared()
        wifiBroadcastReceiver ?: return
        try {
            SmartApp.app.unregisterReceiver(wifiBroadcastReceiver)
        }catch (e: Throwable){}
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            val connectivityManager = SmartApp.app.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            try {
                connectivityManager.unregisterNetworkCallback(networkCallbackOverQ)
            }catch (e:IllegalArgumentException){}
        }
    }

    /**
     * WiFi状态监听器
     */
    inner class WifiBroadcastReceiver: BroadcastReceiver(){
        /**
         * 监听信息
         * @param context
         * @param intent
         */
        override fun onReceive(context: Context?, intent: Intent?) {
            context ?: return
            intent ?: return
            val action = intent.action
            val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            if(action == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION){
                val bbssid = WifiUtils.getConnectedBssid(context)
                val wifiBeanList = mutableListOf<WifiBean>()
                wifiManager.scanResults.forEach {
                    if(it.SSID.isNullOrEmpty()){
                        return@forEach
                    }
                    if(!it.SSID.startsWith("LA")){
                        return@forEach
                    }
                    val wifiBean = WifiBean(it.SSID, it.BSSID, it.capabilities, WifiManager.calculateSignalLevel(it.level, 4))
                    //状态赋值
                    linking?.let { l->
                        //正在连接中
                        if(l.bssid == it.BSSID){
                            wifiBean.linkType = WifiBean.STATE_LINKING
                        }
                        if(!bbssid.isNullOrEmpty() && l.bssid == bbssid){
                            linking = null
                        }
                    }
                    if(!bbssid.isNullOrEmpty() && bbssid == it.BSSID){
                        //已完成连接
                        wifiBean.linkType = WifiBean.STATE_LINKED
                    }

                    wifiBeanList.remove(wifiBean)
                    wifiBeanList.add(wifiBean)
                }
                wifiScanData.postValue(Result.success(wifiBeanList))
            }
            else if(action == WifiManager.ACTION_WIFI_NETWORK_SUGGESTION_POST_CONNECTION){
                Logger.e("WifiManager.ACTION_WIFI_NETWORK_SUGGESTION_POST_CONNECTION")
            }
        }
    }

}