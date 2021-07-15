package com.smartwasp.assistant.app.util

import android.content.Context
import android.os.AsyncTask
import android.os.UserHandle
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import com.smartwasp.assistant.app.R
import java.util.*

/**
 * Fake com.android.settingslib.display.DisplayDensityUtils
 *
 * App requires as a SYSTEM APP, uid as android.uid.system
 *
 * create by seasonyuu
 */
class DisplayDensityUtils {
    companion object {
        private const val LOG_TAG = "DisplayDensityUtils"

        /**
         * Minimum increment between density scales.
         */
        private val MIN_SCALE_INTERVAL = 0.09f

        /**
         * Minimum density scale. This is available on all devices.
         */
        private val MIN_SCALE = 0.85f

        /**
         * Maximum density scale. The actual scale used depends on the device.
         */
        private val MAX_SCALE = 1.50f

        /**
         * Summary used for "default" scale.
         */
        val SUMMARY_DEFAULT = R.string.screen_zoom_summary_default

        /**
         * Summary used for "custom" scale.
         */
        private val SUMMARY_CUSTOM = R.string.screen_zoom_summary_custom

        /**
         * Summaries for scales smaller than "default" in order of smallest to
         * largest.
         */
        private val SUMMARIES_SMALLER = intArrayOf(R.string.screen_zoom_summary_small)

        /**
         * Summaries for scales larger than "default" in order of smallest to
         * largest.
         */
        private val SUMMARIES_LARGER = intArrayOf(
            R.string.screen_zoom_summary_large,
            R.string.screen_zoom_summary_very_large,
            R.string.screen_zoom_summary_extremely_large
        )

        /**
         * Minimum allowed screen dimension, corresponds to resource qualifiers
         * "small" or "sw320dp". This value must be at least the minimum screen
         * size required by the CDD so that we meet developer expectations.
         */
        private const val MIN_DIMENSION_DP = 320

        private fun constrain(amount: Int, low: Int, high: Int): Int {
            return if (amount < low) low else if (amount > high) high else amount
        }

        private fun constrain(amount: Long, low: Long, high: Long): Long {
            return if (amount < low) low else if (amount > high) high else amount
        }

        private fun constrain(amount: Float, low: Float, high: Float): Float {
            return if (amount < low) low else if (amount > high) high else amount
        }

        private fun getContextDisplay(context: Context): Display? {
            return try {
                val getDisplay = Context::class.java.getDeclaredMethod("getDisplay")
                getDisplay.isAccessible = true
                getDisplay.invoke(context) as Display
            } catch (throwable: Throwable) {
                throwable.printStackTrace()
                null
            }

        }

        private fun getDefaultDisplayDensity(displayId: Int): Int {
            return try {
                val wm = getWindowManagerService()
                val wmc = Class.forName("android.view.IWindowManager")
                val getInitialDisplayDensity =
                    wmc.getDeclaredMethod(
                        "getInitialDisplayDensity",
                        Int::class.java
                    )
                getInitialDisplayDensity.isAccessible = true
                getInitialDisplayDensity.invoke(wm, displayId) as Int
            } catch (exc: Exception) {
                -1
            }

        }


        /**
         * Asynchronously applies display density changes to the specified display.
         *
         *
         * The change will be applied to the user specified by the value of
         * [UserHandle.myUserId] at the time the method is called.
         *
         * @param displayId the identifier of the display to modify
         */
        fun clearForcedDisplayDensity(displayId: Int) {
            val userId = newUserHandle("CURRENT")?.hashCode()
            AsyncTask.execute {
                try {
                    val wm = getWindowManagerService()

                    val wmc = Class.forName("android.view.IWindowManager")
                    val clearForcedDisplayDensityForUser = wmc.getMethod(
                        "clearForcedDisplayDensityForUser",
                        Int::class.java,
                        Int::class.java
                    )
                    clearForcedDisplayDensityForUser.isAccessible = true
                    clearForcedDisplayDensityForUser.invoke(wm, displayId, userId)
                } catch (exc: Exception) {
                    Log.w(LOG_TAG, "Unable to clear forced display density setting")
                }
            }
        }

        /**
         * Asynchronously applies display density changes to the specified display.
         *
         *
         * The change will be applied to the user specified by the value of
         * [UserHandle.myUserId] at the time the method is called.
         *
         * @param displayId the identifier of the display to modify
         * @param density the density to force for the specified display
         */
        fun setForcedDisplayDensity(displayId: Int, density: Int) {
            val userId = newUserHandle("CURRENT")?.hashCode()
            AsyncTask.execute {
                try {
                    val wm = getWindowManagerService()

                    val wmc = Class.forName("android.view.IWindowManager")
                    val setForcedDisplayDensityForUser = wmc.getMethod(
                        "setForcedDisplayDensityForUser",
                        Int::class.java,
                        Int::class.java,
                        Int::class.java
                    )
                    setForcedDisplayDensityForUser.isAccessible = true
                    setForcedDisplayDensityForUser.invoke(wm, displayId,density, userId)
                } catch (exc: Exception) {
                    Log.w(LOG_TAG, "Unable to save forced display density setting")
                }
            }
        }

        private fun getWindowManagerService(): Any? {
            return try {
                val wmg = Class.forName("android.view.WindowManagerGlobal")
                val getWindowManagerService = wmg.getDeclaredMethod("getWindowManagerService")
                getWindowManagerService.isAccessible = true
                getWindowManagerService.invoke(null)
            } catch (t: Throwable) {
                t.printStackTrace()
                null
            }
        }

        private fun newUserHandle(userHandle: String): UserHandle? {
            try {
                val clz = UserHandle::class.java
                val all = clz.getDeclaredField(userHandle)
                all.isAccessible = true
                return all.get(null) as UserHandle
            } catch (t: Throwable) {
                t.printStackTrace()
            }
            return null
        }
    }

    private val mEntries: Array<String>
    private val mValues: Array<Int>

    private val mDefaultDensity: Int
    private val mCurrentIndex: Int

    constructor(context: Context) {
        val defaultDensity = getDefaultDisplayDensity(
            Display.DEFAULT_DISPLAY
        )
        if (defaultDensity <= 0) {
            mEntries = emptyArray()
            mValues = emptyArray()
            mDefaultDensity = 0
            mCurrentIndex = -1
            return
        }

        val res = context.resources
        val metrics = DisplayMetrics()
        getContextDisplay(context)?.getRealMetrics(metrics)

        val currentDensity = metrics.densityDpi
        var currentDensityIndex = -1

        // Compute number of "larger" and "smaller" scales for this display.
        val minDimensionPx = Math.min(metrics.widthPixels, metrics.heightPixels)
        val maxDensity = DisplayMetrics.DENSITY_MEDIUM * minDimensionPx / MIN_DIMENSION_DP
        val maxScale = Math.min(MAX_SCALE, maxDensity / defaultDensity.toFloat())
        val minScale = MIN_SCALE
        val numLarger = constrain(
            (maxScale - 1) / MIN_SCALE_INTERVAL,
            0f, SUMMARIES_LARGER.size.toFloat()
        ).toInt()
        val numSmaller = constrain(
            (1 - minScale) / MIN_SCALE_INTERVAL,
            0f, SUMMARIES_SMALLER.size.toFloat()
        ).toInt()

        var entries = arrayOfNulls<String>(1 + numSmaller + numLarger)
        var values = arrayOfNulls<Int>(entries.size)
        var curIndex = 0

        if (numSmaller > 0) {
            val interval = (1 - minScale) / numSmaller
            for (i in numSmaller - 1 downTo 0) {
                // Round down to a multiple of 2 by truncating the low bit.
                val density = (defaultDensity * (1 - (i + 1) * interval)).toInt() and 1.inv()
                if (currentDensity == density) {
                    currentDensityIndex = curIndex
                }
                entries[curIndex] = res.getString(SUMMARIES_SMALLER[i])
                values[curIndex] = density
                curIndex++
            }
        }

        if (currentDensity == defaultDensity) {
            currentDensityIndex = curIndex
        }
        values[curIndex] = defaultDensity
        entries[curIndex] = res.getString(SUMMARY_DEFAULT)
        curIndex++

        if (numLarger > 0) {
            val interval = (maxScale - 1) / numLarger
            for (i in 0 until numLarger) {
                // Round down to a multiple of 2 by truncating the low bit.
                val density = (defaultDensity * (1 + (i + 1) * interval)).toInt() and 1.inv()
                if (currentDensity == density) {
                    currentDensityIndex = curIndex
                }
                values[curIndex] = density
                entries[curIndex] = res.getString(SUMMARIES_LARGER[i])
                curIndex++
            }
        }

        val displayIndex: Int
        if (currentDensityIndex >= 0) {
            displayIndex = currentDensityIndex
        } else {
            // We don't understand the current density. Must have been set by
            // someone else. Make room for another entry...
            val newLength = values.size + 1
            values = Arrays.copyOf(values, newLength)
            values[curIndex] = currentDensity

            entries = Arrays.copyOf(entries, newLength)
            entries[curIndex] = res.getString(SUMMARY_CUSTOM, currentDensity)

            displayIndex = curIndex
        }

        mDefaultDensity = defaultDensity
        mCurrentIndex = displayIndex
        mEntries = Arrays.copyOf(entries, entries.size)
        mValues = Arrays.copyOf(values, values.size)
    }

    fun getValues() = mValues


    fun getEntries(): Array<String> {
        return mEntries
    }

    fun getCurrentIndex(): Int {
        return mCurrentIndex
    }

    fun getDefaultDensity(): Int {
        return mDefaultDensity
    }
}