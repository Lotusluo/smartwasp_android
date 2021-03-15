package com.smartwasp.assistant.app.bean


/**
 * Created by luotao on 2021/3/12 16:12
 * E-Mail Address：gtkrockets@163.com
 */
data class PayBean<T>(val type:String,
                   val data:T?)

/**
 * 支付枚举
 */
enum class PayType(var tag:String){
    ALIPAY("alipay"),
    WXPAY("wx")
}