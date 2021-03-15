package com.smartwasp.assistant.app.bean

/**
 * Created by luotao on 2021/3/11 17:11
 * E-Mail Address：gtkrockets@163.com
 */
data class WxPayBean(val sign:String,
                     val prepayId:String,
                     val partnerId:String,
                     val appId:String,
                     val packageValue:String,
                     val timeStamp:String,
                     val nonceStr:String) {
}