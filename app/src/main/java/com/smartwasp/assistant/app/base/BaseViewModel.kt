package com.smartwasp.assistant.app.base

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

/**
 * Created by luotao on 2021/1/8 10:22
 * E-Mail Address：gtkrockets@163.com
 */
open class BaseViewModel(application:Application): AndroidViewModel(application), CoroutineScope {

    //协程的工作
    private var job: Job = Job()

    //协程上下文环境
    override val coroutineContext: CoroutineContext
        get() =  Dispatchers.Main + job

    /**
     * 与Activity解绑，即将销毁
     */
    override fun onCleared() {
        super.onCleared()
        job?.cancel()
    }
}
