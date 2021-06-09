package com.smartwasp.assistant.app.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.core.location.LocationManagerCompat
import com.smartwasp.assistant.app.R
import com.smartwasp.assistant.app.activity.PrevBindActivity
import com.smartwasp.assistant.app.base.BaseActivity
import com.smartwasp.assistant.app.base.BaseFragment
import com.smartwasp.assistant.app.base.SmartApp

object NoScreenPerUtil {

    fun perCheck(activity:BaseActivity<*,*>){
        if(!check(activity))
            return
        //申请
        activity.easyPermissions(activity.getString(R.string.ap_per),
                PrevBindActivity.REQUEST_LOCATION_CODE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CHANGE_WIFI_STATE)
    }

    fun perCheck(fragment:BaseFragment<*,*>){
        if(!check(fragment.requireContext()))
            return
        //申请
        fragment.easyPermissions(fragment.getString(R.string.ap_per),
                PrevBindActivity.REQUEST_LOCATION_CODE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CHANGE_WIFI_STATE)
    }

    private fun check(context:Context):Boolean{
        //android6.0/6.0.1在任何情况下android.permission.CHANGE_NETWORK_STATE都是拒绝的
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M && !Settings.System.canWrite(context.applicationContext)) {
            //打开修改系统设置权限
            val goToSettings = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            goToSettings.data = Uri.parse("package:${context.packageName}")
            context.startActivity(goToSettings)
            LoadingUtil.showToast(SmartApp.app, context.getString(R.string.need_open_setting))
            return false
        }
        //判断Wifi是否打开
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if(!wifiManager.isWifiEnabled){
            AlertDialog.Builder(context)
                    .setTitle(R.string.tip)
                    .setMessage(R.string.need_open_wifi)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
                    }
                    .show()
            return false
        }

        //判断是否打开位置信息
        //适用于原生权限判断系统,不适用Vivo
        if (!LocationManagerCompat.isLocationEnabled(context.getSystemService(Context.LOCATION_SERVICE) as LocationManager)) {
            AlertDialog.Builder(context)
                    .setTitle(R.string.tip)
                    .setMessage(R.string.need_open_gps)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    }
                    .show()
            return false
        }
        if(Rom.isVivo() && !NotificationsUtils.isLocationEnabled(context) && Build.VERSION.SDK_INT < Build.VERSION_CODES.O){
            //提示用户去设置页打开定位服务
            AlertDialog.Builder(context)
                    .setTitle(R.string.tip)
                    .setMessage(R.string.need_open_gps_vivo)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    }
                    .show()
            return false
        }
        return true
    }
}