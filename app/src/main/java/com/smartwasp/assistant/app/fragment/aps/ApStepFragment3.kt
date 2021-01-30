package com.smartwasp.assistant.app.fragment.aps

import android.app.Activity
import android.content.Intent
import android.opengl.Visibility
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.orhanobut.logger.Logger
import com.smartwasp.assistant.app.R
import com.smartwasp.assistant.app.base.*
import com.smartwasp.assistant.app.bean.WifiBean
import com.smartwasp.assistant.app.databinding.FragmentAp3Binding
import com.smartwasp.assistant.app.databinding.LayoutWifiItem1Binding
import com.smartwasp.assistant.app.viewModel.WifiGetModel
import kotlinx.android.synthetic.main.activity_wifi_list.recyclerView
import kotlinx.android.synthetic.main.fragment_ap3.*

/**
 * Created by luotao on 2021/1/30 11:13
 * E-Mail Address：gtkrockets@163.com
 */
class ApStepFragment3 private constructor():BaseFragment<WifiGetModel,FragmentAp3Binding>() {

    companion object{
        /**
         * 静态生成类
         */
        fun newsInstance():ApStepFragment3{
            return ApStepFragment3()
        }
    }

    /**
     * 产生
     * @param savedInstanceState
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel?.startWifiObserver()
        mViewModel?.wifiScanData?.let {obj->
            obj.removeObservers(this)
            obj.observe(this, Observer {
                wifiBeans.clear()
                progress.visibility = View.GONE
                if(it.isSuccess){
                    it.getOrNull()?.let {list->
                        wifiBeans.addAll(list)
                    }
                }
                noWifi.visibility = if(wifiBeans.isEmpty()) View.VISIBLE else View.GONE
                Logger.e("observe")
                recyclerView.adapter?.notifyDataSetChanged()
                //开始自动切换LA_00开头的WIFI
                if(wifiBeans.size == 1){
                    linking(0)
                }
            })
        }
        mViewModel?.autoConnectData?.let {obj->
            obj.removeObservers(this)
            obj.observe(this, Observer {
                if(it.isSuccess){
                    //有wifi连接成功
                    linked(it.getOrThrow())
                }
            })
        }
    }

    /**
     * 开始wifi连接
     * @param position
     */
    private fun linking(position: Int){
        val bean = wifiBeans[position]
        //当前正在连接的mac地址
        var linkingMac = mViewModel?.autoConnectWifi(requireContext(),bean)
        if(!linkingMac.isNullOrEmpty()){
            wifiBeans.forEach {
                it.linkType = if(it.bssid == linkingMac) 1 else 0
            }
            recyclerView.adapter?.notifyDataSetChanged()
        }
        Logger.e("linking:$linkingMac")
    }

    /**
     * 完成wifi连接
     * @param mac
     */
    private fun linked(mac:String){
        if(!mac.isNullOrEmpty()){
            wifiBeans.forEach {
                it.linkType = if(it.bssid == mac) 2 else 0
                Logger.e("linked:$mac,ssid:${it.bssid},it.linkType${it.linkType}")
            }
            recyclerView.adapter?.notifyDataSetChanged()
        }
    }

    /**
     * 视图生成
     * @param view
     * @param savedInstanceState
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.step = "3"
        mBinding.total = "/5"
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = object:RecyclerView.Adapter<ItemViewHolder>(){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
                val itemView = LayoutInflater.from(SmartApp.app).inflate(R.layout.layout_wifi_item1,parent,false)
                return ItemViewHolder(itemView)
            }
            override fun getItemCount(): Int {
                return wifiBeans.size
            }
            override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
                holder.invalidate(wifiBeans[position])
            }
        }
    }private val wifiBeans = mutableListOf<WifiBean>()

    /**
     * 入场动画完成
     */
    override fun onTransitDone() {
        onRefreshWifi()
    }

    /**
     * 刷新wifi列表
     */
    private fun onRefreshWifi() {
        progress.visibility = View.VISIBLE
        noWifi.visibility = View.GONE
        wifiBeans.clear()
        recyclerView.adapter?.notifyDataSetChanged()
        mViewModel?.startScan(requireContext())
    }

    //布局文件
    override val layoutResID:Int = R.layout.fragment_ap3

    /**
     * 其他按钮点击
     * @param v
     */
    override fun onButtonClick(v: View) {
        super.onButtonClick(v)
        when(v.id){
            R.id.stepBtn ->{

            }
            R.id.refreshBtn->{
                onRefreshWifi()
            }
        }
    }

    /**
     * group 中itemholder
     * @param itemView 视图
     */
    inner class ItemViewHolder(itemView:View): RecyclerView.ViewHolder(itemView){
        private var itemViewBinding: LayoutWifiItem1Binding? = null
        init {
            itemViewBinding = DataBindingUtil.bind(itemView)
            itemView.setOnClickListener {

            }
        }
        private var data: WifiBean? = null
        fun invalidate(data: WifiBean){
            this.data = data
            itemViewBinding?.wifiBean = data
            Logger.e("invalidate:${data.linkType.toString()}")
        }
    }
}