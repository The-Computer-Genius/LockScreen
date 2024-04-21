package com.example.lockscreen

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import com.example.lockscreen.databinding.ActivityMainBinding
import java.util.Timer
import java.util.TimerTask


class MainActivity : AppCompatActivity(), SimpleAlertDlg.OnClickListener
{
    companion object {
        const val OLD_ANDROID_VERSION_DLG : Int = 1
        const val LOCK_SCREEN_ERROR : Int = 2
        const val ACCESSIBILITY_PERM_GRANTED : Int = 3
    }

    private lateinit var mainBinding : ActivityMainBinding

    var permissionStateAtStart : Boolean = false

    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)

        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)


        //permissionStateAtStart = LockScreenService.curService != null
        permissionStateAtStart = (savedInstanceState?.getBoolean("permissionStateAtStart"))
                ?: (LockScreenService.curService != null)

        if(permissionStateAtStart && intent.action != "com.example.lockscreen.OPEN_SETTINGS")
        {
            Log.e("MyTag", "WH")
            LockScreenService.curService?.lockScreen()

            if(lifecycle.currentState == Lifecycle.State.RESUMED)
            {
                Log.e("MyTag", "RESUMED")
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.P)
                {
                    val dlg = SimpleAlertDlg.newInstance(OLD_ANDROID_VERSION_DLG)
                    dlg.title = "Older Android version"
                    dlg.body = "Android versions older than 9.0 do not support direct screen locking, " +
                            "the app did try to lock the screen by another method but failed. " +
                            "Your android version is ${Build.VERSION.RELEASE}"
                    dlg.positiveBtnTitle = "Ok"
                    dlg.show(supportFragmentManager, OLD_ANDROID_VERSION_DLG.toString())
                }
                else
                {
                    val dlg = SimpleAlertDlg.newInstance(LOCK_SCREEN_ERROR)
                    dlg.title = "Unexpected error"
                    dlg.body = "Could not lock the screen due to an unknown reason"
                    dlg.positiveBtnTitle = "ok"
                    dlg.show(supportFragmentManager, LOCK_SCREEN_ERROR.toString())
                }
            }
            else
            {
                Log.e("MyTag", "Not RESUMED")
                val timer = Timer()
                val task = object : TimerTask()
                {
                    override fun run()
                    {
                        Log.e("MyTag", "Finishing task")
                        finishAndRemoveTask()
                    }
                }
                timer.schedule(task, 50)
            }
        }

        mainBinding.settingsBtn.setOnClickListener { onClickSettingsBtn() }
        setCardVisibility()
    }

    override fun onSaveInstanceState(outState : Bundle, outPersistentState : PersistableBundle)
    {
        super.onSaveInstanceState(outState, outPersistentState)
        outState.putBoolean("permissionStateAtStart", permissionStateAtStart)
    }

    override fun onResume()
    {
        super.onResume()
        if(LockScreenService.curService != null && !permissionStateAtStart)
        {
            val dlg = SimpleAlertDlg.newInstance(ACCESSIBILITY_PERM_GRANTED)
            dlg.title = "Permission Granted"
            dlg.body = "You can now close the app. " +
                    "To open this screen again, long press the app icon and click Settings option"
            dlg.positiveBtnTitle = "Ok"

            dlg.show(supportFragmentManager, ACCESSIBILITY_PERM_GRANTED.toString())
        }
        setCardVisibility()
    }

    private fun setCardVisibility()
    {
        if(LockScreenService.curService == null)
            mainBinding.settingsCardView.visibility = View.VISIBLE
        else
            mainBinding.settingsCardView.visibility = View.GONE
    }

    private fun onClickSettingsBtn()
    {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }

    override fun onClickSimpleAlertPositiveBtn(dlg : SimpleAlertDlg, dlgUniqueID : Int)
    {

    }

    override fun onClickSimpleAlertNegativeBtn(dlg : SimpleAlertDlg, dlgUniqueID : Int)
    {

    }
}