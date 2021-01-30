package com.smartwasp.assistant.app.util

import com.orhanobut.logger.Logger

/**
 * @name :      SingletonHolder
 * @author :    luotao
 * @date :      2020/12/23 12:12
 * @description :
 */
open class SingletonHolder<out T, in A>(creator: (A) -> T) {

    private var creator: ((A) -> T)? = creator
    @Volatile
    private var instance: T? = null
    val a = null

    fun getInstance(arg: A): T {
        val i = instance
        if (i != null) {
            return i
        }

        return synchronized(this) {
            val i2 = instance
            if (i2 != null) {
                i2
            } else {
                val created = creator!!(arg)
                instance = created
                creator = null
                created
            }
        }
    }

//    fun getInstance2(arg: A): T =
//        instance ?: synchronized(this) {
//            instance ?: creator!!(arg).apply {
//                instance = this
//            }
//        }

}