package com.custom.notchgesture

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.GestureDetector
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.NotificationCompat

class FloatingOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View
    private lateinit var gestureDetector: GestureDetector

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForegroundServiceWithNotification()
        setupOverlayWindow()
    }

    private fun startForegroundServiceWithNotification() {
        val channelId = "notch_service_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Notch Gesture Service",
                NotificationManager.IMPORTANCE_MIN
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Notch Gesture Active")
            .setContentText("Running lightweight touch listener")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .build()

        startForeground(1, notification)
    }

    private fun setupOverlayWindow() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        
        overlayView = View(this).apply {
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
        }

        val LAYOUT_FLAG = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            120, 120,
            LAYOUT_FLAG,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = 10 
        }

        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                Toast.makeText(applicationContext, "Notch Single Tap!", Toast.LENGTH_SHORT).show()
                return true
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                Toast.makeText(applicationContext, "Notch Double Tap!", Toast.LENGTH_SHORT).show()
                return true
            }
        })

        overlayView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }

        try {
            windowManager.addView(overlayView, params)
        } catch (e: Exception) {
            e.printStackTrace()
            stopSelf()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::overlayView.isInitialized) {
            try {
                windowManager.removeView(overlayView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
