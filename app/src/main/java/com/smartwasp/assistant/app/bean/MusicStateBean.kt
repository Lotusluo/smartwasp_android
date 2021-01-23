package com.smartwasp.assistant.app.bean

import java.io.Serializable


/**
 * Created by luotao on 2021/1/21 17:18
 * E-Mail Address：gtkrockets@163.com
 */
data class MusicStateBean(var music_player:MusicPlayerBean,
                          var music:SongBean,
                          var device_id:String,
                          var speaker:SpeakerBean): Serializable