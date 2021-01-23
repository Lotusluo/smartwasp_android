package com.smartwasp.assistant.app.bean

/**
 * Created by luotao on 2021/1/20 16:21
 * E-Mail Address：gtkrockets@163.com
 */
data class SongsBean(var from:String,
                     var from_type:String,
                     var image:String,
                     var name:String,
                     var items:List<SongBean>)