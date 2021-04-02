package com.smartwasp.assistant.app.viewModel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.smartwasp.assistant.app.base.BaseViewModel
import com.smartwasp.assistant.app.base.SmartApp
import com.smartwasp.assistant.app.bean.test.BaseBean
import com.smartwasp.assistant.app.util.IFLYOS
import com.smartwasp.assistant.app.util.RetrofitManager

/**
 * Created by luotao on 2021/4/2 14:49
 * E-Mail Address：gtkrockets@163.com
 */
class ReportModel(application: Application): BaseViewModel(application) {
    /**
     * 检查更新
     * @param text 意见与建议
     * @param email 邮箱
     */
    fun report(text:String,email:String):MutableLiveData<String>{
        val updateData = MutableLiveData<String>()
        retrofit<BaseBean<String>> {
            api = RetrofitManager.get().retrofitApiService?.report(SmartApp.userBean!!.user_id,text,email)
            onSuccess {
                if(it.errCode == 0){
                    updateData.postValue(IFLYOS.OK)
                }else{
                    updateData.postValue(IFLYOS.ERROR)
                }
            }
            onFail { _, _ ->
                updateData.postValue(IFLYOS.ERROR)
            }
        }
        return updateData
    }
}