package com.example.translator

import MyBounceInterpolator
import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils.loadAnimation
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.chaquo.python.Python
import com.example.translator.R.layout.layout
import kotlinx.android.synthetic.main.layout.*
import java.text.SimpleDateFormat
import java.util.*


class activity2 : AppCompatActivity() , AdapterView.OnItemSelectedListener {

    var tospeak = ""
    private var totrans = ""
    private var tolang_code = ""
    private var tolang = ""
    val handler = Handler()
    var flag = true
    var t = ""
    var connected = false
    private lateinit var fade:AnimatorSet
    private lateinit var left_flip:AnimatorSet
    var anim = false
    private val Recognizer_Result = 1

    //Text To speech
    lateinit var mTTs:TextToSpeech
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {

        handler.postDelayed(object : Runnable {
            //doSomethingHere()
            override fun run() {
                setting_time()
                text_set()
                handler.postDelayed(this, 10)
            }
        }, 0)


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
            spinner.setSelection(2)

        }


        //for card flip animation and fade animation
        val scale:Float = applicationContext.resources.displayMetrics.density
        img_card.cameraDistance = 8000 * scale
        lang_name_card.cameraDistance = 8000 * scale
        fade = AnimatorInflater.loadAnimator(applicationContext, R.animator.fade_card) as AnimatorSet
        left_flip = AnimatorInflater.loadAnimator(applicationContext, R.animator.back_flip) as AnimatorSet
        left_flip.setTarget(lang_name_card)
        fade.setTarget(img_card)

    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        (parent.getChildAt(0) as TextView).setTextColor(Color.WHITE)
        tolang = parent.getItemAtPosition(pos).toString()
        this.text_after.text=""
        flag = true

        if(anim)
        {

            animation()
        }
        anim=true

        Handler().postDelayed({
            //doSomethingHere()
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
                "ODIA" -> {
                    img.setImageResource(R.drawable.od)
                }
                else -> {
                    img.setImageResource(R.drawable.gu)
                }
            }

        }, 100)



    }

    override fun onNothingSelected(parent: AdapterView<*>) {
        // Another interface callback
    }

    fun onClick(view: View) {
        var hand = handler
        totrans = this.inputtrans.text.toString()
        var myAnim = loadAnimation(applicationContext, R.anim.bubble)
        val interpolator = MyBounceInterpolator(0.15, 15.00)
        myAnim.interpolator = interpolator
        if (totrans == "") {
            trans_btn.startAnimation(myAnim)
            Toast.makeText(this, "Enter a text to Translate", Toast.LENGTH_LONG).show()
        } else {
            trans_btn.startAnimation(myAnim)
            connected = isConnected()
            if (connected) {
                if (flag || totrans != t) {
                    runthread()
                    runthread1()
                } else Toast.makeText(
                    this,
                    "Text already Translated, Choose another Language",
                    Toast.LENGTH_LONG
                ).show()
            } else Toast.makeText(
                this,
                "To translate the text, Please connect to Internet",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun gettranslatedtext():String{
        t=totrans
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

        var myAnim = loadAnimation(this, R.anim.bubble);
        val interpolator = MyBounceInterpolator(0.1, 15.00)
        myAnim.interpolator = interpolator
        var a = 1.0f
        if(tolang=="KANNADA" || tolang=="TAMIL" || tolang=="TELUGU" || tolang=="PUNJABI" || tolang=="MALAYALAM" || tolang=="BENGALI")
            a=0.1f
        mTTs.setSpeechRate(a)
        play.startAnimation(myAnim)
        mTTs.speak(tospeak, TextToSpeech.QUEUE_FLUSH, null)
    }

    private fun getlangscript():String{
        val python = Python.getInstance()
        val pythonFile = python.getModule("final")
        return pythonFile.callAttr("lang_script", tolang).toString()
    }

    private fun setting_time()
    {
        var a = SimpleDateFormat("HH", Locale.US).format(Date())
        var b = a.toInt()
        var c = SimpleDateFormat("mm", Locale.US).format(Date())


        if((b>=6 ) && (b< 12))
        {
            wish.text = "Hello,Good Morning"
            day_img.setImageResource(R.drawable.morning)
        }
        else if((b>=12) && (b < 18))
        {
            wish.text = "Hello,Good Afternoon"
            day_img.setImageResource(R.drawable.afternoon)
        }
        else if((b>=18) && (b< 21))
        {
            wish.text = "Hello,Good Evening"
            day_img.setImageResource(R.drawable.evening)
        }
        else if((b>=21) || (b>=0 && b < 6))
        {
            wish.text = "Good Night"
            day_img.setImageResource(R.drawable.night)
        }

        if(b==0) {
            b = 12
        }
        else if(b>12) {
            b = b - 12
        }

        if(b<10)
            time_disp.text = "0"+ b.toString()+":" + c
        else time_disp.text = b.toString()+":" + c

    }

    private fun runthread1() {
        runOnUiThread {
            simpleProgressBar.visibility = View.VISIBLE

        }
    }

    private fun runthread() {
            var t = Thread {
                runOnUiThread {
                    tospeak = gettranslatedtext()
                    this.text_after.text = tospeak
                    simpleProgressBar.visibility = View.INVISIBLE
                    flag = false
                }
            }
            t.start()
            t.priority = Thread.MAX_PRIORITY
    }

    private fun text_set(){
        if(t != inputtrans.text.toString())
            text_after.text=""
    }


    private fun isConnected(): Boolean {
        try {
            val cm = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val nInfo = cm.activeNetworkInfo
            connected = nInfo != null && nInfo.isAvailable && nInfo.isConnected
            return connected
        } catch (e: Exception) {
            e.message?.let { Log.e("Connectivity Exception", it) }
        }
        return connected
    }

    private fun animation() {
        var t = Thread {
            runOnUiThread {
                fade.start()
                left_flip.start()
            }
        }
        t.start()
        t.priority = Thread.MAX_PRIORITY
    }

    fun speech(view: View) {
        var myAnim = loadAnimation(applicationContext, R.anim.bubble);
        val interpolator = MyBounceInterpolator(0.15, 15.00)
        mic.startAnimation(myAnim)
        val speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        speechIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        speechIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "speech to text")
        startActivityForResult(speechIntent, Recognizer_Result)
    }

    override fun onActivityResult(requestcode: Int, resultcode: Int, data: Intent?) {
        if (requestcode == Recognizer_Result && resultcode == RESULT_OK) {
            val matches = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            inputtrans.setText(matches!![0].toString())
        }
        super.onActivityResult(requestcode, resultcode, data)
    }

}