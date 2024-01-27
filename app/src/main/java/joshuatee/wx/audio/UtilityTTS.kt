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

import java.io.File
import java.util.HashMap
import java.util.Locale
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Environment
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.settings.NotificationPreferences
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityLog
import java.io.IOException
import kotlin.math.min

object UtilityTts {

    private var ttsInit = false
    private var ttobjGlobal: TextToSpeech? = null

    //    private const val TEXT_OLD = ""
    var mediaPlayer: MediaPlayer? = null
    private const val FILENAME = "/${GlobalVariables.packageNameAsString}_tts.wav"
    private var mpInit = false
    private var fileCount = 0
    private var currentFile = 0
    var ttsIsPaused = false
    private var playlistTotal = 0
    private var playlistNumber = 0
    private var playlistArr = List(2) { "" }

    fun loadTts(context: Context) {
        if (NotificationPreferences.notifTts) {
            initTts(context)
            UtilityLog.d("wx", "DEBUG: TTS init for notif")
        }
    }

    fun initTts(context: Context) {
        // samsung bug, if users do not have google TTS selected it will crash - add try-catch so user can at least use rest of program
        try {
            ttobjGlobal = TextToSpeech(context) { status ->
                if (status != TextToSpeech.ERROR) {
                    ttobjGlobal?.language = Locale.US
                }
            }
            ttsInit = true
            ttobjGlobal!!.setSpeechRate(Utility.readPrefInt(context, "TTS_SPEED_PREF", 10) / 10.0f)
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
    }

    fun shutdownTts() {
        ttobjGlobal.let {
            ttobjGlobal!!.stop()
            ttobjGlobal!!.shutdown()
        }
    }

    private fun splitInChunks(s: String): List<String> {
        val chunkSize = 250
        return (0..s.length step chunkSize).map { s.substring(it, min(s.length, it + chunkSize)) }
    }

    private fun initMediaPlayer(context: Context) {
        mediaPlayer = MediaPlayer()
        mediaPlayer!!.setOnCompletionListener {
            if (currentFile < fileCount) {
                playMediaPlayerFile(context, currentFile)
                currentFile += 1
            } else if (playlistTotal > 1) {
                playlistNumber += 1
                synthesizeTextAndPlayPlaylist(context, playlistNumber)
            }
        }
        mpInit = true
    }

    internal fun synthesizeTextAndPlayPlaylist(context: Context, index: Int) {
        playlistArr = UIPreferences.playlistStr.split(":")
        playlistNumber = index
        // perform check to see if idx is correct in array
        // got one bug for index being greater
        if (index >= playlistArr.size) {
            return
        }
        val prodg = playlistArr[index]
        playlistTotal = playlistArr.size
        currentFile = 0
        ttsIsPaused = false
        if (!ttsInit) {
            initTts(context)
        }
        // clear the queue of any pending objects
        ttobjGlobal!!.stop()
        if (!mpInit) {
            initMediaPlayer(context)
        }
        synthesizeText(context, Utility.readPref(context, "PLAYLIST_" + playlistArr[index], ""), prodg)
        ttobjGlobal!!.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onDone(utteranceId: String) {
                if (currentFile == 0 && utteranceId.contains(prodg)) {
                    playMediaPlayerFile(context, 0)
                    currentFile += 1
                }
            }

            override fun onError(utteranceId: String) {}

            override fun onStart(utteranceId: String) {}
        })
    }

    fun synthesizeTextAndPlay(context: Context, txt: String, prod: String) {
        playlistTotal = 1
        currentFile = 0
        ttsIsPaused = false
        if (!ttsInit) {
            initTts(context)
        }
        // clear the queue of any pending objects
        ttobjGlobal!!.stop()
        if (!mpInit) {
            initMediaPlayer(context)
        }
        synthesizeText(context, txt, prod)
        ttobjGlobal!!.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onDone(utteranceId: String) {
                if (currentFile == 0 && utteranceId.contains(prod)) {
                    playMediaPlayerFile(context, 0)
                    currentFile += 1
                }
            }

            override fun onError(utteranceId: String) {}

            override fun onStart(utteranceId: String) {}
        })
    }

    private fun synthesizeText(context: Context, txtF: String, prod: String) {
        if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
            mediaPlayer!!.stop()
        }
        val txt = UtilityTtsTranslations.translateAbbreviation(txtF)
        val myHashRender = HashMap<String, String>()
        val musicDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        val wxDir = File(musicDir, GlobalVariables.packageNameAsString)
        if (!wxDir.exists() && !wxDir.mkdirs()) {
            return
        }
        val chunks = splitInChunks(Utility.fromHtml(txt))
        fileCount = chunks.size
        (0 until fileCount).forEach {
            myHashRender.clear()
            myHashRender[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = it.toString() + prod
            val fileName = File(wxDir, FILENAME + it.toString()).absolutePath
            ttobjGlobal!!.synthesizeToFile(chunks[it], myHashRender, fileName)
        }
    }

    internal fun playMediaPlayer(status: Int) {
        if (status == 0) {
            mediaPlayer!!.start()
        }
        if (status == 1) {
            ttsIsPaused = if (!ttsIsPaused) {
                mediaPlayer!!.pause()
                true
            } else {
                mediaPlayer!!.start()
                false
            }
        }
    }

    private fun playMediaPlayerFile(context: Context, fileNum: Int) {
        val musicDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        val wxDir = File(musicDir, GlobalVariables.packageNameAsString)
        if (!wxDir.exists() && !wxDir.mkdirs()) {
            return
        }
        mediaPlayer?.reset()
        val fileName = File(wxDir, FILENAME + fileNum.toString()).absolutePath
        val uri = Uri.parse("file://$fileName")
        try {
            mediaPlayer?.setDataSource(context, uri)
            mediaPlayer?.prepare()
            mediaPlayer?.start()
        } catch (e: IOException) {
            UtilityLog.handleException(e)
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
    }

    fun conditionalPlay(arguments: Array<String>, index: Int, context: Context, html: String, label: String) {
        if (arguments.size > index && arguments[index] == "sound") {
            synthesizeTextAndPlay(context, html, label)
        }
    }
}
