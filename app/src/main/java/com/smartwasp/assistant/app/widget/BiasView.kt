package com.smartwasp.assistant.app.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import com.orhanobut.logger.Logger
import com.smartwasp.assistant.app.R
import kotlin.math.absoluteValue
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Created by luotao on 2021/1/27 09:45
 * E-Mail Address：gtkrockets@163.com
 */
class BiasView @JvmOverloads constructor(context: Context,
                                         attrs: AttributeSet? = null,
                                         defStyleAttr: Int = 0,private var textValue:String? = null) :View(context,attrs,defStyleAttr) {

    init {
        val textValue = attrs?.getAttributeValue("http://schemas.android.com/apk/res/android", "text")
        setText(textValue)
    }

    //背景画笔
    private val paint by lazy {
        Paint().also {
            it.color = resources.getColor(R.color.smartwasp_blue1)
        }
    }

    private val paint1 by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).also {
            it.color = resources.getColor(R.color.smartwasp_yellow1)
            it.textSize = resources.displayMetrics.scaledDensity * 11
        }
    }

    /**
     * 赋值
     * @param text @2131689582 or text
     */
    fun setText(text: String?){
        textValue = text
        textValue?.let {
            if(it.startsWith("@")){
                setText(it.substring(1).toInt())
            }
        }
    }

    /**
     * 赋值
     * @param text
     */
    fun setText(text: Int){
        if(text > 0){
            textValue = resources.getString(text)
        }
    }

    /**
     * 绘画
     * @param canvas 画布
     */
    override fun onDraw(canvas: Canvas?) {
        canvas ?: return
        val m = min(measuredWidth,measuredHeight).toFloat()
        val mLen = m / 2f
        val offset = 16 * resources.displayMetrics.density
        val path = Path().apply {
            moveTo(0f,mLen - offset)
            lineTo(0f,mLen + offset)
            lineTo(mLen - offset,m)
            lineTo(mLen + offset,m)
            close()
        }
        canvas.drawPath(path,paint)
        textValue?.let {
            val textWith = paint1.measureText(it)
            val textLen = sqrt((0f - mLen).pow(2) + (mLen - m).pow(2))
            canvas.drawTextOnPath(it,Path().apply {
                moveTo(0f,mLen)
                lineTo(mLen,m)
            },(textLen - textWith) / 2,
                    paint1.fontMetrics.descent.absoluteValue,paint1)
        }
    }
}