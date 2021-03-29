package com.smartwasp.assistant.app.viewModel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.alipay.sdk.app.PayTask
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.iflytek.home.sdk.IFlyHome
import com.iflytek.home.sdk.callback.ResponseCallback
import com.orhanobut.logger.Logger
import com.smartwasp.assistant.app.base.BaseViewModel
import com.smartwasp.assistant.app.base.SmartApp
import com.smartwasp.assistant.app.bean.*
import com.smartwasp.assistant.app.bean.test.BaseBean
import com.smartwasp.assistant.app.util.AppExecutors
import com.smartwasp.assistant.app.util.ConfigUtils
import com.smartwasp.assistant.app.util.IFLYOS
import com.smartwasp.assistant.app.util.RetrofitManager
import com.tencent.mm.opensdk.constants.Build
import com.tencent.mm.opensdk.modelpay.PayReq
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import retrofit2.Call
import retrofit2.Response

/**
 * Created by luotao on 2021/1/11 15:44
 * E-Mail Address：gtkrockets@163.com
 */
class DeviceSetModel(application: Application):BaseViewModel(application) {
    /**
     * 更新设备别名
     * @param deviceId 设备标识符
     * @param alias 设备别名
     */
    fun updateAlias(deviceId:String,alias:String):LiveData<String>{
        val aliasData = MutableLiveData<String>()
        val result = IFlyHome.updateDeviceInfo(deviceId, alias, null, null, null, object:ResponseCallback{
            override fun onFailure(call: Call<String>, t: Throwable) {
                aliasData.postValue(IFLYOS.ERROR)
            }
            override fun onResponse(response: Response<String>) {
                if(response.isSuccessful){
                    aliasData.postValue(IFLYOS.OK)
                }else{
                    aliasData.postValue(IFLYOS.ERROR)
                }
            }
        })
        if(result != IFlyHome.RESULT_OK){
            aliasData.postValue(IFLYOS.ERROR)
        }
        return aliasData
    }

    /**
     * 更新设备持续交互能力
     * @param deviceId 设备标识符
     * @param isLongWake 设备别名
     */
    fun updateLongWake(deviceId:String,isLongWake:Boolean):LiveData<String>{
        val longWakeData = MutableLiveData<String>()
        val result = IFlyHome.updateDeviceInfo(deviceId, null, null, isLongWake, null, object:ResponseCallback{
            override fun onFailure(call: Call<String>, t: Throwable) {
                longWakeData.postValue(IFLYOS.ERROR)
            }
            override fun onResponse(response: Response<String>) {
                Logger.e(response.body().toString())
                if(response.isSuccessful){
                    longWakeData.postValue(IFLYOS.OK)
                }else{
                    longWakeData.postValue(IFLYOS.ERROR)
                }
            }
        })
        if(result != IFlyHome.RESULT_OK){
            longWakeData.postValue(IFLYOS.ERROR)
        }
        return longWakeData
    }

    /**
     * 解除绑定
     * @param deviceId 设备标识符
     */
    fun unBind(deviceId:String):LiveData<String>{
        val unBindData = MutableLiveData<String>()
        val result = IFlyHome.deleteUserDevice(deviceId, object:ResponseCallback{
            override fun onFailure(call: Call<String>, t: Throwable) {
                unBindData.postValue(IFLYOS.ERROR)
            }
            override fun onResponse(response: Response<String>) {
                if(response.isSuccessful){
                    unBindData.postValue(IFLYOS.OK)
                }else{
                    unBindData.postValue(IFLYOS.ERROR)
                }
            }
        })
        if(result != IFlyHome.RESULT_OK){
            unBindData.postValue(IFLYOS.ERROR)
        }
        return unBindData
    }

    /**
     * 获取一次某一个设备的信息
     * @param deviceID 设备ID
     */
    fun askDevStatus(deviceID:String):MutableLiveData<Result<DeviceBean>>{
        val devStatusData = MutableLiveData<Result<DeviceBean>>()
        val result = IFlyHome.getDeviceDetail(deviceID,object :ResponseCallback{
            override fun onFailure(call: Call<String>, t: Throwable) {
                devStatusData.postValue(Result.failure(Throwable("Err")))
            }
            override fun onResponse(response: Response<String>) {
                if(response.isSuccessful){
                    try {
                        Logger.e(response.body().toString())
                        val deviceInfoBean = Gson().fromJson<DeviceBean>(response.body(), object: TypeToken<DeviceBean>(){}.type)
                        devStatusData.postValue(Result.success(deviceInfoBean))
                    }catch (e:Throwable){
                        devStatusData.postValue(Result.failure(Throwable("Err")))
                    }
                }else{
                    devStatusData.postValue(Result.failure(Throwable("empty")))
                }
            }
        })
        if(result != IFlyHome.RESULT_OK){
            devStatusData.postValue(Result.failure(Throwable("Err")))
        }
        return devStatusData
    }

    /**
     * 获取设备能力
     * @param clientId 项目
     * @param deviceID 设备ID
     */
    fun askDevSkill(clientId:String,deviceID:String):MutableLiveData<Result<List<SkillBean>>>{
        val devSkillData = MutableLiveData<Result<List<SkillBean>>>()
        retrofit<BaseBean<List<SkillBean>>> {
            api = RetrofitManager.get().retrofitApiService?.getDeviceRes(SmartApp.userBean!!.user_id,clientId,deviceID)
            onSuccess {
                if(it.errCode == 0){
                    devSkillData.postValue(Result.success(it.data ?: listOf()))
                }else{
                    devSkillData.postValue(Result.failure(Throwable(it.errCode.toString())))
                }
            }
            onFail { _, _ ->
                devSkillData.postValue(Result.failure(Throwable("err")))
            }
        }
        return devSkillData
    }

    /**
     * 获取设备能力详情
     * @param skillId 项目
     */
    fun askDevSkillDetail(skillId:Int):MutableLiveData<Result<BaseBean<List<SkillDetailBean>>>>{
        val devSkillDetailData = MutableLiveData<Result<BaseBean<List<SkillDetailBean>>>>()
        retrofit<BaseBean<List<SkillDetailBean>>> {
            api = RetrofitManager.get().retrofitApiService?.getDeviceResDetail(skillId)
            onSuccess {
                if(it.errCode == 0){
                    devSkillDetailData.postValue(Result.success(it))
                }else{
                    devSkillDetailData.postValue(Result.failure(Throwable(it.errCode.toString())))
                }
            }
            onFail { _, _ ->
                devSkillDetailData.postValue(Result.failure(Throwable("err")))
            }
        }
        return devSkillDetailData
    }

    /**
     * 绑定
     * @param clientId 项目
     * @param deviceId 设备
     */
    fun bind(clientId:String,deviceId:String):LiveData<String>{
        val bindData = MutableLiveData<String>()
        retrofit<BaseBean<String>> {
            api = RetrofitManager.get().retrofitApiService?.bind(clientId,deviceId,SmartApp.userBean!!.user_id)
            onSuccess {
                if(it.errCode == 0){
                    bindData.postValue(IFLYOS.OK)
                }else{
                    bindData.postValue(IFLYOS.ERROR)
                }
            }
            onFail { _, _ ->
                bindData.postValue(IFLYOS.ERROR)
            }
        }
        return bindData
    }

    /**
     * 解除绑定
     * @param clientId 项目
     * @param deviceId 设备
     */
    fun unBind(clientId:String,deviceId:String):LiveData<String>{
        val bindData = MutableLiveData<String>()
        retrofit<BaseBean<String>> {
            api = RetrofitManager.get().retrofitApiService?.unbind(clientId,deviceId,SmartApp.userBean!!.user_id)
            onSuccess {
                if(it.errCode == 0){
                    bindData.postValue(IFLYOS.OK)
                }else{
                    bindData.postValue(IFLYOS.ERROR)
                }
            }
            onFail { _, _ ->
                bindData.postValue(IFLYOS.ERROR)
            }
        }
        return bindData
    }


    /**
     * 微信下单接口
     * @param skillId
     * @param shopId
     * @param clientId
     */
    fun createWxOrder(skillId:String,shopId:String,clientId:String): LiveData<String>{
        val data = MutableLiveData<String>()
        //微信支付
        val wxapi = WXAPIFactory.createWXAPI(SmartApp.topActivity, ConfigUtils.APP_ID)
        if(wxapi.wxAppSupportAPI < Build.PAY_SUPPORTED_SDK_INT){
            data.postValue(IFLYOS.ERROR)
            return data
        }

        retrofit<BaseBean<PayBean<WxPayBean>>> {
            api = RetrofitManager.get().retrofitApiService?.createWxOrder(skillId,shopId,clientId,SmartApp.userBean!!.user_id)
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
     * @param skillId
     * @param shopId
     * @param clientId
     */
    fun createAliOrder(skillId:String,shopId:String,clientId:String): LiveData<Result<Map<String,String>>>{
        val data = MutableLiveData<Result<Map<String,String>>>()
        //阿里
        retrofit<BaseBean<PayBean<String>>> {
            api = RetrofitManager.get().retrofitApiService?.createAliOrder(skillId,shopId,clientId,SmartApp.userBean!!.user_id)
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