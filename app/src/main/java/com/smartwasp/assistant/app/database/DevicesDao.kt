package com.smartwasp.assistant.app.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

/**
 * 设备操作DAO
 */
@Dao
interface DevicesDao {
    /**
     * 插入一个设备
     * @param device 一个设备
     */
    @Insert
    fun insertDevice(device:DevicesEntity)

    @Query("select * from devices")
    fun getDevices():List<DevicesEntity>

    @Delete
    fun deleteDevice(device:DevicesEntity)
}