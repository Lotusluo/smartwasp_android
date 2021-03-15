package com.smartwasp.assistant.app.wxapi

import android.content.Intent
import android.os.Bundle
import com.orhanobut.logger.Logger
import com.smartwasp.assistant.app.R
import com.smartwasp.assistant.app.base.BaseActivity
import com.smartwasp.assistant.app.base.BaseViewModel
import com.smartwasp.assistant.app.databinding.ActivityPayResultBinding
import com.smartwasp.assistant.app.util.ConfigUtils
import com.tencent.mm.opensdk.constants.ConstantsAPI
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import com.tencent.mm.opensdk.openapi.WXAPIFactory

class WXPayEntryActivity : BaseActivity<BaseViewModel,ActivityPayResultBinding>(), IWXAPIEventHandler {

    override val layoutResID: Int = R.layout.activity_pay_result

    private lateinit var api:IWXAPI

    companion object{
        const val TAG = "WXPayEntryActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        api = WXAPIFactory.createWXAPI(this, ConfigUtils.APP_ID)
        api.handleIntent(intent, this)
    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        api.handleIntent(intent, this)
    }

    /**
     * 微信支付回调接口
     * @param resp 回调结果
     */
    override fun onResp(resp: BaseResp?) {
//     0	成功	展示成功页面
//    -1	错误	可能的原因：签名错误、未注册APPID、项目设置APPID不正确、注册的APPID与设置的不匹配、其他异常等。
//    -2	用户取消	无需处理。发生场景：用户不支付了，点击取消，返回APP。
        if(resp?.type == ConstantsAPI.COMMAND_PAY_BY_WX){
            Logger.e("微信支付结果:${resp.errCode}")
        }
    }

    override fun onReq(req: BaseReq?) {

    }
}