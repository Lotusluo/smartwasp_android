package com.smartwasp.assistant.app.fragment

import android.view.View
import androidx.databinding.ViewDataBinding
import com.smartwasp.assistant.app.BR
import com.smartwasp.assistant.app.R
import com.smartwasp.assistant.app.activity.MainActivity
import com.smartwasp.assistant.app.base.BaseFragment
import com.smartwasp.assistant.app.base.BaseViewModel
import com.smartwasp.assistant.app.bean.DeviceBean
import com.smartwasp.assistant.app.bean.test.BindDevices
import kotlinx.android.synthetic.main.layout_device_header.*
import java.util.logging.Logger

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
    protected val bindDevices:BindDevices?
        get() {
            (activity as MainActivity?)?.let {
                return it.bindDevices
            }
            return null
        }

    /**
     * 更新头部设备信息设备
     * @param deviceBean 当前选中的设备
     */
    fun notifyDeviceHeader(){
        currentDevice?.let {
            mBinding.setVariable(BR.deviceBean,it)
            icon_device_status?.let {status->
                status.isSelected =it.isOnline()
            }
        }
    }

    /**
     * 按钮点击
     */
    override fun onButtonClick(v: View){
        when(v.id){
            R.id.device_name->{
                (activity as MainActivity?)?.let {
                    it.onButtonClick(v)
                }
            }
        }
    }
}