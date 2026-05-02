package com.baig.culturetranslate

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class InputActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input)

        val etPhrase = findViewById<EditText>(R.id.etPhrase)
        val countrySpinner = findViewById<Spinner>(R.id.countrySpinner)
        val btnTranslate = findViewById<Button>(R.id.btnTranslate)
        val btnInfo = findViewById<ImageButton>(R.id.btnInfo)


        // 1. Dropdown List
        val countries = listOf("Select a country", "Japan", "France", "Spain", "Germany")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, countries)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        countrySpinner.adapter = adapter

        // 2. Button Click Logic
        btnTranslate.setOnClickListener {
            val phrase = etPhrase.text.toString()
            val selectedCountry = countrySpinner.selectedItem.toString()

            if (phrase.isEmpty() || selectedCountry == "Select a country" || selectedCountry == "Select Operation") {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, ResultActivity::class.java)
                intent.putExtra("EXTRA_PHRASE", phrase)
                intent.putExtra("EXTRA_COUNTRY", selectedCountry)
                startActivity(intent)
            }
        }

        // 3. Info Modal Button
        btnInfo.setOnClickListener {
            showInfoModal()
        }
    }
    private fun showInfoModal() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("CultureTranslate Instructions")
        builder.setMessage("1. Enter a phrase in the text box. \n2. Select a country from the dropdown list. \n3. Click Translate to see the cultural etiquette and hear the audio.")
        builder.setPositiveButton("Got It") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }
}
