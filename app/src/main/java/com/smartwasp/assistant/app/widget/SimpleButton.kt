package com.smartwasp.assistant.app.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.Button

/**
 * Created by luotao on 2021/1/29 15:51
 * E-Mail Address：gtkrockets@163.com
 */
class SimpleButton @JvmOverloads constructor(context: Context,
                                             attrs: AttributeSet? = null,
                                             defStyleAttr: Int = 0) :Button (context,attrs,defStyleAttr)  {

    override fun drawableStateChanged() {
        val buttonDrawable: Drawable = background
        if(isSelected){
            buttonDrawable.state[buttonDrawable.state.size - 1] = android.R.attr.state_checked
            drawableState[drawableState.size - 1] = android.R.attr.state_checked
        }
        if (buttonDrawable != null && buttonDrawable.isStateful && buttonDrawable.setState(drawableState)) {
            invalidateDrawable(buttonDrawable)
        }
    }
}