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
    }

    override fun onUnbind(intent : Intent?) : Boolean
    {
        curService = null
        return super.onUnbind(intent)
    }

    fun lockScreen()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // For Android Pie (API 28) and above, use GLOBAL_ACTION_LOCK_SCREEN
            performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
        } else {
            // For older versions, simulate a power button press event to lock the screen
            val path = Path()
            path.moveTo(100f, 100f)
            val gestureDescription = GestureDescription.Builder().addStroke(GestureDescription.StrokeDescription(path, 0, 100L)).build()
            dispatchGesture(gestureDescription, null, null)
        }
    }
}
