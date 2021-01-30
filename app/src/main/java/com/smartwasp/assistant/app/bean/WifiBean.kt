package com.smartwasp.assistant.app.bean

/**
 * Created by luotao on 2021/1/29 18:15
 * E-Mail Address：gtkrockets@163.com
 */

data class WifiBean(var ssid:String,var bssid:String,private var capabilities:String,var level:Int = 0,var linkType:Int = 1){

    /**
     * 获取加密类型
     * @return 返回加密类型
     */
    fun getWifiCipherType():WifiCipherType{
        return when {
            capabilities.contains(WifiCipherType.WIFICIPHER_WEP.encrypt) -> {
                WifiCipherType.WIFICIPHER_WEP
            }
            capabilities.contains(WifiCipherType.WIFICIPHER_WPA.encrypt) -> {
                WifiCipherType.WIFICIPHER_WPA
            }
            capabilities.contains(WifiCipherType.WIFICIPHER_WPA2.encrypt) -> {
                WifiCipherType.WIFICIPHER_WPA2
            }
            else -> {
                WifiCipherType.WIFICIPHER_NOPASSWD
            }
        }
    }

    /**
     * WIFI加密方式枚举
     * @param encrypt
     */
    enum class WifiCipherType(val encrypt: String) {
        WIFICIPHER_WEP("wep"),
        WIFICIPHER_WPA("wpa"),
        WIFICIPHER_WPA2("wpa2"),
        WIFICIPHER_NOPASSWD("noPassWd");
    }

    /**
     * 判定两个对象是否相等
     * @param other 比较对象
     */
    override fun equals(other: Any?): Boolean {
        other ?: return false
        if(other !is WifiBean) return false
        return other.ssid == ssid && other.bssid == bssid
    }
}





