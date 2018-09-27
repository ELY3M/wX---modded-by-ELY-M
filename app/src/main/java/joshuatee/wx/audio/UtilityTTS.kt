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

import joshuatee.wx.MyApplication
import joshuatee.wx.UIPreferences
import joshuatee.wx.notifications.UtilityNotification
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityLog

object UtilityTTS {

    private var ttsInit = false
    private var ttobjGlobal: TextToSpeech? = null
    private const val TEXT_OLD = ""
    var mMediaPlayer: MediaPlayer? = null
    private const val FILENAME = "/joshuatee.wx_tts.wav"
    private var mpInit = false
    private var fileCount = 0
    private var currentFile = 0
    var ttsIsPaused = false
    private var playlistTotal = 0
    private var playlistNumber = 0
    private var playlistArr = List(2) { _ -> "" }

    fun initTTS(context: Context) {
        // samsung bug, if users do not have google TTS selected it will crash - add try-catch so user can at least use rest of prog
        try {
            ttobjGlobal = TextToSpeech(context, TextToSpeech.OnInitListener { status ->
                if (status != TextToSpeech.ERROR) {
                    //ttobjGlobal!!.language = Locale.US
                    ttobjGlobal?.language = Locale.US
                }
            })
            ttsInit = true
        } catch (e: Exception) {
            UtilityLog.HandleException(e)
        }
    }

    internal fun playAgainTTS(context: Context) {
        if (!ttsInit) {
            initTTS(context)
        }
        ttobjGlobal!!.setSpeechRate(Utility.readPref(context, "TTS_SPEED_PREF", 10) / 10f)
        splitInChunks(Utility.fromHtml(TEXT_OLD), 1000).forEach { ttobjGlobal!!.speak(it, TextToSpeech.QUEUE_ADD, null) }
    }

    private fun splitInChunks(s: String, chunkSize: Int): List<String> {
        val length = s.length
        return (0..length step chunkSize).mapTo(mutableListOf()) { s.substring(it, Math.min(length, it + chunkSize)) }
    }

    private fun initMediaPlayer(context: Context) {
        mMediaPlayer = MediaPlayer()
        mMediaPlayer!!.setOnCompletionListener {
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

    internal fun synthesizeTextAndPlayPlaylist(context: Context, idx: Int) {
        playlistArr = MyApplication.playlistStr.split(":")
        playlistNumber = idx
        // perform check to see if idx is correct in array
        // got one bug for index being greater
        if (idx >= playlistArr.size) {
            return
        }
        val prodg = playlistArr[idx]
        playlistTotal = playlistArr.size
        currentFile = 0
        ttsIsPaused = false
        if (!ttsInit) initTTS(context)
        // clear the queue of any pending objects
        ttobjGlobal!!.stop()
        if (!mpInit) {
            initMediaPlayer(context)
        }
        synthesizeText(context, Utility.readPref(context, "PLAYLIST_" + playlistArr[idx], ""), prodg)
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

    internal fun synthesizeTextAndPlayNext(context: Context) {
        playlistArr = MyApplication.playlistStr.split(":")
        playlistNumber += 1
        if (playlistNumber >= playlistArr.size) playlistNumber = 1
        if (playlistNumber < playlistArr.size) {
            val prodg = playlistArr[playlistNumber]
            playlistTotal = playlistArr.size
            currentFile = 0
            ttsIsPaused = false
            if (!ttsInit) initTTS(context)
            // clear the queue of any pending objects
            ttobjGlobal!!.stop()
            if (!mpInit) {
                initMediaPlayer(context)
            }
            if (mMediaPlayer!!.isPlaying) mMediaPlayer!!.stop()
            synthesizeText(context, Utility.readPref(context, "PLAYLIST_" + playlistArr[playlistNumber], ""), prodg)
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
    }

    private fun synthesizeTextAndPlayPrevoius(context: Context) {
        playlistArr = MyApplication.playlistStr.split(":")
        playlistNumber -= 1
        if (playlistNumber == -1) playlistNumber = playlistArr.size - 1
        val prodg = playlistArr[playlistNumber]
        playlistTotal = playlistArr.size
        currentFile = 0
        ttsIsPaused = false
        if (!ttsInit) initTTS(context)
        // clear the queue of any pending objects
        ttobjGlobal!!.stop()
        if (!mpInit) {
            initMediaPlayer(context)
        }
        if (mMediaPlayer!!.isPlaying) mMediaPlayer!!.stop()
        synthesizeText(context, Utility.readPref(context, "PLAYLIST_" + playlistArr[playlistNumber], ""), prodg)
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
        if (!ttsInit) initTTS(context)
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
        var txt = txtF
        if (mMediaPlayer != null && mMediaPlayer!!.isPlaying) {
            mMediaPlayer!!.stop()
        }
        txt = UtilityTTSTranslations.tranlasteAbbrev(txt)
        val myHashRender = HashMap<String, String>()
        val musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
        val wxDir = File(musicDir, "joshuatee.wx")
        if (!wxDir.exists() && !wxDir.mkdirs()) {
            return
        }
        var fileName: String
        val chunkAl = splitInChunks(Utility.fromHtml(txt), 250)
        fileCount = chunkAl.size
        (0 until fileCount).forEach {
            myHashRender.clear()
            myHashRender[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = it.toString() + prod
            fileName = File(wxDir, FILENAME + it.toString()).absolutePath
            ttobjGlobal!!.synthesizeToFile(chunkAl[it], myHashRender, fileName)
        }
        if (UIPreferences.mediaControlNotif) {
            UtilityNotification.createMediaControlNotif(context, prod)
        }
    }

    internal fun playMediaPlayer(status: Int) {
        if (status == 0) {
            mMediaPlayer!!.start()
        }
        if (status == 1) {
            ttsIsPaused = if (!ttsIsPaused) {
                mMediaPlayer!!.pause()
                true
            } else {
                mMediaPlayer!!.start()
                false
            }
        }
    }

    internal fun mediaPlayerPause() {
        if (mpInit) {
            mMediaPlayer!!.pause()
            ttsIsPaused = true
        }
    }

    internal fun mediaPlayerRewind(context: Context) {
        if (mpInit) {
            if (mMediaPlayer!!.currentPosition < 10000) {
                UtilityTTS.synthesizeTextAndPlayPrevoius(context)
            } else {
                playMediaPlayerFile(context, 0)
                currentFile += 1
            }
            ttsIsPaused = false
        }
    }

    private fun playMediaPlayerFile(context: Context, fileNum: Int) {
        val musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
        val wxDir = File(musicDir, "joshuatee.wx")
        if (!wxDir.exists() && !wxDir.mkdirs()) {
            return
        }
        try {
            mMediaPlayer?.reset()
            val fileName = File(wxDir, FILENAME + fileNum.toString()).absolutePath
            val uri = Uri.parse("file://$fileName")
            mMediaPlayer?.setDataSource(context, uri)
            mMediaPlayer?.prepare()
            mMediaPlayer?.start()
        } catch (e: Exception) {
            UtilityLog.HandleException(e)
        }
    }
}
