package com.smartwasp.assistant.app.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.smartwasp.assistant.app.R
import com.smartwasp.assistant.app.base.BaseActivity
import com.smartwasp.assistant.app.base.SmartApp
import com.smartwasp.assistant.app.databinding.ActivityPrevBindBinding
import com.smartwasp.assistant.app.util.IFLYOS
import com.smartwasp.assistant.app.viewModel.PrevBindModel

class PrevBindActivity : BaseActivity<PrevBindModel,ActivityPrevBindBinding>() {

    override val layoutResID: Int = R.layout.activity_prev_bind

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTittle(getString(R.string.add_device))
    }

    /**
     * 添加点击事件
     */
    override fun onButtonClick(v: View){
        when(v.id){
            R.id.scanBtn ->{
                //申请摄像头权限
                easyPermissions(getString(R.string.camera_per),ScanActivity.REQUEST_CAMERA_CODE, Manifest.permission.CAMERA)
            }
        }
    }

    /**
     * 等待绑定成功回调
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //请求二维码结果
        if(requestCode == ScanActivity.REQUEST_WEB_CONFIG_CODE && resultCode == Activity.RESULT_OK && null != data){
            startActivityForResult(Intent(this@PrevBindActivity,WebViewActivity::class.java).apply {
                putExtra(IFLYOS.EXTRA_URL,data!!.getStringExtra(IFLYOS.EXTRA))
                putExtra(IFLYOS.EXTRA_TYPE, IFLYOS.TYPE_BIND)
            },REQUEST_BIND_RESULT_CODE)
            return
        }
        //请求绑定成功
        if(requestCode == REQUEST_BIND_RESULT_CODE && resultCode == Activity.RESULT_OK){
            //开始设置绑定的设备
            SmartApp.NEED_MAIN_REFRESH_DEVICES = true
            finish()
            return
        }
    }

    /**
     * 权限回调
     */
    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        when(requestCode){
            ScanActivity.REQUEST_CAMERA_CODE ->{
                //摄像头权限回调成功,设置等待二维码Code
                startActivityForResult(Intent(this,ScanActivity::class.java),ScanActivity.REQUEST_WEB_CONFIG_CODE)
            }
        }
    }

    companion object{
        const val REQUEST_BIND_RESULT_CODE = 10019
    }
}