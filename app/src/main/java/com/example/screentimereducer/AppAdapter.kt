package com.example.screentimereducer
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*

class AppAdapter(private val list: List<ApplicationInfo>, private val pm: PackageManager, private val db: AppDatabase) : RecyclerView.Adapter<AppAdapter.VH>() {
    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val icon = v.findViewById<ImageView>(R.id.appIcon)
        val name = v.findViewById<TextView>(R.id.appName)
        val edit = v.findViewById<EditText>(R.id.editLimit)
        val btn = v.findViewById<Button>(R.id.btnSet)
    }
    override fun onCreateViewHolder(p: ViewGroup, t: Int) = VH(LayoutInflater.from(p.context).inflate(R.layout.item_app, p, false))
    override fun onBindViewHolder(h: VH, p: Int) {
        val app = list[p]
        h.name.text = app.loadLabel(pm)
        h.icon.setImageDrawable(app.loadIcon(pm))

        h.btn.setOnClickListener {
            val mins = h.edit.text.toString().toIntOrNull() ?: 0

            CoroutineScope(Dispatchers.IO).launch {
                if (mins > 0) {
                    // Case: Set a new limit
                    db.appLimitDao().insertLimit(AppLimit(app.packageName, mins))

                    withContext(Dispatchers.Main) {
                        Toast.makeText(h.itemView.context, "Blocked after $mins min", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Case: User entered 0, remove the limit
                    db.appLimitDao().deleteLimit(app.packageName)

                    withContext(Dispatchers.Main) {
                        Toast.makeText(h.itemView.context, "Limit Removed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    override fun getItemCount() = list.size
}