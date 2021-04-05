package com.smartwasp.assistant.app.bean

/**
 * Created by luotao on 2021/4/5 10:56
 * E-Mail Address：gtkrockets@163.com
 */
data class PayRecordBean(val orderId:String,
                         val proId:String,
                         val skillId:String,
                         val payType:String,
                         val createTime:String,
                         val proName:String,
                         val skillName:String,
                         val shopName:String,
                         val price:String,
                         val uid:String){

    /**
     * 获取支付类型
     */
    fun getPayTypeChinese():String{
        return if(payType == "1") "支付宝支付" else "微信支付"
    }

    /**
     * 获取支付抬头
     */
    fun getPayTittle():String{
        return "$skillName($shopName)"
    }

    /**
     * 获取订单详情描述
     */
    fun getMessage():String{
        return "项目名:$proName\n商品名:${getPayTittle()}\n支付方式:${getPayTypeChinese()}\n支付日期:$createTime\n价格:$price\n"
    }

}