package com.smartwasp.assistant.app.fragment

import android.view.View
import androidx.databinding.ViewDataBinding
import com.iflytek.home.sdk.IFlyHome
import com.kyleduo.switchbutton.VoicePlayingIcon
import com.orhanobut.logger.Logger
import com.smartwasp.assistant.app.BR
import com.smartwasp.assistant.app.R
import com.smartwasp.assistant.app.activity.MainActivity
import com.smartwasp.assistant.app.base.BaseFragment
import com.smartwasp.assistant.app.base.BaseViewModel
import com.smartwasp.assistant.app.bean.DeviceBean
import com.smartwasp.assistant.app.bean.BindDevices
import com.smartwasp.assistant.app.bean.MusicStateBean
import com.smartwasp.assistant.app.bean.StatusBean
import kotlinx.android.synthetic.main.layout_device_header.*

/**
 * Created by luotao on 2021/1/15 09:42
 * E-Mail Address：gtkrockets@163.com
 */
abstract class MainChildFragment<VM: BaseViewModel,BD: ViewDataBinding>: BaseFragment<VM,BD>(){

    /**
     * 获取当前选中设备
     */
    protected val currentDevice:DeviceBean?
    get() {
        (activity as MainActivity?)?.let {
            return it.currentDevice
        }
        return null
    }

    /**
     * 获取当前账户绑定的设备
     */
    protected val bindDevices: BindDevices?
        get() {
            (activity as MainActivity?)?.let {
                return it.bindDevices
            }
            return null
        }

    /**
     * 获取当前设备媒体状态
     */
    protected val mediaState: StatusBean<MusicStateBean>?
        get() {
            (activity as MainActivity?)?.let {
                return it.mediaState
            }
            return null
        }

    /**
     * 按钮点击
     */
    override fun onButtonClick(v: View){
        when(v.id){
            R.id.device_name,R.id.media_icon,R.id.device_add,R.id.device_fresh->{
                (activity as MainActivity?)?.let {
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
        icon_device_status?.let {status->
            deviceUIChanged = true
            currentDevice?.let {
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
            mediaState?.data?.music_player?.let {
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


    override fun onStop() {
        super.onStop()
        media_icon?.stop()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        media_icon?.let {
            if(hidden) it.stop()
            else {
                notifyMediaChanged()
            }
        }
    }

}