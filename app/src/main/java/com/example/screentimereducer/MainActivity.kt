package com.example.screentimereducer
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rv = findViewById<RecyclerView>(R.id.rvAppList)
        rv.layoutManager = LinearLayoutManager(this)
        val pm = packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA).filter { pm.getLaunchIntentForPackage(it.packageName) != null }
        rv.adapter = AppAdapter(apps, pm, AppDatabase.getDatabase(this))

        findViewById<Button>(R.id.btnStartService).setOnClickListener {
            if (!hasUsageStatsPermission()) startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            else if (!Settings.canDrawOverlays(this)) startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, android.net.Uri.parse("package:$packageName")))
            else startForegroundService(Intent(this, ScreenTimeService::class.java))
        }
    }
    private fun hasUsageStatsPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        return appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), packageName) == AppOpsManager.MODE_ALLOWED
    }
}