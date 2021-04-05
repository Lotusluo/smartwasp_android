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
import com.smartwasp.assistant.app.bean.MusicStateBean
import com.smartwasp.assistant.app.bean.SearchBean
import com.smartwasp.assistant.app.bean.SongsBean
import retrofit2.Call
import retrofit2.Response

/**
 * Created by luotao on 2021/4/5 15:12
 * E-Mail Address：gtkrockets@163.com
 */
class SearchModel(application: Application): BaseViewModel(application) {
    /**
     * 获取歌单列表
     * @param deviceID 设备ID
     * @param keyword 关键字
     * @param page 需要加载第几页
     * @param limit 一页的个数
     */
    fun getSongsData(deviceID: String,keyword:String,page:Int,limit:Int): LiveData<Result<SearchBean>> {
        val songsData = MutableLiveData<Result<SearchBean>>()
        val result = IFlyHome.searchMusic(deviceID,keyword,page,limit,object : ResponseCallback {
            override fun onFailure(call: Call<String>, t: Throwable) {
                songsData.postValue(Result.failure(Throwable("Err")))
            }
            override fun onResponse(response: Response<String>) {
                if(response.isSuccessful){
                    try {
                        val searchBean = Gson().fromJson<SearchBean>(response.body(), object: TypeToken<SearchBean>(){}.type)
                        songsData.postValue(Result.success(searchBean))
                    }catch (e:Throwable){
                        songsData.postValue(Result.failure(Throwable("Err")))
                    }
                }else{
                    songsData.postValue(Result.failure(Throwable("Err")))
                }
            }
        })
        if(result != IFlyHome.RESULT_OK){
            songsData.postValue(Result.failure(Throwable("Err")))
        }
        return songsData
    }

    /**
     * 播放歌曲
     * @param deviceID 设备ID
     * @param mediaId
     * @param sourceType
     */
    fun playMedia(deviceID:String,mediaId:String,sourceType:String):LiveData<Result<MusicStateBean>> {
        val musicStateBeanData = MutableLiveData<Result<MusicStateBean>>()
        val result = IFlyHome.musicControlPlay(deviceID,mediaId,sourceType,object : ResponseCallback {
            override fun onFailure(call: Call<String>, t: Throwable) {
                musicStateBeanData.postValue(Result.failure(Throwable("Err")))
            }
            override fun onResponse(response: Response<String>) {
                if(response.isSuccessful){
                    try {
                        val stateBean = Gson().fromJson<MusicStateBean>(response.body(), object: TypeToken<MusicStateBean>(){}.type)
                        stateBean.device_id = deviceID
                        musicStateBeanData.postValue(Result.success(stateBean))
                    }catch (e:Throwable){
                        musicStateBeanData.postValue(Result.failure(Throwable("Err")))
                    }
                }else{
                    Logger.e(response.errorBody()!!.string())
                    musicStateBeanData.postValue(Result.failure(Throwable("Err")))
                }
            }
        })
        if(result != IFlyHome.RESULT_OK){
            musicStateBeanData.postValue(Result.failure(Throwable("Err")))
        }
        return musicStateBeanData
    }
}