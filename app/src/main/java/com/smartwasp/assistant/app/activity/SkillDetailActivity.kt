package com.smartwasp.assistant.app.activity

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import com.bumptech.glide.Glide
import com.orhanobut.logger.Logger
import com.smartwasp.assistant.app.R
import com.smartwasp.assistant.app.base.BaseActivity
import com.smartwasp.assistant.app.base.BaseViewModel
import com.smartwasp.assistant.app.base.SmartApp
import com.smartwasp.assistant.app.bean.SkillBean
import com.smartwasp.assistant.app.databinding.ActivitySkillDetailBinding
import com.smartwasp.assistant.app.util.IFLYOS
import com.smartwasp.assistant.app.util.LoadingUtil
import kotlinx.android.synthetic.main.fragment_mine.view.*
import kotlinx.android.synthetic.main.layout_toolbar.*

class SkillDetailActivity : BaseActivity<BaseViewModel,ActivitySkillDetailBinding>() {

    override val layoutResID: Int = R.layout.activity_skill_detail

    lateinit var skillBean:SkillBean

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTittle(getString(R.string.ic_skill_detail))
        media_icon?.let {
            it.visibility = View.GONE
        }
        intent.getSerializableExtra(IFLYOS.EXTRA)?.let {
            skillBean = it as SkillBean
            mBinding.skillTitle1 = skillBean.skillName
            mBinding.skillDesc1 = skillBean.skillDesc
            mBinding.skillWordsCom1 = "您可以说 ${skillBean.hitTextS[0]}"
            mBinding.kaifazhe = skillBean.developer
            mBinding.banben = skillBean.version
            mBinding.gengxinshijian = skillBean.updateTime
            Glide.with(this)
                    .load(skillBean.icon)
                    .dontAnimate()
                    .error(R.drawable.ic_warning_black_24dp)
                    .into(mBinding.bezelImageView)
            addWords()

        } ?: kotlin.run {
            LoadingUtil.showToast(SmartApp.app,"暂无技能详情")
            finish()
        }
    }

    /**
     * 添加对话
     */
    private fun addWords(){
        val container = mBinding.container
        var i = 0
        skillBean.hitTextS.forEach {
            val txt = LayoutInflater.from(this).inflate(R.layout.layout_words,container,false) as AppCompatTextView
            txt.text = it
//            if(i++ % 2 == 0){
//                txt.background = getDrawable(R.drawable.shape_dialog_orange)
//            }else{
//                txt.background = getDrawable(R.drawable.shape_dialog_dark)
//                val lp = txt.layoutParams as LinearLayout.LayoutParams
//                lp.gravity = Gravity.RIGHT
//            }
            container.addView(txt)
        }
    }

    /**
     * 添加点击事件
     */
    override fun onButtonClick(v: View){
        super.onButtonClick(v)
    }
}