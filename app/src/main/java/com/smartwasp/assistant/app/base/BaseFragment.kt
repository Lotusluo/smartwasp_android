package com.smartwasp.assistant.app.base

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import androidx.core.graphics.drawable.DrawableCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProviders
import com.orhanobut.logger.Logger
import com.smartwasp.assistant.app.BR
import com.smartwasp.assistant.app.R
import kotlinx.coroutines.cancel
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.lang.reflect.ParameterizedType

/**
 * Created by luotao on 2021/1/9 11:44
 * E-Mail Address：gtkrockets@163.com
 */
abstract class BaseFragment<
        VM: BaseViewModel,
        BD: ViewDataBinding>:Fragment(), EasyPermissions.PermissionCallbacks, View.OnClickListener {
    //与Fragment绑定的唯一ViewModel
    protected lateinit var mViewModel:VM
    //与Fragment绑定的唯一ViewDataBinding
    protected lateinit var mBinding:BD

    //布局文件
    protected open val layoutResID:Int = 0

    /**
     * 通过反射加载mViewModel
     */
    private fun createModelView(){
        var mModelClass = when(val type = this::class.java.genericSuperclass){
            is ParameterizedType ->{
                (type.actualTypeArguments[0]) as Class<VM>
            }else -> BaseViewModel::class.java
        }
        mModelClass.let {
            mViewModel = ViewModelProviders.of(this).get(it as Class<VM>)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        createModelView()
        Logger.d("onAttach")
    }

    override fun onStart() {
        super.onStart()
        Logger.d("onStart")
    }

    override fun onResume() {
        super.onResume()
        Logger.d("onResume")
    }

    override fun onPause() {
        super.onPause()
        Logger.d("onPause")
    }

    override fun onStop() {
        super.onStop()
        Logger.d("onStop")
    }

    override fun onDetach() {
        super.onDetach()
        Logger.d("onDetach")
    }

    override fun onDestroy() {
        super.onDestroy()
        Logger.d("onDestroy")
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Logger.d("onActivityCreated")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Logger.d("onCreateView")
        mBinding = DataBindingUtil.inflate(inflater, layoutResID,container,false)
        mBinding.lifecycleOwner = this
        mBinding.setVariable(BR.onclickListener,this)
        return mBinding.root
    }

    /**
     * 设置标题
     * @param tittle 标题字符串
     */
    fun setTittle(tittle:String){
        mBinding.setVariable(BR.tittle,tittle)
    }

    /**
     * 设置右侧按钮图标
     * @param resID 资源ID
     * @param position 图标位置
     * @param tiny 渲染样式
     */
    fun setToolBarIcon(resID:Int,position:Int = 1,tiny:Int = 0){
        val icon = if(resID > 0) resources.getDrawable(resID) else null
        if(tiny > 0 && null != icon){
            DrawableCompat.setTint(icon,tiny)
        }
        when(position){
            1->{
                mBinding.setVariable(BR.leftIcon,icon)
            }
            2->{
                mBinding.setVariable(BR.rightIcon,icon)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.d("onCreate")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            requireView().setOnApplyWindowInsetsListener { _, insets ->
                //状态栏
                val statusBars = insets.getInsets(WindowInsets.Type.statusBars())
                requireView().findViewById<View>(R.id.topInset)?.let {
                    it.layoutParams.height = statusBars.bottom - statusBars.top
                }
                //导航栏
                val navigationBars = insets.getInsets(WindowInsets.Type.navigationBars())
                //键盘
                val ime = insets.getInsets(WindowInsets.Type.ime())
                insets
            }
        }else{
            val resource = requireActivity().resources
            val resourceId = resource.getIdentifier("status_bar_height","dimen", "android")
            requireView().findViewById<View>(R.id.topInset)?.let {
                it.layoutParams.height = resource.getDimensionPixelOffset(resourceId)
            }
        }
        Logger.d("onViewCreated")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Logger.d("onDestroyView")
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
    }

    /**
     * 控件点击
     */
    final override fun onClick(v: View){
        when(v.id){
            R.id.toolbar_right_icon,
            R.id.toolbar_left_icon ->{
                onToolbarIconClick(v)
            }
            else ->{
                onButtonClick(v)
            }
        }
    }

    /**
     * 其他控件点击
     */
    open fun onButtonClick(v: View){

    }

    /**
     * 右侧按钮点击
     */
    open fun onToolbarIconClick(v: View){
        when(v.id){
            R.id.toolbar_left_icon->{
                if(!interceptLeftButton()){
                    Logger.e("fragment back!")
                }
            }
        }
    }

    /**
     * 是否拦截左侧按钮
     */
    open fun interceptLeftButton():Boolean{
        return false
    }

    /**
     * 权限禁止
     */
    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {

    }

    /**
     * 权限允许
     */
    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        /**
         * 若是在权限弹窗中，用户勾选了'NEVER ASK AGAIN.'或者'不在提示'，且拒绝权限。
         * 这时候，需要跳转到设置界面去，让用户手动开启。
         */
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog
                .Builder(this)
                .build()
                .show()
        }
    }

    /**
     * 申请权限
     * @param rationale 权限申请提示
     * @param requestCode 权限申请码
     * @param s 申请的权限数组
     */
    fun easyPermissions(rationale:String, requestCode:Int, vararg s:String){
        this.context ?: return
        if(!EasyPermissions.hasPermissions(this.context!!,*s)){
            //开始申请权限
            EasyPermissions.requestPermissions(this@BaseFragment,rationale,requestCode,*s)
            return
        }
        onPermissionsGranted(requestCode, s.toList())
    }

    /**
     * 权限回调
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        //将请求结果传递EasyPermission库处理
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }
}

inline fun FragmentManager.inTransaction(func: FragmentTransaction.() -> FragmentTransaction) {
    beginTransaction().func().commit()
}