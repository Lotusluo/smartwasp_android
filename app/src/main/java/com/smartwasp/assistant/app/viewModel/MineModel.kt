package com.smartwasp.assistant.app.viewModel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.iflytek.home.sdk.IFlyHome
import com.iflytek.home.sdk.callback.IFlyHomeLogoutCallback
import com.smartwasp.assistant.app.util.IFLYOS


/**
 * Created by luotao on 2021/1/11 15:44
 * E-Mail Address：gtkrockets@163.com
 */
class MineModel(application: Application):AskDeviceModel(application) {
    /**
     * 更登出
     */
    fun loginOut(): LiveData<String> {
        val data = MutableLiveData<String>()
        val result = IFlyHome.logout(object : IFlyHomeLogoutCallback {
            override fun onLogoutSuccess() {
                data.postValue(IFLYOS.OK)
            }
            override fun onLogoutFailed(t: Throwable?) {
                data.postValue(IFLYOS.ERROR)
            }
        })
        if(result != IFlyHome.RESULT_OK){
            data.postValue(IFLYOS.ERROR)
        }
        return data
    }
}