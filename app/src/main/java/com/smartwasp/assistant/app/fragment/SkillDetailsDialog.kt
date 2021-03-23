package com.smartwasp.assistant.app.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.orhanobut.logger.Logger
import com.smartwasp.assistant.app.R
import com.smartwasp.assistant.app.activity.DeviceSetActivity
import com.smartwasp.assistant.app.bean.PayType
import com.smartwasp.assistant.app.bean.SkillDetailBean
import com.smartwasp.assistant.app.bean.test.BaseBean
import com.smartwasp.assistant.app.databinding.LayoutDeviceItemBottom1Binding
import com.smartwasp.assistant.app.util.IFLYOS
import kotlinx.android.synthetic.main.fragment_bottom_sheet.*
import kotlinx.android.synthetic.main.fragment_bottom_sheet.recyclerView
import kotlinx.android.synthetic.main.fragment_bottom_sheet.sheet_cancel_btn
import kotlinx.android.synthetic.main.fragment_bottom_sheet1.*

/**
 * Created by luotao on 2021/3/23 10:17
 * E-Mail Address：gtkrockets@163.com
 */
class SkillDetailsDialog private constructor(): BottomSheetDialogFragment() {
    companion object{
        fun newsInstance(data: BaseBean<List<SkillDetailBean>>):SkillDetailsDialog{
            val skillDetailsDialog = SkillDetailsDialog()
            skillDetailsDialog.arguments = Bundle().apply {
                putSerializable(IFLYOS.EXTRA,data)
            }
            return skillDetailsDialog
        }
    }

    /**
     * 生成
     * @param savedInstanceState
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.CommonWindowStyle_BottomSheet)
        isCancelable = false
    }

    /**
     * 产生布局
     * @param inflater
     * @param container
     * @param savedInstanceState
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_bottom_sheet1,container)
    }

    /**
     * 视图呈现
     * @param view
     * @param savedInstanceState
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onRenderDevices()
        //取消
        sheet_cancel_btn.setOnClickListener {
            dismissAllowingStateLoss()
        }
        //确定支付
        sheet_confirm_btn.setOnClickListener {
            details?.get(selectedID)?.let {bean->
                val str = String.format(getString(R.string.pay_tip),
                        bean.shopName,
                        getString(if(wechatPay1.isChecked) R.string.wechat1 else R.string.alipay1),
                        bean.price)
                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle(R.string.tip)
                        .setMessage(str)
                        .setNegativeButton(android.R.string.cancel,null)
                        .setPositiveButton(android.R.string.ok){_,_->
                            (activity as DeviceSetActivity).onPay(
                                    bean,
                                    if(wechatPay1.isChecked) PayType.WXPAY else PayType.ALIPAY)
                            dismissAllowingStateLoss()
                        }
                        .show()
            }
        }
        wechatPay1.setChecked(true,true)
        //微信支付
        wechatPay.setOnClickListener {
            if(aliPay1.isChecked)aliPay1.setChecked(false,true)
            wechatPay1.setChecked(true,true)
        }
        //阿里支付
        aliPay.setOnClickListener {
            aliPay1.setChecked(true,true)
            if(wechatPay1.isChecked)wechatPay1.setChecked(false,true)
        }
    }

    private var details:List<SkillDetailBean>? = null
    /**
     * 渲染可选的设备
     */
    private fun onRenderDevices() {
        (arguments?.getSerializable(IFLYOS.EXTRA) as BaseBean<List<SkillDetailBean>>?)?.let {
            details = it.data
            recyclerView.layoutManager = LinearLayoutManager(this.context)
            recyclerView.adapter = object :RecyclerView.Adapter<DeviceViewHolder>(){
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
                    return DeviceViewHolder(LayoutInflater.from(this@SkillDetailsDialog.context).
                    inflate(R.layout.layout_device_item_bottom1,parent,false))
                }
                override fun getItemCount(): Int {
                    return details?.size ?: 0
                }
                override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
                    details?.let {
                        holder.invalidate(details!![position])
                    }
                }
            }
        }
    }

    /**
     * 修改Window
     */
    override fun onResume() {
        super.onResume()
        resize()
    }

    /**
     * 重新定位
     */
    private fun resize(){
        (dialog as? BottomSheetDialog?)?.let {
            val bottomSheet = it.delegate.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.layoutParams?.height = ViewGroup.LayoutParams.WRAP_CONTENT
        }
        val view = requireView()
        view.post {
            val parent = view.parent as View
            val params = parent.layoutParams as CoordinatorLayout.LayoutParams
            val behavior = params.behavior
            val bottomSheetBehavior = behavior as BottomSheetBehavior<ViewGroup>?
            params.height = CoordinatorLayout.LayoutParams.WRAP_CONTENT
            bottomSheetBehavior?.let {
                it.peekHeight = view.measuredHeight
            }
        }
    }

    //当前选择的商品index
    private var selectedID:Int = 0

    /**
     * 绑定设备的ViewHolder
     * @param itemView 渲染视图
     */
    inner class DeviceViewHolder(itemView:View):
            RecyclerView.ViewHolder(itemView){
        private lateinit var skillDetailBean: SkillDetailBean
        private var itemViewBinding: LayoutDeviceItemBottom1Binding? = null
        init {
            itemViewBinding = DataBindingUtil.bind(itemView)
            itemViewBinding?.skillDetailBean = null
            itemView.setOnClickListener {
                val tempID = selectedID
                selectedID = adapterPosition
                (recyclerView.findViewHolderForLayoutPosition(tempID) as DeviceViewHolder?)?.invalidate()
                invalidate()
            }
        }
        fun invalidate(data: SkillDetailBean){
            skillDetailBean = data
            //绑定的设备渲染
            itemViewBinding?.let {
                it.skillDetailBean = data
                if(it.checkBox.isChecked != (selectedID == adapterPosition)){
                    it.checkBox?.setChecked(selectedID == adapterPosition,true)
                }
            }
        }
        private fun invalidate(){
            invalidate(skillDetailBean)
        }
    }
}