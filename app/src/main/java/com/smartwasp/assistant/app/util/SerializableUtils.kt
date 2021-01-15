package com.smartwasp.assistant.app.util

import java.io.*

/**
 * Created by luotao on 2021/1/12 13:37
 * E-Mail Address：gtkrockets@163.com
 */
object SerializableUtils {

    //序列化对象为String字符串，先对序列化后的结果进行BASE64编码，否则不能直接进行反序列化
    fun writeObject(deserialize: Any): String? {
        synchronized(SerializableUtils::class.java) {
            return runCatching {
                val bos = ByteArrayOutputStream()
                val oos = ObjectOutputStream(bos)
                oos.writeObject(deserialize)
                oos.flush()
                oos.close()
                bos.close()
                String(bos.toByteArray(), charset("ISO-8859-1"))
            }.onSuccess {
                it
            }.onFailure {
                null
            }.getOrNull()
        }
    }

    //反序列化String字符串为对象
    fun readObject(serialize: String): Any? {
        synchronized(SerializableUtils::class.java) {
            return runCatching {
                val bis = ByteArrayInputStream(serialize.toByteArray(charset("ISO-8859-1")))
                val ois = ObjectInputStream(bis)
                var o: Any? = ois.readObject()
                bis.close()
                ois.close()
                o
            }.onSuccess {
                it
            }.onFailure {
                null
            }.getOrNull()
        }
    }
}