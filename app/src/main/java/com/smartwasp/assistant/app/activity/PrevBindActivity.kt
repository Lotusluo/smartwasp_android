package com.smartwasp.assistant.app.activity

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.smartwasp.assistant.app.R
import com.smartwasp.assistant.app.base.BaseActivity
import com.smartwasp.assistant.app.base.BaseViewModel
import com.smartwasp.assistant.app.base.SmartApp
import com.smartwasp.assistant.app.base.addFragmentByTagWithStack
import com.smartwasp.assistant.app.databinding.ActivityPrevBindBinding
import com.smartwasp.assistant.app.fragment.PreBindFragment
import com.smartwasp.assistant.app.util.RomUtils
import kotlinx.android.synthetic.main.layout_toolbar.*

class PrevBindActivity : BaseActivity<BaseViewModel,ActivityPrevBindBinding>() {

    override val layoutResID: Int = R.layout.activity_prev_bind

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTittle(getString(R.string.add_device))
        media_icon?.visibility = View.GONE
    }

    /**
     * 添加点击事件
     */
    override fun onButtonClick(v: View){
        super.onButtonClick(v)
        when(v.id){
            R.id.scanBtn ->{
                //申请摄像头权限
                if(!RomUtils.isAndroidMOrAbove() && !RomUtils.isCameraCanUse()){
                    //部分国产机在6.0以下做了权限处理
                    AlertDialog.Builder(this)
                            .setTitle(R.string.tip)
                            .setMessage(R.string.camera_per1)
                            .setPositiveButton(android.R.string.ok,null)
                            .show()
                }else{
                    easyPermissions(getString(R.string.camera_per),ScanActivity.REQUEST_CAMERA_CODE,
                            Manifest.permission.CAMERA)
                }
            }
            R.id.ApBtn ->{
                //android6.0/6.0.1在任何情况下android.permission.CHANGE_NETWORK_STATE都是拒绝的
                if(Build.VERSION.SDK_INT == Build.VERSION_CODES.M && !Settings.System.canWrite(applicationContext)){
                    //打开修改系统设置权限
                    val goToSettings = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                    goToSettings.data = Uri.parse("package:$packageName")
                    startActivity(goToSettings)
                    return
                }
                //申请
                easyPermissions(getString(R.string.ap_per),REQUEST_LOCATION_CODE,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.CHANGE_WIFI_STATE)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if(SmartApp.NEED_MAIN_REFRESH_DEVICES){
            finish()
        }
    }

    /**
     * 权限回调
     */
    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        when(requestCode){
            ScanActivity.REQUEST_CAMERA_CODE ->{
                addFragmentByTagWithStack(R.id.container,
                        PreBindFragment.newsInstance(
                                getString(R.string.prev_bind_tittle),
                                getString(R.string.prev_bind_subTittle),
                                getString(R.string.prev_bind_subTittle_a),
                                R.drawable.ic_screen_box,
                                1))
            }
            REQUEST_LOCATION_CODE ->{
                startActivity(Intent(this,ApStepActivity::class.java))
            }
        }
    }

    companion object{
        const val REQUEST_BIND_RESULT_CODE = 10019
        const val REQUEST_LOCATION_CODE = 10020
    }
}