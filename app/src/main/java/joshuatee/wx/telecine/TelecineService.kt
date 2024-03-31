/*

Changes were made to this file for inclusion in the wX program.

joshua.tee@gmail.com

 */

package joshuatee.wx.telecine

import android.content.Context

class TelecineService {

    private var recordingSession: RecordingSession? = null

    fun start(context: Context) {
        recordingSession = RecordingSession(context)
        recordingSession!!.showOverlay()
    }
}
