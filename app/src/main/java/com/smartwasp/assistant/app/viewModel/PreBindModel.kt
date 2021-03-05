package com.smartwasp.assistant.app.viewModel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.iflytek.home.sdk.IFlyHome
import com.iflytek.home.sdk.callback.ResponseCallback
import com.orhanobut.logger.Logger
import com.smartwasp.assistant.app.base.BaseViewModel
import com.smartwasp.assistant.app.bean.AuthBean
import com.smartwasp.assistant.app.bean.BindDevices
import com.smartwasp.assistant.app.bean.FindBean
import com.smartwasp.assistant.app.bean.UpdateBean
import com.smartwasp.assistant.app.bean.test.BaseBean
import com.smartwasp.assistant.app.util.RetrofitApiService
import com.smartwasp.assistant.app.util.RetrofitManager
import com.smartwasp.assistant.app.util.retrofit
import retrofit2.Call
import retrofit2.Response

/**
 * Created by luotao on 2021/3/5 14:53
 * E-Mail Address：gtkrockets@163.com
 */
class PreBindModel(application: Application): BaseViewModel(application) {

    /**
     * 获取授权码
     * @param clientID
     */
    fun getAuthCode(clientID:String): LiveData<Result<AuthBean>>{
        val authData = MutableLiveData<Result<AuthBean>>()
        val result = IFlyHome.getClientAuthCode(clientID, null, object : ResponseCallback {
            override fun onResponse(response: Response<String>) {
                if(response.isSuccessful){
                    val authBean = Gson().fromJson<AuthBean>(response.body(), object: TypeToken<AuthBean>(){}.type)
                    authData.postValue(Result.success(authBean))
                }else{
                    authData.postValue(Result.failure(Throwable("Err")))
                }
            }
            override fun onFailure(call: Call<String>, t: Throwable) {
                authData.postValue(Result.failure(Throwable("Err")))
            }
        })
        if(result != IFlyHome.RESULT_OK){
            authData.postValue(Result.failure(Throwable("Err")))
        }
        return authData
    }
}