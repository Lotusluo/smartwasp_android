package com.smartwasp.assistant.app.activity

import android.Manifest
import android.content.Intent
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
import com.smartwasp.assistant.app.base.addFragmentByTagWithStack
import com.smartwasp.assistant.app.bean.UpdateBean
import com.smartwasp.assistant.app.databinding.ActivityAboutBinding
import com.smartwasp.assistant.app.fragment.PreBindFragment
import com.smartwasp.assistant.app.service.DownloadService
import com.smartwasp.assistant.app.util.LoadingUtil
import com.smartwasp.assistant.app.util.NotificationsUtils
import com.smartwasp.assistant.app.viewModel.AboutModel
import kotlinx.android.synthetic.main.layout_toolbar.*
import kotlinx.coroutines.cancel

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
    private var updateBean:UpdateBean? = null
    /**
     * 添加点击事件
     */
    override fun onButtonClick(v: View){
        super.onButtonClick(v)
        when(v.id){
            R.id.btnUpdate->{
                LoadingUtil.create(this, {
                    mViewModel.clearJob()
                } ,true)
                mViewModel.update(versionCode).observe(this, Observer {
                    LoadingUtil.dismiss()
                    var resID = R.string.tip_update_check_ok_least
                    if(it.isSuccess){
                        it.getOrNull()?.data?.let {updateBean->
                            if(updateBean.versionCode > versionCode){
                                AlertDialog.Builder(this)
                                        .setTitle(getString(R.string.tip_update_check_ok_new,updateBean.versionName))
                                        .setMessage(updateBean.describe)
                                        .setNegativeButton(android.R.string.cancel,null)
                                        .setPositiveButton(R.string.update){_,_->
                                            this.updateBean = updateBean
                                            //检测权限
                                            if (!NotificationsUtils.isNotificationEnabled(this@AboutActivity)) {
                                                NotificationsUtils.toNotificationSetting(this@AboutActivity)
                                            } else {
                                                easyPermissions(getString(R.string.write_per),
                                                        REQUEST_ACCESS_FILE,
                                                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                            }
                                        }
                                        .show()
                                return@Observer
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

    /**
     * 权限回调
     */
    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        when(requestCode){
            REQUEST_ACCESS_FILE ->{
                updateBean?.let {
                    LoadingUtil.showToast(SmartApp.app,getString(R.string.update_tip))
                    DownloadService.startActionFoo(this,it.downloadSite,it.md5)
                }
            }
        }
    }

    companion object{
        const val REQUEST_ACCESS_FILE = 10119
    }
}