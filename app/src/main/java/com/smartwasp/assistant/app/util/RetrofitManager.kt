package com.smartwasp.assistant.app.util

import com.smartwasp.assistant.app.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Created by luotao on 2021/1/8 15:14
 * E-Mail Address：gtkrockets@163.com
 */
class RetrofitManager private constructor():Interceptor{
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

    //HTTP服务
    var retrofitApiService:RetrofitApiService? = null
        private set

    init {
        initHttpRequest()
    }

    //初始化Http
    private fun initHttpRequest() {
        var retrofit = Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(NetWorkUtil.getOkHttpsSSLOkHttpClientForRetrofit(this))
                .build()
        retrofitApiService = retrofit!!.create(RetrofitApiService::class.java)
    }

    /**
     * http请求拦截
     * @param chain
     * @return 响应
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        var cache = chain.request().header("cache")
        var originalResponse = chain.proceed(chain.request())
        val cacheControl = originalResponse.header("Cache-Control")
        return cacheControl?.let {
            originalResponse = originalResponse.newBuilder()
                    .header("Cache-Control", "public, max-age=${if(cache.isNullOrBlank()) 5 else cache}")
                    .build()
            originalResponse
        } ?: kotlin.run {
            originalResponse
        }
    }
}