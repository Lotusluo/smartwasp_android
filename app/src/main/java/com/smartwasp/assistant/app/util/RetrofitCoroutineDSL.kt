package com.smartwasp.assistant.app.util

import com.smartwasp.assistant.app.R
import kotlinx.coroutines.*
import retrofit2.Call
import java.io.IOException
import java.net.ConnectException

class RetrofitCoroutineDSL<T> {

    var api: (Call<T>)? = null
    internal var onSuccess: ((T) -> Unit)? = null
        private set
    internal var onFail: ((errorCode: Int,msg: String?) -> Unit)? = null
        private set
    internal var onComplete: (() -> Unit)? = null
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

    /**
     * 访问完成
     * @param block () -> Unit
     */
    fun onComplete(block: () -> Unit) {
        this.onComplete = block
    }

    internal fun clean() {
        onSuccess = null
        onComplete = null
        onFail = null
    }
}

fun <T> CoroutineScope.retrofit(dsl: RetrofitCoroutineDSL<T>.() -> Unit) {
    this.launch(Dispatchers.Main) {
        val coroutine = RetrofitCoroutineDSL<T>().apply(dsl)
        coroutine.api?.let { call ->
            //async 并发执行 在IO线程中
            val deferred = async(Dispatchers.IO) {
                try {
                    call.execute() //已经在io线程中了，所以调用Retrofit的同步方法.
                } catch (e: ConnectException) {
                    coroutine.onFail?.invoke(R.string.error_net_connect,null)
                    null
                } catch (e: IOException) {
                    coroutine.onFail?.invoke(R.string.error_net_unknown,null)
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
                    coroutine.onFail?.invoke(R.string.error_empty_data,null)
                }
            }?: kotlin.run {
                coroutine.onFail?.invoke(R.string.error_empty_data,null)
            }
            coroutine.onComplete?.invoke()
        }?: kotlin.run {
            coroutine.onFail?.invoke(R.string.error_empty_request,null)
        }
    }
}