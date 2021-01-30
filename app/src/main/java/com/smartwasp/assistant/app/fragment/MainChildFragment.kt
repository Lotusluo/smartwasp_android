package com.smartwasp.assistant.app.fragment

import android.view.View
import android.view.animation.Animation
import androidx.databinding.ViewDataBinding
import com.smartwasp.assistant.app.BR
import com.smartwasp.assistant.app.R
import com.smartwasp.assistant.app.base.BaseActivity
import com.smartwasp.assistant.app.base.BaseFragment
import com.smartwasp.assistant.app.base.BaseViewModel
import com.smartwasp.assistant.app.base.SmartApp
import kotlinx.android.synthetic.main.layout_device_header.*

/**
 * Created by luotao on 2021/1/15 09:42
 * E-Mail Address：gtkrockets@163.com
 */
abstract class MainChildFragment<VM: BaseViewModel,BD: ViewDataBinding>: BaseFragment<VM,BD>(){
    /**
     * 按钮点击
     * @param v
     */
    override fun onButtonClick(v: View){
        when(v.id){
            R.id.device_name,R.id.media_icon,R.id.device_add,R.id.device_fresh->{
                (activity as BaseActivity<*,*>)?.let {
                    it.onButtonClick(v)
                }
            }
        }
    }

    /**
     * 设备变更
     * 包括选择的设备与设备离在线状态的改变
     */
    open fun notifyCurDeviceChanged(){
        deviceUIChanged = false
        if(!isResumed || isHidden)
            return
        icon_device_status?.let {status->
            deviceUIChanged = true
            SmartApp.activity?.currentDevice?.let {
                mBinding.setVariable(BR.deviceBean,it)
                status.isSelected =it.isOnline()
            }
        }
    }private var deviceUIChanged = false

    /**
     * 设备媒体状态改变
     */
    open fun notifyMediaChanged(){
        mediaUIChanged = true
        media_icon?.let {music->
            mediaUIChanged = false
            SmartApp.activity?.mediaState?.data?.music_player?.let {
                if (it.playing)music.start()
                else music.stop()
            }?: kotlin.run {
                music.stop()
            }
            childFragmentManager.fragments.forEach {
                if(it is MainChildFragment<*,*>){
                    it.notifyMediaChanged()
                }
            }
        }
    }private var mediaUIChanged = false

    /**
     * 恢复
     */
    override fun onResume() {
        super.onResume()
        if(!deviceUIChanged)
            notifyCurDeviceChanged()
        notifyMediaChanged()
    }

    /**
     * 停止
     */
    override fun onStop() {
        super.onStop()
        media_icon?.stop()
    }

    /**
     * 是否被隐藏
     * @param hidden
     */
    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        media_icon?.let {
            if(hidden) it.stop()
            else {
                if(!deviceUIChanged)
                    notifyCurDeviceChanged()
                notifyMediaChanged()
            }
        }
    }
}