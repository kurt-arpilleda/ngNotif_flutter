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
import android.net.Uri
import android.provider.Settings

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

        // Check and prompt for unknown app sources permission
        checkInstallUnknownAppsPermission()

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
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
        val phorjp = intent?.getStringExtra("phorjp")
        if (phorjp != null) {
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
            e.printStackTrace()
        }
    }

    private fun checkInstallUnknownAppsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!packageManager.canRequestPackageInstalls()) {
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:$packageName")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(intent)
            }
        }
    }
}
