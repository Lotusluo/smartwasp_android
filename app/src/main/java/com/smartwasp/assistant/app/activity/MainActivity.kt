package com.smartwasp.assistant.app.activity

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatButton
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.children
import androidx.lifecycle.Observer
import com.orhanobut.logger.Logger
import com.smartwasp.assistant.app.R
import com.smartwasp.assistant.app.base.*
import com.smartwasp.assistant.app.bean.DeviceBean
import com.smartwasp.assistant.app.bean.test.BindDevices
import com.smartwasp.assistant.app.databinding.ActivityMainBinding
import com.smartwasp.assistant.app.fragment.DeviceChooserDialog
import com.smartwasp.assistant.app.fragment.DialogFragment
import com.smartwasp.assistant.app.fragment.MainChildFragment
import com.smartwasp.assistant.app.fragment.MineFragment
import com.smartwasp.assistant.app.util.IFLYOS
import com.smartwasp.assistant.app.util.LoadingUtil
import com.smartwasp.assistant.app.viewModel.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_tabbar.*

/**
 * Created by luotao on 2021/1/7 15:00
 * E-Mail Address：gtkrockets@163.com
 * APP主界面
 */
class MainActivity : BaseActivity<MainViewModel , ActivityMainBinding>() {

    //布局文件
    override val layoutResID: Int = R.layout.activity_main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTabIconStyle()
        //检测ifyos是否登录
        iflyosLoginCheck()
    }

    /**
     * 设置tab样式
     */
    private fun setTabIconStyle(){
        (tabbar as LinearLayout).children.forEach {
            val tabButton = it as AppCompatButton
            tabButton.compoundDrawables[1]?.let {drawable->
                DrawableCompat.setTintList(drawable,
                        resources.getColorStateList(R.color.tab_text_color))
            }
        }
    }

    /**
     * 检测是否登录
     */
    private fun iflyosLoginCheck() {
        //如果没有登录讯飞账号体系，弹出登录页面
        SmartApp.userBean?.let {
            root.visibility = View.VISIBLE
            onButtonClick(tab_dialog)
        }?: kotlin.run {
            mViewModel.iflyoslogin().observe(this, Observer{
                when {
                    it.startsWith(IFLYOS.OK) -> {
                        //登录成功，获取user_id
                        LoadingUtil.create(this,null)
                        mViewModel.getUserBean().observe(this, Observer{msg->
                            LoadingUtil.dismiss()
                            when(msg){
                                IFLYOS.OK->{
                                    root.visibility = View.VISIBLE
                                    onButtonClick(tab_dialog)
                                }
                                IFLYOS.ERROR->{
                                    //todo 登录失败
                                }
                            }
                        })
                    }
                    it.startsWith(IFLYOS.ERROR) -> {
                        //todo 登录失败
                        Logger.d("login error!")
                    }
                    it.startsWith(IFLYOS.EXTRA_TAG) -> {
                        //需要跳转讯飞交互页
                        val tag = it.substring(IFLYOS.EXTRA.length + 1)
                        startActivity(Intent(this@MainActivity,WebViewActivity::class.java).apply {
                            putExtra(IFLYOS.EXTRA_TAG,tag)
                            putExtra(IFLYOS.EXTRA_TYPE,IFLYOS.TYPE_LOGIN)
                        })
                    }
                }
            })
        }
    }

    /**
     * tabBar点击事件
     * @param tabID tab按钮索引
     * @param tabResID tab按钮ID
     */
    private fun onAttachTabClick(tabID:Int,tabResID:Int) {
        if(currentTabID == tabID)
            return
        (tabbar as LinearLayout).children.forEach { it.isSelected = false}
        if(currentTabID > 0){
            fragments[currentTabID - 1]?.let {
                hideFragment(it)
            }
        }
        currentTabID = tabID
        val index = currentTabID - 1
        val fragment:MainChildFragment<*,*>? = when(tabID){
            5 ->{
                fragments[index] ?: kotlin.run {
                    fragments[index] = MineFragment.newsInstance()
                    fragments[index]
                }
            }
            1->{
                fragments[index] ?: kotlin.run {
                    fragments[index] = DialogFragment.newsInstance()
                    fragments[index]
                }
            }
            else ->{null}
        }
        fragment?.let {
            addFragmentByTag(R.id.container,it)
            showFragment(it)
            findViewById<View>(tabResID).isSelected = true
        }
    }

    /**
     * 除了上部toobar按钮，此contentview其他按钮点击均会回调此函数
     * @param v 点击的视图
     */
    override fun onButtonClick(v: View) {
        val tagInt:Int? = v.tag?.toString()?.toInt()
        if(tagInt in 1..5){
            onAttachTabClick(tagInt!!,v.id)
            return
        }
        val tagDevice:DeviceBean? = v.getTag(R.id.extra_device) as? DeviceBean
        tagDevice?.let {
            if(currentDevice != it)
                currentDevice = it
            return
        }
        when(v.id){
            R.id.device_name->{
                bindDevices?.let {
                    DeviceChooserDialog.newsInstance(it).show(supportFragmentManager,null)
                }?: kotlin.run {
                    //todo 暂不确定是否会执行到此处
                }
            }
            R.id.device_add,R.id.sheet_device_add->{
                //跳转扫码主控设备
                startActivity(Intent(this,PrevBindActivity::class.java))
            }
        }
    }

    //当前的fragment
    private var currentTabID:Int = 0
    //当fragments
    private var fragments = MutableList<MainChildFragment<*,*>?>(5) {
        null
    }
    //当前选择的设备
    var currentDevice:DeviceBean? = null
        private set(value) {
            field = value
            //通知对话框
            fragments[1 - 1]?.let {
                (it as DialogFragment).notifyOpenDialog()
            }
            onUpdateDevice()
            askDeviceStatus()
            onUpdateDevice()
        }
    //当前已绑定的设备列表
    var bindDevices:BindDevices? = null
        private set(value){
            field = value
            //只通知“我的页面”
            fragments[5 - 1]?.let {
                (it as MineFragment).notifyDeviceChanged()
            }
        }

    /**
     * 根据刷新策略刷新绑定的设备
     */
    override fun onResume() {
        super.onResume()
        if(SmartApp.NEED_MAIN_REFRESH_DEVICES){
            askBindDevices()
        }else{
            askDeviceStatus()
        }
    }

    /**
     * 停止的时候取消轮询设备在线状态
     */
    override fun onStop() {
        super.onStop()
        mViewModel.cancelAskDevStatus(this)
    }

    /**
     * 获取绑定的设备
     */
    private fun askBindDevices(){
        LoadingUtil.create(this,null)
        mViewModel.cancelAskDevStatus(this)
        mViewModel.getBindDevices().observe(this, Observer {
            SmartApp.NEED_MAIN_REFRESH_DEVICES = false
            LoadingUtil.dismiss()
            if(it.isSuccess){
                bindDevices = it.getOrNull()
                bindDevices?.user_devices?.let {deviceList->
                    if(deviceList.size>0){
                        currentDevice?.let {cur->
                            if(!deviceList.contains(cur)){
                                //如果不为空并且不在设备列表中默认选择第一个
                                currentDevice = deviceList[0]
                            }
                        }?: kotlin.run {
                            //如果currentDevice为空默认选择第一个
                            currentDevice = deviceList[0]
                        }
                    }else{
                        //未绑定设备
                        currentDevice = null
                    }
                }?: kotlin.run {
                    //未绑定设备
                    currentDevice = null
                }
            }else{
                //todo 获取该账号绑定的设备失败
                Logger.e("获取该账号绑定的设备失败")
            }
        })
    }

    /**
     * 开始轮询当前设备在线状态
     */
    private fun askDeviceStatus(){
        mViewModel.cancelAskDevStatus(this)
        currentDevice?.let {
            mViewModel.askDevStatus(it,1)?.observe(this, Observer {result->
                if(result.isSuccess){
                    result.getOrNull()?.let {info->
                        currentDevice?.copyFromDeviceInfo(info)
                        onUpdateDevice()
                    }
                }
            })
        }
    }

    /**
     * 更新tabBar上的设备信息
     */
    private fun onUpdateDevice() {
        fragments.forEach {
            it?.let {
                it.notifyDeviceHeader()
            }
        }
    }
}