package com.smartwasp.assistant.app.activity

import android.app.Activity
import android.content.Intent
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
import com.smartwasp.assistant.app.base.BaseActivity
import com.smartwasp.assistant.app.base.SmartApp
import com.smartwasp.assistant.app.bean.WifiBean
import com.smartwasp.assistant.app.databinding.ActivityWifiListBinding
import com.smartwasp.assistant.app.databinding.LayoutWifiItemBinding
import com.smartwasp.assistant.app.fragment.aps.ApStepFragment1
import com.smartwasp.assistant.app.util.IFLYOS
import com.smartwasp.assistant.app.viewModel.WifiGetModel
import kotlinx.android.synthetic.main.activity_wifi_list.*
import kotlinx.android.synthetic.main.fragment_find.hotArea1
import kotlinx.android.synthetic.main.fragment_find.swipeRefreshLayout
import kotlinx.android.synthetic.main.layout_toolbar.*

/**
 * Created by luotao on 2021/1/29 17:28
 * E-Mail Address：gtkrockets@163.com
 * wifi选择列表
 */
class WifiListActivity : BaseActivity<WifiGetModel,ActivityWifiListBinding>() {

    override val layoutResID: Int = R.layout.activity_wifi_list

    /**
     * 生成
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTittle(getString(R.string.wifi_list))
        media_icon?.visibility = View.GONE
        swipeRefreshLayout.setColorSchemeColors(resources.getColor(R.color.smartwasp_orange))
        swipeRefreshLayout.setOnRefreshListener {
            onRefreshWifi()
        }
        hotArea1.setOnClickListener {
            //取消刷新
            swipeRefreshLayout.isRefreshing = false
            it.visibility = View.GONE
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = object:RecyclerView.Adapter<ItemViewHolder>(){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
                val itemView = LayoutInflater.from(SmartApp.app).inflate(R.layout.layout_wifi_item,parent,false)
                return ItemViewHolder(itemView)
            }
            override fun getItemCount(): Int {
                return wifiBeans.size
            }
            override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
                holder.invalidate(wifiBeans[position])
            }
        }
        onRefreshWifi()
    }private val wifiBeans = mutableListOf<WifiBean>()

    /**
     * 刷新wifi列表
     */
    private fun onRefreshWifi() {
        swipeRefreshLayout.isRefreshing = true
        hotArea1.visibility = View.VISIBLE
        mViewModel.getWifiList(this).observe(this, Observer {
            swipeRefreshLayout.isRefreshing = false
            hotArea1.visibility = View.GONE
            if(it.isSuccess){
                wifiBeans.clear()
                it.getOrNull()?.let {list->
                    wifiBeans.addAll(list)
                    recyclerView.adapter?.notifyDataSetChanged()
                }
            }
        })
    }

    /**
     * group 中itemholder
     * @param itemView 视图
     */
    inner class ItemViewHolder(itemView:View): RecyclerView.ViewHolder(itemView){
        private var itemViewBinding: LayoutWifiItemBinding? = null
        init {
            itemViewBinding = DataBindingUtil.bind(itemView)
            itemView.setOnClickListener {
                setResult(Activity.RESULT_OK,Intent().apply {
                    putExtra(IFLYOS.EXTRA,this@ItemViewHolder.data?.ssid)
                    putExtra(IFLYOS.EXTRA_TAG,this@ItemViewHolder.data?.bssid)
                })
                finish()
            }
        }
        private var data: WifiBean? = null
        fun invalidate(data: WifiBean){
            this.data = data
            itemViewBinding?.wifiBean = data
        }
    }
}