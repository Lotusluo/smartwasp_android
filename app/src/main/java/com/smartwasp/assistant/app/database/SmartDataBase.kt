package com.smartwasp.assistant.app.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * 数据库
 */
@Database(entities = [DevicesEntity::class],version = 1,exportSchema = false)
abstract class SmartDataBase:RoomDatabase(){
    companion object{
        private const val DB_NAME = "SmartDataBase.db"
        private var instance:SmartDataBase? = null
        fun getInstance(context: Context):SmartDataBase{
            if(null == instance){
                synchronized(SmartDataBase::class){
                    instance = Room.databaseBuilder(context, SmartDataBase::class.java, DB_NAME)
                            .fallbackToDestructiveMigration()
                            .build()
                }
            }
            return instance!!
        }
    }

    abstract fun getDevicesDao():DevicesDao
}