package com.smartwasp.assistant.app.fragment

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import androidx.core.graphics.drawable.DrawableCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.orhanobut.logger.Logger
import com.smartwasp.assistant.app.R
import com.smartwasp.assistant.app.base.addFragmentByTag
import com.smartwasp.assistant.app.bean.GroupBean
import com.smartwasp.assistant.app.bean.ItemBean
import com.smartwasp.assistant.app.databinding.FragmentMoreItemBinding
import com.smartwasp.assistant.app.databinding.LayoutFindItemBinding
import com.smartwasp.assistant.app.databinding.LayoutMoreBinding
import com.smartwasp.assistant.app.databinding.LayoutMoreItemBinding
import com.smartwasp.assistant.app.util.SimpleRecyclerHelper
import com.smartwasp.assistant.app.util.StatusBarUtil
import com.smartwasp.assistant.app.util.isA
import com.smartwasp.assistant.app.viewModel.MusicModel
import kotlinx.android.synthetic.main.fragment_find.*
import kotlinx.android.synthetic.main.fragment_find.magic_indicator4
import kotlinx.android.synthetic.main.fragment_more_item.*
import net.lucode.hackware.magicindicator.FragmentContainerHelper
import net.lucode.hackware.magicindicator.buildins.UIUtil
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.ColorTransitionPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.SimplePagerTitleView

/**
 * Created by luotao on 2021/2/24 12.03
 * E-Mail Address：gtkrockets@163.com
 */
class MusicMoreFragment private constructor(var groupBean: GroupBean):
        MainChildFragment<MusicModel, FragmentMoreItemBinding>() {
    //布局文件
    override val layoutResID:Int = R.layout.fragment_more_item

    companion object{
        fun newsInstance(groupBean: GroupBean):MusicMoreFragment{
            return MusicMoreFragment(groupBean)
        }
    }

    /**
     * 创建视图
     * @param view
     * @param savedInstanceState
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initHeader()
    }

    /**
     * 入场动画完成
     */
    override fun onTransitDone(){
        super.onTransitDone()
        mBinding.toolbar.title = groupBean.name
        //拆分标题
        onRenderIndicator(groupBean.items?.map{bean->
            bean.category_name
        }?.distinct())
        onRenderViewPager2()
    }

    /**
     * 渲染ViewPager
     */
    private fun onRenderViewPager2() {
        with(mBinding.viewpager2){
            registerOnPageChangeCallback(object:ViewPager2.OnPageChangeCallback(){
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    val helper = magic_indicator4.getTag(R.id.global_container_id) as FragmentContainerHelper?
                    helper?.let {
                        it.handlePageSelected(position)
                    }
                }
            })
            val viewPager2Adapter = ViewPager2Adapter()
            offscreenPageLimit = 2
            adapter = viewPager2Adapter
        }
    }

    //列表适配器
    inner class ViewPager2Adapter : RecyclerView.Adapter<RecyclerViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
            return RecyclerViewHolder(RecyclerView(parent.context).apply {
                layoutParams = RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT,RecyclerView.LayoutParams.MATCH_PARENT)
                layoutManager = LinearLayoutManager(parent.context)
            })
        }

        override fun getItemCount(): Int {
            return abbrs?.let { it.size } ?: 0
        }

        /**
         * 绑定数据
         * @param holder
         * @param position
         */
        override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
            val abbr = abbrs!![position]
            holder.setData(groupBean.items?.filter {
                it.category_name == abbr
            })
        }
    }

    //RecyclerView列表holder
    inner class RecyclerViewHolder(itemView:RecyclerView) : RecyclerView.ViewHolder(itemView){
        private val adapter = ItemHolderAdapter()
        init {
            itemView.adapter = adapter
        }
        /**
         * 设置数据
         * @param itemBeans
         */
        internal fun setData(itemBeans:List<ItemBean>){
            adapter.itemBeans = itemBeans
        }
    }

    /**
     * 数据RecyclerView列表holder适配器
     */
    inner class ItemHolderAdapter:RecyclerView.Adapter<ItemViewHolder>(){
        //数据
        internal var itemBeans:List<ItemBean>? = null
            set(value) {
                notifyDataSetChanged()
                field = value
            }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_more_item,parent,false)
            return ItemViewHolder(view)
        }

        override fun getItemCount(): Int {
            return itemBeans?.let { it.size } ?: 0
        }

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            holder.invalidate(itemBeans!![position],position + 1)
        }
    }

    /**
     * group 中itemholder
     * @param itemView 视图
     */
    inner class ItemViewHolder(itemView:View): RecyclerView.ViewHolder(itemView){
        private var itemViewBinding: LayoutMoreItemBinding? = null
        init {
            itemViewBinding = DataBindingUtil.bind(itemView)
            itemViewBinding?.hotArea?.setOnClickListener {
                data?.let {
                    addFragmentByTag(R.id.container,MusicItemFragment.newsInstance(data!!))
                }
            }
        }
        private var data:ItemBean? = null
        fun invalidate(data: ItemBean, position: Int){
            this.data = data
            itemViewBinding?.bean2Pos = position.toString()
            itemViewBinding?.bean2 = data
        }
    }

    /**
     * 准备header
     */
    private fun initHeader(){
        with(mBinding.toolbar){
            val icBack = resources.getDrawable(R.drawable.ic_navback)
            DrawableCompat.setTint(icBack,resources.getColor(R.color.smartwasp_orange))
            navigationIcon = icBack
            (layoutParams as ViewGroup.MarginLayoutParams).topMargin = StatusBarUtil.getStatusBarHeight(requireContext())
            setNavigationOnClickListener {
                parentFragmentManager.popBackStack()
            }
        }
    }

    /**
     * 渲染abbr
     * @param abbrs
     */
    private fun onRenderIndicator(abbrs:List<String>?){
        this.abbrs = abbrs
        this.abbrs?.let {
            val commonNavigator = CommonNavigator(context)
            commonNavigator.isSkimOver = true
            commonNavigator.adapter = object : CommonNavigatorAdapter(){
                override fun getTitleView(context: Context, index: Int): IPagerTitleView {
                    val simplePagerTitleView: SimplePagerTitleView = ColorTransitionPagerTitleView(context)
                    simplePagerTitleView.normalColor = Color.GRAY
                    simplePagerTitleView.selectedColor = Color.BLACK
                    simplePagerTitleView.textSize = 15f
                    simplePagerTitleView.text = it[index]
                    simplePagerTitleView.setOnClickListener {
                        viewpager2.currentItem = abbrs?.indexOf(simplePagerTitleView.text!!) ?: 0
                    }
                    return simplePagerTitleView
                }

                override fun getCount(): Int {
                    return it.size
                }

                override fun getIndicator(context: Context?): IPagerIndicator {
                    val linePagerIndicator = LinePagerIndicator(context)
                    linePagerIndicator.mode = LinePagerIndicator.MODE_EXACTLY
                    linePagerIndicator.lineWidth = UIUtil.dip2px(context, 16.0).toFloat()
                    linePagerIndicator.setColors(resources.getColor(R.color.smartwasp_orange))
                    return linePagerIndicator
                }
            }
            val fragmentContainerHelper = FragmentContainerHelper(magic_indicator4)
            fragmentContainerHelper.setInterpolator(OvershootInterpolator(2.0f))
            fragmentContainerHelper.setDuration(300)
            magic_indicator4.navigator = commonNavigator
            magic_indicator4.setTag(R.id.extra_tag,it)
            magic_indicator4.setTag(R.id.global_container_id,fragmentContainerHelper)
        }
    }
    //标题
    private var abbrs:List<String>? = null

    /**
     * 按钮点击
     */
    override fun onButtonClick(v: View) {
        super.onButtonClick(v)

    }
}