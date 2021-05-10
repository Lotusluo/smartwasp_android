package com.smartwasp.assistant.app.fragment.aps

import android.R.id.message
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.SystemClock
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.orhanobut.logger.Logger
import com.smartwasp.assistant.app.R
import com.smartwasp.assistant.app.activity.ApStepActivity
import com.smartwasp.assistant.app.base.BaseFragment
import com.smartwasp.assistant.app.base.SmartApp
import com.smartwasp.assistant.app.bean.ApBean
import com.smartwasp.assistant.app.databinding.FragmentAp4Binding
import com.smartwasp.assistant.app.fragment.PreBindFragment
import com.smartwasp.assistant.app.service.ApConfigNetService
import com.smartwasp.assistant.app.util.AppExecutors
import com.smartwasp.assistant.app.util.IFLYOS
import com.smartwasp.assistant.app.util.LoadingUtil
import com.smartwasp.assistant.app.util.NetWorkUtil
import com.smartwasp.assistant.app.viewModel.ApBindModel
import kotlinx.android.synthetic.main.fragment_ap1.tvSubTittle
import kotlinx.android.synthetic.main.fragment_ap3.progress
import kotlinx.android.synthetic.main.fragment_ap4.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.Socket
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
        if(sendTag.get() > 0 && isAdded){
            AlertDialog.Builder(requireContext())
                    .setTitle(R.string.tip)
                    .setMessage(R.string.ap_device_exit)
                    .setPositiveButton(android.R.string.ok){ _, _ ->
                        sendTag.set(0)
                        super.onNavigatorClick()
                    }
                    .setNegativeButton(android.R.string.cancel,null)
                    .show()
            return true
        }
        return super.interceptLeftButton()
    }

    //socket监听器
    private val socketListener = object : ApConfigNetService.SocketListener() {
        override fun onOpen(socket: Socket) {
            //开始发送配置文件
//            sendConfig()
//            发送时间戳
            sendTimestamp()
        }

        override fun onClosed(socket: Socket, t: Throwable?) {
            //忽略没有错误的关闭
            t ?: return
            Logger.e("onClosed:${t},$sendTag")
//            //已经发送密码则忽略所有错误
            if(sendTag.get() >= 1) return
            handleRetry()
        }

        override fun onMessage(socket: Socket, byteArray: ByteArray) {
            var string = String(byteArray)
            Logger.d("onMessage:$sendTag,$string")
            if(!string.isNullOrEmpty()){
                if(string.contains("client_id")){
                    if(string.contains(ApStepActivity.clientID)){
                        //获取sn号与projectID
                        apBean = Gson().fromJson(string)
                        Logger.d(apBean.toString())
                        sendConfig()
                    }else{
                        //匹配错误
                        if(isAdded){
                            AlertDialog.Builder(requireContext())
                                    .setTitle(R.string.tip)
                                    .setMessage(R.string.ap_device_err)
                                    .setPositiveButton(android.R.string.ok){ _, _ ->
                                        this@ApStepFragment4.onNavigatorClick()
                                    }
                                    .setCancelable(false)
                                    .setNegativeButton(android.R.string.cancel,null)
                                    .show()
                        }
                    }
                }
            }
        }
    }

    private var apBean:ApBean? = null

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
        sendTag.set(0)
        apConfigNetService?.connect() ?: run {
            context?.startService(Intent(context, ApConfigNetService::class.java).apply {
                action = ApConfigNetService.ACTION_CONNECT
            })
        }
    }


    //    发送标志
    private var sendTag:AtomicInteger = AtomicInteger(0)
    private var gateWay:String?=null
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
                gateWay = NetWorkUtil.getCurrentGateway(requireContext())
                sendTag.set(1)
                service.send(json.toString())
                val authCode = arguments?.getString(PreBindFragment.BIND_AUTH_CODE)
                mViewModel?.askDevAuth(authCode!!)
                GlobalScope.launch(Dispatchers.IO) {
                    SystemClock.sleep(4000)
                    if(!isAdded)
                        return@launch
                    if(progress.progress >= 100)
                        return@launch
                    compatProgress(90)
                }
                SmartApp.NEED_MAIN_REFRESH_DEVICES = true
                return
            } else {
                handleRetry()
            }
        } ?: run {
            handleRetry()
        }
    }

    /**
     * 发送时间戳
     */
    private fun sendTimestamp(){
        apConfigNetService?.let { service ->
            if (service.isConnected() == true) {
                val json = JSONObject().apply {
                    put("timestamp", System.currentTimeMillis())
                }
                service.send(json.toString())
                return
            } else {
                handleRetry()
            }
        } ?: run {
            handleRetry()
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
                //向后台授权
                apBean?.let {apBean->
                    if(!apBean.client_id.isNullOrEmpty() && !apBean.did.isNullOrEmpty()){
                        mViewModel!!.bind(apBean.client_id,apBean.did).observe(this, Observer {})
                    }
                }
                var count = 6
                AppExecutors.get().diskIO().execute {
                    while (count-->=0){
                        AppExecutors.get().mainThread().execute{
                            if(!isAdded)
                                return@execute
                            if(count <= 0)
                                requireActivity().finish()
                            else
                                tvSubTittle.text = String.format(getString(R.string.ap_device_ok),count)
                        }
                        SystemClock.sleep(1000)
                    }
                }
            }else if(it == IFLYOS.ERROR){
                //3001
                if(isAdded){
                    AlertDialog.Builder(requireContext())
                            .setTitle(R.string.tip)
                            .setMessage(R.string.ap_device_err1)
                            .setPositiveButton(android.R.string.ok){ _, _ ->
                                sendTag.set(0)
                                this@ApStepFragment4.onNavigatorClick()
                            }
                            .setCancelable(false)
                            .setNegativeButton(android.R.string.cancel,null)
                            .show()
                }
            }else{
                //超时
                if(isAdded){
                    AlertDialog.Builder(requireContext())
                            .setTitle(R.string.tip)
                            .setMessage(R.string.ap_device_err2)
                            .setPositiveButton(android.R.string.ok){ _, _ ->
                                sendTag.set(0)
                                this@ApStepFragment4.onNavigatorClick()
                            }
                            .setCancelable(false)
                            .setNegativeButton(android.R.string.cancel,null)
                            .show()
                }
            }
        })
    }

    /**
     * 点击重试
     */
    private fun handleRetry(){
        sendTag.set(0)
        if(this.isAdded){
            AlertDialog.Builder(requireContext())
                    .setTitle(R.string.tip)
                    .setMessage(R.string.error_ap_connect)
                    .setCancelable(false)
                    .setPositiveButton(R.string.retry){_,_->
                        val gateWay1 = NetWorkUtil.getCurrentGateway(requireContext())
                        if(gateWay1 == gateWay && gateWay != "127.0.0.1"){
                            progress.progress = 10
                            apConfigNetService?.connect() ?: run {
                                context?.startService(Intent(context, ApConfigNetService::class.java).apply {
                                    action = ApConfigNetService.ACTION_CONNECT
                                })
                            }
                        }else{
                            AlertDialog.Builder(requireContext())
                                    .setTitle(R.string.tip)
                                    .setCancelable(false)
                                    .setMessage(R.string.error_ap_connect1)
                                    .setPositiveButton(android.R.string.ok){
                                        _,_->
                                        onNavigatorClick()
                                    }
                                    .show()
                        }
                    }
                    .setNegativeButton(R.string.back){_,_->
                        onNavigatorClick()
                    }
                    .show()
        }
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
        icon_pic.setImageResource(if(ApStepActivity.clientID == "65e8d4f8-da9e-4633-8cac-84b0b47496b6") R.drawable.danjian else R.drawable.xiaodan)
    }

    //布局文件
    override val layoutResID:Int = R.layout.fragment_ap4
}

inline fun <reified T : Any> Gson.fromJson(json: String): T {
    return Gson().fromJson(json, T::class.java)
}