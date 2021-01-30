package com.smartwasp.assistant.app.base

import android.app.ActivityManager
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.DrawableCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import com.orhanobut.logger.Logger
import com.smartwasp.assistant.app.BR
import com.smartwasp.assistant.app.R
import com.smartwasp.assistant.app.util.LoadingUtil
import com.smartwasp.assistant.app.util.StatusBarUtil
import me.jessyan.autosize.AutoSizeCompat
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.EasyPermissions.PermissionCallbacks
import java.lang.reflect.ParameterizedType

/**
 * Created by luotao on 2021/1/7 15:41
 * E-Mail Address：gtkrockets@163.com
 * 基础类Activity
 */

abstract class BaseActivity<
        VM:BaseViewModel,
        BD:ViewDataBinding>:AppCompatActivity(), PermissionCallbacks, View.OnClickListener {
    //与Activity绑定的唯一ViewModel
    protected lateinit var mViewModel:VM
    //与Activity绑定的唯一ViewDataBinding
    protected lateinit var mBinding:BD

    //布局文件
    protected open val layoutResID:Int? = 0

    private lateinit var manager: ActivityManager

    /**
     * 产生
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarUtil.transparencyBar(this)
        StatusBarUtil.setLightStatusBar(this,true,true)
        StatusBarUtil.setStatusBarColor(this,R.color.smartwasp_white)
        manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        layoutResID?.let {
            setContentView(it)
            mBinding = DataBindingUtil.setContentView(this, it)
            mBinding.lifecycleOwner = this
            mBinding.setVariable(BR.onclickListener,this)
            //默认返回图标
            setNavigator(R.mipmap.ic_navback)
        }
        createModelView()
        Logger.d("onCreate:${this}")
    }

    /**
     * 通过反射加载mViewModel
     */
    private fun createModelView(){
        var mModelClass = when(val type = this::class.java.genericSuperclass){
            is ParameterizedType ->{
                (type.actualTypeArguments[0]) as Class<VM>
            }else -> BaseViewModel::class.java
        }
        if(mModelClass.simpleName == "BaseViewModel")
            return
        mModelClass.let {
            mViewModel = ViewModelProviders.of(this).get(it as Class<VM>)
        }
    }

    /**
     * 呈现不可交互
     */
    override fun onStart() {
        super.onStart()
        Logger.d("onStart:${this}")
    }

    override fun onRestart() {
        super.onRestart()
        Logger.d("onRestart:${this}")
    }

    /**
     * 呈现可以交互
     */
    override fun onResume() {
        super.onResume()
        Logger.d("onResume:${this}")
    }

    /**
     *呈现不可交互
     */
    override fun onPause() {
        super.onPause()
        Logger.d("onPause:${this}")
    }

    /**
     * 不呈现
     */
    override fun onStop() {
        super.onStop()
        Logger.d("onStop:${this}")
    }

    /**
     * 销毁
     */
    override fun onDestroy() {
        super.onDestroy()
        Logger.d("onDestroy:${this}")
    }

    /**
     * 权限禁止
     */
    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
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
     * 权限允许
     */
    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {

    }

    /**
     * 申请权限
     * @param rationale 权限申请提示
     * @param requestCode 权限申请码
     * @param s 申请的权限数组
     */
    fun easyPermissions(rationale:String, requestCode:Int, vararg s:String){
        if(!EasyPermissions.hasPermissions(this,*s)){
            //开始申请权限
            EasyPermissions.requestPermissions(this@BaseActivity,rationale,requestCode,*s)
            return
        }
        onPermissionsGranted(requestCode, s.toList())
    }

    /**
     * 权限回调
     */
    final override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        //将请求结果传递EasyPermission库处理
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
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
     * @param tiny 渲染样式
     */
    fun setNavigator(resID:Int,tiny:Int = 0){
        val icon = resources.getDrawable(resID)
        if(tiny > 0){
            DrawableCompat.setTint(icon,tiny)
        }
        mBinding.setVariable(BR.leftIcon,icon)
    }

    /**
     * 控件点击
     */
    final override fun onClick(v: View){
        when(v.id){
            R.id.toolbar_left_icon ->{
                onNavigatorClick()
            }
            else ->{
                onButtonClick(v)
            }
        }
    }

    /**
     * 右侧按钮点击
     */
    open fun onNavigatorClick(){
        if(!interceptLeftButton())
            finish()
    }

    /**
     * 是否拦截左侧按钮
     */
    open fun interceptLeftButton():Boolean{
        supportFragmentManager.fragments.forEach {
            (it as? BaseFragment<*,*>)?.let {baseFragment ->
                if(baseFragment.interceptLeftButton()){
                    return true
                }
            }
        }
        if(supportFragmentManager.backStackEntryCount > 0){
            supportFragmentManager.popBackStack()
            return true
        }
        return false
    }

    /**
     * 其他控件点击
     */
    open fun onButtonClick(v: View){

    }

    /**
     * 按键点击
     * @param keyCode
     * @param event
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            onNavigatorClick()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    /**
     * 针对AutoSize布局混乱的问题解决
     */
    override fun getResources(): Resources {
        AutoSizeCompat.autoConvertDensityOfGlobal(super.getResources())
        return super.getResources()
    }

}

/**
 * ##################### supportFragmentManager 拓展函数#####################
 */
fun FragmentActivity.addFragmentByTag(frameId: Int,fragment: Fragment){
    val tag = fragment::class.java.simpleName
    if(null != supportFragmentManager.findFragmentByTag(tag))
        return
    supportFragmentManager.inTransaction {
        add(frameId, fragment,tag)
    }
}

fun FragmentActivity.addFragmentByTagWithStack(frameId: Int,fragment: Fragment){
    val tag = fragment::class.java.simpleName
    if(null != supportFragmentManager.findFragmentByTag(tag))
        return
    supportFragmentManager.inTransaction {
        setCustomAnimations(R.anim.slide_right_in,0,0,R.anim.slide_right_out)
        add(frameId, fragment,tag)
        addToBackStack(null)
    }
}

fun FragmentActivity.showFragment(fragment: Fragment) {
    val tag = fragment::class.java.simpleName
    if(null == supportFragmentManager.findFragmentByTag(tag))
        return
    supportFragmentManager.inTransaction{show(fragment)}
}

fun FragmentActivity.hideFragment(fragment: Fragment) {
    val tag = fragment::class.java.simpleName
    if(null == supportFragmentManager.findFragmentByTag(tag))
        return
    supportFragmentManager.inTransaction{hide(fragment)}
}
