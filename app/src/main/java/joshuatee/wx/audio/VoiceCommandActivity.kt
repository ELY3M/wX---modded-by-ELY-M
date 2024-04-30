/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022, 2023, 2024  joshua.tee@gmail.com

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

package joshuatee.wx.audio

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.View
import android.widget.Toast
import joshuatee.wx.settings.Location
import joshuatee.wx.ui.PopupMessage

class VoiceCommandActivity : Activity() {

    private val requestOk = 1
    private lateinit var mainView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainView = findViewById(android.R.id.content)
        if (UtilityTts.mediaPlayer != null && UtilityTts.mediaPlayer!!.isPlaying) {
            UtilityTts.mediaPlayer!!.stop()
            UtilityTts.ttsIsPaused = true
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US")
        try {
            startActivityForResult(intent, requestOk)
        } catch (e: Exception) {
            Toast.makeText(this, "Error initializing speech to text engine.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == requestOk && resultCode == RESULT_OK) {
            val thingsYouSaid = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            PopupMessage(mainView, thingsYouSaid!![0], PopupMessage.SHORT)
            val address = thingsYouSaid[0]
            val gotHit = UtilityVoiceCommand.processCommand(this, address, Location.rid, Location.wfo, Location.state)
            if (!gotHit) {
                finish()
            }
        } else {
            finish()
        }
    }

    override fun onRestart() {
        finish()
        super.onRestart()
    }
}
