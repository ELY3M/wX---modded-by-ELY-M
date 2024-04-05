/*

Changes were made to this file for inclusion in the wX program.

joshua.tee@gmail.com

 */

package joshuatee.wx.telecine

import android.app.Activity

class TelecineService {

    private var recordingSession: RecordingSession? = null

    fun start(activity: Activity) {
        recordingSession = RecordingSession(activity)
        recordingSession!!.showOverlay()
    }
}
