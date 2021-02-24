package com.smartwasp.assistant.app.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.pm.PackageInfoCompat
import androidx.lifecycle.Observer
import com.orhanobut.logger.Logger
import com.smartwasp.assistant.app.BuildConfig
import com.smartwasp.assistant.app.R
import com.smartwasp.assistant.app.base.BaseActivity
import com.smartwasp.assistant.app.base.SmartApp
import com.smartwasp.assistant.app.databinding.ActivityAboutBinding
import com.smartwasp.assistant.app.util.LoadingUtil
import com.smartwasp.assistant.app.viewModel.AboutModel
import kotlinx.android.synthetic.main.layout_toolbar.*
import java.lang.RuntimeException

class AboutActivity : BaseActivity<AboutModel,ActivityAboutBinding>() {

    override val layoutResID: Int = R.layout.activity_about

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTittle(getString(R.string.ic_about))
        media_icon?.let {
            it.visibility = View.GONE
        }
        mBinding.version = "Version $versionName"
    }

    //版本号
    private val versionCode by lazy{
        PackageInfoCompat.getLongVersionCode(packageManager.getPackageInfo(BuildConfig.APPLICATION_ID,0))
    }
    //版本名
    private val versionName by lazy{
        packageManager.getPackageInfo(BuildConfig.APPLICATION_ID,0).versionName
    }
    /**
     * 添加点击事件
     */
    override fun onButtonClick(v: View){
        super.onButtonClick(v)
        Logger.e(SmartApp.app.externalCacheDir.toString())
        when(v.id){
            R.id.btnUpdate->{
                LoadingUtil.create(this)
                mViewModel.update(versionCode).observe(this, Observer {
                    LoadingUtil.dismiss()
                    var resID = R.string.tip_update_check_ok_least
                    if(it.isSuccess){
                        it.getOrNull()?.data?.let {updateBean->
                            if(updateBean.versionCode > versionCode){
                                resID = R.string.tip_update_check_ok_new
                            }
                        }
                    }else{
                        resID = R.string.tip_update_check_err
                    }
                    AlertDialog.Builder(this)
                            .setTitle(R.string.tip)
                            .setMessage(resID)
                            .setPositiveButton(android.R.string.ok,null)
                            .show()
                })
            }
        }
    }
}