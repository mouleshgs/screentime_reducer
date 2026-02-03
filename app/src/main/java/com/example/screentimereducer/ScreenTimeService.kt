package com.example.screentimereducer

import android.app.*
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class ScreenTimeService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()

        val notification = NotificationCompat.Builder(this, "CHANNEL_ID")
            .setContentTitle("ScreenTime Reducer is Monitoring")
            .setContentText("Protecting your productivity...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(1, notification)

        // Monitoring Loop
        Thread {
            while (true) {
                checkUsageAndBlock()
                Thread.sleep(5000) // Check every 5 seconds
            }
        }.start()

        return START_STICKY
    }

    private fun checkUsageAndBlock() {
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val startTime = getStartOfDay()

        // Get total stats for today
        val statsMap = usageStatsManager.queryAndAggregateUsageStats(startTime, endTime)

        // Find the app currently in the foreground
        val recentStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, endTime - 1000 * 10, endTime)
        val currentForegroundApp = recentStats.sortedByDescending { it.lastTimeUsed }.firstOrNull()?.packageName

        if (currentForegroundApp != null) {
            val db = AppDatabase.getDatabase(applicationContext)

            serviceScope.launch {
                // Check if the current app has a limit set in our database
                val appLimit = db.appLimitDao().getLimitForApp(currentForegroundApp)

                if (appLimit != null && appLimit.isEnabled) {
                    val timeUsedMillis = statsMap[currentForegroundApp]?.totalTimeInForeground ?: 0
                    val timeUsedMinutes = timeUsedMillis / 60000

                    if (timeUsedMinutes >= appLimit.timeLimitMinutes) {
                        launchBlockScreen()
                    }
                }
            }
        }
    }

    private fun launchBlockScreen() {
        val blockIntent = Intent(this, BlockActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(blockIntent)
    }

    private fun getStartOfDay(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            "CHANNEL_ID", "Screen Time Monitor Channel",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(serviceChannel)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}