package com.smartwasp.assistant.app.widget

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.orhanobut.logger.Logger
import com.smartwasp.assistant.app.R

/**
 * Created by luotao on 2021/1/7 17:20
 * E-Mail Address：gtkrockets@163.com
 */
class BezelImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : AppCompatImageView(context,attrs,defStyleAttr) {

    //样式遮罩
    private var mMaskDrawable: Drawable? = null
    //样式画笔
    private val mMaskedPaint:Paint
    private val mCopyPaint: Paint

    //图形区域
    private var mBounds: Rect? = null
    private var mBoundsF: RectF? = null

    private val mPfd:PaintFlagsDrawFilter

    //初始化
    init {

        val a = context.obtainStyledAttributes(
            attrs, R.styleable.BezelImageView,
            defStyleAttr, 0
        )

        mMaskDrawable = a.getDrawable(R.styleable.BezelImageView_maskDrawable)
        mMaskDrawable?.let {
            it.callback = this
        }

        a.recycle()

        mMaskedPaint = Paint().apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP)
            isFilterBitmap = true
            isAntiAlias = true
        }

        mCopyPaint = Paint()

        mPfd = PaintFlagsDrawFilter(0, Paint.FILTER_BITMAP_FLAG)

    }

    override fun setFrame(l: Int, t: Int, r: Int, b: Int): Boolean {
        val changed = super.setFrame(l, t, r, b)
        mBounds = Rect(0, 0, r - l, b - t)
        mBoundsF = RectF(mBounds)
        mMaskDrawable?.let {
            it.bounds = mBounds!!
        }
        return changed
    }

    override fun onDraw(canvas: Canvas?) {
        canvas ?: return
        val sc = canvas.saveLayer(mBoundsF, mCopyPaint)
        mMaskDrawable?.let {
            it.draw(canvas)
        }
        canvas.saveLayer(mBoundsF, mMaskedPaint)
        canvas.drawFilter = mPfd
        super.onDraw(canvas)
        canvas.restoreToCount(sc)
    }



    override fun drawableStateChanged() {
        super.drawableStateChanged()
        if(null != mMaskDrawable && mMaskDrawable!!.isStateful){
            mMaskDrawable!!.state = drawableState
        }
        invalidate()
    }
}