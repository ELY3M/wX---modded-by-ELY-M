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

package joshuatee.wx.settings

import java.io.BufferedReader
import java.io.InputStreamReader

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener

import joshuatee.wx.R
import joshuatee.wx.MyApplication
import joshuatee.wx.UIPreferences
import joshuatee.wx.ui.ObjectCard
import joshuatee.wx.ui.UtilityToolbar
import joshuatee.wx.util.UtilityAlertDialog
import joshuatee.wx.util.UtilityLog
import joshuatee.wx.util.UtilityPreferences
import joshuatee.wx.util.UtilityShare

class SettingsPrefIO : AppCompatActivity(), OnClickListener, OnMenuItemClickListener {

    companion object {
        const val NO: String = ""
        private const val READ_REQUEST_CODE = 42
    }

    private lateinit var toolbar: Toolbar
    private lateinit var toolbarBottom: Toolbar
    private lateinit var tv: TextView
    private var settingsTxt = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(UIPreferences.themeInt)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings_prefio)
        toolbar = findViewById(R.id.toolbar_top)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbarBottom = findViewById(R.id.toolbar_bottom)
        if (MyApplication.iconsEvenSpaced)
            UtilityToolbar.setupEvenlyDistributedToolbar(this, toolbarBottom, R.menu.settings_prefio)
        else
            toolbarBottom.inflateMenu(R.menu.settings_prefio)
        toolbarBottom.setOnMenuItemClickListener(this)
        UtilityToolbar.fullScreenMode(toolbar, toolbarBottom)
        toolbar.subtitle = "Beta quality"
        ObjectCard(this, R.id.cv)
        tv = findViewById(R.id.tv)
        tv.setOnClickListener(this)
        settingsTxt = UtilityPreferences.printAllPreferences(this)
        tv.text = settingsTxt
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_share -> UtilityShare.shareTextAsAttachment(this, "wX Settings", settingsTxt, "wX_settings.txt")
            R.id.action_load -> loadSettings()
            R.id.action_apply -> applySettings()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.tv -> UtilityToolbar.showHide(toolbar, toolbarBottom)
        }
    }

    private fun loadSettings() {
        performFileSearch()
    }

    private fun displaySettings(txt: String) {
        settingsTxt = txt
        tv.text = settingsTxt
    }

    private fun applySettings() {
        UtilityPreferences.applySettings(this, settingsTxt)
        UtilityAlertDialog.restart()
    }

    /**
     * Fires an intent to spin up the "file chooser" UI and select an image.
     */
    private fun performFileSearch() {
        if (android.os.Build.VERSION.SDK_INT > 18) {
            // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file browser.
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            // Filter to only show results that can be "opened", such as a
            // file (as opposed to a list of contacts or timezones)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            // Filter to show only images, using the image MIME data type.
            // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
            // To search for all documents available via installed storage providers,
            // it would be "*/*".
            intent.type = "*/*"
            startActivityForResult(intent, READ_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            resultData?.let {
                val uri = it.data
                //UtilityLog.d("wx", "Uri: " + uri.toString())
                displaySettings(readTextFromUri(uri))
            }
        }
    }

    private fun readTextFromUri(uri: Uri): String {
        val stringBuilder = StringBuilder()
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val reader = BufferedReader(InputStreamReader(inputStream!!))
            var line: String? = reader.readLine()
            while (line != null) {
                stringBuilder.append(line)
                line = reader.readLine()
            }
        } catch (e: Exception) {
            UtilityLog.HandleException(e)
        }
        return stringBuilder.toString()
    }
}
