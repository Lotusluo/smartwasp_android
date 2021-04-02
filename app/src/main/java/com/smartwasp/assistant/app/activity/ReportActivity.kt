package com.smartwasp.assistant.app.activity

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.orhanobut.logger.Logger
import com.smartwasp.assistant.app.R
import com.smartwasp.assistant.app.base.BaseActivity
import com.smartwasp.assistant.app.base.BaseViewModel
import com.smartwasp.assistant.app.base.SmartApp
import com.smartwasp.assistant.app.base.addFragmentByTag
import com.smartwasp.assistant.app.bean.AuthBean
import com.smartwasp.assistant.app.databinding.ActivityApStepBinding
import com.smartwasp.assistant.app.databinding.ActivityReportBindingImpl
import com.smartwasp.assistant.app.fragment.PreBindFragment
import com.smartwasp.assistant.app.fragment.aps.ApStepFragment1
import com.smartwasp.assistant.app.util.IFLYOS
import com.smartwasp.assistant.app.util.LoadingUtil
import com.smartwasp.assistant.app.util.PatternUtils
import com.smartwasp.assistant.app.viewModel.ReportModel
import kotlinx.android.synthetic.main.activity_pay_result.*
import kotlinx.android.synthetic.main.activity_report.*
import kotlinx.android.synthetic.main.layout_toolbar.*

/**
 * Created by luotao on 2021/4/2 14:10
 * E-Mail Address：gtkrockets@163.com
 */
class ReportActivity : BaseActivity<ReportModel,ActivityReportBindingImpl>() {

    override val layoutResID: Int = R.layout.activity_report

    /**
     * 生成
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTittle(getString(R.string.usrReport))
        media_icon?.let {
            it.visibility = View.GONE
        }
    }

    private var submitClick:Long = 0
    /**
     * 添加点击事件
     */
    override fun onButtonClick(v: View){
        super.onButtonClick(v)
        when(v.id){
            R.id.submitBtn->{
                if(System.currentTimeMillis() - submitClick < 3000)
                    return
                submitClick = System.currentTimeMillis()
                val text = txtReport.text.toString()
                val email = txtEmail.text.toString()
                if(!text.isNullOrEmpty() && !email.isNullOrEmpty()){
                    if(PatternUtils.checkEmail(email)){
                        mViewModel.report(text,email).observe(this, Observer {
                            if(it == IFLYOS.OK){
                                LoadingUtil.showToast(SmartApp.app,getString(R.string.usrReportOk))
                                finish()
                            }else{
                                LoadingUtil.showToast(SmartApp.app,getString(R.string.usrReportErr2))
                            }
                        })
                    }else{
                        LoadingUtil.showToast(SmartApp.app,getString(R.string.usrReportErr1))
                    }
                }else{
                    LoadingUtil.showToast(SmartApp.app,getString(R.string.usrReportErr))
                }
            }
        }
    }
}