package com.hnradio.common.util.zxing.activity

import android.content.Intent
import android.graphics.Bitmap
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Vibrator
import android.view.SurfaceHolder
import com.alibaba.android.arouter.facade.annotation.Route
import com.google.zxing.BarcodeFormat
import com.google.zxing.Result
import com.hnradio.common.R
import com.hnradio.common.base.BaseActivity
import com.hnradio.common.base.BaseViewModel
import com.hnradio.common.databinding.ActivityScanBinding
import com.hnradio.common.router.MainRouter
import com.hnradio.common.util.ToastUtils
import com.hnradio.common.util.zxing.camera.CameraManager
import com.hnradio.common.util.zxing.decoding.CaptureActivityHandler
import com.hnradio.common.util.zxing.decoding.InactivityTimer
import com.hnradio.common.util.zxing.view.ViewfinderView
import java.io.IOException
import java.util.*

/**
 *
 * @Description: java类作用描述
 * @Author: huqiang
 * @CreateDate: 2021-09-22 20:42
 * @Version: 1.0
 */
class CaptureActivity : BaseActivity<ActivityScanBinding, CaptureModel>(),SurfaceHolder.Callback {

    companion object {
        /**
         * 请求标志位
         */
        const val   requestCode = 0x31
        /**
         * 结果标志位
         */
        const val  resultCode = 0x32
    }


    private val VIBRATE_DURATION = 200L
    private var handler: CaptureActivityHandler? = null
    private var hasSurface = false
    private var decodeFormats: Vector<BarcodeFormat>? = null
    private var characterSet: String? = null
    private var inactivityTimer: InactivityTimer? = null
    private var mediaPlayer: MediaPlayer? = null
    private var playBeep = false
    private val BEEP_VOLUME = 0.10f
    private var vibrate = false
    private val TAG = "CaptureActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        CameraManager.init(application)
        hasSurface = false
        inactivityTimer = InactivityTimer(this)
    }

    override fun onResume() {
        super.onResume()
        val surfaceHolder: SurfaceHolder = viewBinding.previewView.getHolder()
        if (hasSurface) {
            initCamera(surfaceHolder)
        } else {
            surfaceHolder.addCallback(this)
        }
        decodeFormats = null
        characterSet = null
        playBeep = true
        val audioService = getSystemService(AUDIO_SERVICE) as AudioManager
        if (audioService.ringerMode != AudioManager.RINGER_MODE_NORMAL) {
            playBeep = false
        }
        initBeepSound()
        vibrate = true
    }
    override fun onPause() {
        super.onPause()
        if (handler != null) {
            handler!!.quitSynchronously()
            handler = null
        }
        CameraManager.get().closeDriver()
    }
    private fun initBeepSound() {
        if (playBeep && mediaPlayer == null) {
            // The volume on STREAM_SYSTEM is not adjustable, and users found it
            // too loud,
            // so we now play on the music stream.
            volumeControlStream = AudioManager.STREAM_MUSIC
            mediaPlayer = MediaPlayer()
            mediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
            mediaPlayer?.setOnCompletionListener {
             it.seekTo(0)
            }
            val file = resources.openRawResourceFd(
                R.raw.beep
            )
            try {
                mediaPlayer?.setDataSource(
                    file.fileDescriptor,
                    file.startOffset, file.length
                )
                file.close()
                mediaPlayer?.setVolume(BEEP_VOLUME,BEEP_VOLUME)
                mediaPlayer?.prepare()
            } catch (e: IOException) {
                mediaPlayer = null
            }
        }
    }

    private fun initCamera(surfaceHolder: SurfaceHolder) {
        try {
            CameraManager.get().openDriver(surfaceHolder)
        } catch (ioe: IOException) {
            return
        } catch (e: RuntimeException) {
            return
        }
        if (handler == null) {
            handler = CaptureActivityHandler(
                this, decodeFormats,
                characterSet
            )
        }
    }

    override fun getTitleText(): String {
        return "扫一扫"
    }

    fun getViewfinderView(): ViewfinderView? {
        return viewBinding.viewfinderView
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        if (!hasSurface) {
            hasSurface = true
            initCamera(holder)
        }
    }
    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        hasSurface = false
   }

    private fun playBeepSoundAndVibrate() {
        if (playBeep && mediaPlayer != null) {
            mediaPlayer!!.start()
        }
        if (vibrate) {
            val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VIBRATE_DURATION)
        }
    }

    fun handleDecode(result: Result, barcode: Bitmap?) {
        inactivityTimer!!.onActivity()
        playBeepSoundAndVibrate()
        val resultString = result.text
        if (resultString.isEmpty()) {
            ToastUtils.show("Scan failed!")
        } else {
            val intent: Intent = intent
            val bundle = Bundle()
            bundle.putString("result", resultString)
            intent.putExtra("CaptureResult", bundle)
            setResult(resultCode, intent)
            finish()
        } /*else {
            Toast.makeText(context, "无法识别", Toast.LENGTH_SHORT).show();
			if (handler != null) {
				handler.restartPreviewAndDecode();
			}
		}*/
    }
    fun drawViewfinder() {
        viewBinding.viewfinderView.drawViewfinder()
    }

    fun getHandler(): Handler? {
        return handler
    }


}


class CaptureModel : BaseViewModel() {

}