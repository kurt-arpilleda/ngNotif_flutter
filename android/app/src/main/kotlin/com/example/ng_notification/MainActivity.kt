package com.example.ng_notification

import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import android.view.inputmethod.InputMethodManager
import android.content.Context
import android.content.Intent
import android.content.ComponentName
import android.os.Build
import android.content.SharedPreferences

class MainActivity: FlutterActivity() {
    private val CHANNEL = "input_method_channel"
    private val NOTIF_PACKAGE = "com.example.ark_notif"
    private val NOTIF_SERVICE = "com.example.ark_notif.RingMonitoringService"
    private val PREFS_NAME = "FlutterSharedPreferences"
    private val PHORJP_KEY = "flutter.phorjp"

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        // Handle incoming intent
        handleIntent(intent)

        // Start the notification service when app launches
        startNotificationService()

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler {
                call, result ->
            if (call.method == "showInputMethodPicker") {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showInputMethodPicker()
                result.success(null)
            } else {
                result.notImplemented()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        // Check if the intent has the phorjp extra
        val phorjp = intent?.getStringExtra("phorjp")
        if (phorjp != null) {
            // Save to shared preferences
            val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(PHORJP_KEY, phorjp).apply()
        }
    }

    private fun startNotificationService() {
        try {
            val intent = Intent().apply {
                component = ComponentName(NOTIF_PACKAGE, NOTIF_SERVICE)
                action = "START_MONITORING"
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        } catch (e: Exception) {
            // Handle exception if the service or app isn't installed
            e.printStackTrace()
        }
    }
}