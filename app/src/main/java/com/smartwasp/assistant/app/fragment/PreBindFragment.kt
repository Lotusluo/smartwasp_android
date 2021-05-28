package com.smartwasp.assistant.app.fragment


import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import androidx.lifecycle.Observer
import com.orhanobut.logger.Logger
import com.smartwasp.assistant.app.R
import com.smartwasp.assistant.app.activity.ApStepActivity
import com.smartwasp.assistant.app.activity.PrevBindActivity
import com.smartwasp.assistant.app.activity.ScanActivity
import com.smartwasp.assistant.app.activity.WebViewActivity
import com.smartwasp.assistant.app.base.*
import com.smartwasp.assistant.app.databinding.FragmentPreBindBinding
import com.smartwasp.assistant.app.fragment.aps.ApStepFragment3
import com.smartwasp.assistant.app.util.AppExecutors
import com.smartwasp.assistant.app.util.IFLYOS
import com.smartwasp.assistant.app.util.LoadingUtil
import com.smartwasp.assistant.app.util.isA
import com.smartwasp.assistant.app.viewModel.PreBindModel
import kotlinx.android.synthetic.main.fragment_ap3.*
import kotlinx.android.synthetic.main.fragment_pre_bind.*
import kotlinx.android.synthetic.main.fragment_pre_bind.stepBtn
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

/**
 * Created by luotao on 2021/1/28 17:30
 * E-Mail Address：gtkrockets@163.com
 */
class PreBindFragment private constructor():BaseFragment<PreBindModel,FragmentPreBindBinding>() {

    companion object{
        const val BIND_TITTLE:String = "bind_tittle"
        const val BIND_SUB_TITTLE:String = "bind_sub_tittle"
        const val BIND_SUB_TITTLE1:String = "bind_sub_tittle1"
        const val BIND_LOGO_RES:String = "bind_log_res"
        const val BIND_LOGO_URL:String = "bind_log_url"
        const val BIND_LOGO_STEP:String = "bind_log_step"
        const val BIND_LOGO_TOTAL:String = "bind_log_total"
        const val BIND_AUTH_CODE:String = "bind_client_id"
        /**
         * 静态生成类
         * @param tittle
         * @param subTittle
         * @param subTittle1
         * @param logo
         * @param type 类型
         * @param step
         * @param total
         * @param ssid
         * @param bssid
         * @param pwd
         */
        fun newsInstance(tittle:String,
                         subTittle:String? = null,
                         subTittle1:String,
                         logo:Any,
                         type:Int,
                         step:String?=null,
                         total:String?=null):PreBindFragment{
            val preBindFragment = PreBindFragment()
            preBindFragment.arguments = Bundle().apply {
                putString(BIND_TITTLE,tittle)
                putString(BIND_SUB_TITTLE,subTittle)
                putString(BIND_SUB_TITTLE1,subTittle1)
                putString(BIND_LOGO_STEP, step)
                putString(BIND_LOGO_TOTAL, total)
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
                        .error(R.drawable.ic_warning_black_24dp)
                        .into(mBinding.ivImage)
            }
            stepBtn.isEnabled = false
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                stepBtn.isEnabled = isChecked
            }
            subTittle11.setOnClickListener {
                checkBox.setChecked(!checkBox.isChecked,true)
            }
        }
    }

    //布局文件
    override val layoutResID:Int = R.layout.fragment_pre_bind
    private var qrCode:String? = ""
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
                var tempCode = data!!.getStringExtra(IFLYOS.EXTRA)
                qrCode = tempCode?.replace(Regex("^XHF"),"")
                Logger.d("qrCode:$qrCode")
                putExtra(IFLYOS.EXTRA_URL,qrCode)
                putExtra(IFLYOS.EXTRA_TYPE, IFLYOS.TYPE_BIND)
                //XHFhttps://auth.iflyos.cn/oauth/device?user_code=366652&sn=4c00141194130861e0e&mac=94B82170E022&ctei=&proId=65&t=1622103784855&clientId=8a43df58-71d2-4e63-a733-6077dc1c3c3f
            }, PrevBindActivity.REQUEST_BIND_RESULT_CODE)
            return
        }
        //请求绑定成功
        if(requestCode == PrevBindActivity.REQUEST_BIND_RESULT_CODE && resultCode == Activity.RESULT_OK){
            //请求服务器开始绑定,已提交服务器绑定成功为标准
            val uri = Uri.parse(qrCode)
            val sn = uri.getQueryParameter("sn")
            val clientId = uri.getQueryParameter("clientId")
            if(null != sn && null != clientId){
                mViewModel!!.bind(clientId,sn).observe(this, Observer {
                    if(it == IFLYOS.OK){
                        SmartApp.NEED_MAIN_REFRESH_DEVICES = true
                        requireActivity().finish()
                    }else{
                        //绑定失败
//                        LoadingUtil.showToast(SmartApp.app,"数据错误,请重试")
                        requireActivity().finish()
                    }
                })
            }else{
                //绑定失败
//                LoadingUtil.showToast(SmartApp.app,"数据错误,请重试")
                requireActivity().finish()
            }
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
                        //获取授权码
                        ApStepActivity.authBean?.let {authBean->
                            //如果有授权码,检测是否过期
                            if(authBean.isExpires()){
                                //过期重新获取
                                ApStepActivity.authBean = null
                                getAuthCode()
                            }else{
                                onNavigatorClick()
                                requireActivity()?.addFragmentByTagWithStack(R.id.container,ApStepFragment3.newsInstance())
                            }
                        } ?: kotlin.run {
                            //没有授权码直接获取
                            getAuthCode()
                        }
                    }
                }
            }
        }
    }

    /**
     * 获取授权码
     */
    private fun getAuthCode(){
        Logger.e("clientID:${ApStepActivity.clientID}")
        LoadingUtil.create(requireActivity())
        mViewModel!!.getAuthCode(ApStepActivity.clientID).observe(this, Observer {result->
            LoadingUtil.dismiss()
            if(result.isSuccess && null != result.getOrNull()){
                ApStepActivity.authBean = result.getOrNull()!!
                ApStepActivity.authBean!!.local_created_at = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())
                onNavigatorClick()
                requireActivity()?.addFragmentByTagWithStack(R.id.container, ApStepFragment3.newsInstance())
            }else{
                AlertDialog.Builder(requireContext())
                        .setTitle(R.string.tip)
                        .setMessage(R.string.error_ap_auth)
                        .setPositiveButton(android.R.string.ok,null)
                        .show()
            }
        })
    }
}