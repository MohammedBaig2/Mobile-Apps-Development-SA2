package com.baig.culturetranslate

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale

class ResultActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private var translatedText: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        tts = TextToSpeech(this, this)

        val tvResult = findViewById<TextView>(R.id.tvResult)
        val tvCultureTip = findViewById<TextView>(R.id.tvCultureTip)
        val btnSpeak = findViewById<Button>(R.id.btnSpeak)
        val btnGoBack = findViewById<Button>(R.id.btnGoBack)

        val phrase = intent.getStringExtra("EXTRA_PHRASE") ?: ""
        val country = intent.getStringExtra("EXTRA_COUNTRY") ?: ""

        tvResult.text = "Translating..."

        // Call the translation API in the background
        translateText(phrase, country) { resultText ->
            translatedText = resultText
            runOnUiThread {
                tvResult.text = translatedText
            }
        }

        tvCultureTip.text = "Culture Tip: When traveling to $country, it helps to learn a few basic local phrases. Research local customs before arrival!"

        btnSpeak.setOnClickListener {
            speakOut()
        }

        btnGoBack.setOnClickListener {
            finish()
        }
    }

    private fun translateText(text: String, country: String, callback: (String) -> Unit) {
        val targetLang = when (country) {
            "Spain" -> "es"
            "France" -> "fr"
            "Japan" -> "ja"
            "Germany" -> "de"
            else -> "en"
        }

        Thread {
            try {
                val encodedText = URLEncoder.encode(text, "UTF-8")
                val urlString = "https://api.mymemory.translated.net/get?q=$encodedText&langpair=en|$targetLang"
                val url = URL(urlString)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()

                val jsonResponse = JSONObject(response.toString())
                val responseData = jsonResponse.getJSONObject("responseData")
                val translated = responseData.getString("translatedText")

                callback(translated)
            } catch (e: Exception) {
                Log.e("TranslationError", e.message.toString())
                callback(text)
            }
        }.start()
    }

    private fun speakOut() {
        if (translatedText.isNotEmpty()) {
            tts.speak(translatedText, TextToSpeech.QUEUE_FLUSH, null, "")
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.US)
        }
    }

    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}
