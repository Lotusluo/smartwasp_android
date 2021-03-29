package com.smartwasp.assistant.app.wxapi

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
import kotlinx.android.synthetic.main.activity_pay_result.*

class WXPayEntryActivity : AppCompatActivity(), IWXAPIEventHandler {


    private lateinit var api:IWXAPI

    companion object{
        const val TAG = "WXPayEntryActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val controller = ViewCompat.getWindowInsetsController(window.decorView)
        setContentView(R.layout.activity_pay_result)
        controller?.also {
            it.hide(WindowInsetsCompat.Type.statusBars())
        }?: kotlin.run {
            val decorView: View = window.decorView
            var uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            decorView.systemUiVisibility = uiOptions
        }
        api = WXAPIFactory.createWXAPI(this, ConfigUtils.APP_ID)
        api.handleIntent(intent, this)

        sheet_confirm_btn.setOnClickListener {
            //开始派发广播
            if(txt.text == getString(R.string.yes_pay)){
                sendBroadcast(Intent("DeviceSetActivity.Wx.OK"))
            }
            finish()
        }
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
            txt.text = when(resp.errCode){
                0-> {getString(R.string.yes_pay)}
                -2-> {getString(R.string.cancel_pay_err1)}
                else -> {getString(R.string.wx_pay_err)}
            }
        }
    }

    override fun onReq(req: BaseReq?) {

    }
}