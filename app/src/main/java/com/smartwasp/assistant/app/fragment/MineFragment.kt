package com.smartwasp.assistant.app.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.orhanobut.logger.Logger
import com.smartwasp.assistant.app.R
import com.smartwasp.assistant.app.activity.DeviceSetActivity
import com.smartwasp.assistant.app.activity.PrevBindActivity
import com.smartwasp.assistant.app.base.SmartApp
import com.smartwasp.assistant.app.bean.DeviceBean
import com.smartwasp.assistant.app.bean.BindDevices
import com.smartwasp.assistant.app.databinding.FragmentMineBinding
import com.smartwasp.assistant.app.databinding.LayoutDeviceItemBinding
import com.smartwasp.assistant.app.util.IFLYOS
import com.smartwasp.assistant.app.viewModel.MineModel
import com.youth.banner.adapter.BannerAdapter
import com.youth.banner.listener.OnPageChangeListener
import com.youth.banner.util.BannerUtils
import kotlinx.android.synthetic.main.fragment_mine.*

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
        onRenderBindDevices(bindDevices)
    }

    /**
     * 渲染绑定的设备
     */
    private fun onRenderBindDevices(devices: BindDevices?) {
        mViewModel.cancelAskDevStatus(this)
        val deviceBeans:MutableList<DeviceBean> =  ArrayList()
        devices?.user_devices?.let {
            deviceBeans.addAll(it)
        }
        deviceBeans.add(0,DeviceBean())
        banner.adapter = object : BannerAdapter<DeviceBean,DeviceViewHolder>(deviceBeans){
            override fun onCreateHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
                val itemView = LayoutInflater.from(SmartApp.app).inflate(R.layout.layout_device_item,parent,false)
                itemView.layoutParams = ViewGroup.MarginLayoutParams(-1,-1)
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
        if(deviceBeans.size > 1){
            banner.currentItem = deviceBeans.indexOf(currentDevice) + 1
        }
        //todo 探寻impl中用到interface哪个方法就实现哪个
        banner.addOnPageChangeListener(object:OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {}
        })
    }

    /**
     * 设备变更
     */
    override fun notifyCurDeviceChanged() {
        super.notifyCurDeviceChanged()
        banner.adapter?.let {adapter->
            adapter.notifyDataSetChanged()
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

    }

    /**
     * 页面呈交互状态
     */
    override fun onResume() {
        super.onResume()
        SmartApp.DOS_MINE_FRAGMENT_SHOWN = true
    }

    /**
     * 页面不可见状态
     */
    override fun onStop() {
        super.onStop()
//        不可见的时候取消轮询设备是否在线
        mViewModel.cancelAskDevStatus(this)
        SmartApp.DOS_MINE_FRAGMENT_SHOWN = false
    }

    /**
     *加载设备视图数据
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.post {
            setToolBarIcon(R.mipmap.ic_login_out,R.color.smartwasp_blue)
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
//        不可见的时候取消轮询设备是否在线
            mViewModel.cancelAskDevStatus(this)
        }else{
//            呈现的时候轮询设备是否在线
        }
    }

    /**
     * 绑定设备的ViewHolder
     * @param itemView 渲染视图
     * @param viewType 视图渲染类型
     */
    inner class DeviceViewHolder(itemView:View, private val viewType:Int):
            RecyclerView.ViewHolder(itemView){
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
                }
            }
        }
    }
}