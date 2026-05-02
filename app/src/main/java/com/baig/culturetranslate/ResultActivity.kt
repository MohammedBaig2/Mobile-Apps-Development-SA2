package com.baig.culturetranslate

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.Locale

class ResultActivity : AppCompatActivity() {

    private val client = OkHttpClient()
    private var tts: TextToSpeech? = null
    private var currentTranslatedText = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        // 1. Find the views on the screen
        val tvTranslatedPhrase = findViewById<TextView>(R.id.tvTranslatedPhrase)
        val tvCultureTip = findViewById<TextView>(R.id.tvCultureTip)
        val btnHome = findViewById<Button>(R.id.btnHome)
        val btnSpeak = findViewById<Button>(R.id.btnSpeak)

        // 2. Get the data passed from the previous screen
        val phrase = intent.getStringExtra("EXTRA_PHRASE") ?: ""
        val country = intent.getStringExtra("EXTRA_COUNTRY") ?: ""

        // 3. Set the cultural tip immediately
        setCultureTipText(country, tvCultureTip)

        // 4. Fetch the translated text from the API
        fetchTranslation(phrase, country, tvTranslatedPhrase)

        // 5. Initialize Text-To-Speech safely
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
            }
        }

        // 6. Speak Button Action
        btnSpeak.setOnClickListener {
            if (currentTranslatedText.isNotEmpty() && tts != null) {
                tts!!.speak(currentTranslatedText, TextToSpeech.QUEUE_FLUSH, null, null)
            } else {
                Toast.makeText(this, "Translation is still loading or TTS is not ready.", Toast.LENGTH_SHORT).show()
            }
        }

        // 7. Back button logic to return to input screen
        btnHome.setOnClickListener {
            finish()
        }
    }

    private fun fetchTranslation(textToTranslate: String, targetCountry: String, textView: TextView) {
        val targetLanguage = when (targetCountry.lowercase()) {
            "japan" -> "ja"
            "france" -> "fr"
            "spain" -> "es"
            "germany" -> "de"
            else -> "en"
        }

        val url = "https://api.mymemory.translated.net/get?q=$textToTranslate&langpair=en|$targetLanguage"
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    currentTranslatedText = textToTranslate
                    textView.text = "${targetCountry.replaceFirstChar { it.uppercase() }}: $textToTranslate"
                    Toast.makeText(this@ResultActivity, "Network error. Showing original text.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val responseData = response.body?.string()
                    val json = JSONObject(responseData)
                    val translatedText = json.getJSONObject("responseData").getString("translatedText")

                    runOnUiThread {
                        currentTranslatedText = translatedText
                        textView.text = "${targetCountry.replaceFirstChar { it.uppercase() }}: $translatedText"
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        currentTranslatedText = textToTranslate
                        textView.text = "${targetCountry.replaceFirstChar { it.uppercase() }}: $textToTranslate"
                    }
                }
            }
        })
    }

    private fun setCultureTipText(country: String, textView: TextView) {
        val cultureTipText = when (country.lowercase()) {
            "japan" -> "Culture Tip: Bowing is standard in Japan. Deeper bows show more respect to elders or business partners."
            "france" -> "Culture Tip: Always say 'Bonjour' before asking a question in a French shop. It's considered very rude not to!"
            "spain" -> "Culture Tip: Locals enjoy late lunches (2 PM - 4 PM) and dinner around 9 PM or 10 PM."
            "germany" -> "Culture Tip: Punctuality is highly valued. If you are running 5 minutes late, it is polite to inform them."
            else -> "Culture Tip: When traveling to $country, it helps to learn a few basic local phrases. Research local customs before arrival!"
        }
        textView.text = cultureTipText
    }

    override fun onDestroy() {
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }
        super.onDestroy()
    }
}