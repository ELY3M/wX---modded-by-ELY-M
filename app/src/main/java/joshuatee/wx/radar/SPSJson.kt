package joshuatee.wx.radar



import org.json.JSONObject
import android.annotation.SuppressLint
import joshuatee.wx.util.UtilityLog


class SPSJson {


    var headline = "headline"
    var NWSheadline = "NWSheadline"
    var icon = "Weatherimage"


    @Volatile
    var parsingComplete = true

    @SuppressLint("NewApi")
    fun readAndParseJSON(`in`: String) {
        try {
            val reader = JSONObject(`in`)

            val current = reader.getJSONObject("features")
            headline = current.getString("headline")
            NWSheadline = current.getString("NWSheadline")
            icon = current.getString("Weatherimage")
            parsingComplete = false

        } catch (e: Exception) {
            UtilityLog.d("SpecialWeather", "failed to readAndParseJSON(...)...")
            e.printStackTrace()
        }

    }

    companion object {

        internal fun convertStreamToString(`is`: java.io.InputStream): String {
            val s = java.util.Scanner(`is`).useDelimiter("\\A")
            return if (s.hasNext()) s.next() else ""
        }
    }
}
