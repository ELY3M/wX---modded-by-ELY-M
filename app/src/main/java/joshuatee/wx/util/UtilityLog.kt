package joshuatee.wx.util


object UtilityLog {

    fun d(TAG: String, message: String) {
        val maxLogSize = 2000
        (0..message.length / maxLogSize).forEach {
            val start = it * maxLogSize
            var end = (it + 1) * maxLogSize
            end = if (end > message.length) message.length else end
            android.util.Log.d(TAG, message.substring(start, end))
        }
    }

    /*fun bigLog(tag: String, message: String) {
        var delim = "\n"
        if (!message.contains(delim)) {
            delim = "<br/>"
        }
        message.split(delim).forEach { line ->
            android.util.Log.d(tag, line)
        }
    }*/

    fun handleException(exception: Exception): Unit = exception.printStackTrace()

    fun handleException(exception: OutOfMemoryError): Unit = exception.printStackTrace()
}
