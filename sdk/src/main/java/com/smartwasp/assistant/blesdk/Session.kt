package com.smartwasp.assistant.blesdk

import android.bluetooth.*
import android.os.SystemClock
import android.util.Log
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONException
import com.alibaba.fastjson.JSONObject
import java.nio.ByteBuffer

// Created by luotao on 2021/3/29
internal class Session private constructor(
        private var bluetoothAdapter:BluetoothAdapter,
        private var device: LeDevice,
        private var leSessionCallback:ILeSessionCallback):BluetoothGattCallback() {

    companion object{
        const val TAG = "Session"
        /**
         * 建立gatt连接
         * @param bluetoothAdapter
         * @param device
         * @param leConnectCallback
         */
        fun acceptGatt(bluetoothAdapter:BluetoothAdapter,
                       device: LeDevice,
                       leSessionCallback:ILeSessionCallback){
            Session(bluetoothAdapter,device,leSessionCallback).fire()
        }
    }

    /**
     * 判定两个对象是否相等
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Session
        return device == other.device
    }

    /**
     * 建立连接
     */
    private fun fire() {
        val ble = bluetoothAdapter.getRemoteDevice(device.getAddress())
        ble?.let {
            //获取蓝牙通讯协议
            AppExecutors.get().diskIO().execute {
                ble.connectGatt(IBluetoothClient.appEnt,false,this)
            }
        }?: kotlin.run {
            //有可能为空的情况
            leSessionCallback.onSessionClose(this)
        }
    }

    /**
     * 协议通道成功
     * @param gatt 协议
     */
    private fun onConnectSuccess(gatt: BluetoothGatt){
        mGatt = gatt
        leSessionCallback.onSessionOpen(this)
    }

    /**
     * 协议通道失败
     */
    private fun onConnectError(){
        AppExecutors.get().diskIO().execute {
            mGatt?.close()
            mGatt = null
        }
        leSessionCallback.onSessionClose(this)
    }

//    那么这个两个方法有什么区别,又该如何使用呢.
//
//    disconnect()方法: 如果调用了该方法之后可以调用connect()方法进行重连,这样还可以继续进行断开前的操作.
//
//    close()方法: 一但调用了该方法, 如果你想再次连接,必须调用BluetoothDevice的connectGatt()方法. 因为close()方法将释放BluetootheGatt的所有资源.
//
//    需要注意的问题:
//    当你需要手动断开时,调用disconnect()方法，此时断开成功后会回调onConnectionStateChange方法,在这个方法中再调用close方法释放资源。
//    如果在disconnect后立即调用close，会导致无法回调onConnectionStateChange方法。


    //蓝牙协议
    private var mGatt:BluetoothGatt? = null
    //通知
    private var mNotified: Boolean = false
    //一个包的字节默认为20
    private var mMtu = 20

    //数据请求
    private val mRequestQueue = GattUtils.createRequestQueue()

    /**
     * 蓝牙连接状态
     */
    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        super.onConnectionStateChange(gatt, status, newState)
        val isSucceed = status == BluetoothGatt.GATT_SUCCESS
        val isConnected = newState == BluetoothAdapter.STATE_CONNECTED
        if(isSucceed && isConnected){
            //打开蓝牙协议服务
            AppExecutors.get().diskIO().execute {
                SystemClock.sleep(3000)
                if(gatt!!.discoverServices()){
                    onConnectSuccess(gatt)
                }else{
                    onConnectError()
                }
            }
            return
        }
        if(!isSucceed||!isConnected){
            onConnectError()
        }
    }

    /**
     * 发现蓝牙服务
     */
    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
        super.onServicesDiscovered(gatt, status)
        val isSucceed = status == BluetoothGatt.GATT_SUCCESS
        if(isSucceed){
            //申请一个包更多的字节
            mGatt!!.requestMtu(134 + 3)
            mNotified = false
        }else{
            onConnectError()
        }
    }

    override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
        super.onCharacteristicRead(gatt, characteristic, status)
        next()
    }

    override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
        super.onCharacteristicWrite(gatt, characteristic, status)
        next()
    }

    override fun onDescriptorRead(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
        super.onDescriptorRead(gatt, descriptor, status)
        next()
    }

    override fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
        super.onDescriptorWrite(gatt, descriptor, status)
        next()
    }

    /**
     * 发送回来的数据
     */
    override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
        super.onCharacteristicChanged(gatt, characteristic)
        val bytes = characteristic.value
        //忽略APK发送出去的数据
        if (bytes.first() == (0x01).toByte() || bytes.last() == (0x04).toByte())
            return
        if(bytes.size == 1){
            //配网成功与否
           AppExecutors.get().mainThread().execute {
               if(bytes[0] == (0x01).toByte()){
                   mWifiSetupCallback?.onWifiSetupSuccess()
               }else if (bytes[0] == (0x02).toByte()){
                   mWifiSetupCallback?.onWifiSetupFail()
               }
           }
            mWifiSetupCallback = null
            return
        }
        if(byteBuffer.remaining() < bytes.size){
            val temp = ByteBuffer.allocate(byteBuffer.position() + 1024)
            byteBuffer.flip()
            temp.put(byteBuffer)
            byteBuffer = temp
        }
        byteBuffer.put(bytes)
        //拉取字符串
        val txtBytes = ByteArray(byteBuffer.position())
        for (i in txtBytes.indices){
            txtBytes[i] = byteBuffer[i]
        }
        var jsonRoot:JSONObject? = null
        try {
            jsonRoot = JSON.parse(String(txtBytes)) as? JSONObject?
        }catch (e:JSONException){}
        jsonRoot?.let {
            if(bytes.size < mMtu){
                val cmd = jsonRoot.getString("cmd")
                if(cmd == "wifilists"){
                    val lists = mutableListOf<WifiInfo>()
                    val content = jsonRoot.getJSONArray("ret")
                    for (i in content.indices){
                        val jsonObject = content.getJSONObject(i)
                        val ssid = jsonObject.getString("ssid")
                        if(ssid.isNullOrEmpty())
                            continue
                        val wifiInfo = WifiInfo(
                                ssid,
                                jsonObject.getString("rssi"),
                                jsonObject.getString("flags"))
                        lists.add(wifiInfo)
                    }
                    byteBuffer.clear()
                    AppExecutors.get().mainThread().execute {
                        mWifiScanCallback?.onWifiScan(lists)
                    }
                    mWifiScanCallback = null
                }
            }
        }
    }

    //字节缓冲区
    private var byteBuffer = ByteBuffer.allocate(10 * 1024)

    /**
     * 包字节数更改
     */
    override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
        super.onMtuChanged(gatt, mtu, status)
        mMtu = mtu - 3
    }

    /**
     * 描述改变通知
     */
    fun notifyGatt(){
        if(mNotified)
            return
        mGatt ?:return
        mNotified = true
        //获取特征
        val characteristic = GattUtils.getCharacteristic(mGatt,
                BluetoothClient.WIFI_SERVICE_UUID,
                BluetoothClient.WIFI_CHARACTERISTIC_UUID)
        characteristic ?: return
        //获取描述
        val descriptor = GattUtils.getDescriptor(mGatt,
                BluetoothClient.WIFI_SERVICE_UUID,
                BluetoothClient.WIFI_CHARACTERISTIC_UUID,
                BluetoothClient.CLIENT_CONFIG_DESCRIPTOR_UUID)
        descriptor ?: return
        //设置改变可通知
        mGatt!!.setCharacteristicNotification(characteristic, true)
        descriptor.value = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
        mRequestQueue.addWriteDescriptor(mGatt!!, descriptor, BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE)
        descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        mRequestQueue.addWriteDescriptor(mGatt!!, descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
        mRequestQueue.execute()
    }

    /**
     * 搜索可用的wifi列表
     */
    fun startWifiScan(wifiScanCallback: IWifiScanCallback) {
        mGatt ?:return
        this.mWifiScanCallback = wifiScanCallback
        //获取特征
        val characteristic = GattUtils.getCharacteristic(mGatt,
                BluetoothClient.WIFI_SERVICE_UUID,
                BluetoothClient.WIFI_CHARACTERISTIC_UUID)
        characteristic ?: return
        val b = ByteArray(14){ 0x00 }
        val str = "wifilists"
        b[0] = 0x01
        var i = 1
        str.forEach {char-> b[i++] = char.toByte() }
        b[13] = 0x04
        characteristic.value = b
        mRequestQueue.addWriteCharacteristic(mGatt, characteristic)
        mRequestQueue.execute()
    }private var mWifiScanCallback: IWifiScanCallback? = null


    /**
     * 蓝牙配网
     */
    fun startWifiSetup(ssid:String,pwd:String,wifiSetupCallback: IWifiSetupCallback){
        mGatt ?:return
        this.mWifiSetupCallback = wifiSetupCallback
        //获取特征
        val characteristic = GattUtils.getCharacteristic(mGatt,
                BluetoothClient.WIFI_SERVICE_UUID,
                BluetoothClient.WIFI_CHARACTERISTIC_UUID)
        val b = ByteArray(78){ 0x00 }
        b[0] = 0x01

        var str = "wifisetup"
        var i = 1
        str.forEach {char-> b[i++] = char.toByte() }
        i = 13
        ssid.forEach {char-> b[i++] = char.toByte() }
        i = 45
        pwd.forEach {char-> b[i++] = char.toByte() }
        b[77] = 0x04
        characteristic.value = b
        mRequestQueue.addWriteCharacteristic(mGatt, characteristic)
        mRequestQueue.execute()

    }private var mWifiSetupCallback:IWifiSetupCallback? = null


    /**
     * 断开连接
     */
    fun disconnect(){
        AppExecutors.get().diskIO().execute {
            mGatt?.disconnect()
        }
    }

    private val lock = Any()
    operator fun next() {
        synchronized(lock) { mRequestQueue.next() }
    }

    internal interface ILeSessionCallback{
        /**
         * gatt会话建立成功回调
         * @param session 会话
         */
        fun onSessionOpen(session: Session)
        /**
         * gatt会话建立失败回调
         * @param session 会话
         */
        fun onSessionClose(session: Session)
    }
}