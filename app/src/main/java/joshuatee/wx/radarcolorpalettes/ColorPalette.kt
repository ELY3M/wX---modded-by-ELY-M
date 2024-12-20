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

package joshuatee.wx.radarcolorpalettes

import android.content.Context
import android.graphics.Color
import joshuatee.wx.R
import joshuatee.wx.util.To
import joshuatee.wx.util.UtilityIO
import java.nio.ByteBuffer
import java.nio.ByteOrder
import joshuatee.wx.util.UtilityLog

class ColorPalette(val context: Context, private val colormapCode: Int) {

    var redValues: ByteBuffer = ByteBuffer.allocateDirect(16)
        private set
    var greenValues: ByteBuffer = ByteBuffer.allocateDirect(16)
        private set
    var blueValues: ByteBuffer = ByteBuffer.allocateDirect(16)
        private set

    private fun setupBuffers(size: Int) {
        redValues = ByteBuffer.allocateDirect(size)
        redValues.order(ByteOrder.nativeOrder())
        greenValues = ByteBuffer.allocateDirect(size)
        greenValues.order(ByteOrder.nativeOrder())
        blueValues = ByteBuffer.allocateDirect(size)
        blueValues.order(ByteOrder.nativeOrder())
    }

    fun position(index: Int) {
        redValues.position(index)
        blueValues.position(index)
        greenValues.position(index)
    }

    fun putInt(colorAsInt: Int) {
        try {
            redValues.put(Color.red(colorAsInt).toByte())
            greenValues.put(Color.green(colorAsInt).toByte())
            blueValues.put(Color.blue(colorAsInt).toByte())
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
    }

    private fun putBytes(redByte: Int, greenByte: Int, blueByte: Int) {
        if (redValues.hasRemaining()) {
            redValues.put(redByte.toByte())
        }
        if (greenValues.hasRemaining()) {
            greenValues.put(greenByte.toByte())
        }
        if (blueValues.hasRemaining()) {
            blueValues.put(blueByte.toByte())
        }
    }

    fun putLine(colorPaletteLine: ColorPaletteLine) {
        putBytes(colorPaletteLine.red, colorPaletteLine.green, colorPaletteLine.blue)
    }

    fun initialize() {
        when (colormapCode) {
            19, 30, 56 -> {
                setupBuffers(16)
                generate4bitGeneric(context, colormapCode)
            }

            165 -> {
                setupBuffers(256)
                try {
                    loadColorMap165(context, radarColorPalette[165]!!)
                } catch (e: Exception) {
                    UtilityLog.handleException(e)
                }
            }

            else -> {
                setupBuffers(256)
                try {
                    loadColorMap(context, colormapCode)
                } catch (e: Exception) {
                    UtilityLog.handleException(e)
                }
            }
        }
    }

    companion object {

        val colorMap = mutableMapOf<Int, ColorPalette>()
        val radarColorPalette = mutableMapOf<Int, String>()
        val radarColorPaletteList = mutableMapOf<Int, String>()

        private fun generate4bitGeneric(context: Context, product: Int) {
            colorMap[product]!!.position(0)
            val fileId = when (product) {
                19 -> R.raw.colormap19
                30 -> R.raw.colormap30
                56 -> R.raw.colormap56
                else -> R.raw.colormap19
            }
            UtilityIO.readTextFileFromRaw(context.resources, fileId).split("\n").forEach { line ->
                if (line.contains(",")) {
                    val objectColorPaletteLines = ColorPaletteLine.fourBit(line.split(","))
                    colorMap[product]!!.putLine(objectColorPaletteLines)
                }
            }
        }

        private fun loadColorMap165(context: Context, code: String) {
            val radarColorPaletteCode = 165
            val objectColorPalette = colorMap[radarColorPaletteCode]!!
            objectColorPalette.position(0)
            val colorPaletteLines = mutableListOf<ColorPaletteLine>()
            val text =
                UtilityColorPalette.getColorMapStringFromDisk(context, radarColorPaletteCode, code)
            val lines = text.split("\n").dropLastWhile { it.isEmpty() }
            lines.forEach { line ->
                if (line.contains("olor") && !line.contains("#")) {
                    val items = if (line.contains(",")) {
                        line.split(",")
                    } else {
                        line.split(" ")
                    }
                    if (items.size > 4) {
                        colorPaletteLines.add(ColorPaletteLine(items))
                    }
                }
            }
            val diff = 10
            colorPaletteLines.forEach {
                (0 until diff).forEach { _ ->
                    objectColorPalette.putLine(it)
                }
            }
        }

        private fun generate(context: Context, colorMapProductCode: Int, code: String) {
            // -32 to 95
            // add 32 and double to get 255 ( prod 94 )
            // def 2 and -32
            val scale: Int
            val lowerEnd: Int
            var prodOffset = 0.0
            var prodScale = 1.0
            val objectColorPalette = colorMap[colorMapProductCode]!!
            when (colorMapProductCode) {
                94 -> {
                    scale = 2
                    lowerEnd = -32
                }

                99 -> {
                    scale = 1
                    lowerEnd = -127
                }

                134 -> {
                    scale = 1
                    lowerEnd = 0
                    prodOffset = 0.0
                    prodScale = 3.64
                }

                135 -> {
                    scale = 1
                    lowerEnd = 0
                }

                159 -> {
                    scale = 1
                    lowerEnd = 0
                    prodOffset = 128.0
                    prodScale = 16.0
                }

                161 -> {
                    scale = 1
                    lowerEnd = 0
                    prodOffset = -60.5
                    prodScale = 300.0
                }

                163 -> {
                    scale = 1
                    lowerEnd = 0
                    prodOffset = 43.0
                    prodScale = 20.0
                }

                172 -> {
                    scale = 1
                    lowerEnd = 0
                }

                else -> {
                    scale = 2
                    lowerEnd = -32
                }
            }
            objectColorPalette.position(0)
            val colorPaletteLines = mutableListOf<ColorPaletteLine>()
            var r = "0"
            var g = "0"
            var b = "0"
            var priorLineHas6 = false
            UtilityColorPalette.getColorMapStringFromDisk(context, colorMapProductCode, code)
                .split("\n").forEach { line ->
                    if (line.contains("olor") && !line.contains("#")) {
                        val items = if (line.contains(",")) {
                            line.split(",")
                        } else {
                            line.split(" ")
                        }
                        if (items.size > 4) {
                            if (priorLineHas6) {
                                colorPaletteLines.add(
                                    ColorPaletteLine(
                                        (To.double(items[1]) * prodScale + prodOffset - 1).toInt(),
                                        r,
                                        g,
                                        b
                                    )
                                )
                                colorPaletteLines.add(
                                    ColorPaletteLine(
                                        (To.double(items[1]) * prodScale + prodOffset).toInt(),
                                        items[2],
                                        items[3],
                                        items[4]
                                    )
                                )
                                priorLineHas6 = false
                            } else {
                                colorPaletteLines.add(
                                    ColorPaletteLine(
                                        (To.double(items[1]) * prodScale + prodOffset).toInt(),
                                        items[2],
                                        items[3],
                                        items[4]
                                    )
                                )
                            }
                            if (items.size > 7) {
                                priorLineHas6 = true
                                r = items[5]
                                g = items[6]
                                b = items[7]
                            }
                        }
                    }
                }
            if (colorMapProductCode == 161) {
                // pad first 16, think this is needed
                repeat(10) {
                    if (colorPaletteLines.size > 0) {
                        objectColorPalette.putLine(colorPaletteLines[0])
                    }
                }
            }
            if (colorMapProductCode == 99 || colorMapProductCode == 135) {
                // first two levels are range folder per ICD
                if (colorPaletteLines.size > 0) {
                    objectColorPalette.putLine(colorPaletteLines[0])
                    objectColorPalette.putLine(colorPaletteLines[0])
                }
            }
            if (colorPaletteLines.size > 0) {
                (lowerEnd until colorPaletteLines[0].dbz).forEach { _ ->
                    objectColorPalette.putLine(colorPaletteLines[0])
                    if (scale == 2) { // 94 reflectivity
                        objectColorPalette.putLine(colorPaletteLines[0])
                    }
                }
            }
            colorPaletteLines.indices.forEach { index ->
                if (index < colorPaletteLines.lastIndex) {
                    val low = colorPaletteLines[index].dbz
                    val lowColor = colorPaletteLines[index].asInt
                    val high = colorPaletteLines[index + 1].dbz
                    val highColor = colorPaletteLines[index + 1].asInt
                    val diff = high - low
                    objectColorPalette.putLine(colorPaletteLines[index])
                    if (scale == 2) {
                        objectColorPalette.putLine(colorPaletteLines[index])
                    }
                    (1 until diff).forEach { j ->
                        @Suppress("KotlinConstantConditions")
                        if (scale == 1) {
                            val colorInt = UtilityNexradColors.interpolateColor(
                                lowColor,
                                highColor,
                                j.toFloat() / (diff * scale).toFloat()
                            )
                            objectColorPalette.putInt(colorInt)
                        } else if (scale == 2) {
                            val colorInt = UtilityNexradColors.interpolateColor(
                                lowColor,
                                highColor,
                                (j * scale - 1).toFloat() / (diff * scale).toFloat()
                            )
                            val colorInt2 = UtilityNexradColors.interpolateColor(
                                lowColor,
                                highColor,
                                (j * scale).toFloat() / (diff * scale).toFloat()
                            )
                            objectColorPalette.putInt(colorInt)
                            objectColorPalette.putInt(colorInt2)
                        }
                    }
                } else {
                    objectColorPalette.putLine(colorPaletteLines[index])
                    if (scale == 2) {
                        objectColorPalette.putLine(colorPaletteLines[index])
                    }
                }
            }
        }

        fun loadColorMap(context: Context, product: Int) {
            // This is the entrance method to load a colormap called at various spots
            // http://www.usawx.com/grradarexamples.htm
            var code = radarColorPalette[product] ?: ""
            if (code == "COD") {
                code = "CODENH"
            }
            generate(context, product, code)
        }
    }
}

/*# NSSL derived Reflectivity Pallette -32 -> 95
        #
        # Units: DBZ
        #
        # Commented lines begin with the hash mark '#'
        #
        # Format Color,MinZ,LR,LG,LB,HR,HG,HB
        # where LR,LG,LB =RGB color of MinZ value
        # and HR,HG,HB =RGB color of Next line's MinZ minus one (e.g. upper end of the sections' range)
        # MinZ is the minimum dBz value in the range.
        # Intermediate values are interpolated via HSB Interpolation
        # ND = Color for no detection generally black
        # You must specify an upper limit dBZ value as indicated by the
        # Color,93,250,250,250 line.
        #
        # If you mess things up, delete this file and a fresh version will be installed
        #
        Color,5,128,128,128
        Color,10,128,128,128
        Color,15,85,85,85
        Color,20,0,247,247
        Color,25,0,0,255
        Color,30,0,164,0
        Color,35,0,104,0,0,57,0
        Color,40,255,255,0
        Color,45,255,111,40
        Color,50,255,0,0
        Color,55,166,0,0
        Color,60,117,0,0
        Color,65,255,0,255
        Color,70,157,0,157
        Color,75,153,85,201
        Color,80,255,255,255
        Color,93,250,250,250
        ND, 0, 0, 0*/
