package com.smartwasp.assistant.app.util

import com.smartwasp.assistant.app.bean.*
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
     * 绑定设备
     */
    @FormUrlEncoded
    @POST("api/bind")
    fun bind(@Field("clientIds") clientIds:String,
             @Field("deviceIds") deviceIds:String,
             @Field("uid") uid:String):Call<BaseBean<String>>

    /**
     * 解绑设备
     */
    @FormUrlEncoded
    @POST("api/unbind")
    fun unbind(@Field("clientId") clientIds:String,
             @Field("deviceId") deviceIds:String,
             @Field("uid") uid:String):Call<BaseBean<String>>

    /**
     * 获取设备能力
     */
    @FormUrlEncoded
    @POST("api/getSkillList")
    fun getDeviceRes(@Field("uid") uid:String,
                       @Field("clientId") clientId:String,
                       @Field("deviceId") sn:String):Call<BaseBean<List<SkillBean>>>

    /**
     * 获取设备能力详情
     */
    @FormUrlEncoded
    @POST("api/getShopInfo")
    fun getDeviceResDetail(@Field("skillId") skillId:Int):Call<BaseBean<List<SkillDetailBean>>>

    /**
     * 微信下单接口
     */
    @FormUrlEncoded
    @POST("https://dms.smartwasp.com.cn:8086/pay/createOrder")
    fun createWxOrder(@Field("skillId") skillId: String,
                      @Field("shopId") shopId: String,
                      @Field("clientId") clientId: String,
                      @Field("uid") uid: String,
                      @Field("type") type:String = PayType.WXPAY.tag):Call<BaseBean<PayBean<WxPayBean>>>

    /**
     * 支付宝下单接口
     */
    @FormUrlEncoded
    @POST("https://dms.smartwasp.com.cn:8086/pay/createOrder")
    fun createAliOrder(@Field("skillId") skillId: String,
                       @Field("shopId") shopId: String,
                       @Field("clientId") clientId: String,
                       @Field("uid") uid: String,
                       @Field("type") type:String = PayType.ALIPAY.tag):Call<BaseBean<PayBean<String>>>

}