package com.smartwasp.assistant.app.bean

import java.io.Serializable

/**
 * Created by luotao on 2021/3/22 14:19
 * E-Mail Address：gtkrockets@163.com
 */
data class SkillDetailBean(val id:Int,
                           val skillId:Int,
                           val price:String,
                           val shopName:String):Serializable{


    /**
     * 获取价格
     */
    fun tranPrice():String{
        return "￥$price"
    }

}