package com.smartwasp.assistant.app.base

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.orhanobut.logger.Logger
import com.smartwasp.assistant.app.R
import com.smartwasp.assistant.app.util.RetrofitCoroutineDSL
import kotlinx.coroutines.*
import java.io.IOException
import java.lang.RuntimeException
import java.net.ConnectException
import kotlin.coroutines.CoroutineContext

/**
 * Created by luotao on 2021/1/8 10:22
 * E-Mail Address：gtkrockets@163.com
 */
abstract class BaseViewModel(application:Application): AndroidViewModel(application), CoroutineScope {

    //协程的工作
    private var allJob: Job = Job()

    var job:Job? = null

    //协程上下文环境
    override val coroutineContext: CoroutineContext
        get() =  Dispatchers.Main + allJob

    fun clearJob(){
        if(job?.isCancelled == false){
            job!!.cancel()
        }
        job = null
    }

    protected fun <T> retrofit(dsl: RetrofitCoroutineDSL<T>.() -> Unit) {
        val coroutine = RetrofitCoroutineDSL<T>().apply(dsl)
        clearJob()
        job = launch(Dispatchers.IO) {
            coroutine.api?.let { call ->
                //async 并发执行 在IO线程中
                val deferred = async {
                    try {
                        var result = call.execute() //已经在io线程中了，所以调用Retrofit的同步方法.
                        result
                    } catch (e: ConnectException) {
                        Logger.e(e.toString())
                        coroutine.onFail?.invoke(-1,e.toString())
                        null
                    } catch (e: IOException) {
                        Logger.e(e.toString())
                        coroutine.onFail?.invoke(-1,e.toString())
                        null
                    } catch (e: RuntimeException){
                        Logger.e(e.toString())
                        coroutine.onFail?.invoke(-1,e.toString())
                        null
                    }
                }
                //当协程取消的时候，取消网络请求
                deferred.invokeOnCompletion {
                    if (deferred.isCancelled) {
                        call.cancel()
                        coroutine.clean()
                        Logger.e("clear request")
                    }
                }
                //await 等待异步执行的结果
                val response = deferred.await()
                response?.let {
                    if(it.isSuccessful){
                        it.body()?.let { body ->
                            coroutine.onSuccess?.invoke(body)
                        }?: kotlin.run {
                            Logger.e(it.message())
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

    /**
     * 与Activity解绑，即将销毁
     */
    override fun onCleared() {
        super.onCleared()
        allJob?.cancel()
    }
}
