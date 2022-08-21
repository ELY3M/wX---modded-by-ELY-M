//modded by ELY M.
//done by ELY M.  so you can report your SN location within this app.


package joshuatee.wx.radar

import android.app.Service
import joshuatee.wx.MyApplication
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.*


class SpotterNetworkPositionReportService : Service() {


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    private val handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable = object : Runnable {
        override fun run() {
            // Repeat every 5 mins
            Log.i("wx","Spotter Service Ran")
            SpotterNetworkPositionReport.SendPosition(MyApplication.appContext)
            handler.postDelayed(this, 300000)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("wx","Spotter onStartCommand")
        handler.post(runnable)
        return START_STICKY
    }







}




