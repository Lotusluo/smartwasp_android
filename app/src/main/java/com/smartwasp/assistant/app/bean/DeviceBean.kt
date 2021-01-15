package com.smartwasp.assistant.app.bean

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Created by luotao on 2021/1/12 10:15
 * E-Mail Address：gtkrockets@163.com
 * 绑定设备的bean
 */
data class DeviceBean(var alias:String = "",
                      var client_id:String = "",
                      var device_id:String = "-1",
                      var image:String = "",
                      var continous_mode:Boolean = false,
                      @SerializedName(value = "music",alternate = ["music_access"])
                      var music:ContentBean? = null,
                      var name:String = "",
                      var status:String = "",
                      var zone:String = "",
                      @Expose(serialize = false, deserialize = false)
                      var position:Int = 0):Serializable{
    /**
     * 获取设备状态
     */
    fun getStatusWords():String{
        return when (status) {
            "offline" -> {
                "离线"
            }
            "online" -> {
                "在线"
            }
            else -> {
                "未知状态"
            }
        }
    }

    //是否添加主控设备
    fun isHeader():Boolean{
        return "-1" == device_id
    }

    //是否在线
    fun isOnline():Boolean {
        return "online" == status
    }

    /**
     * 将设备信息赋值到此Bean上
     * 注意deviceInfoBean中缺少client_id与device_id
     * @param deviceInfoBean 设备信息
     */
    fun copyFromDeviceInfo(deviceInfoBean: DeviceBean){
        this.image = deviceInfoBean.image
        this.alias = deviceInfoBean.alias
        this.name = deviceInfoBean.name
        this.zone = deviceInfoBean.zone
        this.status = deviceInfoBean.status
    }

    /**
     * 判定两个对象是否相等
     * @param other 比较对象
     */
    override fun equals(other: Any?): Boolean {
        other ?: return false
        if(other !is DeviceBean) return false
        return other.device_id == other.device_id
    }
}