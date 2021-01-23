package com.smartwasp.assistant.app.fragment

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.appbar.AppBarLayout
import com.orhanobut.logger.Logger
import com.smartwasp.assistant.app.R
import com.smartwasp.assistant.app.bean.ItemBean
import com.smartwasp.assistant.app.bean.SongBean
import com.smartwasp.assistant.app.databinding.FragmentMusicItemBinding
import com.smartwasp.assistant.app.databinding.LayoutSongItemBinding
import com.smartwasp.assistant.app.util.LoadingUtil
import com.smartwasp.assistant.app.util.ScreenUtil
import com.smartwasp.assistant.app.util.SimpleRecyclerHelper
import com.smartwasp.assistant.app.viewModel.MusicModel
import kotlinx.android.synthetic.main.fragment_music_item.*
import kotlinx.android.synthetic.main.layout_song_item.view.*
import kotlin.math.abs

/**
 * Created by luotao on 2021/1/19 13:41
 * E-Mail Address：gtkrockets@163.com
 */
class MusicItemFragment private constructor(var itemBean: ItemBean): MainChildFragment<MusicModel, FragmentMusicItemBinding>(){
    //布局文件
    override val layoutResID:Int = R.layout.fragment_music_item

    companion object{
        fun newsInstance(itemBean: ItemBean):MusicItemFragment{
            return MusicItemFragment(itemBean)
        }
    }

    /**
     * 视图
     * @param view
     * @param savedInstanceState
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initHeader()
        initImage()
        initRecyclerView()
    }

    /**
     * 准备header
     */
    private fun initHeader(){
        with(mBinding.toolbar){
            val icBack = resources.getDrawable(R.mipmap.ic_navback)
            DrawableCompat.setTint(icBack,resources.getColor(R.color.smartwasp_blue))
            navigationIcon = icBack
            (layoutParams as ViewGroup.MarginLayoutParams).topMargin = ScreenUtil.statusHeight(requireContext())
            setNavigationOnClickListener {
                parentFragmentManager.popBackStack()
            }
        }
        mBinding.itemBean = itemBean
        mBinding.appBarLayout.addOnOffsetChangedListener(AppBarLayout.BaseOnOffsetChangedListener<AppBarLayout> {
            appBar: AppBarLayout, position: Int ->
            requireContext()?.let {
                val fittedHeight = appBar.measuredHeight - mBinding.toolbar.measuredHeight - ScreenUtil.statusHeight(it)
                val limitedHeight = fittedHeight / 2
                val absPosition = abs(position)
                mBinding.titleVisible = absPosition >= limitedHeight
            }
        })
    }

    /**
     * 初始化图像
     */
    private fun initImage(){
        Glide.with(requireContext())
                .asBitmap()
                .load(itemBean.image)
                .into(object:SimpleTarget<Bitmap>(){
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        blurredView.setBlurredImg(resource)
                        blurredView.setBlurredLevel(100)
                        bezelImageView.setImageBitmap(resource)
                    }
                })
    }

    /**
     * 初始化列表控件
     */
    private fun initRecyclerView(){
        simpleRecyclerHelper = SimpleRecyclerHelper(recyclerView,R.layout.layout_song_item,::SongViewHolder)
        with(simpleRecyclerHelper!!){
            onRequest { page, limit, isRefresh ->
                mViewModel.getSongsData(itemBean.id,page,limit).observe(this@MusicItemFragment, Observer {result->
                    if(result.isSuccess){
                        addMore(isRefresh,page,result.getOrNull()?.items)
                    }else{
                        addMoreErr(isRefresh,page)
                    }
                })
            }
            loadMoreInit()
        }
    }private var simpleRecyclerHelper:SimpleRecyclerHelper<SongBean,LayoutSongItemBinding,SongViewHolder>? = null


    /**
     * 歌单列表控件
     * @param itemView
     */
    inner class SongViewHolder(itemView:View):
            SimpleRecyclerHelper.SimpleViewHolder<SongBean, LayoutSongItemBinding>(itemView) {
        init {
            itemView.setOnClickListener {
                if(checkOffline())
                    return@setOnClickListener
                currentDevice?.let {
                    mViewModel.playMedia(it.device_id,bean?.id,itemBean.id).observe(this@MusicItemFragment,
                            Observer {result->
                                if(result.isSuccess){

                                }else{
                                    this@MusicItemFragment.requireContext()?.let {context->
                                        LoadingUtil.showToast(context,context.getString(R.string.try_again))
                                    }
                                }
                    })
                }
            }
            itemView.btnPlayer.setOnClickListener {
                bean?.let {
                    SongPlayDialog.newsInstance(it).show(childFragmentManager,null)
                }
            }
        }
        override fun onDataChanged() {
            itemViewBinding?.let {
                it.bean2 = bean
                it.bean2Pos = (pos + 1).toString()
                it.tvName.isSelected = mediaState?.data?.music?.id == bean?.id
            }
        }
    }

    //提示框
    private fun checkOffline():Boolean{
        currentDevice?.let {
            if(!it.isOnline()){
                this@MusicItemFragment.requireContext()?.let {context->
                    LoadingUtil.showToast(context,context.getString(R.string.offline))
                }
                return true
            }
        }
        return false
    }

    /**
     * 刷新列表中歌曲的选项
     */
    override fun notifyMediaChanged() {
        super.notifyMediaChanged()
        //确定是否需要刷新某个Holder
        mediaState?.data?.music?.let {
            simpleRecyclerHelper?.notifyDataChanged(it)
        }
    }

    /**
     * 按钮点击
     */
    override fun onButtonClick(v: View) {
        super.onButtonClick(v)
        if(checkOffline())
            return
        when(v.id){
            R.id.playAll ->{
                currentDevice?.let {
                    mViewModel.playMedia(it.device_id,"",itemBean.id).observe(this@MusicItemFragment,
                            Observer {result->
                                if(result.isSuccess){

                                }else{
                                    this@MusicItemFragment.requireContext()?.let {context->
                                        LoadingUtil.showToast(context,context.getString(R.string.try_again))
                                    }
                                }
                            })
                }
            }
            R.id.sheet_play_btn ->{
                currentDevice?.let {
                     (v.getTag(R.id.extra_tag) as SongBean?)?.let {song->
                        mViewModel.playMedia(it.device_id,song.id,itemBean.id).observe(this@MusicItemFragment,
                                Observer {result->
                                    if(result.isSuccess){

                                    }else{
                                        this@MusicItemFragment.requireContext()?.let {context->
                                            LoadingUtil.showToast(context,context.getString(R.string.try_again))
                                        }
                                    }
                                })
                    }
                }
            }
        }
    }
}