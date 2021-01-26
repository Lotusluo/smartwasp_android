package com.smartwasp.assistant.app.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.os.PowerManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatTextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.NestedScrollView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.appbar.AppBarLayout
import com.orhanobut.logger.Logger
import com.smartwasp.assistant.app.R
import com.smartwasp.assistant.app.activity.WebViewActivity
import com.smartwasp.assistant.app.base.SmartApp
import com.smartwasp.assistant.app.base.addFragmentByTag
import com.smartwasp.assistant.app.bean.BannerBean
import com.smartwasp.assistant.app.bean.GroupBean
import com.smartwasp.assistant.app.bean.ItemBean
import com.smartwasp.assistant.app.databinding.FragmentFindBinding
import com.smartwasp.assistant.app.databinding.LayoutFindGroupBinding
import com.smartwasp.assistant.app.databinding.LayoutFindItemBinding
import com.smartwasp.assistant.app.util.IFLYOS
import com.smartwasp.assistant.app.util.LoadingUtil
import com.smartwasp.assistant.app.viewModel.FindModel
import com.smartwasp.assistant.app.widget.BezelImageView
import com.youth.banner.adapter.BannerAdapter
import com.youth.banner.indicator.RoundLinesIndicator
import com.youth.banner.util.BannerUtils
import kotlinx.android.synthetic.main.fragment_find.*
import kotlinx.android.synthetic.main.layout_device_header.*
import kotlinx.android.synthetic.main.layout_find_item.*
import kotlinx.coroutines.cancel
import net.lucode.hackware.magicindicator.FragmentContainerHelper
import net.lucode.hackware.magicindicator.buildins.UIUtil
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.ColorTransitionPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.SimplePagerTitleView
import kotlin.math.abs

/**
 * Created by luotao on 2021/1/18 10:58
 * E-Mail Address：gtkrockets@163.com
 */
class FindFragment private constructor():MainChildFragment<FindModel,FragmentFindBinding>() {

    companion object{
        fun newsInstance():FindFragment{
            return FindFragment()
        }
    }

    /**
     * 按钮点击
     * @param v
     */
    override fun onButtonClick(v: View) {
        super.onButtonClick(v)
        when(v.id){
            R.id.card ->{
                SmartApp.activity?.currentDevice?.music?.let {
                    startActivity(Intent(requireActivity(), WebViewActivity::class.java).apply {
                        SmartApp.NEED_MAIN_REFRESH_DEVICES = true
                        putExtra(IFLYOS.EXTRA_URL, it?.redirect_url)
                        putExtra(IFLYOS.EXTRA_TYPE, IFLYOS.TYPE_PAGE)
                    })
                }
            }
        }
    }

    //布局文件
    override val layoutResID:Int = R.layout.fragment_find


    /**
     * 注册webView
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        swipeRefreshLayout.setColorSchemeColors(resources.getColor(R.color.smartwasp_blue))
        swipeRefreshLayout.setOnRefreshListener {
            notifyCurDeviceChanged()
        }
        hotArea1.setOnClickListener {
            //取消刷新
            swipeRefreshLayout.isRefreshing = false
            it.visibility = View.GONE
        }
        appBarLayout.addOnOffsetChangedListener(AppBarLayout.BaseOnOffsetChangedListener<AppBarLayout> {
            _: AppBarLayout, position: Int ->
            if(swipeRefreshLayout.isRefreshing)
                return@BaseOnOffsetChangedListener
            swipeRefreshLayout.isEnabled = position >= 0
        })
        scrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener{
            _,_,y,_,_->
            val helper = magic_indicator4.getTag(R.id.global_container_id) as FragmentContainerHelper?
            helper?.let {
                it.handlePageSelected(getAbbrIndex(y))
            }
        })
    }

    /**
     * 通知选择的设备变更
     */
    @SuppressLint("FragmentLiveDataObserve")
    override fun notifyCurDeviceChanged(){
        super.notifyCurDeviceChanged()
        swipeRefreshLayout?.let {swipeRefreshLayout->
            mViewModel.cancel()
            swipeRefreshLayout.isRefreshing = true
            hotArea1.visibility = View.VISIBLE
            SmartApp.activity?.currentDevice?.let {
                mViewModel.getFindData(it.device_id).observe(this@FindFragment, Observer {findBean->
                    swipeRefreshLayout.isRefreshing = false
                    hotArea1.visibility = View.GONE
                    if(findBean.isSuccess){
                        card.alpha = 1f
                        linearLayout.alpha = 1f
                        val findBean = findBean.getOrNull()
                        onRenderBanner(findBean?.banners)
                        onRenderIndicator(findBean?.groups?.map{groupBean->
                            groupBean.abbr.trim()
                        }?.distinct())
                        onRenderGroupItem(findBean?.groups)
                    }else{
                        requireContext()?.let {context ->
                            LoadingUtil.showToast(context,getString(R.string.try_again))
                        }
                    }
                })
            }?: kotlin.run {
                swipeRefreshLayout.isRefreshing = false
                hotArea1.visibility = View.GONE
                requireContext()?.let {
                    AlertDialog.Builder(it)
                            .setMessage(R.string.add_device1)
                            .setPositiveButton(android.R.string.ok,null)
                            .show()
                }
            }
        }
    }


    /**
     * 渲染banner
     * @param banners
     */
    private fun onRenderBanner(banners:List<BannerBean>?){
        banners?.let {
            banner.adapter = object: BannerAdapter<BannerBean, NetImageHolder>(it) {
                override fun onCreateHolder(parent: ViewGroup, viewType: Int): NetImageHolder {
                    val itemView = LayoutInflater.from(SmartApp.app).inflate(R.layout.layout_banner_image,parent,false)
                    val params = ViewGroup.MarginLayoutParams(-1,-1)
                    params.marginStart = BannerUtils.dp2px(10f).toInt()
                    params.marginEnd = BannerUtils.dp2px(10f).toInt()
                    itemView.layoutParams = params
                    return NetImageHolder(itemView)
                }
                override fun onBindView(holder: NetImageHolder, data: BannerBean, position: Int, size: Int) {
                    holder.invalidate(data)
                }
            }
            banner.setLoopTime(8000)
            banner.indicator = RoundLinesIndicator(context)
            banner.setIndicatorSelectedWidth(BannerUtils.dp2px(16f).toInt())
        }
    }

    /**
     * 渲染abbr
     * @param abbrs
     */
    private fun onRenderIndicator(abbrs:List<String>?){
        abbrs?.let {
            val commonNavigator = CommonNavigator(context)
            commonNavigator.adapter = object :CommonNavigatorAdapter(){
                override fun getTitleView(context: Context, index: Int): IPagerTitleView {
                    val simplePagerTitleView: SimplePagerTitleView = ColorTransitionPagerTitleView(context)
                    simplePagerTitleView.normalColor = Color.GRAY
                    simplePagerTitleView.selectedColor = Color.BLACK
                    simplePagerTitleView.textSize = 15f
                    simplePagerTitleView.text = abbrs[index]
                    simplePagerTitleView.setOnClickListener {
                        scrollView.smoothScrollTo(0,getScrollPosition(abbrs[index]))
                    }
                    return simplePagerTitleView
                }

                override fun getCount(): Int {
                    return abbrs.size
                }

                override fun getIndicator(context: Context?): IPagerIndicator {
                    val linePagerIndicator = LinePagerIndicator(context)
                    linePagerIndicator.mode = LinePagerIndicator.MODE_EXACTLY
                    linePagerIndicator.lineWidth = UIUtil.dip2px(context, 16.0).toFloat()
                    linePagerIndicator.setColors(resources.getColor(R.color.smartwasp_blue))
                    return linePagerIndicator
                }
            }
            val fragmentContainerHelper = FragmentContainerHelper(magic_indicator4)
            fragmentContainerHelper.setInterpolator(OvershootInterpolator(2.0f))
            fragmentContainerHelper.setDuration(300)
            magic_indicator4.navigator = commonNavigator
            magic_indicator4.setTag(R.id.extra_tag,abbrs)
            magic_indicator4.setTag(R.id.global_container_id,fragmentContainerHelper)
        }
    }

    /**
     * 渲染子item
     * @param groups
     */
    private fun onRenderGroupItem(groups: List<GroupBean>?) {
        groups?.let {groupBean->
            groupsAdapter = mutableListOf()
            item_container.removeAllViews()
            groupBean.forEach {itemBean->
                val groupView = LayoutInflater.from(context).inflate(R.layout.layout_find_group,item_container,false)
                item_container.addView(groupView)
                groupsAdapter!!.add(GroupViewHolder(itemBean,groupView))
            }
        }
    }private var groupsAdapter:MutableList<GroupViewHolder>? = null

    /**
     * 获取滚动到的位置
     * @param abbr 滚动的位置
     */
    private fun getScrollPosition(abbr:String):Int{
        val layoutParams: CoordinatorLayout.LayoutParams = appBarLayout.layoutParams as CoordinatorLayout.LayoutParams
        val behavior: AppBarLayout.Behavior = layoutParams.behavior as AppBarLayout.Behavior
        val y = linearLayout.top
        behavior.topAndBottomOffset = -y
        groupsAdapter?.forEach {
            if(it.header == abbr){
                return it.top
            }
        }
        return 0
    }

    /**
     * 获取Indicator指示器位置
     * @param position scrollView滑动的位置
     */
    private fun getAbbrIndex(position:Int):Int{
        val abbrs = magic_indicator4.getTag(R.id.extra_tag) as List<String>
        groupsAdapter?.forEachIndexed { _, groupViewHolder ->
            if(scrollView.measuredHeight >= item_container.measuredHeight){
                return 0
            }
            val offsetHeight = abs(item_container.measuredHeight - scrollView.measuredHeight)
            if(position >= offsetHeight){
                return abbrs.size - 1
            }
            if(groupViewHolder.top + groupViewHolder.height > position){
                return abbrs.indexOf(groupViewHolder.header).coerceAtLeast(0)
            }
        }
        return 0
    }

    /**
     * 进入歌单
     * @param itemBean 歌单数据
     */
    private fun onItemChanged(itemBean: ItemBean){
        //todo 回退键回退stack无效
        addFragmentByTag(R.id.container,MusicItemFragment.newsInstance(itemBean))
    }

    /**
     * banner网络图片holder
     * @param itemView 视图
     */
    inner class NetImageHolder(itemView:View): RecyclerView.ViewHolder(itemView){
        private lateinit var bean:BannerBean
        init {
            itemView.setOnClickListener {
                SmartApp.activity?.currentDevice?.music?.let {
                    startActivity(Intent(requireActivity(), WebViewActivity::class.java).apply {
                        putExtra(IFLYOS.EXTRA_URL, bean?.url)
                        putExtra(IFLYOS.EXTRA_TYPE, IFLYOS.TYPE_PAGE)
                    })
                }
            }
        }
        fun invalidate(data: BannerBean){
            bean = data
            Glide.with(itemView)
                    .load(data.image)
                    .dontAnimate()
                    .into(itemView as BezelImageView)
        }
    }

    /**
     * 歌单组视图holder
     */
    inner class GroupViewHolder(private var bean:GroupBean,private var itemView:View){
        private var itemViewBinding: LayoutFindGroupBinding? = null
        init {
            itemViewBinding = DataBindingUtil.bind(itemView)
            itemViewBinding?.groupBean = bean
            onRender()
        }
        var top:Int = 0
            get() {
                return itemView.top
            }
            private set
        var height:Int = 0
        get() {
            return itemView.measuredHeight
        }
        private set
        var header:String = ""
            get() {
                return bean.abbr
            }
            private set
        private fun onRender(){
            itemViewBinding?.tvMore?.setOnClickListener {
                LoadingUtil.showToast(requireContext(),getString(R.string.un_open))
            }
            val recyclerView:RecyclerView? = itemViewBinding?.groupList
            recyclerView?.let {
                recyclerView.layoutManager = object:GridLayoutManager(context,3){
//                    override fun canScrollVertically(): Boolean {
//                        return false
//                    }
                }
                recyclerView.adapter = object:RecyclerView.Adapter<ItemViewHolder>(){
                    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
                        val itemView = LayoutInflater.from(SmartApp.app).inflate(R.layout.layout_find_item,parent,false)
                        itemView.layoutParams.width = UIUtil.getScreenWidth(context) / 3
                        return ItemViewHolder(itemView)
                    }
                    override fun getItemCount(): Int {
                        return bean.items.size
                    }
                    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
                        holder.invalidate(bean.items[position])
                    }
                }
                try {
                    recyclerView.removeItemDecorationAt(0)
                }catch (e:Throwable){}
                recyclerView.addItemDecoration(object :RecyclerView.ItemDecoration(){
                    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                        val pos = parent.getChildAdapterPosition(view)
                        if(pos >= 3){
                            outRect.top = 30
                        }
                    }
                })
            }
        }
    }

    /**
     * group 中itemholder
     * @param itemView 视图
     */
    inner class ItemViewHolder(itemView:View): RecyclerView.ViewHolder(itemView){
        private var itemViewBinding: LayoutFindItemBinding? = null
        init {
            itemViewBinding = DataBindingUtil.bind(itemView)
            itemViewBinding?.hotArea?.setOnClickListener {
                data?.let {
                    onItemChanged(it)
                }
            }
        }
        private var data:ItemBean? = null
        fun invalidate(data: ItemBean){
            this.data = data
            itemViewBinding?.bezelImageView?.let {
                Glide.with(itemView)
                        .load(data.image)
                        .dontAnimate()
                        .error(R.mipmap.ic_warning_black_24dp)
                        .into(it)
            }
            itemViewBinding?.tvTitle?.text = data.name
        }
    }
}
