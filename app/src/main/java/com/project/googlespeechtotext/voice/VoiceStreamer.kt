package com.project.googlespeechtotext.voice

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

interface VoiceStreamListener {
    fun onVoiceStreaming(data: ByteArray, size: Int)
}

class VoiceStreamer  {

    private var audioRecord: AudioRecord? = null
    private var isStreaming: Boolean = false
    private var voiceStreamListener: VoiceStreamListener? = null
    private val executorService: ExecutorService = Executors.newSingleThreadExecutor()

    fun registerOnVoiceListener(listener: VoiceStreamListener) {
        this.voiceStreamListener = listener
    }

    private val audioStreamRunnable = Runnable {
        try {
            val buffer = ByteArray(BUFFER_SIZE)
            if (audioRecord == null) {
                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    BUFFER_SIZE
                )
            }
            audioRecord?.startRecording()

            while (isStreaming) {
                val bytesRead = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                if (bytesRead > 0) {
                    voiceStreamListener?.onVoiceStreaming(buffer, bytesRead)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun startStreaming() {
        isStreaming = true
        executorService.submit(audioStreamRunnable)
    }

    fun stopStreaming() {
        isStreaming = false
        audioRecord?.release()
        audioRecord = null
        executorService.shutdown()
    }

    companion object {
        private const val SAMPLE_RATE = 48000
        private const val BUFFER_SIZE = 4096
    }
}
