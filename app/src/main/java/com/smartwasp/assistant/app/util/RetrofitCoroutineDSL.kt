package com.smartwasp.assistant.app.util

import com.orhanobut.logger.Logger
import com.smartwasp.assistant.app.R
import kotlinx.coroutines.*
import retrofit2.Call
import java.io.IOException
import java.lang.RuntimeException
import java.net.ConnectException

class RetrofitCoroutineDSL<T> {

    var api: (Call<T>)? = null
    internal var onSuccess: ((T) -> Unit)? = null
        private set
    internal var onFail: ((errorCode: Int,msg: String?) -> Unit)? = null
        private set

    /**
     * 获取数据成功
     * @param block (T) -> Unit
     */
    fun onSuccess(block: (T) -> Unit) {
        this.onSuccess = block
    }

    /**
     * 获取数据失败
     * @param block (errorCode: Int,msg: String) -> Unit
     */
    fun onFail(block: (errorCode: Int,msg: String?) -> Unit) {
        this.onFail = block
    }

    internal fun clean() {
        onSuccess = null
        onFail = null
    }
}

fun <T> CoroutineScope.retrofit(dsl: RetrofitCoroutineDSL<T>.() -> Unit) {
    val coroutine = RetrofitCoroutineDSL<T>().apply(dsl)
    launch(Dispatchers.IO) {
        coroutine.api?.let { call ->
            //async 并发执行 在IO线程中
            val deferred = async {
                try {
                    var result = call.execute() //已经在io线程中了，所以调用Retrofit的同步方法.
                    result
                } catch (e: ConnectException) {
                    coroutine.onFail?.invoke(-1,e.toString())
                    null
                } catch (e: IOException) {
                    coroutine.onFail?.invoke(-1,e.toString())
                    null
                } catch (e:RuntimeException){
                    coroutine.onFail?.invoke(-1,e.toString())
                    null
                }
            }
            //当协程取消的时候，取消网络请求
            deferred.invokeOnCompletion {
                if (deferred.isCancelled) {
                    call.cancel()
                    coroutine.clean()
                }
            }
            //await 等待异步执行的结果
            val response = deferred.await()
            response?.let {
                if(it.isSuccessful){
                    it.body()?.let { body ->
                        coroutine.onSuccess?.invoke(body)
                    }?: kotlin.run {
                        coroutine.onFail?.invoke(it.code(),it.message())
                    }
                }else{
                    Logger.e(it.errorBody()?.string()!!)
                    coroutine.onFail?.invoke(R.string.error_empty_data,null)
                }
            }?: kotlin.run {
                coroutine.onFail?.invoke(R.string.error_empty_data,null)
            }
        }?: kotlin.run {
            coroutine.onFail?.invoke(R.string.error_empty_request,null)
        }
    }
}