package com.smartwasp.assistant.app.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 构建绑定的设备表
 */
@Entity(tableName = "devices")
class DevicesEntity(
                 @PrimaryKey(autoGenerate = true)
                 var id:Int = 0,
                 @ColumnInfo(name = "dev_platform")
                 var client_id:String,
                 @ColumnInfo(name = "dev_id")
                 var device_id:String) {
}