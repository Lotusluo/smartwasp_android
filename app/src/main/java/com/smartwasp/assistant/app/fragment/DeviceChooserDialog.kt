package com.smartwasp.assistant.app.fragment

import android.app.Dialog
import android.os.Bundle
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
import com.smartwasp.assistant.app.bean.DeviceBean
import com.smartwasp.assistant.app.bean.test.BindDevices
import com.smartwasp.assistant.app.databinding.LayoutDeviceItemBinding
import com.smartwasp.assistant.app.databinding.LayoutDeviceItemBottomBinding
import com.smartwasp.assistant.app.util.IFLYOS
import kotlinx.android.synthetic.main.fragment_bottom_sheet.*

/**
 * Created by luotao on 2021/1/14 16:13
 * E-Mail Address：gtkrockets@163.com
 */
class DeviceChooserDialog private constructor(): BottomSheetDialogFragment() {
    companion object{
        fun newsInstance(devices: BindDevices):DeviceChooserDialog{
            val deviceChooserDialog = DeviceChooserDialog()
            deviceChooserDialog.arguments = Bundle().apply {
                putSerializable(IFLYOS.EXTRA,devices)
            }
            return deviceChooserDialog
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState)
    }

    /**
     * 生成
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.CommonWindowStyle)
    }

    /**
     * 产生布局
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_bottom_sheet,container)
    }

    /**
     * 视图呈现
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onRenderDevices()
    }

    /**
     * 渲染可选的设备
     */
    private fun onRenderDevices() {
        (arguments?.getSerializable(IFLYOS.EXTRA) as BindDevices?)?.let {
            recyclerView.layoutManager = LinearLayoutManager(this.context)
            recyclerView.adapter = object :RecyclerView.Adapter<DeviceViewHolder>(){
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
                    return DeviceViewHolder(LayoutInflater.from(this@DeviceChooserDialog.context).
                    inflate(R.layout.layout_device_item_bottom,parent,false))
                }

                override fun getItemCount(): Int {
                    return it.user_devices?.size ?: 0
                }

                override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
                    it.user_devices?.let {lists->
                        holder.invalidate(lists[position])
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
    fun resize(){
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

    /**
     * 绑定设备的ViewHolder
     * @param itemView 渲染视图
     */
    inner class DeviceViewHolder(itemView:View):
            RecyclerView.ViewHolder(itemView){
        private lateinit var deviceBean: DeviceBean
        private var itemViewBinding: LayoutDeviceItemBottomBinding? = null
        init {
            itemViewBinding = DataBindingUtil.bind(itemView)
            itemViewBinding?.deviceBean = null
            itemView.setOnClickListener {
                Logger.e(deviceBean.device_id)
            }
        }
        fun invalidate(data: DeviceBean){
            deviceBean = data
            //绑定的设备渲染
            itemViewBinding?.let {
                it.deviceBean = data
                it.deviceName.isSelected = data.isOnline()
                it.iconDeviceStatus.isSelected = data.isOnline()
            }
        }
    }
}