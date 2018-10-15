/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018  joshua.tee@gmail.com

    This file is part of wX.

    wX is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    wX is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with wX.  If not, see <http://www.gnu.org/licenses/>.

*/

//done by ELY M.  so you can reprot your SN location within this app.


package joshuatee.wx.radar

import android.annotation.SuppressLint
import joshuatee.wx.MyApplication
import android.content.Context
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.util.*




object SpotterNetworkPositionReport {
    var TAG = "joshuatee-SpotterNetworkPositionReport"

    var success: Boolean = false

    var key: String = MyApplication.sn_key
    var lat: Double = 0.0
    var lon: Double = 0.0
    var altitude: Double = 0.0
    var time: Long = 0
    var speed: Float = 0.0f
    var bearing: Float = 0.0f
    var gpsprovider: String = ""


    var strkey: String? = MyApplication.sn_key
    var strlat: String? = ""
    var strlon: String? = ""
    var straltitude: String? = ""
    var strtime: String? = ""
    var strspeed: String? = ""
    var strbearing: String? = ""
    var strgpsprovider: String? = ""


    fun sendToast(context: Context, text: String) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show()
    }

    val criteria = Criteria()

    private var locationManager: LocationManager? = null
    private var location: Location? = null

    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            GetLocation(location)
            Log.i(TAG, "onLocationChanged lat: "+location.latitude+ " lon: "+location.longitude)
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}

        override fun onProviderEnabled(provider: String) {}

        override fun onProviderDisabled(provider: String) {}
    }

    private fun GetLocation(location: Location) {
        lat = location.latitude
        lon = location.longitude
        altitude = location.altitude
        time = location.time
        speed = location.speed
        bearing = location.bearing
        gpsprovider = location.provider

    }

    open fun SendPosition(context: Context): Boolean {

        criteria.accuracy = Criteria.ACCURACY_FINE
        criteria.isAltitudeRequired = true;
        criteria.isSpeedRequired = true;
        criteria.isBearingRequired = true;


        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as (LocationManager)
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 20000.toLong(), 30.toFloat(), locationListener)

        val provider = locationManager?.getBestProvider(criteria, false)
        location = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)


        lat = location!!.latitude
        lon = location!!.longitude
        altitude = location!!.altitude
        time = location!!.time
        speed = location!!.speed
        bearing = location!!.bearing
        gpsprovider = location!!.provider


        if (lat == null && lon == null) {
            Log.i(TAG, "No SN report sent - Null position packet")
        }
        var key = MyApplication.sn_key
        if (key.length < 2 || lat == 0.0 && lon == 0.0) {
            Log.i(TAG, "No SN report sent - Invalid data: Key " + key + " Position " + lat + "/" + lon)
            if (key.length < 2) {
                Log.i(TAG, "Please check your SpotterNetwork login credentials.")
                sendToast(context, "Please check your SpotterNetwork login credentials.")
            }
            return true
        }
        val sdf = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val info = arrayOfNulls<String>(9)
        info[0] = key
        info[1] = sdf.format(java.lang.Long.valueOf(time))
        info[2] = String.format(Locale.US, "%.6f", lat)
        info[3] = String.format(Locale.US, "%.6f", lon)
        info[4] = String.format(Locale.US, "%.0f", (altitude * 3.281))
        info[5] = String.format(Locale.US, "%.1f", (speed.toDouble() * 2.23694))
        info[6] = String.format(Locale.US, "%.0f", bearing)
        info[7] = "1"
        if (provider == "gps") {
            info[8] = "1"
        } else {
            info[8] = "0"
        }

        strkey = info[0]
        strtime = info[1]
        strlat = info[2]
        strlon = info[3]
        straltitude = info[4]
        strspeed = info[5]
        strbearing = info[6]
        strgpsprovider = info[8]


        var task = Send_Location_Task()
        task.execute()
        return false
    }

    /*
    *

   {
    "id": "APPLICATION-ID",
    "report_at": "YYYY-MM-DD HH:MM:SS",
    "lat": 39.7553101,
    "lon": -105.2330093,
    "elev": 0,
    "mph": 7.5,
    "dir": 328,
    "active": 1,
    "gps": 1
}

    *
    * */




    class Send_Location_Task : AsyncTask<String, String, String>() {

    @SuppressLint("StaticFieldLeak")


        var TAG = "SpotterNetworkPositionReport"
        var success: Boolean = false

        override fun doInBackground(vararg params: String): String {
            val sh = OkHttpClient()
            val JSON = MediaType.parse("application/json; charset=utf-8")
            val url = "https://www.spotternetwork.org/positions/update"
            val `object` = JSONObject()
            try {
                `object`.put("id", strkey)
                `object`.put("report_at", strtime)
                `object`.put("lat", strlat)
                `object`.put("lon", strlon)
                `object`.put("elev", Math.round(straltitude!!.toDouble()))
                `object`.put("mph", Math.round(strspeed!!.toDouble()))
                `object`.put("dir", Math.round(strbearing!!.toDouble()))
                `object`.put("active", 1)
                `object`.put("gps", Integer.parseInt(strgpsprovider))
            } catch (e: JSONException) {
                e.printStackTrace()
            }

            Log.i(TAG, "Sending SN Position " + strlat + " " + strlon)


            val body = RequestBody.create(JSON, `object`.toString())
            val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "wX SN location report - email me at elymbmx@gmail.com if there is problems!")
                    .post(body)
                    .build()

            var response: Response? = null
            var jsonStr: String? = null
            try {
                response = sh.newCall(request).execute()
                jsonStr = response!!.body()!!.string()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            Log.i(TAG, "jsonStr: "+jsonStr)

            if (jsonStr != null) {
                try {
                    success = JSONObject(jsonStr).getBoolean("success")
                    if (!success) {
                        //sendToast("SpotterNetwork was unable to process your position update")
                        Log.i(TAG, "SpotterNetwork was unable to process your position update")
                    }
                } catch (e: JSONException) {
                    Log.i(TAG, "SpotterNetwork did not process position update JSON ERROR")
                }

            } else {
                Log.i(TAG, "SpotterNetwork did not process position update NULL RETURNED")
            }
            if (success) {

                Log.i(TAG, "SpotterNetwork did sent location report successful!")
            }
            return "Executed"
        }


    }

}
