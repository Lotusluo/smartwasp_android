package com.smartwasp.assistant.app.fragment


import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.iflytek.home.sdk.IFlyHome
import com.orhanobut.logger.Logger
import com.smartwasp.assistant.app.R
import com.smartwasp.assistant.app.base.SmartApp
import com.smartwasp.assistant.app.bean.SongBean
import com.smartwasp.assistant.app.databinding.FragmentDanBinding
import com.smartwasp.assistant.app.databinding.LayoutDanSongItemBinding
import com.smartwasp.assistant.app.util.LoadingUtil
import com.smartwasp.assistant.app.util.SimpleRecyclerHelper
import com.smartwasp.assistant.app.viewModel.SearchModel
import kotlinx.android.synthetic.main.fragment_dan.*
import kotlinx.android.synthetic.main.fragment_dialog.*


/**
 * Created by luotao on 2021/6/28 17:07
 * E-Mail Address：gtkrockets@163.com
 */
class DanFragment private constructor(): MainChildFragment<SearchModel,FragmentDanBinding>() {

    companion object{
        fun newsInstance():DanFragment{
            return DanFragment()
        }
    }

    //布局文件
    override val layoutResID:Int = R.layout.fragment_dan

    /**
     * 注册webView
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        simpleRecyclerHelper?.loadMoreInit()
    }

    /**
     * 通知选择的设备变更
     */
    override fun notifyCurDeviceChanged() {
        super.notifyCurDeviceChanged()
        simpleRecyclerHelper?.loadMoreInit()
    }

    /**
     * 初始化列表控件
     */
    private fun initRecyclerView(){
        simpleRecyclerHelper = SimpleRecyclerHelper(recyclerView,R.layout.layout_dan_song_item,::SongViewHolder)
        with(simpleRecyclerHelper!!){
            onRequest { page, limit, isRefresh ->
                SmartApp.activity?.currentDevice?.let {
                    LoadingUtil.create(requireActivity())
                    mViewModel?.getSongsData("asdasd","党建",page,limit)?.observe(requireActivity(), Observer { result->
                        LoadingUtil.dismiss()
                        if(result.isSuccess){
                            addMore(isRefresh,page,result.getOrNull()?.results)
                        }else{
                            addMoreErr(isRefresh,page)
                        }
                    })
                }?: kotlin.run {
                    Logger.e("No devices")
                }
            }
        }
    }private var simpleRecyclerHelper: SimpleRecyclerHelper<SongBean, LayoutDanSongItemBinding, SongViewHolder>? = null


    /**
     * 歌单列表控件
     * @param itemView
     */
    inner class SongViewHolder(itemView:View):
            SimpleRecyclerHelper.SimpleViewHolder<SongBean, LayoutDanSongItemBinding>(itemView) {
        init {
            itemView.setOnClickListener {
                if(checkOffline())
                    return@setOnClickListener
                SmartApp.activity?.currentDevice?.let {
                    Logger.e(bean.toString())
                    mViewModel?.playMedia(it.device_id,bean!!.id,bean!!.source_type)?.observe(this@DanFragment,
                            Observer {result->
                                if(result.isSuccess){
                                    LoadingUtil.showToast(SmartApp.app,String.format(getString(R.string.playing),bean!!.name))
                                }else{
                                    LoadingUtil.showToast(SmartApp.app,getString(R.string.try_again1))
                                }
                            })
                }?: kotlin.run {

                }
            }
        }
        override fun onDataChanged(compare: SongBean?) {
            itemViewBinding?.let {
                it.bean2 = bean
            }
        }
    }


    //提示框
    private fun checkOffline():Boolean{
        SmartApp.activity?.currentDevice?.let {
            if(!it.isOnline()){
                LoadingUtil.showToast(SmartApp.app,getString(R.string.offline))
                return true
            }
        }
        return false
    }

    /**
     * 按钮点击
     * @param v
     */
    override fun onButtonClick(v: View) {
        super.onButtonClick(v)
        Logger.e("onButtonClick:${v.id}")
    }

}