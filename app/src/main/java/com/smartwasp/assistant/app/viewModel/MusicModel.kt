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
import com.smartwasp.assistant.app.bean.SongsBean
import retrofit2.Call
import retrofit2.Response

/**
 * Created by luotao on 2021/1/19 13:43
 * E-Mail Address：gtkrockets@163.com
 */
class MusicModel(application: Application): BaseViewModel(application) {
    /**
     * 获取歌单列表
     * @param groupID 歌单GroupID
     * @param page 需要加载第几页
     * @param limit 一页的个数
     */
    fun getSongsData(groupID:String,page:Int,limit:Int): LiveData<Result<SongsBean>> {
        //todo 全部使用IFlyHome处理result
        val songsData = MutableLiveData<Result<SongsBean>>()
        val result = IFlyHome.getSongs(groupID,page,limit,object : ResponseCallback {
            override fun onFailure(call: Call<String>, t: Throwable) {
                songsData.postValue(Result.failure(Throwable("Err")))
            }
            override fun onResponse(response: Response<String>) {
                if(response.isSuccessful){
                    try {
                        val songsBean = Gson().fromJson<SongsBean>(response.body(), object: TypeToken<SongsBean>(){}.type)
                        songsData.postValue(Result.success(songsBean))
                    }catch (e:Throwable){
                        songsData.postValue(Result.failure(Throwable("Err")))
                    }
                }else{
                    songsData.postValue(Result.failure(Throwable("empty")))
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
     * @param groupId
     */
    fun playMedia(deviceID:String,mediaId: String?,groupId:String):LiveData<Result<MusicStateBean>> {
        val musicStateBeanData = MutableLiveData<Result<MusicStateBean>>()
        val result = IFlyHome.musicControlPlayGroup(deviceID,mediaId,groupId,object : ResponseCallback {
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