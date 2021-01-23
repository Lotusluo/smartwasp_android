package com.smartwasp.assistant.app.bean

import java.io.Serializable

/**
 * Created by luotao on 2021/1/21 15:38
 * E-Mail Address：gtkrockets@163.com
 */
data class StatusBean<T>(var data:T,
                      var type:String = "",
                      var timestamp:String = "0"):Serializable