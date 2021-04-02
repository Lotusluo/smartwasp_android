package com.smartwasp.assistant.app.viewModel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.iflytek.home.sdk.IFlyHome
import com.iflytek.home.sdk.callback.IFlyHomeLogoutCallback
import com.smartwasp.assistant.app.base.BaseViewModel
import com.smartwasp.assistant.app.base.SmartApp
import com.smartwasp.assistant.app.bean.test.BaseBean
import com.smartwasp.assistant.app.util.IFLYOS
import com.smartwasp.assistant.app.util.RetrofitManager

/**
 * Created by luotao on 2021/4/2 14:49
 * E-Mail Address：gtkrockets@163.com
 */
class UsrModel(application: Application): BaseViewModel(application) {
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