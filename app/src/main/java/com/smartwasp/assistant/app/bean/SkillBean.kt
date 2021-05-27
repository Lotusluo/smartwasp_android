package com.smartwasp.assistant.app.bean

import java.io.Serializable

/**
 * Created by luotao on 2021/3/22 13:52
 * E-Mail Address：gtkrockets@163.com
 */
data class SkillBean(val skillId:Int,
                     val skillName:String,
                     val shopName:String,
                     val proId:Int,
                     val icon:String,
                     val isBuy:Boolean,
                     val expireTime:String = "0",
                     val skillDesc:String,
                     val developer:String,
                     val version:String,
                     val updateTime:String,
                     val hitTextS:List<String>):Serializable