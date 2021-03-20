package com.smartwasp.assistant.app.util

import com.smartwasp.assistant.app.bean.PayBean
import com.smartwasp.assistant.app.bean.PayType
import com.smartwasp.assistant.app.bean.UpdateBean
import com.smartwasp.assistant.app.bean.WxPayBean
import com.smartwasp.assistant.app.bean.test.BaseBean
import com.smartwasp.assistant.app.bean.test.TimeBean
import com.smartwasp.assistant.app.bean.test.TimeData
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

/**
 * Created by luotao on 2021/1/8 15:23
 * E-Mail Address：gtkrockets@163.com
 */
interface RetrofitApiService {
    /**
     * 同步时间
     */
    @GET("https://api.m.taobao.com/rest/api3.do/api=mtop.common.getTimestamp")
    fun launch(): Call<TimeBean<TimeData>>

    /**
     * 获取版本信息
     */
    @POST("api/checkUpdate")
    fun update(): Call<BaseBean<UpdateBean>>

    /**
     * 注册
     */
    @FormUrlEncoded
    @POST("api/register")
    fun register(@FieldMap map:Map<String, String>): Call<BaseBean<String>>

    /**
     * 断点下载
     * RANGE bytes=0-1000
     * @Header("Range") range:String? = null,
     */
    @Streaming
    @GET
    fun download(@Url path:String):Call<ResponseBody>

    /**
     * 微信下单接口
     */
    @FormUrlEncoded
    @POST("pay/createOrder")
    fun createWxOrder(@Field("type") type:String = PayType.WXPAY.tag):Call<BaseBean<PayBean<WxPayBean>>>

    /**
     * 支付宝下单接口
     */
    @FormUrlEncoded
    @POST("pay/createOrder")
    fun createAliOrder(@Field("type") type:String = PayType.ALIPAY.tag):Call<BaseBean<PayBean<String>>>

}