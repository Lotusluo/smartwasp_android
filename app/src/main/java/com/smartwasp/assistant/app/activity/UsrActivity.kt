package com.smartwasp.assistant.app.activity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.smartwasp.assistant.app.R
import com.smartwasp.assistant.app.base.BaseActivity
import com.smartwasp.assistant.app.base.SmartApp
import com.smartwasp.assistant.app.databinding.ActivityUsrBinding
import com.smartwasp.assistant.app.util.ConfigUtils
import com.smartwasp.assistant.app.util.IFLYOS
import com.smartwasp.assistant.app.util.LoadingUtil
import com.smartwasp.assistant.app.viewModel.UsrModel
import kotlinx.android.synthetic.main.layout_toolbar.*

/**
 * Created by luotao on 2021/4/2 15:21
 * E-Mail Address：gtkrockets@163.com
 */
class UsrActivity : BaseActivity<UsrModel,ActivityUsrBinding>() {

    override val layoutResID: Int = R.layout.activity_usr

    /**
     * 生成
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTittle(getString(R.string.usrCenter))
        media_icon?.let {
            it.visibility = View.GONE
        }
        SmartApp.userBean?.let {
            mBinding.phone = it.phone
            mBinding.uid = it.user_id
        }
    }

    /**
     * 添加点击事件
     */
    override fun onButtonClick(v: View){
        super.onButtonClick(v)
        when(v.id){
            R.id.copyBtn->{
                //获取剪贴板管理器：
                val cm: ClipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val mClipData: ClipData = ClipData.newPlainText("Label", mBinding.uid )
                cm.setPrimaryClip(mClipData)
                LoadingUtil.showToast(SmartApp.app,"已经复制到剪贴板")
            }
            R.id.exitBtn->{
                AlertDialog.Builder(this)
                        .setMessage(R.string.exit_confirm)
                        .setNegativeButton(android.R.string.cancel,null)
                        .setPositiveButton(android.R.string.ok) {
                            _, _ ->
                            LoadingUtil.create(this)
                            mViewModel!!.loginOut().observe(this, androidx.lifecycle.Observer {
                                if(it == IFLYOS.OK){
                                    ConfigUtils.removeAll()
                                    v.postDelayed({
                                        SmartApp.restart()
                                    },1000)
                                }else{
                                    LoadingUtil.dismiss()
                                    LoadingUtil.showToast(SmartApp.app,getString(R.string.try_again))
                                }
                            })

                        }
                        .show()

            }

        }
    }
}