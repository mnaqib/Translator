package com.example.translator

import MyBounceInterpolator
import android.Manifest
import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
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
import androidx.core.app.ActivityCompat
import com.chaquo.python.Python
import com.example.translator.R.layout.layout
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.layout.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class activity2 : AppCompatActivity() , AdapterView.OnItemSelectedListener {

    var tospeak = ""
    private var totrans = ""
    private var tolang_code = ""
    private var tolang = ""
    val handler = Handler()
    val l_handler = Handler()
    var flag = true
    var t = ""
    var connected = false
    private lateinit var fade_img:AnimatorSet
    private lateinit var fade_script:AnimatorSet
    var anim = false
    private val Recognizer_Result = 1
    //Text To speech
    lateinit var mTTs:TextToSpeech
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    var state = ""
    var state_check = ""

    companion object {
        const val REQUEST_CHECK_SETTINGS = 999
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {

        //location
        createLocationRequest()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)



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

        //setting location while opening app
        set_lang(spinner)


        //for card fade animation
        fade_img = AnimatorInflater.loadAnimator(applicationContext, R.animator.fade_card) as AnimatorSet
        fade_script = AnimatorInflater.loadAnimator(applicationContext, R.animator.fade_card) as AnimatorSet
        fade_script.setTarget(lang_name_card)
        fade_img.setTarget(img_card)

        //to check location periodically
        l_handler.postDelayed(object : Runnable {
            //doSomethingHere()
            override fun run() {
                location()
                if (state != state_check) {
                    state_check = state
                    set_lang(spinner)
                }
                l_handler.postDelayed(this, 2000)
            }
        }, 0)

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

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
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
                    mTTs.language = Locale.forLanguageTag(tolang_code)
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
        tolang_code = getlangcode()
        val python = Python.getInstance()
        val pythonFile = python.getModule("final")
        return pythonFile.callAttr("input", totrans, tolang_code).toString()
    }

    private fun getlangcode():String{
        val python = Python.getInstance()
        val pythonFile = python.getModule("final")
        return pythonFile.callAttr("lang_key", tolang).toString()
    }


    fun open(view: View) {

        var myAnim = loadAnimation(this, R.anim.bubble);
        val interpolator = MyBounceInterpolator(0.1, 15.00)
        myAnim.interpolator = interpolator
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
                fade_img.start()
                fade_script.start()
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


    public fun createLocationRequest() {
        val locationRequest = LocationRequest.create()?.apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val builder = locationRequest?.let {
            LocationSettingsRequest.Builder()
                .addLocationRequest(it)
        }

        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder?.build())

        task.addOnSuccessListener { locationSettingsResponse ->
            // All location settings are satisfied. The client can initialize
            // location requests here.
            // ...
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException){
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(this@activity2,
                        REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    fun location(){

        if (ActivityCompat.checkSelfPermission(
                this@activity2,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED)
        {
            fusedLocationClient?.getLastLocation()?.addOnCompleteListener { task ->
                //Initialize location
                val location = task.result
                if (location != null) {
                    try {
                        //Initialize geocoder
                        val geocoder = Geocoder(this@activity2, Locale.getDefault())

                        //Initialize address list
                        val addresses = geocoder.getFromLocation(
                            location.latitude,
                            location.longitude,
                            1
                        )
                        state = addresses[0].adminArea
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
        else {
            ActivityCompat.requestPermissions(
                this@activity2,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                44
            )
        }
    }

    private fun set_lang(spinner: Spinner) {
        if (state == "Karnataka")
            spinner.setSelection(3)
        else if (state == "Maharashtra")
            spinner.setSelection(5)
        else if (state == "Odisha")
            spinner.setSelection(6)
        else if (state == "Kerala")
            spinner.setSelection(4)
        else if (state == "Tamil Nadu")
            spinner.setSelection(8)
        else if (state == "Andhra Pradesh" || state == "Telangana")
            spinner.setSelection(9)
        else if (state == "Gujarat")
            spinner.setSelection(1)
        else if (state == "West Bengal")
            spinner.setSelection(0)
        else if (state == "Punjab")
            spinner.setSelection(7)
        else
            spinner.setSelection(2)
    }



}