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
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.lockscreen.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar


class MainActivity : AppCompatActivity(), SimpleAlertDlg.OnClickListener
{
    companion object
    {
        const val OLD_ANDROID_VERSION_DLG : Int = 1
        const val LOCK_SCREEN_ERROR_DLG : Int = 2
        const val ACCESSIBILITY_PERM_GRANTED_DLG : Int = 3
        const val ABOUT_DEV_DLG : Int = 4
        const val REQ_PERM_DLG : Int = 5

        const val GITHUB_LINK : String = "https://github.com/The-Computer-Genius/LockScreen"
        const val DEV_EMAIL : String = "haraseessingh01@gmail.com"

        fun setFunctionalityStatus(context : Context, enabled : Boolean)
        {
            context.getSharedPreferences("MainActivity", MODE_PRIVATE).edit()
                    .putBoolean("enabled", enabled)
                    .apply()
        }

        fun getFunctionalityStatus(context : Context) : Boolean
        {
            return context.getSharedPreferences("MainActivity", MODE_PRIVATE)
                    .getBoolean("enabled", LockScreenService.curService != null)
        }
    }

    private lateinit var mainBinding : ActivityMainBinding

    private var permissionStateAtStart : Boolean = false

    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)

        val p = savedInstanceState?.getInt("permissionStateAtStart", -1) ?: -1
        permissionStateAtStart = if (p != -1) p == 1 else (LockScreenService.curService != null)

        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)

        initTextView()

        initFunctionalityBtn()
    }

    override fun onSaveInstanceState(outState : Bundle, outPersistentState : PersistableBundle)
    {
        super.onSaveInstanceState(outState, outPersistentState)
        outState.putInt("permissionStateAtStart", if (permissionStateAtStart) 1 else 0)
    }

    override fun onResume()
    {
        super.onResume()

        if (LockScreenService.curService != null && !permissionStateAtStart)
        {
            setFunctionalityStatus(this, true)
            initFunctionalityBtn()
        }
    }

    private fun initTextView()
    {
        val ss =
                SpannableString("The Lock Screen Project developed by Harasees Singh is free and open source, which means you can view and examine its source code on GitHub. This eliminates any privacy or security concerns that may arise when giving the app accessibility permissions.")
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

        mainBinding.textViewOpenSourceCode.setOnClickListener { openGitHub() }
    }


    private fun initFunctionalityBtn()
    {
        val status = if(LockScreenService.curService == null)
        {
            setFunctionalityStatus(this, false)
            false
        }
        else
            getFunctionalityStatus(this)

        if (status)
        {
            val ss = SpannableString("Screen Lock feature is currently enabled.\nClick here to disable it.")
            val clickableSpan : ClickableSpan = object : ClickableSpan()
            {
                override fun onClick(textView : View)
                {
                    val s = Snackbar.make(mainBinding.root, "Disable Screen Lock feature?", Snackbar.LENGTH_LONG)
                    s.setAction("Disable") {
                        setFunctionalityStatus(this@MainActivity, false)
                        initFunctionalityBtn()
                    }
                    s.show()
                }

                override fun updateDrawState(ds : TextPaint)
                {
                    super.updateDrawState(ds)
                    ds.color = getColorFromTheme(this@MainActivity, R.attr.linkColor)
                    ds.isUnderlineText = true
                }
            }
            ss.setSpan(clickableSpan, 42, 52, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            mainBinding.textViewFunctionalityStatus.text = ss
            mainBinding.textViewFunctionalityStatus.movementMethod = LinkMovementMethod.getInstance()
            mainBinding.textViewFunctionalityStatus.highlightColor = Color.TRANSPARENT

        }
        else
            mainBinding.textViewFunctionalityStatus.text =
                    "Screen Lock feature is currently disabled"

        mainBinding.enabledFunctionalityBtn.visibility = if(status)
            View.GONE
        else
            View.VISIBLE


        mainBinding.enabledFunctionalityBtn.setOnClickListener {
            val curStatus = getFunctionalityStatus(this)
            if (curStatus)
                setFunctionalityStatus(this, false)
            else
            {
                if (LockScreenService.curService == null)
                    showPermDlg()
                else
                    setFunctionalityStatus(this, true)
            }
            initFunctionalityBtn()
        }
    }


    private fun openGitHub()
    {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_LINK))
        startActivity(browserIntent)
    }

    private fun showPermDlg()
    {
        val frag = supportFragmentManager.findFragmentByTag(REQ_PERM_DLG.toString())
        val dlg = if (frag != null) frag as PermDialog else PermDialog()

        if (!dlg.isAdded)
            dlg.show(supportFragmentManager, REQ_PERM_DLG.toString())
    }

    private fun showAboutDevDlg()
    {
        val dlg = SimpleAlertDlg.newInstance(ABOUT_DEV_DLG)
        dlg.title = "About the developer"
        dlg.body =
                "Harasees Singh is a 12th grade school student. He likes to program desktop applications in C++, Microsoft VC++, Web applications in Python and Android apps in Kotlin.\n\nFor any queries contact:\nharaseessingh01@gmail.com"
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

    fun getColorFromTheme(context : Context, color : Int) : Int
    {
        val typedValue = TypedValue()

        val a : TypedArray =
                context.obtainStyledAttributes(typedValue.data, intArrayOf(color))
        val retrievedColor = a.getColor(0, 0)
        a.recycle()
        return retrievedColor
    }

    override fun onClickSimpleAlertPositiveBtn(dlg : SimpleAlertDlg, dlgUniqueID : Int)
    {
        if (dlgUniqueID == ACCESSIBILITY_PERM_GRANTED_DLG)
            finishAndRemoveTask()
    }

    override fun onClickSimpleAlertNegativeBtn(dlg : SimpleAlertDlg, dlgUniqueID : Int)
    {
        if (dlgUniqueID == ABOUT_DEV_DLG)
        {
            copyTextToClipboard(this, DEV_EMAIL)
            if (Build.VERSION.SDK_INT <= 32)
                Toast.makeText(this, "Copied to Clipboard", Toast.LENGTH_LONG).show()
        }
    }
}