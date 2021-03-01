package com.smartwasp.assistant.app.bean

import java.io.Serializable
import java.util.concurrent.TimeUnit

/**
 * Created by luotao on 2021/2/21 15:25
 * E-Mail Address：gtkrockets@163.com
 * 用户信息Bean
 */
data class AuthBean(val interval:Int,
                    var expires_in:String = "0",
                    var created_at:String = "0",
                    var local_created_at:Long,
                    var auth_code:String):Serializable{


    /**
     * 判断是否过期
     * @return 是否过期
     */
    fun isExpires():Boolean{
        val expiresInL = expires_in.toLong() / 2
        val localCurrent = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())
        return local_created_at + expiresInL < localCurrent
    }
}