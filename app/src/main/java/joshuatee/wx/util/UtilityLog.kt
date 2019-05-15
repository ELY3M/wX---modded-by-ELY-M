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

    // FIXME camelcase
    fun HandleException(exception: Exception): Unit = exception.printStackTrace()

    fun handleException(exception: Exception): Unit = exception.printStackTrace()

    fun handleException(exception: OutOfMemoryError): Unit = exception.printStackTrace()
}
