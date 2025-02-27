package com.project.googlespeechtotext.voice

import android.content.Context
import android.util.Log
import com.google.api.gax.core.FixedCredentialsProvider
import com.google.api.gax.rpc.ClientStream
import com.google.api.gax.rpc.ResponseObserver
import com.google.api.gax.rpc.StreamController
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.speech.v1.*
import com.google.protobuf.ByteString
import com.project.googlespeechtotext.R
import java.io.InputStream

class SpeechService(context: Context) {

    private var speechClient: SpeechClient? = null
    private var clientStream: ClientStream<StreamingRecognizeRequest>? = null
    private val speechListeners = mutableListOf<SpeechListener>()

    init {
        initSpeechClient(context)
    }

    private fun initSpeechClient(context: Context) {
        val credentialsInputStream: InputStream = context.resources.openRawResource(R.raw.my_credential)
        val credentials = GoogleCredentials.fromStream(credentialsInputStream)
        val credentialsProvider = FixedCredentialsProvider.create(credentials)
        speechClient = SpeechClient.create(
            SpeechSettings.newBuilder().setCredentialsProvider(credentialsProvider).build()
        )

//        val credentials = ServiceAccountCredentials.fromStream(context.resources.openRawResource(R.raw.my_credential))
//        val settings = SpeechStubSettings.newBuilder()
//            .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
//            .build()
//        speechClient = SpeechClient.create(settings)
    }

    fun startRecognizing(sampleRate: Int) {
        if (speechClient == null) return

        clientStream = speechClient?.streamingRecognizeCallable()?.splitCall(responseObserver)

        val streamingConfig = StreamingRecognizeRequest.newBuilder()
            .setStreamingConfig(StreamingRecognitionConfig.newBuilder()
                .setConfig(RecognitionConfig.newBuilder()
                    .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                    .setSampleRateHertz(sampleRate)
                    .setLanguageCode("ko-KR")
                    .build())
                .setInterimResults(true)
                .build())
            .build()

        clientStream?.send(streamingConfig)
    }

    fun recognize(data: ByteArray, size: Int) {
        clientStream?.send(
            StreamingRecognizeRequest.newBuilder()
                .setAudioContent(ByteString.copyFrom(data, 0, size))
                .build()
        )
    }

    fun stopRecognizing() {
        clientStream?.closeSend()
        clientStream = null
    }

    fun addListener(listener: SpeechListener) {
        speechListeners.add(listener)
    }

    fun removeListener(listener: SpeechListener) {
        speechListeners.remove(listener)
    }

    private val responseObserver = object : ResponseObserver<StreamingRecognizeResponse> {
        override fun onStart(controller: StreamController) {}

        override fun onResponse(response: StreamingRecognizeResponse) {
            response.resultsList.forEach { result ->
                if (result.alternativesCount > 0) {
                    val transcript = result.getAlternatives(0).transcript
                    val isFinal = result.isFinal
                    speechListeners.forEach { it.onSpeechRecognized(transcript, isFinal) }
                }
            }
        }

        override fun onError(t: Throwable) {
            Log.e("SpeechService", "Error: ${t.message}")
        }

        override fun onComplete() {}
    }
}

interface SpeechListener {
    fun onSpeechRecognized(recognizedText: String, isFinal: Boolean)
}
