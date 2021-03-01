package com.smartwasp.assistant.app.viewModel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.iflytek.home.sdk.IFlyHome
import com.iflytek.home.sdk.callback.ResponseCallback
import com.smartwasp.assistant.app.R
import com.smartwasp.assistant.app.base.BaseViewModel
import com.smartwasp.assistant.app.base.SmartApp
import com.smartwasp.assistant.app.bean.MusicStateBean
import com.smartwasp.assistant.app.util.LoadingUtil
import retrofit2.Call
import retrofit2.Response

//操作动作枚举类
sealed class MediaControlType
object PAUSE : MediaControlType()
object RESUME : MediaControlType()
object NEXT : MediaControlType()
object PREV : MediaControlType()
object VOLUME : MediaControlType()
/**
 * Created by luotao on 2021/1/11 15:44
 * E-Mail Address：gtkrockets@163.com
 */
class MusicBindModel(application: Application):BaseViewModel(application) {

    data class MediaControlBean(var data:MusicStateBean,var type:MediaControlType)
    //媒体观察者
    val mediaControl = MutableLiveData<Result<MediaControlBean>>()
    lateinit var deviceID:String
    private var currentType:MediaControlType? = null

    /**
     * 播放下一首
     * @param type 类型
     * @param params 参数
     */
    fun mediaControlFun(type:MediaControlType,vararg params:String) {
        if(null != currentType){
//            LoadingUtil.showToast(SmartApp.app,SmartApp.app.getString(R.string.plz_wait))
            return
        }
        currentType = type
        val result = when(currentType!!){
            is PAUSE->{
                 IFlyHome.musicControlStop(deviceID,responseCallback)
            }
            is RESUME->{
                IFlyHome.musicControlResume(deviceID,responseCallback)
            }
            is NEXT->{
                IFlyHome.musicControlNext(deviceID,responseCallback)
            }
            is PREV->{
                IFlyHome.musicControlPrevious(deviceID,responseCallback)
            }
            is VOLUME->{
                IFlyHome.musicControlSetVolume(deviceID,params[0].toInt(),responseCallback)
            }
        }
        if(result != IFlyHome.RESULT_OK){
            currentType = null
            mediaControl.postValue(Result.failure(Throwable("Err")))
        }
    }

    /**
     * 回调
     */
    private val responseCallback = object : ResponseCallback {
        override fun onFailure(call: Call<String>, t: Throwable) {
            currentType = null
            mediaControl.postValue(Result.failure(Throwable("Err")))
        }
        override fun onResponse(response: Response<String>) {
            currentType?.let {
                if(response.isSuccessful){
                    try {
                        val stateBean = Gson().fromJson<MusicStateBean>(response.body(), object: TypeToken<MusicStateBean>(){}.type)
                        stateBean.device_id = deviceID
                        mediaControl.postValue(Result.success(MediaControlBean(stateBean,it)))
                    }catch (e:Throwable){
                        mediaControl.postValue(Result.failure(Throwable("Err")))
                    }
                }else{
                    mediaControl.postValue(Result.failure(Throwable("empty")))
                }
            }
            currentType = null
        }
    }


}