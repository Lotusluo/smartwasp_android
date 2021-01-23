package com.smartwasp.assistant.app.bean

import java.io.Serializable

/**
 * Created by luotao on 2021/1/20 16:23
 * E-Mail Address：gtkrockets@163.com
 */
data class SongBean(var artist:String = "",
                    var available:Boolean = false,
                    var business:String = "",
                    var can_like:Boolean = false,
                    var id:String = "",
                    var image:String = "",
                    var source_icon:String = "",
                    var liked:Boolean = false,
                    var name:String = "",
                    var source_type:String = "",
                    var source:String = "",
                    var source_description:String = "",
                    var tag_id:Int = 0,
                    var variable:Boolean = false):Serializable{

    override fun equals(other: Any?): Boolean {
        other ?: return false
        if(other !is SongBean) return false
        return other.id == id
    }
}