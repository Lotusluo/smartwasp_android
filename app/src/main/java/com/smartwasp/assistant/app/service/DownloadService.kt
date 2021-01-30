package com.smartwasp.assistant.app.service

import android.app.IntentService
import android.content.Context
import android.content.Intent
import com.orhanobut.logger.Logger
import com.smartwasp.assistant.app.base.SmartApp
import com.smartwasp.assistant.app.util.RetrofitManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

private const val ACTION_FOO = "com.smartwasp.assistant.app.service.action.FOO"

private const val EXTRA_PARAM1 = "com.smartwasp.assistant.app.service.extra.PARAM1"
private const val EXTRA_PARAM2 = "com.smartwasp.assistant.app.service.extra.PARAM2"
/**
 * An [IntentService] subclass for handling asynchronous task requests in
 * a service on a separate handler thread.

 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.

 */
class DownloadService : IntentService("DownService"){

    override fun onHandleIntent(intent: Intent?) {
        when (intent?.action) {
            ACTION_FOO -> {
                //下载地址
                val param1 = intent.getStringExtra(EXTRA_PARAM1)
                //MD5文件校验值
                val param2 = intent.getStringExtra(EXTRA_PARAM2)
                handleActionFoo(param1,param2)
            }
        }
    }

    /**
     * 更新下载文件
     * @param path 下载的地址
     * @param md5 md5文件校验值
     */
    private fun handleActionFoo(path:String?,md5:String?){
        if(path.isNullOrEmpty() || md5.isNullOrEmpty()){
            showErrNotification()
            return
        }
        GlobalScope.async {
            val result = RetrofitManager.get().retrofitApiService?.download(path)
            result?.let {call->
                val responseBody = call.execute()
                if(responseBody.isSuccessful){
                    responseBody?.body()?.let {
                        try{
                            val saveFile = File(SmartApp.app.externalCacheDir,"update.bat")
                            if(saveFile.exists() || saveFile.createNewFile()){
                                //开始下载
                                val mTotal = it.contentLength()
                                val inputStream = BufferedInputStream(it.byteStream())
                                val outputStream = FileOutputStream(saveFile)
                                var buffer = ByteArray(1024)
                                var len:Int
                                var mLength:Long = 0
                                while (((inputStream.read(buffer)).also {rd-> len = rd }) != -1) {
                                    outputStream.write(buffer, 0, len)
                                    mLength+=len
                                }
                                try {
                                    inputStream.close()
                                    outputStream.close()
                                }catch (e:IOException){}
                                finally {
                                    onDownloadDone(saveFile,md5)
                                }
                            }else{
                                showErrNotification()
                            }
                        }catch(e:IOException) {
                            showErrNotification()
                        }
                    }?: kotlin.run {
                        showErrNotification()
                    }
                }else{
                    showErrNotification()
                }
            }?: kotlin.run {
                showErrNotification()
            }
        }
    }

    /**
     * 向通知栏进程控件发送进度消息
     * @param progress
     */
    private fun sendRemoteProgress(progress: Int) {

    }

    /**
     * 显示下载失败提示
     */
    private fun showErrNotification(){

    }

    /**
     *下载完成
     * @param file 下载的文件
     * @param md5 md5文件校验值
     */
    private fun onDownloadDone(file: File,md5: String){

    }

    companion object {
        @JvmStatic
        fun startActionFoo(context: Context, param1: String) {
            val intent = Intent(context, DownloadService::class.java).apply {
                action = ACTION_FOO
                putExtra(EXTRA_PARAM1, param1)
            }
            context.startService(intent)
        }
    }
}