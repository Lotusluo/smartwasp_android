package com.smartwasp.assistant.app.activity

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import com.google.zxing.BarcodeFormat
import com.google.zxing.Result
import com.orhanobut.logger.Logger
import com.smartwasp.assistant.app.R
import com.smartwasp.assistant.app.util.IFLYOS
import me.dm7.barcodescanner.core.zxing.ZXingScannerView

class ScanActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {

    companion object {
        const val REQUEST_CAMERA_CODE = 10017
        const val REQUEST_WEB_CONFIG_CODE = 10018
        private const val FLASH_STATE = "FLASH_STATE"
        private const val AUTO_FOCUS_STATE = "AUTO_FOCUS_STATE"
        private const val SELECTED_FORMATS = "SELECTED_FORMATS"
        private const val CAMERA_ID = "CAMERA_ID"
    }

    private lateinit var qrView: ZXingScannerView

    private var mFlash: Boolean = false
    private var mAutoFocus: Boolean = false
    private var mSelectedIndices: ArrayList<Int>? = null
    private var mCameraId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.statusBarColor = Color.TRANSPARENT

        if (savedInstanceState != null) {
            mFlash = savedInstanceState.getBoolean(FLASH_STATE, false)
            mAutoFocus = savedInstanceState.getBoolean(AUTO_FOCUS_STATE, true)
            mSelectedIndices = savedInstanceState.getIntegerArrayList(SELECTED_FORMATS)
            mCameraId = savedInstanceState.getInt(CAMERA_ID, -1)
        } else {
            mFlash = false
            mAutoFocus = true
            mSelectedIndices = null
            mCameraId = -1
        }

        val contentFrame = findViewById<ViewGroup>(R.id.content_frame)
        qrView = ZXingScannerView(this)
        setupFormats()
        contentFrame.addView(qrView)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !=
                PermissionChecker.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_CODE)
        } else {
            startScan()
        }
    }

    private fun setupFormats() {
        val formats = ArrayList<BarcodeFormat>()
        if (mSelectedIndices == null || mSelectedIndices!!.isEmpty()) {
            mSelectedIndices = ArrayList()
            for (i in 0 until ZXingScannerView.ALL_FORMATS.size) {
                mSelectedIndices!!.add(i)
            }
        }

        for (index in mSelectedIndices!!) {
            formats.add(ZXingScannerView.ALL_FORMATS[index])
        }
        qrView.setFormats(formats)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(FLASH_STATE, mFlash)
        outState.putBoolean(AUTO_FOCUS_STATE, mAutoFocus)
        outState.putIntegerArrayList(SELECTED_FORMATS, mSelectedIndices)
        outState.putInt(CAMERA_ID, mCameraId)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_CODE) {
            startScan()
        }
    }

    private fun startScan() {
        qrView.postDelayed({
            qrView.setBorderColor(Color.WHITE)
            qrView.setLaserColor(Color.WHITE)
            qrView.setLaserEnabled(true)
            qrView.setScanTipsEnabled(false)
            qrView.setAutoFocus(true)
            qrView.startCamera()
            qrView.setResultHandler(this)
        }, 300)
    }

    override fun handleResult(rawResult: Result?) {
        Log.i("Scan", "scan result text: " + rawResult?.text)
        val result = rawResult?.text
        if (result != null && result.contains("oauth/device?user_code")) {
            vibrate()
            startWebConfig(result)
            finish()
        } else {
            AlertDialog.Builder(this)
                    .setMessage("二维码数据不合法")
                    .setPositiveButton(android.R.string.yes, null)
                    .setOnDismissListener {
                        qrView.resumeCameraPreview(this)
                    }
                    .show()
        }
    }

    override fun onResume() {
        super.onResume()
        qrView.resumeCameraPreview(this)
    }

    private fun vibrate() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= 26)
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        else
            vibrator.vibrate(20)
    }

    private fun startWebConfig(result: String?) {
        setResult(Activity.RESULT_OK,Intent().apply {
            putExtra(IFLYOS.EXTRA,result)
        })
    }

    override fun onStop() {
        super.onStop()
        qrView.stopCamera()
    }
}