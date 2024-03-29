package com.smartwasp.assistant.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat


/**
 * Created by luotao on 2021/2/25 11:51
 * E-Mail Address：gtkrockets@163.com
 */
object NotificationsUtils {
    /**
     * 是否已经开启通知
     * @param context
     */
    fun isNotificationEnabled(context: Context):Boolean{
        val manager: NotificationManagerCompat = NotificationManagerCompat.from(context)
        return manager.areNotificationsEnabled()
    }

    /**
     * 跳转打开通知页面
     * @param context
     */
    fun toNotificationSetting(context: Context) {
        // vivo 点击设置图标>加速白名单>我的app
        //      点击软件管理>软件管理权限>软件>我的app>信任该软件
//        var appIntent = context.packageManager.getLaunchIntentForPackage("com.iqoo.secure")
//        if (appIntent != null) {
//            context.startActivity(appIntent)
//            return
//        }
//
//        // oppo 点击设置图标>应用权限管理>按应用程序管理>我的app>我信任该应用
//        //      点击权限隐私>自启动管理>我的app
//        appIntent = context.packageManager.getLaunchIntentForPackage("com.oppo.safe")
//        if (appIntent != null) {
//            context.startActivity(appIntent)
//            return
//        }

        try {
            context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                putExtra(Settings.EXTRA_CHANNEL_ID, context.applicationInfo.uid)
                putExtra("app_package", context.packageName)
                putExtra("app_uid", context.applicationInfo.uid)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })

        }catch (e: Exception){
            context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                val uri = Uri.fromParts("package", context.packageName, null)
                data = uri
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        }
    }


    /**
     * 判断定位服务是否开启
     * @param
     * @return true 表示开启
     */
    fun isLocationEnabled(context: Context): Boolean {
        var locationMode = 0
        val locationProviders: String
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            locationMode = try {
                Settings.Secure.getInt(context.contentResolver, Settings.Secure.LOCATION_MODE)
            } catch (e: Settings.SettingNotFoundException) {
                e.printStackTrace()
                return false
            }
            locationMode != Settings.Secure.LOCATION_MODE_OFF
        } else {
            true
        }
    }
}