package com.example.lockscreen
import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.os.Build
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent

class LockScreenService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent) {}
    override fun onInterrupt() {}

    companion object
    {
        var curService : LockScreenService? = null
    }

    override fun onServiceConnected()
    {
        super.onServiceConnected()
        curService = this
        MainActivity.setFunctionalityStatus(this, true)
    }

    override fun onUnbind(intent : Intent?) : Boolean
    {
        curService = null
        return super.onUnbind(intent)
    }

    fun lockScreen()
    {
        performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
    }
}
