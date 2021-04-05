package com.smartwasp.assistant.app.bean

/**
 * Created by luotao on 2021/4/5 15:23
 * E-Mail Address：gtkrockets@163.com
 */
data class SearchBean(var total:Int,
                      var page:Int,
                      var limit:Int,
                      var results:List<SongBean>)