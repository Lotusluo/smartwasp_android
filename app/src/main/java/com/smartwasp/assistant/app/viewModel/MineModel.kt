package com.smartwasp.assistant.app.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.alipay.sdk.app.PayTask
import com.iflytek.home.sdk.IFlyHome
import com.iflytek.home.sdk.callback.IFlyHomeLogoutCallback
import com.orhanobut.logger.Logger
import com.smartwasp.assistant.app.base.BaseViewModel
import com.smartwasp.assistant.app.base.SmartApp
import com.smartwasp.assistant.app.bean.PayBean
import com.smartwasp.assistant.app.bean.PayType
import com.smartwasp.assistant.app.bean.WxPayBean
import com.smartwasp.assistant.app.bean.test.BaseBean
import com.smartwasp.assistant.app.util.*
import com.tencent.mm.opensdk.constants.Build
import com.tencent.mm.opensdk.modelpay.PayReq
import com.tencent.mm.opensdk.openapi.WXAPIFactory


/**
 * Created by luotao on 2021/1/11 15:44
 * E-Mail Address：gtkrockets@163.com
 */
class MineModel(application: Application): BaseViewModel(application) {
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

    /**
     * 微信下单接口
     * @param context
     */
    fun createWxOrder(): LiveData<String>{
        val data = MutableLiveData<String>()
        //微信支付
        val wxapi = WXAPIFactory.createWXAPI(SmartApp.topActivity, ConfigUtils.APP_ID)
        if(wxapi.wxAppSupportAPI < Build.PAY_SUPPORTED_SDK_INT){
            data.postValue(IFLYOS.ERROR)
            return data
        }

        retrofit<BaseBean<PayBean<WxPayBean>>> {
            api = RetrofitManager.get().retrofitApiService?.createWxOrder()
            onSuccess {
//                    开始拉起微信支付
                it.data?.data?.let {pay->
                    if(wxapi.sendReq(PayReq().apply {
                                appId = pay.appId
                                partnerId = pay.partnerId
                                prepayId = pay.prepayId
                                nonceStr = pay.nonceStr
                                timeStamp = pay.timeStamp
                                packageValue = pay.packageValue
                                sign = pay.sign
                            })){
                        data.postValue(IFLYOS.OK)
                    }else{
                        data.postValue(IFLYOS.ERROR)
                    }
                } ?: kotlin.run {
                    data.postValue(IFLYOS.ERROR)
                }
            }
            onFail { _, _ ->
                data.postValue(IFLYOS.ERROR)
            }
        }
        return data
    }

    /**
     * 支付宝下单接口
     */
    fun createAliOrder(): LiveData<Result<Map<String,String>>>{
        val data = MutableLiveData<Result<Map<String,String>>>()
        //阿里
        retrofit<BaseBean<PayBean<String>>> {
            api = RetrofitManager.get().retrofitApiService?.createAliOrder()
            onSuccess {
//                    开始拉起支付宝支付
                it.data?.data?.let { pay ->
                    AppExecutors.get().netIO().execute {
                        val alipay = PayTask(SmartApp.topActivity)
                        val result = alipay.payV2(pay, true)
                        data.postValue(Result.success(result))
                    }
                } ?: kotlin.run {
                    data.postValue(Result.failure(Throwable("Err")))
                }
            }
            onFail { _, _ ->
                data.postValue(Result.failure(Throwable("Err")))
            }
        }
        return data
    }

}