package com.smartwasp.assistant.app.util

import com.smartwasp.assistant.app.bean.test.TimeBean
import com.smartwasp.assistant.app.bean.test.TimeData
import retrofit2.Call
import retrofit2.http.GET

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
}