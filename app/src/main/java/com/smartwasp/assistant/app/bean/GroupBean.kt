package com.smartwasp.assistant.app.bean

/**
 * Created by luotao on 2021/1/18 13:45
 * E-Mail Address：gtkrockets@163.com
 */
data class GroupBean(var abbr:String,
                     var display_mode:String,
                     var has_more:Boolean,
                     var items:List<ItemBean>,
                     var name:String,
                     var section_id:String)