package com.project.googlespeechtotext

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.project.googlespeechtotext.face.FaceOverlayView
import com.project.googlespeechtotext.face.FaceRecognitionHelper
import com.project.googlespeechtotext.voice.SpeechListener
import com.project.googlespeechtotext.voice.SpeechService
import com.project.googlespeechtotext.voice.VoiceStreamListener
import com.project.googlespeechtotext.voice.VoiceStreamer
import java.util.concurrent.Executors


class MergeActivity : AppCompatActivity(), VoiceStreamListener, SpeechListener {
    private lateinit var previewView: PreviewView
    private lateinit var faceOverlay: FaceOverlayView
    private lateinit var faceCountText: TextView
    private lateinit var faceHelper: FaceRecognitionHelper

    // 음성
    private lateinit var voiceStreamer: VoiceStreamer
    private lateinit var speechService: SpeechService
    private lateinit var textView: TextView
    private var isListening = false


    companion object {
        private const val CAMERA_PERMISSION_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_layout)

        previewView = findViewById(R.id.previewView)
        faceOverlay = findViewById(R.id.faceOverlay)
        faceCountText = findViewById(R.id.faceCountText)

        // FaceRecognitionHelper는 얼굴 감지, 랜드마크 추출, 디스크립터 생성 후 고유 ID를 부여합니다.
        faceHelper = FaceRecognitionHelper(this) { faces ->
            runOnUiThread {
                faceOverlay.setFaces(faces)
                faceCountText.text = "감지된 얼굴 수: ${faces.size}"
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        } else {
            startCamera()
        }

        //Voice
        textView = findViewById(R.id.result_text_view)
        voiceStreamer = VoiceStreamer()
        speechService = SpeechService(this)

        voiceStreamer.registerOnVoiceListener(this)
        speechService.addListener(this)

        val startButton = findViewById<Button>(R.id.start_button)
        val stopButton = findViewById<Button>(R.id.stopBtn)

        startButton.setOnClickListener { startListening() }
        stopButton.setOnClickListener { stopListening() }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            Log.e("Camera", "카메라 권한이 필요합니다.")
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().apply {
                setSurfaceProvider(previewView.surfaceProvider)
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build().also {
                    it.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                        runOnUiThread {
                            val bitmap = previewView.bitmap
                            if (bitmap != null) {
                                faceHelper.processImage(bitmap)
                            }
                        }
                        imageProxy.close()
                    }
                }

            val cameraSelector = androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)
        }, ContextCompat.getMainExecutor(this))
    }

    private fun startListening() {
        if (!isListening) {
            isListening = true
            speechService.startRecognizing(44000)
            voiceStreamer.startStreaming()
        }
    }

    private fun stopListening() {
        if (isListening) {
            isListening = false
            voiceStreamer.stopStreaming()
            speechService.stopRecognizing()
        }
    }

    override fun onVoiceStreaming(data: ByteArray, size: Int) {
        speechService.recognize(data, size)
    }

    override fun onSpeechRecognized(recognizedText: String, isFinal: Boolean) {
        runOnUiThread {
            textView.text = recognizedText
        }
    }
}
