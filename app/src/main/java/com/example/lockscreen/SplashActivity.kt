package com.example.lockscreen

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import com.example.lockscreen.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity()
{

    private lateinit var mainBinding : ActivitySplashBinding
    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)

        if(LockScreenService.curService != null && MainActivity.getFunctionalityStatus(this))
        {
            LockScreenService.curService?.lockScreen()
            finishAndRemoveTask()
        }
        else
        {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}