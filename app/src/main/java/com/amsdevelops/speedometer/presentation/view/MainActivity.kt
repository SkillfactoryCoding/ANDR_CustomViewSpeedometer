package com.amsdevelops.speedometer.presentation.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.amsdevelops.speedometer.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button_increase.setOnClickListener {
            speedometer.onSpeedChanged(speedometer.getCurrentSpeed() + 8)
        }

        button_decrease.setOnClickListener {
            speedometer.onSpeedChanged(speedometer.getCurrentSpeed() - 8)
        }
    }
}