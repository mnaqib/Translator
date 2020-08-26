package com.example.translator

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.chaquo.python.Python
import com.example.translator.R.layout.layout
import kotlinx.android.synthetic.main.layout.*
import java.text.DateFormat
import java.util.*


class activity2 : AppCompatActivity() , AdapterView.OnItemSelectedListener {

    var tospeak = ""
    var totrans = ""
    var tolang_code = ""
    var tolang = ""


    //Text To speech
    lateinit var mTTs:TextToSpeech
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        setContentView(layout)

        mTTs = TextToSpeech(applicationContext, TextToSpeech.OnInitListener { status ->
            if (status != TextToSpeech.ERROR) {
                //if there is no error then set language
                mTTs.language = Locale.forLanguageTag("hi")
            }
        })

        val spinner: Spinner = findViewById(R.id.lang_opt)
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            this,
            R.array.Languages,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner.adapter = adapter
            spinner.onItemSelectedListener = this

        }

        val currentDateTimeString = DateFormat.getDateTimeInstance().format(Date())
       // this.time.text = currentDateTimeString

    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        (parent.getChildAt(0) as TextView).setTextColor(Color.WHITE)
        tolang = parent.getItemAtPosition(pos).toString()
        lang_name.text = getlangscript()
        when (tolang) {
            "KANNADA" -> {
                img.setImageResource(R.drawable.kn)
            }
            "HINDI" -> {
                img.setImageResource(R.drawable.hi)
            }
            "BENGALI" -> {
                img.setImageResource(R.drawable.bn)
            }
            "MARATHI" -> {
                img.setImageResource(R.drawable.mr)
            }
            "PUNJABI" -> {
                img.setImageResource(R.drawable.pa)
            }
            "TAMIL" -> {
                img.setImageResource(R.drawable.ta)
            }
            "TELUGU" -> {
                img.setImageResource(R.drawable.te)
            }
            "MALAYALAM" -> {
                img.setImageResource(R.drawable.ml)
            }
            else -> {
                img.setImageResource(R.drawable.gu)
            }
        }

    }

    override fun onNothingSelected(parent: AdapterView<*>) {
        // Another interface callback
    }

    fun onClick(view: View) {
        totrans = this.inputtrans.text.toString()
        tospeak = gettranslatedtext()
        this.text_after.text = tospeak
    }

    private fun gettranslatedtext():String{
        tolang_code = getcountrycode()
        val python = Python.getInstance()
        val pythonFile = python.getModule("final")
        return pythonFile.callAttr("input", totrans, tolang_code).toString()
    }

    private fun getcountrycode():String{
        val python = Python.getInstance()
        val pythonFile = python.getModule("final")
        return pythonFile.callAttr("lang_key", tolang).toString()
    }

    fun open(view: View) {
       // Toast.makeText(this, tospeak, Toast.LENGTH_SHORT).show()
        mTTs.setSpeechRate(0.1f)
        mTTs.speak(tospeak, TextToSpeech.QUEUE_FLUSH, null)
    }

    private fun getlangscript():String{
        val python = Python.getInstance()
        val pythonFile = python.getModule("final")
        return pythonFile.callAttr("lang_script", tolang).toString()
    }

}
