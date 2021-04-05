package com.smartwasp.assistant.app.fragment

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import com.smartwasp.assistant.app.R
import com.smartwasp.assistant.app.base.BaseFragment
import com.smartwasp.assistant.app.bean.PayRecordBean
import com.smartwasp.assistant.app.databinding.FragmentPayListBinding
import com.smartwasp.assistant.app.databinding.LayoutPayItemBinding
import com.smartwasp.assistant.app.util.SimpleRecyclerHelper
import com.smartwasp.assistant.app.viewModel.PayRecordModel
import kotlinx.android.synthetic.main.fragment_pay_list.*
import kotlinx.android.synthetic.main.layout_device_header.*

/**
 * Created by luotao on 2021/4/5 10:37
 * E-Mail Address：gtkrockets@163.com
 */
class PayRecordFragment private constructor(): BaseFragment<PayRecordModel, FragmentPayListBinding>(){
    //布局文件
    override val layoutResID:Int = R.layout.fragment_pay_list

    companion object{
        fun newsInstance():PayRecordFragment{
            return PayRecordFragment()
        }
    }

    /**
     * 创建视图
     * @param view
     * @param savedInstanceState
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTittle(getString(R.string.usrPay))
        setToolBarIcon(R.drawable.ic_navback,R.color.smartwasp_dark)
        media_icon?.visibility = View.GONE
    }

    /**
     * 入场动画完成
     */
    override fun onTransitDone(){
        super.onTransitDone()
        simpleRecyclerHelper?: kotlin.run {
            initRecyclerView()
        }
    }

    /**
     * 初始化列表控件
     */
    private fun initRecyclerView(){
        simpleRecyclerHelper = SimpleRecyclerHelper(recyclerView,R.layout.layout_pay_item,::PayViewHolder)
        with(simpleRecyclerHelper!!){
            onRequest { page, limit, isRefresh ->
                mViewModel?.getPaysData(page,limit)?.observe(this@PayRecordFragment, Observer { result->
                    if(result.isSuccess){
                        addMore(isRefresh,page,result.getOrNull()?.data)
                    }else{
                        addMoreErr(isRefresh,page)
                    }
                })
            }
            loadMoreInit()
        }
    }private var simpleRecyclerHelper:SimpleRecyclerHelper<PayRecordBean,LayoutPayItemBinding,PayViewHolder>? = null


    /**
     * 支付记录列表控件
     * @param itemView
     */
    inner class PayViewHolder(itemView:View):
            SimpleRecyclerHelper.SimpleViewHolder<PayRecordBean, LayoutPayItemBinding>(itemView) {
        init {
            itemView.setOnClickListener {
                AlertDialog.Builder(requireContext())
                        .setTitle(R.string.tip)
                        .setMessage(bean!!.getMessage())
                        .setPositiveButton(android.R.string.ok,null)
                        .show()
            }
        }
        override fun onDataChanged(compare: PayRecordBean?) {
            itemViewBinding?.let {
                it.bean2 = bean
                it.bean2Pos = (pos + 1).toString()
            }
        }
    }
}