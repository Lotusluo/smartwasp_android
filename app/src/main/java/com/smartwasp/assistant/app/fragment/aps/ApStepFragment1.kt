package com.smartwasp.assistant.app.fragment.aps

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import com.smartwasp.assistant.app.R
import com.smartwasp.assistant.app.activity.WifiListActivity
import com.smartwasp.assistant.app.base.*
import com.smartwasp.assistant.app.databinding.FragmentAp1Binding
import com.smartwasp.assistant.app.fragment.PreBindFragment
import com.smartwasp.assistant.app.util.IFLYOS
import com.smartwasp.assistant.app.util.LoadingUtil
import com.smartwasp.assistant.app.util.ServiceUtil
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
        mBinding.total = "/5"
        wifiPasswd.inputType = EditorInfo.TYPE_CLASS_TEXT or EditorInfo.TYPE_TEXT_VARIATION_PASSWORD
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
            wifiName.setText(data.getStringExtra(IFLYOS.EXTRA).toString())
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
                val wifiName = wifiName.text.toString()
                val wifiPassWd = wifiPasswd.text.toString()
                if(!wifiName.isNullOrEmpty() && !wifiPassWd.isNullOrEmpty() && wifiPassWd.length >= 8){
//              临时测试党建的项目，后期动态获取
                requireActivity()?.addFragmentByTagWithStack(R.id.container,
                        PreBindFragment.newsInstance(
                                getString(R.string.prev_bind_tittle1),
                                getString(R.string.prev_bind_subTittle1),
                                getString(R.string.prev_bind_subTittle_a1),
                                R.mipmap.ic_screen_box1,
                                2,
                                "65e8d4f8-da9e-4633-8cac-84b0b47496b6",
                                "2",
                                "/5"))
                }else{
                    LoadingUtil.showToast(requireContext(),getString(R.string.wifiInfoErr))
                }
            }
        }
    }
}