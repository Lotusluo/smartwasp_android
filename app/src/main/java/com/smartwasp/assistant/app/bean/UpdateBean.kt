package com.smartwasp.assistant.app.bean

import androidx.core.content.pm.PackageInfoCompat
import com.smartwasp.assistant.app.BuildConfig
import com.smartwasp.assistant.app.base.SmartApp

/**
 * Created by luotao on 2021/2/1 11:34
 * E-Mail Address：gtkrockets@163.com
 */
data class UpdateBean(var obli:Int,
                      var describe:String,
                      var versionCode:Int,
                      var versionName:String,
                      var md5:String,
                      var downloadSite:String){

    //是否为新版本
    fun isNewVersion():Boolean{
        return SmartApp.topActivity?.packageManager?.let {
            val currentVersionCode =  PackageInfoCompat.getLongVersionCode(it.getPackageInfo(BuildConfig.APPLICATION_ID,0))
            versionCode > currentVersionCode
        } ?: kotlin.run {
            false
        }
    }
}