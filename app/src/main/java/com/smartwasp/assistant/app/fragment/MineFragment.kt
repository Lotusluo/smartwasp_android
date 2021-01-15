package com.smartwasp.assistant.app.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.smartwasp.assistant.app.R
import com.smartwasp.assistant.app.activity.DeviceSetActivity
import com.smartwasp.assistant.app.activity.PrevBindActivity
import com.smartwasp.assistant.app.base.BaseFragment
import com.smartwasp.assistant.app.base.SmartApp
import com.smartwasp.assistant.app.bean.DeviceBean
import com.smartwasp.assistant.app.bean.test.BindDevices
import com.smartwasp.assistant.app.databinding.FragmentMineBinding
import com.smartwasp.assistant.app.databinding.LayoutDeviceItemBinding
import com.smartwasp.assistant.app.util.IFLYOS
import com.smartwasp.assistant.app.util.LoadingUtil
import com.smartwasp.assistant.app.viewModel.MineModel
import com.youth.banner.adapter.BannerAdapter
import com.youth.banner.listener.OnPageChangeListener
import com.youth.banner.util.BannerUtils
import kotlinx.android.synthetic.main.fragment_mine.*

/**
 * Created by luotao on 2021/1/11 15:44
 * E-Mail Address：gtkrockets@163.com
 */
class MineFragment private constructor():BaseFragment<MineModel,FragmentMineBinding>() {
    companion object{
        fun newsInstance():MineFragment{
            return MineFragment()
        }
    }

    //布局文件
    override val layoutResID:Int = R.layout.fragment_mine

    /**
     * 获取绑定的设备
     */
    @SuppressLint("FragmentLiveDataObserve")
    private fun getBindDevices(){
        val flag = LoadingUtil.create(requireActivity() as AppCompatActivity,null)
        mViewModel.cancelAskDevStatus(this)
        mViewModel.getBindDevices().observe(this, Observer {
            LoadingUtil.dismiss(flag)
            SmartApp.NEED_MINE_REFRESH_DEVICES = false
            if(it.isSuccess){
                onRenderBindDevices(it.getOrNull())
            }else{
                //todo 获取该账号绑定的设备失败
                //暂时默认没有设备
                onRenderBindDevices(null)
            }
        })
    }

    /**
     * 渲染绑定的设备
     */
    private fun onRenderBindDevices(devices: BindDevices?) {
        mPosition = 0
        val deviceBeans:MutableList<DeviceBean> = devices?.user_devices ?: kotlin.run {
            ArrayList<DeviceBean>()
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
            banner.currentItem = 2
            mPosition = 1
            askDeviceStatus()
        }
        //todo 探寻impl中用到interface哪个方法就实现哪个
        banner.addOnPageChangeListener(object:OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                mPosition = position
                askDeviceStatus()
            }
        })
    }private var mPosition = 0

    /**
     * 开始轮询当前设备在线状态
     */
    @SuppressLint("FragmentLiveDataObserve")
    private fun askDeviceStatus(){
        mViewModel.cancelAskDevStatus(this)
        banner.adapter ?: return
        val deviceBean = banner.adapter.getData(mPosition) as DeviceBean
        if(deviceBean.isHeader()) return
        deviceBean.position = mPosition
        mViewModel.askDevStatus(deviceBean,1)?.observe(this, Observer {
            if(it.isSuccess){
                it.getOrNull()?.let { deviceInfoBean -> onRenderBindDevice(deviceInfoBean) }
            }
        })
    }

    /**
     * 渲染最新的设备信息
     * @param deviceInfoBean 设备信息
     * 暂时只更新离在线状态
     */
    private fun onRenderBindDevice(deviceInfoBean:DeviceBean){
        val adapter = banner.adapter
        val deviceBean = adapter.getData(deviceInfoBean.position) as DeviceBean
        deviceBean.copyFromDeviceInfo(deviceInfoBean)
        //todo Cannot call this method in a scroll callback. Scroll callbacks mightbe run during a measure & layout pass where you cannot change theRecyclerView data. Any method call that might change the structureof the RecyclerView or the adapter contents should be postponed tothe next frame.
        adapter.notifyItemChanged(deviceBean.position + 1)
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
     * 页面呈交互状态
     */
    override fun onResume() {
        super.onResume()
        if(SmartApp.NEED_MINE_REFRESH_DEVICES){
            getBindDevices()
        }else{
            askDeviceStatus()
        }
    }

    /**
     * 页面不可见状态
     */
    override fun onStop() {
        super.onStop()
        mViewModel.cancelAskDevStatus(this)
    }

    /**
     * 当前页是否在隐藏
     */
    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if(hidden){
            mViewModel.cancelAskDevStatus(this)
        }else{
            if(SmartApp.NEED_MINE_REFRESH_DEVICES){
                getBindDevices()
            }else{
                askDeviceStatus()
            }
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