package com.smartwasp.assistant.app.util

import com.smartwasp.assistant.app.BuildConfig
import com.smartwasp.assistant.app.base.SmartApp
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Created by luotao on 2021/1/8 15:14
 * E-Mail Address：gtkrockets@163.com
 */
class RetrofitManager private constructor(){
    companion object{
        private var instance:RetrofitManager? = null
        fun get(): RetrofitManager{
            instance?.let {
                return it
            }?: kotlin.run {
                synchronized(this){
                    return RetrofitManager()
                }
            }
        }
    }

    //服务
    var retrofitApiService:RetrofitApiService? = null
        private set

    //retrofit
    private var retrofit: Retrofit? = null

    init {
        initHttpRequest()
    }

    //初始化Http
    private fun initHttpRequest() {
        retrofit = Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(NetWorkUtil.getOkHttpsSSLOkHttpClientForRetrofit())
                .build()

        retrofitApiService = retrofit!!.create(RetrofitApiService::class.java)
    }
}