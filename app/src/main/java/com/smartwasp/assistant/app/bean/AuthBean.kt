package com.smartwasp.assistant.app.bean

import java.io.Serializable

/**
 * Created by luotao on 2021/2/21 15:25
 * E-Mail Address：gtkrockets@163.com
 * 用户信息Bean
 */
data class AuthBean(val interval:Int,
                    var expires_in:String,
                    var created_at:String,
                    var auth_code:String):Serializable