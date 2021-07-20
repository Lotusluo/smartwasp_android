package com.smartwasp.assistant.app.fragment.aps

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.orhanobut.logger.Logger
import com.smartwasp.assistant.app.R
import com.smartwasp.assistant.app.activity.ApStepActivity
import com.smartwasp.assistant.app.base.*
import com.smartwasp.assistant.app.bean.AuthBean
import com.smartwasp.assistant.app.bean.WifiBean
import com.smartwasp.assistant.app.databinding.FragmentAp3Binding
import com.smartwasp.assistant.app.databinding.LayoutWifiItem1Binding
import com.smartwasp.assistant.app.fragment.PreBindFragment
import com.smartwasp.assistant.app.util.AppExecutors
import com.smartwasp.assistant.app.util.LoadingUtil
import com.smartwasp.assistant.app.util.NetWorkUtil
import com.smartwasp.assistant.app.util.WifiUtils
import com.smartwasp.assistant.app.viewModel.WifiGetModel
import kotlinx.android.synthetic.main.activity_wifi_list.recyclerView
import kotlinx.android.synthetic.main.fragment_ap3.*
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit

/**
 * Created by luotao on 2021/1/30 11:13
 * E-Mail Address：gtkrockets@163.com
 */
class ApStepFragment3 private constructor():BaseFragment<WifiGetModel,FragmentAp3Binding>() {

    companion object{
        /**
         * 静态生成类
         * @param clientID 客户端ID
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
        mViewModel?.startWifiObserver(requireContext())
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
                recyclerView.adapter?.notifyDataSetChanged()
                //开始自动切换LA_00开头的WIFI
                if(wifiBeans.size == 1){
                    linking(wifiBeans[0])
                }
                //如果有热点则激活下一步
                stepBtn.isEnabled = false
                wifiBeans.forEach {wifi->
                    if(wifi.linkType == WifiBean.STATE_LINKED){
                        stepBtn.isEnabled = true
                    }
                }
            })
        }
        mViewModel?.autoConnectData?.let {obj->
            obj.removeObservers(this)
            obj.observe(this, Observer {
                if(it.isSuccess){
                    //有wifi连接成功
                    //清除其它多余连接
                    linked(it.getOrThrow())
                }else{
                    if(isAdded) {
                        val wifiManager = context?.getSystemService(Context.WIFI_SERVICE) as WifiManager
                        wifiManager?.let {w->
                            linked(w.connectionInfo.bssid)
                            if (!stepBtn.isEnabled) {
                                AlertDialog.Builder(requireContext())
                                        .setTitle(R.string.tip)
                                        .setMessage(R.string.long_wifi_err)
                                        .setCancelable(false)
                                        .setPositiveButton(android.R.string.ok, null)
                                        .show()
                            }
                        }
                    }
                }
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        AppExecutors.get().mainThread().removeCallbacks(delayToCheck)
    }

    /**
     * 开始wifi连接
     * @param link
     */
    private fun linking(bean: WifiBean){
        //当前正在连接的mac地址
        var linkingMac = mViewModel?.autoConnectWifi(requireContext(),bean)
        if(linkingMac != null){
            wifiBeans.forEach {
                it.linkType = if(it.bssid == linkingMac.bssid) WifiBean.STATE_LINKING else WifiBean.STATE_IDLE
            }
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
                recyclerView.adapter?.notifyDataSetChanged()
                AppExecutors.get().mainThread().removeCallbacks(delayToCheck)
                AppExecutors.get().mainThread().executeDelay(delayToCheck,11 * 1000)
                Logger.d("准备联网超时检测")
            }else{
                //等待连接失败回调
            }
        }
    }

    /**
     * 完成wifi连接
     * @param mac
     */
    private fun linked(mac:String?){
        if(!mac.isNullOrEmpty()){
            wifiBeans.forEach {
                it.linkType = if(it.bssid == mac) WifiBean.STATE_LINKED else WifiBean.STATE_IDLE
                if(it.linkType == WifiBean.STATE_LINKED){
                    stepBtn.isEnabled = true
                }
            }
            recyclerView.adapter?.notifyDataSetChanged()
        }
        AppExecutors.get().mainThread().removeCallbacks(delayToCheck)
        Logger.d("取消联网超时检测")
    }

    /**
     * 强制连接某一个wifi
     * @param link
     */
    private fun onForceLinking(link:WifiBean){
        mViewModel?.linking?.let {
            if(it.bssid == link.bssid){
                return
            }
        }
        mViewModel?.removeLinkingSSID(requireContext())?.let {
            linked(it)
        }
        linking(link)
    }

    private val delayToCheck = Runnable {
        Logger.d("联网超时!")
        if(isAdded){
            val wifiManager = context?.getSystemService(Context.WIFI_SERVICE) as WifiManager
            wifiManager?.let {
                linked(it.connectionInfo.bssid)
                if(!stepBtn.isEnabled){
                    AlertDialog.Builder(requireContext())
                            .setTitle(R.string.tip)
                            .setMessage(R.string.long_wifi_no)
                            .setCancelable(false)
                            .setPositiveButton(android.R.string.ok ,null)
                            .show()
                }
            }
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
        mBinding.total = "/4"
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
        stepBtn.isEnabled = false
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
                ApStepActivity.authBean?.auth_code?.let {authCode->
                    onNavigatorClick()
                    requireActivity()?.addFragmentByTagWithStack(R.id.container,ApStepFragment4.newsInstance(authCode))
                }?: kotlin.run {}
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
                data?.let {
                    onForceLinking(it)
                }
            }
        }
        private var data: WifiBean? = null
        fun invalidate(data: WifiBean){
            this.data = data
            itemViewBinding?.wifiBean = data

        }
    }
}