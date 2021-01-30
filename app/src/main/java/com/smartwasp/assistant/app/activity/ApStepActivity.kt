package com.smartwasp.assistant.app.activity

import android.os.Bundle
import android.view.View
import com.smartwasp.assistant.app.R
import com.smartwasp.assistant.app.base.BaseActivity
import com.smartwasp.assistant.app.base.BaseViewModel
import com.smartwasp.assistant.app.base.addFragmentByTag
import com.smartwasp.assistant.app.databinding.ActivityApStepBinding
import com.smartwasp.assistant.app.fragment.aps.ApStepFragment1

/**
 * Created by luotao on 2021/1/28 16:17
 * E-Mail Address：gtkrockets@163.com
 * AP配网步骤
 */
class ApStepActivity : BaseActivity<BaseViewModel,ActivityApStepBinding>() {

    override val layoutResID: Int = R.layout.activity_ap_step

    /**
     * 生成
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addFragmentByTag(R.id.container,ApStepFragment1.newsInstance())
    }

    /**
     * 添加点击事件
     */
    override fun onButtonClick(v: View){
        super.onButtonClick(v)
    }
}