package com.example.lockscreen


import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.TypedArray
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.provider.Settings
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.TypedValue
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import com.example.lockscreen.databinding.ActivityMainBinding
import java.util.Timer
import java.util.TimerTask


class MainActivity : AppCompatActivity(), SimpleAlertDlg.OnClickListener
{
    companion object {
        const val OLD_ANDROID_VERSION_DLG : Int = 1
        const val LOCK_SCREEN_ERROR_DLG : Int = 2
        const val ACCESSIBILITY_PERM_GRANTED_DLG : Int = 3
        const val ABOUT_DEV_DLG : Int = 4

        const val GITHUB_LINK : String = "https://github.com/The-Computer-Genius/LockScreen"
        const val DEV_EMAIL : String = "haraseessingh01@gmail.com"
    }

    private lateinit var mainBinding : ActivityMainBinding

    private var permissionStateAtStart : Boolean = false

    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)

        val p = savedInstanceState?.getInt("permissionStateAtStart", -1) ?: -1
        permissionStateAtStart = if(p != -1) p == 1  else (LockScreenService.curService != null)


        if(permissionStateAtStart && intent.action != "com.example.lockscreen.OPEN_SETTINGS")
        {
            LockScreenService.curService?.lockScreen()

            if(lifecycle.currentState == Lifecycle.State.RESUMED)
            {
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.P)
                {
                    val dlg = SimpleAlertDlg.newInstance(OLD_ANDROID_VERSION_DLG)
                    dlg.title = "Older Android version"
                    dlg.body = "Android versions older than 9.0 do not support direct screen locking, " +
                            "the app did try to lock the screen by another method but failed. " +
                            "Your android version is ${Build.VERSION.RELEASE}"
                    dlg.positiveBtnTitle = "OK"
                    dlg.show(supportFragmentManager, OLD_ANDROID_VERSION_DLG.toString())
                }
                else
                {
                    val dlg = SimpleAlertDlg.newInstance(LOCK_SCREEN_ERROR_DLG)
                    dlg.title = "Unexpected error"
                    dlg.body = "Could not lock the screen due to an unknown reason"
                    dlg.positiveBtnTitle = "OK"
                    dlg.show(supportFragmentManager, LOCK_SCREEN_ERROR_DLG.toString())
                }
            }
            else
                finish()
        }

        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)

        initTextView()

        mainBinding.settingsBtn.setOnClickListener { onClickSettingsBtn() }
        mainBinding.textViewOpenSourceCode.setOnClickListener { openGitHub() }

        setCardVisibility()
    }

    override fun onSaveInstanceState(outState : Bundle, outPersistentState : PersistableBundle)
    {
        super.onSaveInstanceState(outState, outPersistentState)
        outState.putInt("permissionStateAtStart", if(permissionStateAtStart) 1 else 0)
    }

    override fun onResume()
    {
        super.onResume()

        if(LockScreenService.curService != null && !permissionStateAtStart)
        {
            val dlg = SimpleAlertDlg.newInstance(ACCESSIBILITY_PERM_GRANTED_DLG)
            dlg.title = "Permission Granted"
            dlg.body = "The app must exit once after permission is granted.\n"
            dlg.positiveBtnTitle = "Exit app"
            dlg.cancellable = false

            dlg.show(supportFragmentManager, ACCESSIBILITY_PERM_GRANTED_DLG.toString())
        }

        setCardVisibility()
    }

    private fun initTextView()
    {
        val ss = SpannableString("The Lock Screen Project developed by Harasees Singh is free and open source, which means you can view and examine its source code on GitHub. This eliminates any privacy or security concerns that may arise when giving the app accessibility permissions.")
        val clickableSpan : ClickableSpan = object : ClickableSpan()
        {
            override fun onClick(textView : View)
            {
                showAboutDevDlg()
            }

            override fun updateDrawState(ds : TextPaint)
            {
                super.updateDrawState(ds)
                ds.color = getColorFromTheme(this@MainActivity, R.attr.linkColor)
                ds.isUnderlineText = false
            }
        }
        ss.setSpan(clickableSpan, 37, 51, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        val textView = mainBinding.textViewOpenSourceInfo
        textView.text = ss
        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.highlightColor = Color.TRANSPARENT
    }
    private fun openGitHub()
    {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_LINK))
        startActivity(browserIntent)
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

    private fun showAboutDevDlg()
    {
        val dlg = SimpleAlertDlg.newInstance(ABOUT_DEV_DLG)
        dlg.title = "About the developer"
        dlg.body =
                "Harasees Singh is a 12th grade school student who also likes to develop android apps as a hobby.\n\nFor any queries contact:\nharaseessingh01@gmail.com"
        dlg.positiveBtnTitle = "OK"
        dlg.negativeBtnTitle = "Copy email to clipboard"
        dlg.show(supportFragmentManager, ABOUT_DEV_DLG.toString())
    }

    private fun copyTextToClipboard(context : Context, text : String)
    {
        val clipboardManager =
                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("Email", text)
        clipboardManager.setPrimaryClip(clipData)
    }

    fun getColorFromTheme(context: Context, color : Int) : Int
    {
        val typedValue = TypedValue()

        val a: TypedArray =
                context.obtainStyledAttributes(typedValue.data, intArrayOf(color))
        val retrievedColor = a.getColor(0, 0)
        a.recycle()
        return retrievedColor
    }

    override fun onClickSimpleAlertPositiveBtn(dlg : SimpleAlertDlg, dlgUniqueID : Int)
    {
        if(dlgUniqueID == ACCESSIBILITY_PERM_GRANTED_DLG)
            finish()
    }

    override fun onClickSimpleAlertNegativeBtn(dlg : SimpleAlertDlg, dlgUniqueID : Int)
    {
        if(dlgUniqueID == ABOUT_DEV_DLG)
        {
            copyTextToClipboard(this, DEV_EMAIL)
            Toast.makeText(this, "Copied to Clipboard", Toast.LENGTH_LONG).show()
        }
    }
}