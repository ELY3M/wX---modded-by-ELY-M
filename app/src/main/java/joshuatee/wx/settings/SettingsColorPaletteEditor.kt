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
//modded by ELY M.  

package joshuatee.wx.settings

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import joshuatee.wx.R
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.objects.ObjectDateTime
import joshuatee.wx.radar.NexradUtil
import joshuatee.wx.radarcolorpalettes.ColorPalette
import joshuatee.wx.radarcolorpalettes.UtilityColorPalette
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.Card
import joshuatee.wx.ui.FabExtended
import joshuatee.wx.ui.ObjectDialogue
import joshuatee.wx.ui.TextEdit
//elys mod - leave this alone
import joshuatee.wx.util.*
import java.io.File

class SettingsColorPaletteEditor : BaseActivity(), OnMenuItemClickListener {

    companion object {
        const val URL = ""
        private const val READ_REQUEST_CODE = 42
    }

    private lateinit var arguments: Array<String>
    private var formattedDate = ""
    private var name = ""
    private var type = ""
    private var typeAsInt = 0
    private lateinit var palTitle: TextEdit
    private lateinit var palContent: TextEdit

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(
            savedInstanceState,
            R.layout.activity_settings_color_palette_editor,
            R.menu.settings_color_palette_editor,
            true
        )
	showLoadFromFileMenuItem()
        arguments = intent.getStringArrayExtra(URL)!!
        type = arguments[0]
        typeAsInt = type.toIntOrNull() ?: 94
        setTitle("Palette Editor", NexradUtil.productCodeStringToName[typeAsInt]!!)
        setupUI()
        formattedDate = ObjectDateTime.getDateAsString("MMdd")
        name = if (arguments[2].contains("false")) {
            arguments[1]
        } else {
            arguments[1] + "_" + formattedDate
        }
        palTitle.text = name
        palContent.text =
            UtilityColorPalette.getColorMapStringFromDisk(this, typeAsInt, arguments[1])
    }

    private fun setupUI() {
        palTitle = TextEdit(this, R.id.palTitle)
        palContent = TextEdit(this, R.id.palContent)
        toolbarBottom.setOnMenuItemClickListener(this)
        FabExtended(this, R.id.fab, GlobalVariables.ICON_DONE, "Save") { savePalette(this) }
        Card(this, R.id.cv1)
        if (UIPreferences.themeInt == R.style.MyCustomTheme_white_NOAB) {
            listOf(palTitle, palContent).forEach {
                it.setTextColor(Color.BLACK)
                it.setHintTextColor(Color.GRAY)
            }
        }
        palTitle.setTextSize(UIPreferences.textSizeLarge)
        palContent.setTextSize(UIPreferences.textSizeNormal)
    }

    private fun savePalette(context: Context) {
        val date = ObjectDateTime.getDateAsString("HH:mm")
        val errorCheck = checkMapForErrors()
        if (errorCheck == "") {
            var textToSave = palContent.text
            textToSave = textToSave.replace(",,".toRegex(), ",")
            palContent.text = textToSave
            Utility.writePref(
                context,
                "RADAR_COLOR_PAL_" + type + "_" + palTitle.text,
                textToSave
            )
            if (!ColorPalette.radarColorPaletteList[typeAsInt]!!.contains(palTitle.text)) {
                ColorPalette.radarColorPaletteList[typeAsInt] =
                    ColorPalette.radarColorPaletteList[typeAsInt]!! + ":" + palTitle.text + ":"
                Utility.writePref(
                    context,
                    "RADAR_COLOR_PALETTE_" + type + "_LIST",
                    ColorPalette.radarColorPaletteList[typeAsInt]!!
                )
            }
	    //elys mod
            savepalfile(palTitle.text+"_"+type+".txt", textToSave)
            toolbar.subtitle = "Last saved: $date"
        } else {
            ObjectDialogue(this, errorCheck)
        }
        val fileName = "colormap" + type + palTitle.text
        if (UtilityFileManagement.internalFileExist(context, fileName)) {
            UtilityFileManagement.deleteFile(context, fileName)
        }
    }

    private fun checkMapForErrors(): String {
        val text = convertPalette(palContent.text)
        palContent.text = text
        val lines = text.split("\n".toRegex()).dropLastWhile { it.isEmpty() }
        var errors = ""
        var priorValue = -200.0
        var lineCount = 0
        lines.forEach { line ->
            if (line.contains("olor") && !line.contains("#")) {
                val list = if (line.contains(",")) {
                    line.split(",")
                } else {
                    line.split(" ")
                }
                lineCount += 1
                try {
                    if (list.size > 4) {
                        if (priorValue >= To.double(list[1])) {
                            errors += "The following lines do not have dbz values in increasing order: " + GlobalVariables.newline + priorValue + " " + list[1] + GlobalVariables.newline
                        }
                        priorValue = To.double(list[1])
                        if (To.double(list[2]) > 255.0 || To.double(list[2]) < 0.0) {
                            errors =
                                errors + "Red value must be between 0 and 255: " + GlobalVariables.newline + line + GlobalVariables.newline
                        }
                        if (To.double(list[3]) > 255.0 || To.double(list[3]) < 0.0) {
                            errors += "Green value must be between 0 and 255: " + GlobalVariables.newline + line + GlobalVariables.newline
                        }
                        if (To.double(list[4]) > 255.0 || To.double(list[4]) < 0.0) {
                            errors += "Blue value must be between 0 and 255: " + GlobalVariables.newline + line + GlobalVariables.newline
                        }
                    } else {
                        errors += "The following line does not have the correct number of command separated entries: " + GlobalVariables.newline + line + GlobalVariables.newline
                    }
                } catch (e: Exception) {
                    errors += "Problem parsing number."
                    UtilityLog.handleException(e)
                }
            }
        }
        if (lineCount < 2) {
            errors += "Not enough lines present."
        }
        return errors
    }

    @SuppressLint("SetTextI18n")
    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_reset -> palContent.text =
                UtilityColorPalette.getColorMapStringFromDisk(
                    this,
                    typeAsInt,
                    arguments[1]
                )

            R.id.action_clear -> palContent.text = ""
            R.id.action_share -> UtilityShare.textAsAttachment(
                this,
                palTitle.text,
                palContent.text,
                "wX_colormap_" + palTitle.text + ".txt"
            )
            R.id.action_load -> loadSettings()

            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onStop() {
        UtilityFileManagement.deleteFile(this, "colormap" + type + palTitle.text)
        super.onStop()
    }


    private fun showLoadFromFileMenuItem() {
        toolbarBottom.menu.findItem(R.id.action_load).isVisible = true
    }

    private fun loadSettings() {
        performFileSearch()
    }

    private fun displaySettings(txt: String) {
        //palContent.setText(txt)
	palContent.text = txt
    }

    private fun convertPalette(txt: String): String {
        var txtLocal = txt
            .replace("color", "Color")
            .replace("product", "#product")
            .replace("unit", "#unit")
            .replace("step", "#step")
            .replace(":", " ")
            .trim { it <= ' ' }.replace(" +".toRegex(), " ")
            .trim { it <= ' ' }.replace(" ".toRegex(), ",")
            .replace("\\s".toRegex(), "")
        val lines = txtLocal.split(GlobalVariables.newline.toRegex()).dropLastWhile { it.isEmpty() }
        if (lines.size < 3) {
            txtLocal = txtLocal.replace("Color", GlobalVariables.newline + "Color")
        }
        txtLocal = txtLocal.replace("Step", GlobalVariables.newline + "#Step")
            .replace("Units", GlobalVariables.newline + "#Units")
            .replace("ND", GlobalVariables.newline + "#ND")
            .replace("RF", GlobalVariables.newline + "#RF")
        return txtLocal
    }
    /**
     * Fires an intent to spin up the "file chooser" UI and select an image.
     */
    private fun performFileSearch() {
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            //val uri: Uri
            resultData?.let {
                val uri = it.data
                displaySettings(readTextFromUri(uri!!))
            }
        }
    }

    private fun readTextFromUri(uri: Uri): String {
        val content = UtilityIO.readTextFromUri(this, uri)
        val uriArr = uri.lastPathSegment!!.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        var fileName = "map"
        if (uriArr.isNotEmpty()) {
            fileName = uriArr.last()
        }
        fileName = fileName.replace(".txt", "").replace(".pal", "")
        name = fileName + "_" + formattedDate
        //palTitle.setText(name)
	palTitle.text = name
        return convertPalette(content)
    }
    //elys mod
    private fun savepalfile(fileName: String, text: String) {
        val dir = GlobalVariables.PalFilesPath
        //println(content)
        File("$dir/$fileName").printWriter().use {
            it.println(text)
        }
    }
}
