package com.smartwasp.assistant.app.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.smartwasp.assistant.app.util.AppExecutors
import java.io.*
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketException
import java.util.*

class ApConfigNetService : Service() {
    companion object {
        private const val TAG = "ApConfigNetService"
        private const val ACTION_PREFIX = "com.iflytek.home.app.action.ap"
        const val ACTION_CONNECT = "$ACTION_PREFIX.CONNECT"
        const val ACTION_DISCONNECT = "$ACTION_PREFIX.DISCONNECT"
        const val ACTION_SEND_MESSAGE = "$ACTION_PREFIX.SEND_MESSAGE"

        const val EXTRA_MESSAGE = "message"
    }

    class ApServiceBinder(val service: ApConfigNetService) : Binder()

    private var socket: Socket? = null
    private val binder = ApServiceBinder(this)

    private val listeners = hashSetOf<SocketListener>()

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CONNECT -> {
                connect()
            }
            ACTION_DISCONNECT -> {
                disconnect()
            }
            ACTION_SEND_MESSAGE -> {
                intent.getStringExtra(EXTRA_MESSAGE)?.let {
                    send(it)
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    fun connect() {
        disconnect()
        val gateway = getCurrentGateway()

        Log.d(TAG, "gateway : $gateway")

        Thread {
            var socket: Socket? = null
            try {
                socket = Socket()

                socket.connect(InetSocketAddress(InetAddress.getByName(gateway), 8080))

                if (socket.isConnected) {
                    onOpen(socket)

                    if (socket.isClosed)
                        Log.e(TAG, "连接成功，但 isClosed 标识是 true")

                    val stream = socket.getInputStream()

                    val bytes = ByteArray(1024)
                    while (socket.isConnected && !socket.isClosed) {
                        var size = stream.read(bytes)
                        if (size != 0 && size != -1) {
                            if (size < bytes.size) {
                                onMessage(socket, bytes.copyOf(size))
                            } else {
                                val outputStream = ByteArrayOutputStream(8 * 1024)
                                outputStream.write(bytes)
                                while (size != 0 && size == bytes.size) {
                                    size = stream.read(bytes)
                                    if (size != 0) {
                                        outputStream.write(bytes)
                                    }
                                }
                                val target = outputStream.toByteArray()
                                if (target.isNotEmpty()) {
                                    onMessage(socket, target)
                                }
                            }
                        } else if (size == -1) {
                            break
                        }
                    }
                    onClosed(socket, null)
                }
            } catch (t: Throwable) {
                t.printStackTrace()

                if (socket != null)
                    onClosed(socket, t)

                when (t) {
                    is UnsupportedOperationException -> {
                    }
                    is IllegalArgumentException -> {
                    }
                    is SocketException -> {
                    }
                }
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        disconnect()
    }

    fun disconnect() {
        socket?.let { socket ->
            socket.close()
            onClosed(socket, null)
        } ?: run {
            Log.w(TAG, "Socket is closed while trying to disconnect")
        }
    }

    fun isConnected() = socket?.isConnected

    private fun onOpen(socket: Socket) {
        this.socket = socket

        AppExecutors.get().mainThread().execute {
            listeners.map {
                try {
                    it.onOpen(socket)
                } catch (t: Throwable) {
                    t.printStackTrace()
                }
            }
        }

    }

    private fun onClosed(socket: Socket, t: Throwable?) {
        try {
            socket.close()
        }catch (e:Throwable){}
        if (this.socket == socket) {
            this.socket = null
        }

        AppExecutors.get().mainThread().execute {
            listeners.map {
                try {
                    it.onClosed(socket, t)
                } catch (t: Throwable) {
                    t.printStackTrace()
                }
            }
        }
    }

    private fun onMessage(socket: Socket, byteArray: ByteArray) {
        AppExecutors.get().mainThread().execute {
            listeners.map {
                try {
                    it.onMessage(socket, byteArray)
                } catch (t: Throwable) {
                    t.printStackTrace()
                }
            }
        }
    }

    fun getCurrentGateway(): String? {
        val wm = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        val dhcpInfo = wm.dhcpInfo

        return ipToString(dhcpInfo.gateway)
    }

    fun addListener(listener: SocketListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: SocketListener) {
        listeners.remove(listener)
    }

    fun send(message: String) {
        socket?.let { socket ->
            Thread {
                try {
                    if (socket.isClosed) {
                        onClosed(socket, null)
                    } else {
                        socket.getOutputStream()?.write(message.toByteArray())
                    }
                } catch (t: Throwable) {
                    onClosed(socket, t)
                }
            }.start()
            Log.d(TAG, "try to send $message")
        } ?: run {
            Log.w(TAG, "Socket is closed while trying to send $message")
        }
    }

    private fun ipToString(ip: Int): String {
        val sb = StringBuffer()
        sb.append(ip and 0xFF)
        sb.append('.')
        sb.append(ip.shr(8) and 0xFF)
        sb.append('.')
        sb.append(ip.shr(16) and 0xFF)
        sb.append('.')
        sb.append(ip.shr(24) and 0xFF)
        return sb.toString()
    }

    abstract class SocketListener {
        abstract fun onOpen(socket: Socket)
        abstract fun onClosed(socket: Socket, t: Throwable?)
        abstract fun onMessage(socket: Socket, byteArray: ByteArray)
    }
}