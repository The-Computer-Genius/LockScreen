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

            if(lifecycle.currentState == Lifecycle.State.RESUMED)
            {
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.P)
                {
                    val dlg = SimpleAlertDlg.newInstance(MainActivity.OLD_ANDROID_VERSION_DLG)
                    dlg.title = "Older Android version"
                    dlg.body = "Android versions older than 9.0 do not support direct screen locking, " +
                            "the app did try to lock the screen by another method but failed. " +
                            "Your android version is ${Build.VERSION.RELEASE}"
                    dlg.positiveBtnTitle = "OK"
                    dlg.show(supportFragmentManager, MainActivity.OLD_ANDROID_VERSION_DLG.toString())
                }
                else
                {
                    val dlg = SimpleAlertDlg.newInstance(MainActivity.LOCK_SCREEN_ERROR_DLG)
                    dlg.title = "Unexpected error"
                    dlg.body = "Could not lock the screen due to an unknown reason"
                    dlg.positiveBtnTitle = "OK"
                    dlg.show(supportFragmentManager, MainActivity.LOCK_SCREEN_ERROR_DLG.toString())
                }
            }
        }
        else
        {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}