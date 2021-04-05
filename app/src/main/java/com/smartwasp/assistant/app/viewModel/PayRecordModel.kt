package com.smartwasp.assistant.app.viewModel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.smartwasp.assistant.app.base.BaseViewModel
import com.smartwasp.assistant.app.base.SmartApp
import com.smartwasp.assistant.app.bean.PayRecordsBean
import com.smartwasp.assistant.app.bean.SkillBean
import com.smartwasp.assistant.app.bean.test.BaseBean
import com.smartwasp.assistant.app.util.RetrofitManager

/**
 * Created by luotao on 2021/4/5 10:38
 * E-Mail Address：gtkrockets@163.com
 */
class PayRecordModel(application: Application): BaseViewModel(application) {
    /**
     * 支付记录列表获取
     * @param pageNum
     * @param pageSize
     */
    fun getPaysData(pageNum:Int,pageSize:Int): LiveData<Result<PayRecordsBean>> {
        val data = MutableLiveData<Result<PayRecordsBean>>()
        retrofit<BaseBean<PayRecordsBean>> {
            api = RetrofitManager.get().retrofitApiService?.getPayRecords(SmartApp.userBean!!.user_id,pageNum,pageSize)
            onSuccess {
                if(it.errCode == 0){
                    data.postValue(Result.success(it.data!!))
                }else{
                    data.postValue(Result.failure(Throwable(it.errCode.toString())))
                }
            }
            onFail { _, _ ->
                data.postValue(Result.failure(Throwable("err")))
            }
        }
        return data
    }
}