package com.smartwasp.assistant.app.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.orhanobut.logger.Logger
import com.smartwasp.assistant.app.R
import com.smartwasp.assistant.app.base.*
import com.smartwasp.assistant.app.databinding.ActivityPrevBindBinding
import com.smartwasp.assistant.app.fragment.PreBindFragment
import com.smartwasp.assistant.app.util.IFLYOS
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
                easyPermissions(getString(R.string.camera_per),ScanActivity.REQUEST_CAMERA_CODE,
                        Manifest.permission.CAMERA)
            }
            R.id.ApBtn ->{
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
                                R.mipmap.ic_screen_box,
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