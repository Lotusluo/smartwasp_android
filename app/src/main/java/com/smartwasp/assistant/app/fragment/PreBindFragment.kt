package com.smartwasp.assistant.app.fragment


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide
import com.orhanobut.logger.Logger
import com.smartwasp.assistant.app.R
import com.smartwasp.assistant.app.activity.PrevBindActivity
import com.smartwasp.assistant.app.activity.ScanActivity
import com.smartwasp.assistant.app.activity.WebViewActivity
import com.smartwasp.assistant.app.base.BaseFragment
import com.smartwasp.assistant.app.base.BaseViewModel
import com.smartwasp.assistant.app.base.SmartApp
import com.smartwasp.assistant.app.base.addFragmentByTagWithStack
import com.smartwasp.assistant.app.databinding.FragmentPreBindBinding
import com.smartwasp.assistant.app.fragment.aps.ApStepFragment3
import com.smartwasp.assistant.app.util.IFLYOS
import com.smartwasp.assistant.app.util.isA
import kotlinx.android.synthetic.main.fragment_pre_bind.*
import java.util.*

/**
 * Created by luotao on 2021/1/28 17:30
 * E-Mail Address：gtkrockets@163.com
 */
class PreBindFragment private constructor():BaseFragment<BaseViewModel,FragmentPreBindBinding>() {

    companion object{
        const val BIND_TITTLE:String = "bind_tittle"
        const val BIND_SUB_TITTLE:String = "bind_sub_tittle"
        const val BIND_SUB_TITTLE1:String = "bind_sub_tittle1"
        const val BIND_LOGO_RES:String = "bind_log_res"
        const val BIND_LOGO_URL:String = "bind_log_url"
        const val BIND_LOGO_STEP:String = "bind_log_step"
        const val BIND_LOGO_TOTAL:String = "bind_log_total"
        const val BIND_CLIENT_ID:String = "bind_client_id"

        /**
         * 静态生成类
         * @param tittle
         * @param subTittle
         * @param subTittle1
         * @param logo
         * @param type 类型
         * @param step
         * @param total
         */
        fun newsInstance(tittle:String,
                         subTittle:String? = null,
                         subTittle1:String,
                         logo:Any,
                         type:Int,
                         clientID:String?=null,
                         step:String?=null,
                         total:String?=null):PreBindFragment{
            val preBindFragment = PreBindFragment()
            preBindFragment.arguments = Bundle().apply {
                putString(BIND_TITTLE,tittle)
                putString(BIND_SUB_TITTLE,subTittle)
                putString(BIND_SUB_TITTLE1,subTittle1)
                putString(BIND_LOGO_STEP, step)
                putString(BIND_LOGO_TOTAL, total)
                putString(BIND_CLIENT_ID, clientID)
                putInt(IFLYOS.EXTRA, type)
                if(isA<Int>(logo)){
                    putInt(BIND_LOGO_RES, logo as Int)
                }
            }
            return preBindFragment
        }
    }

    /**
     * 视图生成
     * @param view
     * @param savedInstanceState
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.run {
            mBinding.step = getString(BIND_LOGO_STEP)
            mBinding.total = getString(BIND_LOGO_TOTAL)
            mBinding.tittle = getString(BIND_TITTLE)
            mBinding.subTittle = getString(BIND_SUB_TITTLE)
            mBinding.subTittle1 = getString(BIND_SUB_TITTLE1)
            getInt(BIND_LOGO_RES)?.let {
                Glide.with(requireView())
                        .load(it)
                        .error(R.mipmap.ic_warning_black_24dp)
                        .into(mBinding.ivImage)
            }
            stepBtn.isEnabled = false
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                stepBtn.isEnabled = isChecked
            }
        }
    }

    //布局文件
    override val layoutResID:Int = R.layout.fragment_pre_bind


    /**
     * 等待绑定成功回调
     * @param requestCode 请求码
     * @param resultCode 结果码
     * @param data 数据
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //请求二维码结果
        if(requestCode == ScanActivity.REQUEST_WEB_CONFIG_CODE && resultCode == Activity.RESULT_OK && null != data){
            startActivityForResult(Intent(requireActivity(), WebViewActivity::class.java).apply {
                putExtra(IFLYOS.EXTRA_URL,data!!.getStringExtra(IFLYOS.EXTRA))
                putExtra(IFLYOS.EXTRA_TYPE, IFLYOS.TYPE_BIND)
            }, PrevBindActivity.REQUEST_BIND_RESULT_CODE)
            return
        }
        //请求绑定成功
        if(requestCode == PrevBindActivity.REQUEST_BIND_RESULT_CODE && resultCode == Activity.RESULT_OK){
            //开始设置绑定的设备
            SmartApp.NEED_MAIN_REFRESH_DEVICES = true
            requireActivity().finish()
            return
        }
    }

    /**
     * 其他按钮点击
     * @param v
     */
    override fun onButtonClick(v: View) {
        super.onButtonClick(v)
        when(v.id){
            R.id.stepBtn ->{
                arguments?.getInt(IFLYOS.EXTRA)?.let{
                    if(it == 1){
                        //摄像头权限回调成功,设置等待二维码Code
                        startActivityForResult(Intent(requireContext(),ScanActivity::class.java),ScanActivity.REQUEST_WEB_CONFIG_CODE)
                    }else if(it == 2){
                        requireActivity()?.addFragmentByTagWithStack(R.id.container,ApStepFragment3.newsInstance())
                    }
                }
            }
        }
    }
}