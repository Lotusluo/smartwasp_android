package com.smartwasp.assistant.app.service

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.iflytek.home.sdk.push.OsPushService
import com.orhanobut.logger.Logger

class PushService : OsPushService() {
    open inner class ServiceBinder : Binder() {
        fun getService(): PushService {
            return this@PushService
        }
    }

    private val binder = ServiceBinder()

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }
}