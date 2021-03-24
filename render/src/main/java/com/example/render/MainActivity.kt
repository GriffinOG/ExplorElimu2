package com.example.render

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import com.example.render.view.MenuActivity
import com.example.render.view.ModelActivity
import org.andresoviedo.util.android.AndroidURLStreamHandlerFactory
import java.net.URL

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        System.setProperty("java.protocol.handler.pkgs", "org.andresoviedo.util.android")
        URL.setURLStreamHandlerFactory(AndroidURLStreamHandlerFactory())

        setContentView(R.layout.activity_main)

        // Start Model activity.
        this@MainActivity.startActivity(
            Intent(
                this@MainActivity.applicationContext,
                MenuActivity::class.java
            )
        )
        finish()
    }

    private fun init() {
        this@MainActivity.startActivity(
            Intent(
                this@MainActivity.applicationContext,
                ModelActivity::class.java
            )
        )
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }
}