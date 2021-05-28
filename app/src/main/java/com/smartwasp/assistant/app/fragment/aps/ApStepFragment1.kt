package com.smartwasp.assistant.app.fragment.aps

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import com.orhanobut.logger.Logger
import com.smartwasp.assistant.app.R
import com.smartwasp.assistant.app.activity.ApStepActivity
import com.smartwasp.assistant.app.activity.WifiListActivity
import com.smartwasp.assistant.app.base.*
import com.smartwasp.assistant.app.databinding.FragmentAp1Binding
import com.smartwasp.assistant.app.fragment.PreBindFragment
import com.smartwasp.assistant.app.util.*
import kotlinx.android.synthetic.main.fragment_ap1.*

/**
 * Created by luotao on 2021/1/28 17:30
 * E-Mail Address：gtkrockets@163.com
 */
class ApStepFragment1 private constructor():BaseFragment<BaseViewModel,FragmentAp1Binding>() {

    companion object{
        /**
         * 静态生成类
         */
        fun newsInstance():ApStepFragment1{
            return ApStepFragment1()
        }
        private const val SSID_CHOOSE_REQUEST = 10025
    }

    /**
     * 视图生成
     * @param view
     * @param savedInstanceState
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.step = "1"
        mBinding.total = "/4"
        wifiPasswd.inputType = EditorInfo.TYPE_CLASS_TEXT or EditorInfo.TYPE_TEXT_VARIATION_PASSWORD
        ApStepActivity.CUR_WIFI_SSID = WifiUtils.getConnectedSsid(context)
        ApStepActivity.CUR_WIFI_BSSID = WifiUtils.getConnectedBssid(context)
        wifiName.setText(ApStepActivity.CUR_WIFI_SSID)
        wifiPasswd.setText("")
    }


    /**
     * 等待选择的wifi成功回调
     * @param requestCode 请求码
     * @param resultCode 结果码
     * @param data 数据
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == SSID_CHOOSE_REQUEST && resultCode == Activity.RESULT_OK && null != data){
            ApStepActivity.CUR_WIFI_SSID = data.getStringExtra(IFLYOS.EXTRA)
            ApStepActivity.CUR_WIFI_BSSID = data.getStringExtra(IFLYOS.EXTRA_TAG)
            wifiName.setText(ApStepActivity.CUR_WIFI_SSID)
            wifiPasswd.setText("")
        }
    }

    //布局文件
    override val layoutResID:Int = R.layout.fragment_ap1
    /**
     * 其他按钮点击
     * @param v
     */
    override fun onButtonClick(v: View) {
        super.onButtonClick(v)
        when(v.id){
            R.id.passWdIcon ->{
                v.isSelected = !v.isSelected
                wifiPasswd.inputType = if(v.isSelected)
                    EditorInfo.TYPE_CLASS_TEXT or EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                else
                    EditorInfo.TYPE_CLASS_TEXT or EditorInfo.TYPE_TEXT_VARIATION_PASSWORD
                wifiPasswd.setSelection(wifiPasswd.text?.length!!)
            }
            R.id.wifiIcon ->{
                startActivityForResult(Intent(requireContext(),WifiListActivity::class.java),SSID_CHOOSE_REQUEST)
            }
            R.id.stepBtn ->{
                ServiceUtil.hintKbTwo(root)
                ApStepActivity.CUR_WIFI_SSID = wifiName.text.toString()
                ApStepActivity.CUR_WIFI_PWD = wifiPasswd.text.toString()
                if(!ApStepActivity.CUR_WIFI_SSID.isNullOrEmpty()
                        && !ApStepActivity.CUR_WIFI_PWD.isNullOrEmpty()
                        && ApStepActivity.CUR_WIFI_PWD?.length!! >= 8){
                    WifiUtils.forgetAll(requireContext())
//              临时测试党建的项目，后期动态获取
                requireActivity()?.addFragmentByTagWithStack(R.id.container,
                        PreBindFragment.newsInstance(
                                getString(R.string.prev_bind_tittle1),
                                getString(R.string.prev_bind_subTittle1),
                                getString(R.string.prev_bind_subTittle_a1),
                                if(ApStepActivity.clientID == "65e8d4f8-da9e-4633-8cac-84b0b47496b6") R.drawable.danjian else R.drawable.xiaodan,
                                2,
                                "2",
                                "/4"))
                }else{
                    LoadingUtil.showToast(SmartApp.app,getString(R.string.wifiInfoErr))
                }
            }
        }
    }
}