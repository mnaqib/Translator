package com.example.translator

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Handler().postDelayed({
            //doSomethingHere()
            val intent = Intent(applicationContext, activity2::class.java)
            startActivity(intent)
            finish()
        }, 800)


    }

}