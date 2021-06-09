package com.smartwasp.assistant.app.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.location.LocationManagerCompat
import com.orhanobut.logger.Logger
import com.smartwasp.assistant.app.R
import com.smartwasp.assistant.app.base.BaseActivity
import com.smartwasp.assistant.app.base.BaseViewModel
import com.smartwasp.assistant.app.base.SmartApp
import com.smartwasp.assistant.app.base.addFragmentByTagWithStack
import com.smartwasp.assistant.app.databinding.ActivityPrevBindBinding
import com.smartwasp.assistant.app.fragment.PreBindFragment
import com.smartwasp.assistant.app.util.*
import kotlinx.android.synthetic.main.layout_toolbar.*

class PrevBindActivity : BaseActivity<BaseViewModel, ActivityPrevBindBinding>() {

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
            R.id.scanBtn -> {
                //申请摄像头权限
                if (!RomUtils.isAndroidMOrAbove() && !RomUtils.isCameraCanUse()) {
                    //部分国产机在6.0以下做了权限处理
                    AlertDialog.Builder(this)
                            .setTitle(R.string.tip)
                            .setMessage(R.string.camera_per1)
                            .setPositiveButton(android.R.string.ok, null)
                            .show()
                } else {
                    easyPermissions(getString(R.string.camera_per), ScanActivity.REQUEST_CAMERA_CODE,
                            Manifest.permission.CAMERA)
                }
            }
            R.id.danjian, R.id.xiaodan -> {
                ApStepActivity.clientID = if (v.id == R.id.danjian) "65e8d4f8-da9e-4633-8cac-84b0b47496b6" else "cddcdf2d-f300-4616-922c-d46a9905c09f"
                NoScreenPerUtil.perCheck(this)
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
            ScanActivity.REQUEST_CAMERA_CODE -> {
                addFragmentByTagWithStack(R.id.container,
                        PreBindFragment.newsInstance(
                                getString(R.string.prev_bind_tittle),
                                getString(R.string.prev_bind_subTittle),
                                getString(R.string.prev_bind_subTittle_a),
                                R.drawable.ic_screen_box,
                                1))
            }
            REQUEST_LOCATION_CODE -> {
                if (perms.size >= 2) {
                    startActivity(Intent(this, ApStepActivity::class.java))
                }
            }
        }
    }

    companion object{
        const val REQUEST_BIND_RESULT_CODE = 10019
        const val REQUEST_LOCATION_CODE = 10020
    }
}