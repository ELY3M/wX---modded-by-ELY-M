package joshuatee.wx.util


object UtilityLog {

    fun d(tag: String, message: String) {
        val maxLogSize = 2000
        (0..message.length / maxLogSize).forEach {
            val start = it * maxLogSize
            var end = (it + 1) * maxLogSize
            end = if (end > message.length) message.length else end
            android.util.Log.d(tag, message.substring(start, end))
        }
    }

    fun handleException(exception: Exception): Unit = exception.printStackTrace()

    fun handleException(exception: OutOfMemoryError): Unit = exception.printStackTrace()
}
