package com.smartwasp.assistant.app.bean

import com.smartwasp.assistant.app.BuildConfig
import java.io.Serializable

/**
 * Created by luotao on 2021/1/12 15:36
 * E-Mail Address：gtkrockets@163.com
 */
data class BindDevices(private var user_devices:MutableList<DeviceBean>?):Serializable{

    fun getUser_devices():List<DeviceBean>?{
        if(BuildConfig.FLAVOR == "xiaodan"){
            return user_devices?.filter { de->
                de.client_id == "65e8d4f8-da9e-4633-8cac-84b0b47496b6"
            }
        }
        return user_devices
    }

}