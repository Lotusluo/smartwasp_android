package com.smartwasp.assistant.app.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import com.iflytek.home.sdk.IFlyHome
import com.smartwasp.assistant.app.BR
import com.smartwasp.assistant.app.R
import com.smartwasp.assistant.app.base.BaseActivity
import com.smartwasp.assistant.app.base.SmartApp
import com.smartwasp.assistant.app.bean.DeviceBean
import com.smartwasp.assistant.app.databinding.ActivityDeviceSetBinding
import com.smartwasp.assistant.app.util.IFLYOS
import com.smartwasp.assistant.app.util.LoadingUtil
import com.smartwasp.assistant.app.viewModel.DeviceSetModel
import kotlinx.android.synthetic.main.activity_device_set.*

/**
 * Created by luotao on 2021/1/13 18:00
 * E-Mail Address：gtkrockets@163.com
 * 设备设置页
 */
class DeviceSetActivity : BaseActivity<DeviceSetModel,ActivityDeviceSetBinding>() {

    override val layoutResID: Int = R.layout.activity_device_set
    private var deviceBean:DeviceBean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //监听软键盘确定按钮
        nameEdit.setOnEditorActionListener { _, actionId, _ ->
            if(actionId == EditorInfo.IME_ACTION_DONE){
                onUpdateAlias(nameEdit.text.toString())
            }
            false
        }
        sb_ios.setOnCheckedChangeListener { _, isChecked ->
            onUpdateLongWake(isChecked)
        }
    }

    /**
     * 页面交互态更新信息
     */
    override fun onResume() {
        super.onResume()
        refresh()
    }

    /**
     * 是否刷新数据
     */
    private fun refresh(){
        if(SmartApp.NEED_REFRESH_DEVICES_DETAIL){
            intent.getStringExtra(IFLYOS.DEVICE_ID)?.let {
                LoadingUtil.create(this,null)
                mViewModel.askDevStatus(it).observe(this, Observer {result->
                    SmartApp.NEED_REFRESH_DEVICES_DETAIL = false
                    LoadingUtil.dismiss()
                    if(result.isSuccess){
                        deviceBean = result.getOrNull()?.apply {
                            device_id = it
                            setTittle(name)
                            sb_ios.setCheckedImmediatelyNoEvent(continous_mode)
                            mBinding.setVariable(BR.deviceBean,this)
                        }
                    }else{
                        LoadingUtil.showToast(this,getString(R.string.try_again))
                        deviceBean ?: kotlin.run { finish() }
                    }
                })
            }
        }
    }

    /**
     * 提交是否持续交互
     * @param isLongWake  是否持久交互
     */
    private fun onUpdateLongWake(isLongWake: Boolean) {
        LoadingUtil.create(this,null)
        intent.getStringExtra(IFLYOS.DEVICE_ID)?.let {
            LoadingUtil.create(this,null)
            mViewModel.updateLongWake(it,isLongWake).observe(this, Observer {result->
                LoadingUtil.dismiss()
                when(result){
                    IFLYOS.OK->{
                        SmartApp.NEED_REFRESH_DEVICES_DETAIL = true
                        refresh()
                    }
                    IFLYOS.ERROR->{
                        LoadingUtil.showToast(this,getString(R.string.try_again))
                    }
                }
            })
        }
    }

    /**
     * 设置设备名称
     * @param alias 设备名称
     */
    private fun onUpdateAlias(alias: String) {
        LoadingUtil.create(this,null)
        intent.getStringExtra(IFLYOS.DEVICE_ID)?.let {
            LoadingUtil.create(this,null)
            mViewModel.updateAlias(it,alias).observe(this, Observer {result->
                LoadingUtil.dismiss()
                when(result){
                    IFLYOS.OK->{
                        SmartApp.NEED_REFRESH_DEVICES_DETAIL = true
                        SmartApp.NEED_MAIN_REFRESH_DEVICES = true
                        refresh()
                    }
                    IFLYOS.ERROR->{
                        LoadingUtil.showToast(this,getString(R.string.try_again))
                    }
                }
            })
        }
    }

    /**
     * 其他按钮点击
     */
    override fun onButtonClick(v: View) {
        when(v.id){
            R.id.positionBtn ->{
                //修改设备位置
                deviceBean?.let {
                    SmartApp.NEED_REFRESH_DEVICES_DETAIL = true
                    SmartApp.NEED_MAIN_REFRESH_DEVICES = true
                    startActivity(Intent(this@DeviceSetActivity,WebViewActivity::class.java).apply {
                        putExtra(IFLYOS.EXTRA_PAGE, IFlyHome.DEVICE_ZONE)
                        putExtra(IFLYOS.EXTRA_TYPE,IFLYOS.TYPE_PAGE)
                        putExtra(IFLYOS.DEVICE_ID,it.device_id)
                    })
                }
            }
            R.id.musicBtn ->{
                //进入音乐链接
                deviceBean?.let {
                    startActivity(Intent(this@DeviceSetActivity,WebViewActivity::class.java).apply {
                        putExtra(IFLYOS.EXTRA_URL, it.music?.redirect_url)
                        putExtra(IFLYOS.EXTRA_TYPE,IFLYOS.TYPE_PAGE)
                    })
                }
            }
            R.id.unBindBtn->{
                //解除绑定
                AlertDialog.Builder(this)
                        .setTitle(R.string.tip)
                        .setMessage(R.string.unBindTip)
                        .setNegativeButton(android.R.string.cancel,null)
                        .setPositiveButton(android.R.string.ok){_,_->
                            LoadingUtil.create(this,null)
                            intent.getStringExtra(IFLYOS.DEVICE_ID)?.let {
                                LoadingUtil.create(this,null)
                                mViewModel.unBind(it).observe(this, Observer {result->
                                    LoadingUtil.dismiss()
                                    when(result){
                                        IFLYOS.OK->{
                                            SmartApp.NEED_MAIN_REFRESH_DEVICES = true
                                            finish()
                                        }
                                        IFLYOS.ERROR->{
                                            LoadingUtil.showToast(this,getString(R.string.try_again))
                                        }
                                    }
                                })
                            }

                        }
                        .show()
            }
        }
    }

    /**
     * 销毁标记下
     */
    override fun onDestroy() {
        super.onDestroy()
        SmartApp.NEED_REFRESH_DEVICES_DETAIL = true
    }
}