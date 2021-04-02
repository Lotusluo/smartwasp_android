package com.smartwasp.assistant.blesdk

// Created by luotao on 2021/3/30
data class WifiInfo(private val ssid:String,
                    private val rssi:String,
                    private val flags:String) {

    /**
     * 获取wifi名称
     */
    fun getSsid():String{
        return ssid
    }
}