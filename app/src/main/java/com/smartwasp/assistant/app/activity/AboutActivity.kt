package com.smartwasp.assistant.app.activity

import android.os.Bundle
import android.view.View
import com.smartwasp.assistant.app.R
import com.smartwasp.assistant.app.base.BaseActivity
import com.smartwasp.assistant.app.base.BaseViewModel
import com.smartwasp.assistant.app.databinding.ActivityAboutBinding
import com.smartwasp.assistant.app.service.DownloadService
import kotlinx.android.synthetic.main.layout_toolbar.*

class AboutActivity : BaseActivity<BaseViewModel,ActivityAboutBinding>() {

    override val layoutResID: Int = R.layout.activity_about

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTittle(getString(R.string.ic_about))
        media_icon?.let {
            it.visibility = View.GONE
        }
        mBinding.version = "Version " + packageManager.getPackageInfo("com.smartwasp.assistant.app",0).versionName
    }

    /**
     * 添加点击事件
     */
    override fun onButtonClick(v: View){
        super.onButtonClick(v)
        when(v.id){
            R.id.btnUpdate->{
                DownloadService.startActionFoo(this,"http://download.sdk.mob.com/apkbus.apk")
            }
        }
    }
}