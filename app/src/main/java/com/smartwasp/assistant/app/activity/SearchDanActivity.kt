package com.smartwasp.assistant.app.activity


import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.lifecycle.Observer
import com.orhanobut.logger.Logger
import com.smartwasp.assistant.app.R
import com.smartwasp.assistant.app.base.BaseActivity
import com.smartwasp.assistant.app.base.SmartApp
import com.smartwasp.assistant.app.bean.SongBean
import com.smartwasp.assistant.app.databinding.ActivityDanSearchBinding
import com.smartwasp.assistant.app.databinding.LayoutDanSongItemBinding
import com.smartwasp.assistant.app.util.LoadingUtil
import com.smartwasp.assistant.app.util.ServiceUtil
import com.smartwasp.assistant.app.util.SimpleRecyclerHelper
import com.smartwasp.assistant.app.viewModel.SearchModel
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.activity_search.recyclerView

class SearchDanActivity:BaseActivity<SearchModel,ActivityDanSearchBinding>() {

    override val layoutResID: Int = R.layout.activity_dan_search


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initRecyclerView()
        simpleRecyclerHelper?.loadMoreInit()
    }

    /**
     * 初始化列表控件
     */
    private fun initRecyclerView(){
        SmartApp.activity?.currentDevice?.let {
            simpleRecyclerHelper = SimpleRecyclerHelper(recyclerView,R.layout.layout_dan_song_item,::SongViewHolder)
            with(simpleRecyclerHelper!!){
                onRequest { page, limit, isRefresh ->
                    LoadingUtil.create(this@SearchDanActivity)
                    mViewModel?.getSongsData(it.device_id,"党建",page,limit)?.observe(this@SearchDanActivity, Observer { result->
                        LoadingUtil.dismiss()
                        if(result.isSuccess){
                            addMore(isRefresh,page,result.getOrNull()?.results)
                        }else{
                            addMoreErr(isRefresh,page)
                        }
                    })
                }
            }
        }?: kotlin.run {
            finish()
        }
    }private var simpleRecyclerHelper: SimpleRecyclerHelper<SongBean, LayoutDanSongItemBinding, SongViewHolder>? = null


    /**
     * 添加点击事件
     */
    override fun onButtonClick(v: View){
        super.onButtonClick(v)
        when(v.id){
            R.id.appCompatImageButton ->{
                finish()
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
                    mViewModel?.playMedia(it.device_id,bean!!.id,bean!!.source_type)?.observe(this@SearchDanActivity,
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
//                it.bean2Pos = (pos + 1).toString()
            }
        }
    }
}