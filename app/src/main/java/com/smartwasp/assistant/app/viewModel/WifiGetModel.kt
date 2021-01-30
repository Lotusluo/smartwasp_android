package com.smartwasp.assistant.app.viewModel

import android.annotation.SuppressLint
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.NetworkInfo
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.SystemClock
import androidx.lifecycle.MutableLiveData
import com.orhanobut.logger.Logger
import com.smartwasp.assistant.app.base.BaseViewModel
import com.smartwasp.assistant.app.base.SmartApp
import com.smartwasp.assistant.app.bean.WifiBean
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
            if(it.SSID.isNullOrEmpty()){
                return@forEach
            }
            //wifiManager.calculateSignalLevel todo java.lang.NoSuchMethodError: No virtual method calculateSignalLevel(I)I in class Landroid/net/wifi/WifiManager; or its super classes (declaration of 'android.net.wifi.WifiManager' appears in /system/framework/framework.jar!classes2.dex)
            val wifiBean = WifiBean(it.SSID,it.BSSID, it.capabilities,WifiManager.calculateSignalLevel(it.level,4))
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
//        The WifiManager.startScan() usage is limited to: - Each foreground app is restricted to 4 scans every 2 minutes. - All background apps combined are restricted to one scan every 30 minutes."
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
     */
    fun startWifiObserver(){
        wifiBroadcastReceiver = wifiBroadcastReceiver ?: WifiBroadcastReceiver()
        try {
            SmartApp.app.registerReceiver(wifiBroadcastReceiver, IntentFilter().apply {
                addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
                addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
                addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
            })
        }catch (e:Throwable){}
    }

    /**
     * 自动连接wifi
     * @param context
     * @param wifiBean
     * @return 当前正在连接的ssid
     */
    @SuppressLint("MissingPermission")
    fun autoConnectWifi(context: Context, wifiBean: WifiBean):String?{

        if(!linkingSSID.isNullOrEmpty()){
            //当前有正在连接的mac
            return linkingSSID
        }

        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager

        if(wifiManager.connectionInfo.bssid == wifiBean.bssid){
            //当前已连接
            Logger.e("null")
            return null
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
        linkingSSID = if(wifiManager.enableNetwork(netID, true)){
            wifiBean.bssid
        }else{
            null
        }
        return linkingSSID
    }
    //当前正在连接的网络ID
    private var linkingSSID:String? = null

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
        }catch (e:Throwable){}
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
                val wifiBeanList = mutableListOf<WifiBean>()
                wifiManager.scanResults.forEach {
                    if(it.SSID.isNullOrEmpty()){
                        return@forEach
                    }
                    if(!it.SSID.startsWith("LA_005")){
                        return@forEach
                    }
                    val wifiBean = WifiBean(it.SSID,it.BSSID, it.capabilities,WifiManager.calculateSignalLevel(it.level,4))
                    wifiBeanList.remove(wifiBean)
                    wifiBeanList.add(wifiBean)
                }
                if(wifiManager.connectionInfo.bssid == linkingSSID){
                    autoConnectData.postValue(Result.success(wifiManager.connectionInfo.bssid))
                    linkingSSID = null
                }
                wifiScanData.postValue(Result.success(wifiBeanList))
            }else if(action == WifiManager.WIFI_STATE_CHANGED_ACTION){
                intent.getParcelableExtra<NetworkInfo>(WifiManager.EXTRA_NETWORK_INFO)?.let {
                    if (NetworkInfo.State.CONNECTED == it.state){
                        if(wifiManager.connectionInfo.bssid == linkingSSID){
                            autoConnectData.postValue(Result.success(wifiManager.connectionInfo.bssid))
                            linkingSSID = null
                        }
                    }
                }
            }
        }
    }
}