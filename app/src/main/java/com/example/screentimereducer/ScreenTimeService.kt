package com.example.screentimereducer
import android.app.*
import android.app.usage.UsageStatsManager
import android.content.*
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import java.util.*

class ScreenTimeService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val chan = NotificationChannel("ID", "Monitor", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(chan)
        startForeground(1, NotificationCompat.Builder(this, "ID").setContentTitle("Monitoring").setSmallIcon(R.drawable.ic_launcher_foreground).build())

        Thread {
            while (true) {
                check()
                Thread.sleep(5000)
            }
        }.start()
        return START_STICKY
    }

    private fun check() {
        val usm = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val time = System.currentTimeMillis()
        val stats = usm.queryAndAggregateUsageStats(getStartOfDay(), time)
        val recent = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 10000, time).sortedByDescending { it.lastTimeUsed }

        recent.firstOrNull()?.let { app ->
            CoroutineScope(Dispatchers.IO).launch {
                val limit = AppDatabase.getDatabase(applicationContext).appLimitDao().getLimitForApp(app.packageName)
                if (limit != null && (stats[app.packageName]?.totalTimeInForeground ?: 0) / 60000 >= limit.timeLimitMinutes) {
                    startActivity(Intent(applicationContext, BlockActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
            }
        }
    }

    private fun getStartOfDay() = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0) }.timeInMillis
    override fun onBind(intent: Intent?): IBinder? = null
}