package com.smartwasp.assistant.app.activity

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.children
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.orhanobut.logger.Logger
import com.smartwasp.assistant.app.R
import com.smartwasp.assistant.app.base.*
import com.smartwasp.assistant.app.bean.DeviceBean
import com.smartwasp.assistant.app.bean.BindDevices
import com.smartwasp.assistant.app.bean.MusicStateBean
import com.smartwasp.assistant.app.bean.StatusBean
import com.smartwasp.assistant.app.databinding.ActivityMainBinding
import com.smartwasp.assistant.app.fragment.*
import com.smartwasp.assistant.app.util.*
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
    //设备状态观察者
    private val devicesStateObserver: MutableLiveData<StatusBean<DeviceBean>> = MutableLiveData()
    //媒体状态
    private val mediaStateObserver: MutableLiveData<StatusBean<MusicStateBean>> = MutableLiveData()
    /**
     * 初始化主Activity
     * @param savedInstanceState
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarUtil.transparencyBar(this)
        StatusBarUtil.setLightStatusBar(this,true,true)
        initObserver()
        setTabIconStyle()
        login()

        AppExecutors.get().netIO().execute {
            SystemClock.sleep(2000)
            if(!NetWorkUtil.isGoodInternet(this)){
                runOnUiThread {
                    AlertDialog.Builder(this)
                            .setTitle(R.string.tip)
                            .setMessage("网络不太好，请确定网络状态！")
                            .setPositiveButton(android.R.string.ok,null)
                            .show()
                }
            }
        }
    }

    /**
     * 销毁
     */
    override fun onDestroy() {
        mViewModel.clearBinder(this)
        super.onDestroy()
    }

    private var lastExitTime:Long = 0
    /**
     * 回退拦截
     */
    override fun interceptLeftButton():Boolean{
        if(super.interceptLeftButton())
            return true
        return if (System.currentTimeMillis() - lastExitTime > 2000) {
            LoadingUtil.showToast(this,getString(R.string.exit_again))
            lastExitTime = System.currentTimeMillis()
            true
        } else{
            SmartApp.finish(0)
            true
        }
    }

    /**
     * 初始化观察者
     */
    private fun initObserver(){
        SmartApp.addDevStateObserver(devicesStateObserver)
        SmartApp.addMediaObserver(mediaStateObserver)
        devicesStateObserver.observe(this, Observer {state->
            //与本地的设备列表比较,更新离在线状态
            bindDevices?.user_devices?.forEach {
                if(it.device_id == state.data.device_id){
                    if(it.timestamp.toLong() < state.timestamp.toLong()){
                        //通知有设备的状态改变
                        it.status = state.data.status
                        it.timestamp = state.timestamp
                        fragments.forEach {fragment->
                            fragment?.notifyCurDeviceChanged()
                        }
                    }
                    return@forEach
                }
            }
        })

        mediaStateObserver.observe(this, Observer {state->
            mediaState?.let {
                if(it.data.device_id == state.data.device_id){
                    if(it.timestamp.toLong() < state.timestamp.toLong()){
                        mediaState = state
                    }
                }
            }?: kotlin.run {
                mediaState = state
            }
        })
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
    private fun login() {
        if(SmartApp.isLogin())
            return
        LoadingUtil.create(this)
        mViewModel.iflyoslogin().observe(this, Observer{
            when {
                it.startsWith(IFLYOS.OK) -> {
                    //登录成功，获取user_id
                    mViewModel.getUserBean().observe(this, Observer{msg->
                        LoadingUtil.dismiss()
                        when(msg){
                            IFLYOS.OK->{
                                //提交uid到服务器
                                registerUid()
                                requestData()
                            }
                            IFLYOS.ERROR->{
                                AlertDialog.Builder(this)
                                        .setMessage(R.string.login_err)
                                        .setPositiveButton(android.R.string.ok,null)
                                        .show()
                            }
                        }
                    })
                }
                it.startsWith(IFLYOS.ERROR) -> {
                    LoadingUtil.dismiss()
                    AlertDialog.Builder(this)
                            .setMessage(R.string.login_err)
                            .setPositiveButton(android.R.string.ok,null)
                            .show()
                }
                it.startsWith(IFLYOS.EXTRA) -> {
                    //需要跳转讯飞交互页
                    LoadingUtil.dismiss()
                    val tag = it.substring(IFLYOS.EXTRA.length + 1)
                    startActivity(Intent(this@MainActivity,WebViewActivity::class.java).apply {
                        putExtra(IFLYOS.EXTRA_TAG,tag)
                        putExtra(IFLYOS.EXTRA_TYPE,IFLYOS.TYPE_LOGIN)
                    })
                }
            }
        })
    }

    /**
     * 注册
     */
    private fun registerUid(time:Int = 0) {
        if(time > 1)
            return
        mViewModel.register(SmartApp.userBean!!.user_id).observe(this, Observer {
            if(it != IFLYOS.OK){
                registerUid(time + 1)
            }
        })
    }

    /**
     * 刷新数据
     */
    private fun requestData(){
        if(SmartApp.isLogin() && SmartApp.NEED_MAIN_REFRESH_DEVICES){
            askBindDevices()
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
        val index = tabID - 1
        val tabFragment:MainChildFragment<*,*>? =
                fragments[index] ?: kotlin.run {
                    fragments[index] = when(tabID){
                        1 -> DialogFragment.newsInstance()
                        2 -> FindFragment.newsInstance()
                        3 -> SkillFragment.newsInstance()
                        4 -> SmartFragment.newsInstance()
                        5 -> MineFragment.newsInstance()
                        else -> null
                    }
                    fragments[index]
        }
        tabFragment?.let {
            (tabbar as LinearLayout).children.forEach { view->view.isSelected = false}
            if(currentTabID > 0){
                fragments[currentTabID - 1]?.let {fragment->
                    hideFragment(fragment)
                }
            }
            currentTabID = tabID
            addFragmentByTag(R.id.container,it)
            showFragment(it)
            findViewById<View>(tabResID).isSelected = true
        }?: kotlin.run {
            AlertDialog.Builder(this)
                    .setMessage(R.string.un_open)
                    .setPositiveButton(android.R.string.ok,null)
                    .show()
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
//            if(currentDevice != it)
                currentDevice = it
            ConfigUtils.putString(ConfigUtils.KEY_DEVICE_ID,currentDevice?.device_id)
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
            R.id.media_icon->{
                turnToMusic()
            }
            R.id.device_fresh->{
                //刷新设备列表
                askBindDevices()
            }
        }
    }

    /**
     * 跳转音乐播放界面
     */
    fun turnToMusic(){
        //跳转音乐播放界面
        currentDevice?.let {
            mViewModel.subscribeMediaStatus(it.device_id)
            startActivity(Intent(this,MusicActivity::class.java).apply {
                putExtra(IFLYOS.DEVICE_ID,currentDevice)
            })
        }?: kotlin.run {
            LoadingUtil.showToast(this,getString(R.string.plz_add))
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
            //通知当前设备变更
            fragments.forEach {
                it?.notifyCurDeviceChanged()
            }
            askMediaState()
        }
    //当前已绑定的设备列表
    var bindDevices: BindDevices? = null
        private set(value){
            field = value
            //只通知“我的页面”
            fragments[5 - 1]?.let {
                (it as MineFragment).notifyBindDevicesChanged()
            }
        }

    //当前音乐播放的状态
    var mediaState:StatusBean<MusicStateBean>? = null
        private set(value){
            field = value
            //媒体状态改变
            fragments.forEach {
                it?.notifyMediaChanged()
            }
        }

    /**
     * 呈交互态的时候区别逻辑
     */
    override fun onResume() {
        super.onResume()
        requestData()
    }

    /**
     * 停止的时候取消轮询设备在线状态
     */
    override fun onStop() {
        super.onStop()
        SmartApp.userBean?.let {
//            mViewModel.cancelAskDevStatus(this)
        }
    }

    /**
     * 获取绑定的设备
     */
    private fun askBindDevices(){
        LoadingUtil.create(this,null)
//        mViewModel.cancelAskDevStatus(this)
        mViewModel.getBindDevices().observe(this, Observer {
            SmartApp.NEED_MAIN_REFRESH_DEVICES = false
            LoadingUtil.dismiss()
            if(it.isSuccess){
                bindDevices = it.getOrNull()
                bindDevices?.user_devices?.let {deviceList->
                    if(deviceList.size>0){
                        currentDevice?.let {cur->
                            if(!deviceList.contains(cur)){
                                //如果不为空并且不在设备列表中默认选择最后一个
                                currentDevice = deviceList.lastOrNull()
                            }else{
                                //存在设备信息更新
                                val device = deviceList[deviceList.indexOf(cur)]
                                if(!cur.isSameStatus(device)){
                                    currentDevice = device
                                }
                            }
                        }?: kotlin.run {
                            //如果currentDevice为空默认选择最后一个
                            ConfigUtils.getString(ConfigUtils.KEY_DEVICE_ID,null)?.let {saveID->
                                deviceList.forEach {device->
                                    if(device.device_id == saveID){
                                        currentDevice = device
                                        return@run
                                    }
                                }
                            }
                            currentDevice = deviceList.lastOrNull()
                            ConfigUtils.putString(ConfigUtils.KEY_DEVICE_ID,currentDevice?.device_id)
                        }
                    }else{
                        //未绑定设备
                        currentDevice = null
                    }
                }?: kotlin.run {
                    //未绑定设备
                    currentDevice = null
                }
                mViewModel.initBinder(this)
            }else{
                bindDevices?.let {
                    LoadingUtil.showToast(this,getString(R.string.get_devices_err))
                }?: kotlin.run {
                    AlertDialog.Builder(this)
                            .setMessage(R.string.get_devices_err)
                            .setPositiveButton(android.R.string.ok,null)
                            .show()
                }
            }
            //初始设置后，设置默认点击的tab
            if(root.visibility == View.GONE){
                root.visibility = View.VISIBLE
                onButtonClick(tab_dialog)
            }
        })
    }

    /**
     * 询问当前设备的媒体状态
     */
    private fun askMediaState(){
        currentDevice?.let {
            mViewModel.getMediaStatus(it.device_id).observe(this, Observer {result->
                if(result.isSuccess){
                    result.getOrNull()?.let {music->
                        music.device_id = it.device_id
                        mediaState = StatusBean(data = music)
                    }
                }
            })
            mViewModel.subscribeMediaStatus(it.device_id)
        }?: kotlin.run {
            mediaState = null
        }
    }
}