package com.smartwasp.assistant.app.bean

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Created by luotao on 2021/1/12 10:17
 * E-Mail Address：gtkrockets@163.com
 * 内容bean
 */
data class ContentBean(@SerializedName(value = "enable",alternate = ["music_access"])
                       var enable:Boolean,
                       val redirect_url:String,
                       @SerializedName(value = "text",alternate = ["name"])
                       var text:String,
                       var value:String):Serializable