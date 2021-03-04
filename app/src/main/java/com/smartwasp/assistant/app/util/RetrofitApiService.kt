package com.smartwasp.assistant.app.util

import com.smartwasp.assistant.app.bean.UpdateBean
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
    @POST("http://box.smartwasp.com.cn:8888/api/checkUpdate")
    fun update(): Call<BaseBean<UpdateBean>>

    /**
     * 注册
     */
    @FormUrlEncoded
    @POST("http://box.smartwasp.com.cn:8888/api/register")
    fun register(@FieldMap map:Map<String, String>): Call<BaseBean<String>>

    /**
     * 绑定设备
     */
    @FormUrlEncoded
    @POST("http://box.smartwasp.com.cn:8888/api/bind")
    fun bindDevice(@FieldMap map:Map<String, String>): Call<BaseBean<String>>

    /**
     * 解除绑定设备
     */
    @FormUrlEncoded
    @POST("http://box.smartwasp.com.cn:8888/api/unbind")
    fun unBindDevice(@FieldMap map:Map<String, String>): Call<BaseBean<String>>

    /**
     * 断点下载
     * RANGE bytes=0-1000
     * @Header("Range") range:String? = null,
     */
    @Streaming
    @GET
    fun download(@Url path:String):Call<ResponseBody>
}