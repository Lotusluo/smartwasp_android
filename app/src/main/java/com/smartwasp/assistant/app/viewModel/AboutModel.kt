package com.smartwasp.assistant.app.viewModel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.iflytek.home.sdk.IFlyHome
import com.iflytek.home.sdk.callback.ResponseCallback
import com.orhanobut.logger.Logger
import com.smartwasp.assistant.app.BuildConfig
import com.smartwasp.assistant.app.base.BaseViewModel
import com.smartwasp.assistant.app.bean.BindDevices
import com.smartwasp.assistant.app.bean.FindBean
import com.smartwasp.assistant.app.bean.UpdateBean
import com.smartwasp.assistant.app.bean.test.BaseBean
import com.smartwasp.assistant.app.util.RetrofitApiService
import com.smartwasp.assistant.app.util.RetrofitManager
import kotlinx.coroutines.Job
import retrofit2.Call
import retrofit2.Response

/**
 * Created by luotao on 2021/2/1 11:27
 * E-Mail Address：gtkrockets@163.com
 */
class AboutModel(application: Application): BaseViewModel(application) {
    /**
     * 检查更新
     * @param versionCode 当前客户端版本
     */
    fun update():MutableLiveData<Result<BaseBean<UpdateBean>>>{
        val updateData = MutableLiveData<Result<BaseBean<UpdateBean>>>()
        retrofit<BaseBean<UpdateBean>> {
            api = if(BuildConfig.FLAVOR == "xiaodan"){
                RetrofitManager.get().retrofitApiService?.updateForXiaodan()
            } else{
                RetrofitManager.get().retrofitApiService?.update()
            }
            onSuccess {
                if(it.errCode == 0){
                    updateData.postValue(Result.success(it))
                }else{
                    updateData.postValue(Result.failure(Throwable("err")))
                }
            }
            onFail { _, _ ->
                updateData.postValue(Result.failure(Throwable("err")))
            }
        }
        return updateData
    }
}