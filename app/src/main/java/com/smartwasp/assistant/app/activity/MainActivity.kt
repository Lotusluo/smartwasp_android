package com.smartwasp.assistant.app.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.children
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.orhanobut.logger.Logger
import com.smartwasp.assistant.app.R
import com.smartwasp.assistant.app.base.*
import com.smartwasp.assistant.app.bean.DeviceBean
import com.smartwasp.assistant.app.bean.test.BindDevices
import com.smartwasp.assistant.app.databinding.ActivityMainBinding
import com.smartwasp.assistant.app.fragment.DeviceChooserDialog
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
        //开启广播监听
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
                        val flag = LoadingUtil.create(this,null)
                        mViewModel.getUserBean().observe(this, Observer{msg->
                            LoadingUtil.dismiss(flag)
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
        toolbar.visibility = View.GONE
        toolbar1.visibility = View.VISIBLE
        val index = currentTabID - 1
        val fragment:BaseFragment<*,*>? = when(tabID){
            5 ->{
                setTittle(getString(R.string.tab_mime))
                toolbar.visibility = View.VISIBLE
                toolbar1.visibility = View.GONE
                fragments[index] ?: kotlin.run {
                    fragments[index] = MineFragment.newsInstance()
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
        val tagInt:Int? = v.tag.toString().toInt()
        if(tagInt in 1..5){
            onAttachTabClick(tagInt!!,v.id)
        }
    }

    //当前的fragment
    private var currentTabID:Int = 0
    //当fragments
    private var fragments = MutableList<BaseFragment<*,*>?>(5) {
        null
    }
    //当前选择的设备
    private var currentDevice:DeviceBean? = null
    //当前已绑定的设备列表
    private var bindDevices:BindDevices? = null

    /**
     * 根据刷新策略刷新绑定的设备
     */
    override fun onResume() {
        super.onResume()
        if(SmartApp.NEED_MAIN_REFRESH_DEVICES){
            getBindDevices()
        }else{

        }
    }

    /**
     * 停止的时候取消轮询设备在线状态
     */
    override fun onStop() {
        super.onStop()

    }

    /**
     * 获取绑定的设备
     */
    private fun getBindDevices(){
        LoadingUtil.create(this,null)
        mViewModel.getBindDevices().observe(this, Observer {
            LoadingUtil.dismiss()
            if(it.isSuccess){
                bindDevices = it.getOrNull()
            }else{
                //todo 获取该账号绑定的设备失败
            }
        })
    }
}