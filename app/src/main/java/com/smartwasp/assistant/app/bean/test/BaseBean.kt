package com.smartwasp.assistant.app.bean.test

import java.io.Serializable

/**
 * Created by luotao on 2021/2/1 11:32
 * E-Mail Address：gtkrockets@163.com
 */
data class BaseBean<T>(var errCode:Int,var errMsg:String,var data: T):Serializable