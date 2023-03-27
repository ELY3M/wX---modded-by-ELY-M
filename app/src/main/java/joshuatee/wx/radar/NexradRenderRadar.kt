/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022  joshua.tee@gmail.com

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

package joshuatee.wx.radar

import android.content.Context
import android.graphics.Color
import joshuatee.wx.Jni
import joshuatee.wx.radarcolorpalettes.ColorPalette
import joshuatee.wx.settings.RadarPreferences
import joshuatee.wx.util.UtilityIO
import joshuatee.wx.util.UtilityLog

object NexradRenderRadar {

    fun downloadRadarFile(context: Context, data: NexradRenderData, state: NexradRenderState, fileName: String, urlStr: String) {
        data.radarBuffers.fileName = fileName
        // added to allow animations to skip a frame and continue
        // comment2 - the comment above does not apply to the following 7 lines, not sure why this is here
        if (NexradUtil.isProductTdwr(state.product)) {
            val oldRid = state.rid
            if (state.rid == "") {
                state.rid = oldRid
                state.product = "N0Q"
            }
        }
        //
        // Download the radar file
        // if fileName is an empty string then we need to fetch the radar file
        // if not empty, its part of an animation sequence
        //
        if (data.radarBuffers.fileName == "") {
            NexradDownload.getRadarFile(context, urlStr, state.rid, state.product, state.indexString)
            data.radarBuffers.fileName = if (!state.product.contains("L2")) {
                "nids${state.indexString}"
            } else {
                "l2${state.indexString}"
            }
        }
    }

    fun decodeRadarHeader(context: Context,
            data: NexradRenderData,
            state: NexradRenderState,
            wxglNexradLevel2: NexradLevel2,
            wxglNexradLevel3: NexradLevel3,
            performDecomp: Boolean
    ) {
        //
        // extract information from the header
        //
        data.radarBuffers.setProductCodeFromString(state.product)
        try {
            when {
                // Level 2
                state.product.contains("L2") -> {
                    wxglNexradLevel2.decodeAndPlot(context, data.radarBuffers.fileName, state.product, state.timeStampId, state.indexString, performDecomp)
                    data.radarBuffers.extractL2Data(wxglNexradLevel2)
                }
                // 4bit products spectrum width, comp ref, storm relative mean velocity
                state.product.contains("NSW") || state.product.startsWith("NC") || state.product.matches(Regex("N[0-3]S")) -> {
                    wxglNexradLevel3.decodeAndPlotFourBit(context, data.radarBuffers.fileName, state.rid, state.timeStampId)
                    data.radarBuffers.extractL3Data(wxglNexradLevel3)
                }
                // Level 3 8bit
                else -> {
                    wxglNexradLevel3.decodeAndPlot(context, data.radarBuffers.fileName, state.rid, state.timeStampId)
                    data.radarBuffers.extractL3Data(wxglNexradLevel3)
                }
            }
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        if (data.radarBuffers.numRangeBins == 0) {
            data.radarBuffers.numRangeBins = 460
            data.radarBuffers.numberOfRadials = 360
        }
        data.radarBuffers.initialize()
        data.radarBuffers.setToPositionZero()
    }

    fun createRadials(context: Context,
            data: NexradRenderData,
            state: NexradRenderState,
            wxglNexradLevel2: NexradLevel2,
            wxglNexradLevel3: NexradLevel3,
    ): Int {
        //
        // decode the radar file after setting up the color map
        //
        var totalBins = 0
        val colorPalette =
                if (ColorPalette.colorMap.containsKey(data.radarBuffers.productCode.toInt())) {
                    ColorPalette.colorMap[data.radarBuffers.productCode.toInt()]!!
                } else {
                    ColorPalette.colorMap[94]!!
                }
        try {
            val fourBitProducts = listOf<Short>(56, 30, 181, 78, 80, 37, 38, 41, 57)
            if (state.product.startsWith("NC") || data.radarBuffers.productCode.toInt() == 41 || data.radarBuffers.productCode.toInt() == 57) {
                totalBins = NexradRaster.create(data.radarBuffers, wxglNexradLevel3.binWord)
            } else if (!state.product.contains("L2")) {
                totalBins = if (!fourBitProducts.contains(data.radarBuffers.productCode)) {
                    if (!RadarPreferences.useJni || data.radarBuffers.productCode.toInt() == 2153 || data.radarBuffers.productCode.toInt() == 2154)
                        NexradDecodeEightBit.andCreateRadials(context, data.radarBuffers)
                    else {
                        Jni.decode8BitAndGenRadials(
                                UtilityIO.getFilePath(context, data.radarBuffers.fileName),
                                wxglNexradLevel3.seekStart,
                                wxglNexradLevel3.compressedFileSize,
                                wxglNexradLevel3.iBuff,
                                wxglNexradLevel3.oBuff,
                                data.radarBuffers.floatBuffer,
                                data.radarBuffers.colorBuffer,
                                data.radarBuffers.binSize,
                                Color.red(data.radarBuffers.bgColor).toByte(),
                                Color.green(data.radarBuffers.bgColor).toByte(),
                                Color.blue(data.radarBuffers.bgColor).toByte(),
                                colorPalette.redValues,
                                colorPalette.greenValues,
                                colorPalette.blueValues,
                                data.radarBuffers.productCode.toInt()
                        )
                    }
                } else {
                    NexradDecodeEightBit.createRadials(data.radarBuffers, wxglNexradLevel3.binWord, wxglNexradLevel3.radialStart)
                }
            } else {
                wxglNexradLevel2.binWord.position(0)
                totalBins = if (RadarPreferences.useJni)
                    Jni.level2GenRadials(
                            data.radarBuffers.floatBuffer,
                            data.radarBuffers.colorBuffer,
                            wxglNexradLevel2.binWord,
                            wxglNexradLevel2.radialStartAngle,
                            data.radarBuffers.numberOfRadials,
                            data.radarBuffers.numRangeBins,
                            data.radarBuffers.binSize,
                            data.radarBuffers.bgColor,
                            colorPalette.redValues,
                            colorPalette.greenValues,
                            colorPalette.blueValues,
                            data.radarBuffers.productCode.toInt()
                    )
                else
                    NexradDecodeEightBit.createRadials(data.radarBuffers, wxglNexradLevel2.binWord, wxglNexradLevel2.radialStartAngle)
            } // level 2 , level 3 check
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        return totalBins
    }
}
