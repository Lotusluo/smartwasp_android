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
import com.smartwasp.assistant.app.bean.BindDevices
import com.smartwasp.assistant.app.bean.FindBean
import com.smartwasp.assistant.app.bean.GroupBean
import retrofit2.Call
import retrofit2.Response

/**
 * Created by luotao on 2021/1/18 10:59
 * E-Mail Address：gtkrockets@163.com
 */
class FindModel(application: Application): BaseViewModel(application) {
    /**
     * 获取发现页数据
     */
    fun getFindData(deviceID:String): LiveData<Result<FindBean>> {
        val findData = MutableLiveData<Result<FindBean>>()
        val result = IFlyHome.getMusicGroups(deviceID,object : ResponseCallback {
            override fun onFailure(call: Call<String>, t: Throwable) {
                findData.postValue(Result.failure(Throwable("Err")))
            }
            override fun onResponse(response: Response<String>) {
                if(response.isSuccessful){
                    try {
                        val deviceBeans = Gson().fromJson<FindBean>(response.body(), object: TypeToken<FindBean>(){}.type)
                        findData.postValue(Result.success(deviceBeans))
                    }catch (e:Throwable){
                        findData.postValue(Result.failure(Throwable("Err")))
                    }
                }else{
                    findData.postValue(Result.failure(Throwable("empty")))
                }
            }
        })
        if(result != IFlyHome.RESULT_OK){
            findData.postValue(Result.failure(Throwable("Err")))
        }
        return findData
    }

    /**
     * 更多媒体资源
     * @param deviceID
     * @param selectedID
     */
    fun getMoreData(deviceID:String,selectedID:String): LiveData<Result<GroupBean>> {
        val moreDatas = MutableLiveData<Result<GroupBean>>()
        val result = IFlyHome.getGroupSection(selectedID,deviceID,object : ResponseCallback {
            override fun onFailure(call: Call<String>, t: Throwable) {
                moreDatas.postValue(Result.failure(Throwable("Err")))
            }
            override fun onResponse(response: Response<String>) {
                if(response.isSuccessful){
                    try {
                        val moreBean = Gson().fromJson<GroupBean>(response.body(), object: TypeToken<GroupBean>(){}.type)
                        moreDatas.postValue(Result.success(moreBean))
                    }catch (e:Throwable){
                        moreDatas.postValue(Result.failure(Throwable("Err")))
                    }
                }else{
                    moreDatas.postValue(Result.failure(Throwable("empty")))
                }
            }
        })
        if(result != IFlyHome.RESULT_OK){
            moreDatas.postValue(Result.failure(Throwable("Err")))
        }
        return moreDatas
    }
}