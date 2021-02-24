package com.smartwasp.assistant.app.fragment.aps

import android.content.*
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.SystemClock
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import com.orhanobut.logger.Logger
import com.smartwasp.assistant.app.R
import com.smartwasp.assistant.app.activity.ApStepActivity
import com.smartwasp.assistant.app.base.*
import com.smartwasp.assistant.app.databinding.FragmentAp4Binding
import com.smartwasp.assistant.app.fragment.PreBindFragment
import com.smartwasp.assistant.app.service.ApConfigNetService
import com.smartwasp.assistant.app.util.*
import com.smartwasp.assistant.app.viewModel.ApBindModel
import kotlinx.android.synthetic.main.fragment_ap1.*
import kotlinx.android.synthetic.main.fragment_ap3.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.lang.Exception
import java.lang.ref.WeakReference
import java.net.Socket
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by luotao on 2021/2/21 11:57
 * E-Mail Address：gtkrockets@163.com
 */
class ApStepFragment4 private constructor():BaseFragment<ApBindModel,FragmentAp4Binding>() {

    companion object{
        /**
         * 静态生成类
         * @param authCode
         */
        fun newsInstance(authCode:String):ApStepFragment4{
            val apStepFragment4 = ApStepFragment4()
            apStepFragment4.arguments = Bundle().apply {
                putString(PreBindFragment.BIND_AUTH_CODE,authCode)
            }
            return apStepFragment4
        }
    }


    /**
     * 拦截劝等待
     */
    override fun interceptLeftButton():Boolean{
        if(flag){
            //已经发送了密码,劝等待
            if(isAdded){
                AlertDialog.Builder(requireContext())
                        .setTitle(R.string.tip)
                        .setMessage(R.string.ap_device_exit)
                        .setPositiveButton(android.R.string.ok){ _, _ ->
                            flag = false
                            super.onNavigatorClick()
                        }
                        .setNegativeButton(android.R.string.cancel,null)
                        .show()
            }
            return true
        }
        return super.interceptLeftButton()
    }


    private var tryConnect:AtomicBoolean = AtomicBoolean(false)

    //socket监听器
    private val socketListener = object : ApConfigNetService.SocketListener() {
        override fun onOpen(socket: Socket) {
            //开始发送配置文件
            tryConnect.set(false)
            sendConfig()
        }
        override fun onClosed(socket: Socket, t: Throwable?) {
            if(!this@ApStepFragment4.isAdded)
                return
            if(flag) return
            t ?: return
            if(t.message?.toLowerCase()?.contains("connect") == true && !tryConnect.getAndSet(true)){
                apConfigNetService?.connect() ?: run {
                    context?.startService(Intent(context, ApConfigNetService::class.java).apply {
                        action = ApConfigNetService.ACTION_CONNECT
                    })
                }
                return
            }
            AlertDialog.Builder(requireContext())
                    .setTitle(R.string.tip)
                    .setMessage(R.string.error_ap_connect)
                    .setPositiveButton(R.string.retry){_,_->
                        progress.progress = 10
                        apConfigNetService?.connect() ?: run {
                            context?.startService(Intent(context, ApConfigNetService::class.java).apply {
                                action = ApConfigNetService.ACTION_CONNECT
                            })
                        }
                    }
                    .setNegativeButton(R.string.back){_,_->
                        onNavigatorClick()
                    }
                    .show()
        }
        override fun onMessage(socket: Socket, byteArray: ByteArray) {}
    }

    //socket服务
    private var apConfigNetService: ApConfigNetService? = null
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            apConfigNetService?.removeListener(socketListener)
            apConfigNetService = null
        }
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            (service as? ApConfigNetService.ApServiceBinder)?.let {
                apConfigNetService = it.service
                apConfigNetService?.addListener(socketListener)
            }
            compatProgress(10)
        }
    }

    /**
     * 入场动画完成
     */
    override fun onTransitDone() {
        super.onTransitDone()
        //建立连接
        flag = false
        apConfigNetService?.connect() ?: run {
            context?.startService(Intent(context, ApConfigNetService::class.java).apply {
                action = ApConfigNetService.ACTION_CONNECT
            })
        }
    }

    //    发送标志
    private var flag = false
    /**
     * 开始发送配置文件
     */
    private fun sendConfig(){
        val authCode = arguments?.getString(PreBindFragment.BIND_AUTH_CODE)
        val json = JSONObject().apply {
            put("ssid", ApStepActivity.CUR_WIFI_SSID)
            put("bssid", ApStepActivity.CUR_WIFI_BSSID)
            put("password", ApStepActivity.CUR_WIFI_PWD)
            put("auth_code", authCode)
        }
        apConfigNetService?.let { service ->
            if (service.isConnected() == true) {
                compatProgress(50)
                service.send(json.toString())
                flag = true
                SmartApp.NEED_MAIN_REFRESH_DEVICES = true
                mViewModel?.askDevAuth(authCode!!)
                GlobalScope.launch(Dispatchers.IO) {
                    SystemClock.sleep(4000)
                    if(!isAdded)
                        return@launch
                    if(progress.progress >= 100)
                        return@launch
                    compatProgress(90)
                    service.disconnect()
                    SystemClock.sleep(15 * 1000)
                    if(!NetWorkUtil.isGoodInternet(requireContext())){
                        GlobalScope.launch(Dispatchers.Main){
                            if(!isAdded)
                                return@launch
                            AlertDialog.Builder(requireContext())
                                    .setTitle(R.string.tip)
                                    .setMessage(R.string.ap_device_no_net)
                                    .setPositiveButton(android.R.string.ok,null)
                                    .show()
                        }
                    }
                }
                return
            } else {
                if(!isAdded)
                    return
                AlertDialog.Builder(requireContext())
                        .setTitle(R.string.tip)
                        .setMessage(R.string.error_ap_connect)
                        .setPositiveButton(R.string.retry){_,_->
                            progress.progress = 10
                            apConfigNetService?.connect() ?: run {
                                context?.startService(Intent(context, ApConfigNetService::class.java).apply {
                                    action = ApConfigNetService.ACTION_CONNECT
                                })
                            }
                        }
                        .setNegativeButton(R.string.back){_,_->
                            onNavigatorClick()
                        }
                        .show()
            }
        } ?: run {
            if(this.isAdded){
                AlertDialog.Builder(requireContext())
                        .setTitle(R.string.tip)
                        .setMessage(R.string.error_ap_connect)
                        .setPositiveButton(R.string.retry){_,_->
                            progress.progress = 10
                            apConfigNetService?.connect() ?: run {
                                context?.startService(Intent(context, ApConfigNetService::class.java).apply {
                                    action = ApConfigNetService.ACTION_CONNECT
                                })
                            }
                        }
                        .setNegativeButton(R.string.back){_,_->
                            onNavigatorClick()
                        }
                        .show()
            }
        }
    }

    /**
     * 建立服务
     * @param savedInstanceState
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //绑定服务
        context?.bindService(Intent(context, ApConfigNetService::class.java), serviceConnection, Context.BIND_AUTO_CREATE)
        //监听授权服务
        mViewModel?.askDeferred!!.observe(this, Observer {
            if(it == IFLYOS.OK){
                compatProgress(100)
                tvSubTittle.setText(R.string.ap_device_ok)
                AppExecutors.get().mainThread().executeDelay(Runnable {
                    if(isAdded)
                        return@Runnable
                    requireActivity().finish()
                },4000)
            }
        })
    }


    /**
     * 授权的进度
     * @param p 进度
     */
    private fun compatProgress(p:Int){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            progress.setProgress(p,true)
        }else{
            progress.progress = p
        }
    }

    /**
     * 销毁服务
     */
    override fun onDestroy() {
        super.onDestroy()
        //解绑服务
        try {
            context?.unbindService(serviceConnection)
        }catch (e:Throwable){}
        context?.stopService(Intent(context, ApConfigNetService::class.java))
    }


    /**
     * 视图生成
     * @param view
     * @param savedInstanceState
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.step = "4"
        mBinding.total = "/4"
    }

    //布局文件
    override val layoutResID:Int = R.layout.fragment_ap4
}