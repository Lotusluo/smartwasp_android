package com.smartwasp.assistant.app.fragment

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.iflytek.home.sdk.IFlyHome
import com.orhanobut.logger.Logger
import com.smartwasp.assistant.app.R
import com.smartwasp.assistant.app.activity.*
import com.smartwasp.assistant.app.base.SmartApp
import com.smartwasp.assistant.app.bean.DeviceBean
import com.smartwasp.assistant.app.bean.BindDevices
import com.smartwasp.assistant.app.bean.PayType
import com.smartwasp.assistant.app.databinding.FragmentMineBinding
import com.smartwasp.assistant.app.databinding.LayoutDeviceItemBinding
import com.smartwasp.assistant.app.util.ConfigUtils
import com.smartwasp.assistant.app.util.IFLYOS
import com.smartwasp.assistant.app.util.LoadingUtil
import com.smartwasp.assistant.app.viewModel.MineModel
import com.youth.banner.adapter.BannerAdapter
import com.youth.banner.listener.OnPageChangeListener
import com.youth.banner.util.BannerUtils
import kotlinx.android.synthetic.main.fragment_mine.*
import kotlinx.android.synthetic.main.fragment_mine.badge
import kotlinx.android.synthetic.main.layout_device_item.view.*
import kotlinx.android.synthetic.main.layout_tabbar.*

/**
 * Created by luotao on 2021/1/11 15:44
 * E-Mail Address：gtkrockets@163.com
 */
class MineFragment private constructor():MainChildFragment<MineModel,FragmentMineBinding>() {
    companion object{
        fun newsInstance():MineFragment{
            return MineFragment()
        }
    }

    //布局文件
    override val layoutResID:Int = R.layout.fragment_mine

    /**
     * 通知绑定的设备的改变
     */
    fun notifyBindDevicesChanged(){
        onRenderBindDevices(SmartApp.activity?.bindDevices)
    }

    /**
     * 渲染绑定的设备
     */
    private fun onRenderBindDevices(devices: BindDevices?) {
        deviceBeans =  ArrayList()
        devices?.user_devices?.let {
            deviceBeans!!.addAll(it)
        }
        deviceBeans!!.add(0,DeviceBean())
        banner.adapter = object : BannerAdapter<DeviceBean,DeviceViewHolder>(deviceBeans){
            override fun onCreateHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
                val itemView = LayoutInflater.from(SmartApp.app).inflate(R.layout.layout_device_item,parent,false)
                itemView.layoutParams = ViewGroup.MarginLayoutParams(-1,-1)
                itemView.type1Container.visibility = View.GONE
                itemView.type2Container.visibility = View.GONE
                return DeviceViewHolder(itemView,viewType)
            }
            override fun getItemViewType(position: Int): Int {
                return when(position){
                    0 ->{
                        1 //添加主控设备
                    }else ->{
                        2 //绑定的设备
                    }
                }
            }
            override fun onBindView(holder: DeviceViewHolder, data: DeviceBean, position: Int, size: Int) {
                holder.invalidate(data)
            }
        }
        banner.setIndicator(indicator,false)
        banner.setIndicatorSelectedWidth(BannerUtils.dp2px(8f).toInt())
        banner.addBannerLifecycleObserver(this)
        banner.currentItem = 1
        onSelectedPage()
        //todo 探寻impl中用到interface哪个方法就实现哪个
        banner.addOnPageChangeListener(object:OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {}
        })
    }private var deviceBeans:MutableList<DeviceBean>? = null

    /**
     * 设备变更
     */
    override fun notifyCurDeviceChanged() {
        super.notifyCurDeviceChanged()
        banner.adapter?.let {adapter->
            adapter.notifyDataSetChanged()
            onSelectedPage()
        }
    }

    /**
     * 选中相应的设备
     */
    private fun onSelectedPage(){
        if(null != deviceBeans && deviceBeans!!.size > 1){
            val selected = deviceBeans!!.indexOf(SmartApp.activity?.currentDevice) + 1
            banner.setCurrentItem(selected,false)
            indicator.onPageSelected(selected - 1)
        }
    }

    /**
     * 设备点击
     * @param deviceBean 点击的设备
     */
    private fun onBindDeviceClick(deviceBean: DeviceBean){
        if(deviceBean.isHeader()){
            //跳转扫码主控设备
            startActivity(Intent(requireActivity(),PrevBindActivity::class.java))
            return
        }
        //进入设备详情页与解绑
        startActivity(Intent(requireActivity(),DeviceSetActivity::class.java).apply {
            putExtra(IFLYOS.DEVICE_ID,deviceBean.device_id)
        })
    }

    /**
     * 左侧导航按钮点击
     */
    override fun onNavigatorClick(){
        AlertDialog.Builder(requireContext())
                .setMessage(R.string.exit_confirm)
                .setNegativeButton(android.R.string.cancel,null)
                .setPositiveButton(android.R.string.ok) {
                    _, _ ->
                    LoadingUtil.create(requireActivity())
                    mViewModel?.loginOut()?.observe(this, Observer {
                        if(it == IFLYOS.OK){
                            ConfigUtils.removeAll()
                            requireView().postDelayed({
                                 SmartApp.restart()
                            },1000)
                        }else{
                            LoadingUtil.dismiss()
                            LoadingUtil.showToast(SmartApp.app,getString(R.string.try_again))
                        }
                    })
                }
                .show()
    }

    /**
     * 页面呈交互状态
     */
    override fun onResume() {
        super.onResume()
        SmartApp.DOS_MINE_FRAGMENT_SHOWN = true
        SmartApp.updateBean?.let {
            badge.visibility = if(it.isNewVersion()) View.VISIBLE else View.GONE
        } ?: kotlin.run {
            badge.visibility = View.GONE
        }
    }

    /**
     * 页面不可见状态
     */
    override fun onStop() {
        super.onStop()
//        不可见的时候取消轮询设备是否在线
        SmartApp.DOS_MINE_FRAGMENT_SHOWN = false
    }

    /**
     * 按钮点击
     * @param v
     */
    override fun onButtonClick(v: View){
        super.onButtonClick(v)
        when(v.id){
            R.id.btnRoutines->{
                onTurnToPage(IFlyHome.TRAINING_PLAN)
            }
            R.id.btnAlarm->{
                onTurnToPage(IFlyHome.CLOCKS)
            }
            R.id.btnContent->{
                onTurnToPage(IFlyHome.ACCOUNTS)
            }
            R.id.btnAbount->{
                startActivity(Intent(requireActivity(),AboutActivity::class.java))
            }
        }
    }

    /**
     * 跳转讯飞集成页
     * @param pageIndex 讯飞集成页
     */
    private fun onTurnToPage(pageIndex:String){
        SmartApp.activity?.currentDevice?.let {
            startActivity(Intent(requireContext(), WebViewActivity::class.java).apply {
                putExtra(IFLYOS.EXTRA_PAGE,pageIndex)
                putExtra(IFLYOS.EXTRA_TYPE,IFLYOS.TYPE_PAGE)
                putExtra(IFLYOS.DEVICE_ID,it.device_id)
            })
        }?: kotlin.run {
            LoadingUtil.showToast(SmartApp.app,getString(R.string.add_device1))
        }
    }

    /**
     *加载设备视图数据
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.post {
            setToolBarIcon(R.drawable.ic_login_out,R.color.smartwasp_orange)
            setTittle(getString(R.string.tab_mime))
            notifyBindDevicesChanged()
        }
    }

    /**
     * 当前页是否在隐藏
     */
    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        SmartApp.DOS_MINE_FRAGMENT_SHOWN = !hidden
        if(hidden){
//            不可见的时候取消轮询设备是否在线
        }else{
//            呈现的时候轮询设备是否在线
        }
    }

    /**
     * 绑定设备的ViewHolder
     * @param itemView 渲染视图
     * @param viewType 视图渲染类型
     */
    inner class DeviceViewHolder(itemView:View, private val viewType:Int): RecyclerView.ViewHolder(itemView){
        private lateinit var deviceBean: DeviceBean
        private var itemViewBinding:LayoutDeviceItemBinding? = null
        init {
            itemViewBinding = DataBindingUtil.bind(itemView)
            itemViewBinding?.deviceBean = null
            itemView.setOnClickListener {
                onBindDeviceClick(deviceBean)
            }
        }
        fun invalidate(data: DeviceBean){
            deviceBean = data
            //绑定的设备渲染
            itemViewBinding?.let {
                it.deviceBean = data
                if(viewType == 2){
                    //添加主控设备渲染
                    Glide.with(itemView)
                            .load(data.image)
                            .dontAnimate()
                            .into(it.deviceImage)
                    it.iconDeviceStatus.isSelected = data.isOnline()
                    it.biasView.setText(if(data?.music?.enable == true) R.string.music_enabled else R.string.music_disEnabled)
                }
            }
        }
    }
}