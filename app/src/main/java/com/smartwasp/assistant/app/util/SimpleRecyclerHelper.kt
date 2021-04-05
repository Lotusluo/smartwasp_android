package com.smartwasp.assistant.app.util

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.orhanobut.logger.Logger
import com.smartwasp.assistant.app.R
import com.smartwasp.assistant.app.databinding.LayoutMoreBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

/**
 * Created by luotao on 2021/1/20 13:59
 * E-Mail Address：gtkrockets@163.com
 */
class SimpleRecyclerHelper<
        T,
        VM:ViewDataBinding,
        VH:SimpleRecyclerHelper.SimpleViewHolder<
                T,
                VM>>(var recyclerView: RecyclerView,
                     var layoutRes:Int,
                     var creator: (View) -> VH) {

    companion object{
        //请求一页的数量
        val ONE_PAGE_SIZE = 10
        val FIRST_PAGE = 2
    }
    //当前页
    private var currentPage:Int = 0
    //请求的页
    private var preRequestPage:Int = 0
    //请求函数
    private var requestFun : ((page:Int,limit:Int,isRefresh:Boolean) -> Unit)? = null
    //设置监听
    fun onRequest(block : ((page:Int,limit:Int,isRefresh:Boolean) -> Unit)){
        requestFun = block
    }

    //类型枚举
    enum class HeaderType(var value:Int){
        NORMAL(1),
        MORE(2)
    }
    /**
     * 适配器
     */
    private val adapter = object :RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        override fun getItemViewType(position: Int): Int {
            return if (position == dataBeans.size) HeaderType.MORE.value else HeaderType.NORMAL.value
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val mLayoutRes = if(viewType == HeaderType.MORE.value) R.layout.layout_more else layoutRes
            val view = LayoutInflater.from(recyclerView.context).inflate(mLayoutRes,parent,false)
            return if(viewType == HeaderType.MORE.value) MoreViewHolder(view) else creator!!(view)
        }
        override fun getItemCount(): Int {
            return dataBeans.size + 1
        }
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if(dataBeans.size > position && isA<SimpleViewHolder<*,*>>(holder)){
                (holder as SimpleViewHolder<T,*>).invalidate(dataBeans[position],position,selectedBean)
                return
            }
            if(noMore && isA<MoreViewHolder>(holder)){
                onUpdateMoreStyle()
            }
        }
    }

    //数据列表
    private var dataBeans:MutableList<T?> = mutableListOf()
    //初始化RecyclerView
    init {
        recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
        //监听最后一个子集的出现
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                var manager = recyclerView.layoutManager as LinearLayoutManager
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    var lastVisibleItem = manager.findLastCompletelyVisibleItemPosition()
                    var totalItemCount = manager.itemCount
                    if (lastVisibleItem == (totalItemCount - 1)) {
                        loadMore()
                    }
                }
            }
        })
        recyclerView.setHasFixedSize(true)
    }

    /**
     * 初始化加载
     */
    fun loadMoreInit(){
        if(null == recyclerView.adapter){
            recyclerView.adapter = adapter
        }
        requestFun?.invoke(1,FIRST_PAGE * ONE_PAGE_SIZE,true)
    }

    /**
     * 通知数据改变
     * @param bean 如果为空 则表示从第一个数据开始
     */
    fun notifyDataChanged(bean:T? = null){
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager?
        selectedBean = bean
        if(null == selectedBean && dataBeans.size > 0){
            selectedBean = dataBeans[0]
        }
        layoutManager?.let {
            val firstIndex = it.findFirstVisibleItemPosition()
            val lastIndex = it.findLastVisibleItemPosition()
            for (index in firstIndex..lastIndex){
                val holder = recyclerView.findViewHolderForAdapterPosition(index)
                holder?.let {holder->
                    (holder as? SimpleViewHolder<T,*> )?.onDataChanged(bean)
                }
            }
        }
    }private var selectedBean:T? = null

    /**
     * 加载下一页
     */
    fun loadMore(){
        if(preRequestPage > 0)
            return
        if(noMore)
            return
        preRequestPage = currentPage + 1
        requestFun?.invoke(preRequestPage,ONE_PAGE_SIZE,false)
    }

    /**
     * 回调加载的数据
     * @param isRefresh 是否是刷新
     * @param requestPage 回调请求的页数
     * @param songs 列表，为空表示没有requestPage这一页
     */
    fun addMore(isRefresh:Boolean,requestPage:Int,songs:List<T>?){
        currentPage = if(isRefresh){
            //刷新
             if(null == songs || songs!!.size <= ONE_PAGE_SIZE) 1 else FIRST_PAGE
        }else{
            //加载更多
            if(preRequestPage <= 0)
                return //当前没有请求
            if(preRequestPage != requestPage)
                return //请求混乱
            requestPage
        }
        preRequestPage = 0
        noMore = false
        var changedItem = (dataBeans?.size).coerceAtLeast(0)
        if(songs.isNullOrEmpty()){
            onUpdateMoreStyle()
            return
        }else{
            dataBeans.addAll(songs)
            adapter.notifyItemRangeChanged(changedItem,songs.size)
            if(songs.size % ONE_PAGE_SIZE != 0){
                onUpdateMoreStyle()
            }
        }
    }

    /**
     * 加载更多页失败
     * @param isRefresh 是否是初次加载
     * @param requestPage 请求的页数
     */
    fun addMoreErr(isRefresh:Boolean,requestPage:Int){
        if(!isRefresh && requestPage != preRequestPage)
            return
        if(isRefresh){
            AlertDialog.Builder(recyclerView.context)
                    .setMessage(R.string.err_)
                    .setPositiveButton(android.R.string.ok,null)
                    .show()
        }
        preRequestPage = 0
    }

    //是否有更多页
    private var noMore:Boolean = false

    /**
     * 设置加载更多的样式
     */
    private fun onUpdateMoreStyle(){
        AppExecutors.get().mainThread().executeDelay(Runnable {
            if(recyclerView.isAttachedToWindow){
                val loadMoreHolder = recyclerView.findViewHolderForAdapterPosition(dataBeans.size) as MoreViewHolder?
                loadMoreHolder?.let {
                    loadMoreHolder?.updateUI(true)
                }
            }
        },200)
    }

    /**
     * 视图holder基类
     */
    abstract class SimpleViewHolder<T,VM:ViewDataBinding>(itemView: View):
            RecyclerView.ViewHolder(itemView){
        protected var itemViewBinding: VM? = null
        protected var bean:T? = null
        protected var pos:Int = 0
        init {
            itemViewBinding = DataBindingUtil.bind(itemView)
        }
        /**
         * 渲染数据
         * @param data
         * @param position
         * @param compare
         * 数据为空并且viewType为MORE的情况为more控件
         */
        internal fun invalidate(data: T?,position: Int,compare:T?=null){
            bean = data
            pos = position
            onDataChanged(compare)
        }

        abstract fun onDataChanged(compare:T?)
    }

    class MoreViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        private var itemViewBinding:LayoutMoreBinding? =  DataBindingUtil.bind(itemView)
        fun updateUI(boolean: Boolean){
            itemViewBinding?.noMore = boolean
        }
    }
}

//判断泛型
inline fun <reified T> isA(value: Any) = value is T