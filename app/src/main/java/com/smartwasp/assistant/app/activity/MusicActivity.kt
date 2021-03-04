package com.smartwasp.assistant.app.activity

import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.orhanobut.logger.Logger
import com.smartwasp.assistant.app.R
import com.smartwasp.assistant.app.base.BaseActivity
import com.smartwasp.assistant.app.base.SmartApp
import com.smartwasp.assistant.app.bean.DeviceBean
import com.smartwasp.assistant.app.bean.MusicStateBean
import com.smartwasp.assistant.app.bean.StatusBean
import com.smartwasp.assistant.app.databinding.ActivityMusicBinding
import com.smartwasp.assistant.app.util.AppExecutors
import com.smartwasp.assistant.app.util.IFLYOS
import com.smartwasp.assistant.app.util.LoadingUtil
import com.smartwasp.assistant.app.viewModel.*
import kotlinx.android.synthetic.main.activity_music.*
import kotlinx.android.synthetic.main.fragment_music_item.bezelImageView
import kotlinx.android.synthetic.main.layout_device_header.*
import kotlinx.android.synthetic.main.layout_device_header.media_icon

class MusicActivity : BaseActivity<MusicBindModel,ActivityMusicBinding>() {

    override val layoutResID: Int = R.layout.activity_music

    //设备状态观察者
    private val devicesStateObserver: MutableLiveData<StatusBean<DeviceBean>> = MutableLiveData()
    //媒体状态
    private val mediaStateObserver: MutableLiveData<StatusBean<MusicStateBean>> = MutableLiveData()

    //当前的设备
    private val currentDevice by lazy {
        intent.getSerializableExtra(IFLYOS.DEVICE_ID) as DeviceBean
    }
    //当前的媒体状态
    private var currentMedia:StatusBean<MusicStateBean>? = null
        set(value) {
            field = value
            notifyMediaChanged()
        }

    /**
     * 狗赞
     * @param savedInstanceState
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent.getSerializableExtra(IFLYOS.DEVICE_ID) ?: finish()
        mViewModel.deviceID = currentDevice.device_id
        media_icon.visibility = View.GONE
        mBinding.deviceBean = currentDevice
        icon_device_status.isSelected = currentDevice.isOnline()
        currentMedia = SmartApp.activity?.mediaState
        initObserver()
        mViewModel.deviceID = currentDevice.device_id
        mViewModel.mediaControl.observe(this, Observer {
            if(it.isSuccess){

            }else{
                LoadingUtil.showToast(this,getString(R.string.try_again))
            }
        })
        seekBar.setOnSeekBarChangeListener(object :SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(fromUser){
                    if(checkOffline())
                        return
                    mViewModel.mediaControlFun(VOLUME,progress.toString())
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    /**
     * 初始化观察者
     */
    private fun initObserver(){
        SmartApp.addDevStateObserver(devicesStateObserver)
        SmartApp.addMediaObserver(mediaStateObserver)
        devicesStateObserver.observe(this, Observer {state->
            //与本地的设备列表比较,更新离在线状态
            currentDevice?.let {
                if(it.device_id == state.data.device_id){
                    if(it.timestamp.toLong() < state.timestamp.toLong()){
                        //通知有设备的状态改变
                        it.status = state.data.status
                        it.timestamp = state.timestamp
                        mBinding.deviceBean = currentDevice
                        icon_device_status.isSelected = currentDevice.isOnline()
                    }
                }
            }
        })

        mediaStateObserver.observe(this, Observer {state->
            currentMedia?.let {
                if(it.data.device_id == state.data.device_id){
                    if(it.timestamp.toLong() < state.timestamp.toLong()){
                        currentMedia = state
                    }
                }
            }?: kotlin.run {
                currentMedia = state
            }
        })
    }

//    延迟执行音量刷新
    private val runnable = Runnable {
    currentMedia?.data?.let {
        it.speaker?.let {sb->
            seekBar.progress = sb.volume
        }
    }
    }

    /**
     * 设备媒体状态改变
     */
    private fun notifyMediaChanged(){
        currentMedia?.data?.let {
            Glide.with(this)
                    .asBitmap()
                    .error(R.drawable.ic_warning_black_24dp)
                    .load(it.music?.image)
                    .into(bezelImageView)
            it.music?.let {song->
                mediaName.text = song.name
                mediaArt.text = song.artist
            }
            btnPlayer.isSelected = it.music_player.playing
            AppExecutors.get().mainThread().removeCallbacks(runnable)
            AppExecutors.get().mainThread().executeDelay(runnable,400)
        }?: kotlin.run {
            btnPlayer.isSelected = false
        }
    }

    /**
     * 交互态
     */
    override fun onResume() {
        super.onResume()
        notifyMediaChanged()
    }

    /**
     * 销毁
     */
    override fun onDestroy() {
        super.onDestroy()
        SmartApp.removeDevObserver(devicesStateObserver)
        SmartApp.removeMediaObserver(mediaStateObserver)
        AppExecutors.get().mainThread().removeCallbacks(runnable)
    }

    /**
     * 按钮点击
     * @param v
     */
    override fun onButtonClick(v: View) {
        super.onButtonClick(v)
        if(checkOffline())
            return
        when(v.id){
            R.id.btnPre ->{
                //上一首
                mViewModel.mediaControlFun(PREV)
            }
            R.id.btnPlayer ->{
                if(v.isSelected){
                    //暂停
                    mViewModel.mediaControlFun(PAUSE)
                }else{
                    //继续
                    mViewModel.mediaControlFun(RESUME)
                }
            }
            R.id.btnAft ->{
                //下一首
                mViewModel.mediaControlFun(NEXT)
            }
        }
    }

    //提示框
    private fun checkOffline():Boolean{
        currentDevice?.let {
            if(!it.isOnline()){
                LoadingUtil.showToast(this,getString(R.string.offline))
                return true
            }
        }
        return false
    }
}