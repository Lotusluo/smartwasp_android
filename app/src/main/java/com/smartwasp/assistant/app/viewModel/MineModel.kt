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


}