/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020  joshua.tee@gmail.com

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

package joshuatee.wx.models

import joshuatee.wx.util.Utility
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.AnimationDrawable
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import joshuatee.wx.Extensions.safeGet
import joshuatee.wx.ui.ObjectFab

class ObjectModelNoSpinner(val context: Context, var prefModel: String, numPanesStr: String) {

    var run = "00Z"
    var time = "00"
    var sector = ""
    var currentParam = ""
    var numPanes = 1
    var model = "WRF"
    var sectorInt = 0
    var curImg = 0
    var modelType = ModelType.NSSL
    var startStep = 0
    var endStep = 0
    var stepAmount = 1
    var numberRuns = 4
    var timeTruncate = 2
    var format = "%03d"
    var truncateTime = true
    var prefSector = "MODEL_" + prefModel + numPanesStr + "_SECTOR_LAST_USED"
    var prefParam = "MODEL_" + prefModel + numPanesStr + "_PARAM_LAST_USED"
    var prefParamLabel = "MODEL_" + prefModel + numPanesStr + "_PARAM_LAST_USED_LABEL"
    var prefRunPosn = "MODEL_" + prefModel + numPanesStr + "_RUN_POSN"
    var modelProvider = "MODEL_$prefModel$numPanesStr"
    var prefModelIndex = "MODEL_$prefModel${numPanesStr}_INDEX"
    private var modelIndex = 0
    var rtd = RunTimeData()
    lateinit var displayData: DisplayDataNoSpinner
    var sectors = listOf("")
    var labels = listOf("")
    var params = listOf("")
    var models = listOf("")
    var ncepRuns = mutableListOf("")
    var times = mutableListOf("")
    var timeIndex = 0
    private var defaultModel = ""
    var spinnerTimeValue = 0
    var animRan = false
    var firstRun = false
    var imageLoaded = false
    lateinit var miStatusParam1: MenuItem
    lateinit var miStatusParam2: MenuItem
    var fab1: ObjectFab? = null
    var fab2: ObjectFab? = null
    lateinit var toolbar: Toolbar
    lateinit var getContent: () -> Unit

    fun setUiElements(toolbar: Toolbar, fab1: ObjectFab?, fab2: ObjectFab?, miStatusParam1: MenuItem, miStatusParam2: MenuItem, getContent: () -> Unit) {
        this.miStatusParam1 = miStatusParam1
        this.miStatusParam2 = miStatusParam2
        this.fab1 = fab1
        this.fab2 = fab2
        this.toolbar = toolbar
        this.getContent = getContent
    }

    init {
        numPanes = numPanesStr.toIntOrNull() ?: 0
        when (prefModel) {
            "WPCGEFS" -> {
                modelType = ModelType.WPCGEFS
                defaultModel = "WPCGEFS"
                models = UtilityModelWpcGefsInterface.models
            }
            "NSSL" -> {
                modelType = ModelType.NSSL
                models = UtilityModelNsslWrfInterface.models
                defaultModel = "WRF"
            }
            "ESRL" -> {
                modelType = ModelType.ESRL
                models = UtilityModelEsrlInterface.models
            }
            "NCEP" -> {
                modelType = ModelType.NCEP
                models = UtilityModelNcepInterface.models
                defaultModel = "GFS"
            }
            "GLCFS" -> {
                modelType = ModelType.GLCFS
                models = UtilityModelGlcfsInterface.models
                defaultModel = "GLCFS"
            }
            "SPCSREF" -> {
                modelType = ModelType.SPCSREF
                models = UtilityModelSpcSrefInterface.models
                defaultModel = "SREF"
                timeTruncate = 4
                startStep = 0
                endStep = 88
                stepAmount = 3
            }
            "SPCHREF" -> {
                modelType = ModelType.SPCHREF
                models = UtilityModelSpcHrefInterface.models
                sectors = UtilityModelSpcHrefInterface.sectors
                defaultModel = "HREF"
                timeTruncate = 2
                startStep = 1
                endStep = 49
                stepAmount = 1
            }
            "SPCHRRR" -> {
                modelType = ModelType.SPCHRRR
                models = UtilityModelSpcHrrrInterface.models
                params = UtilityModelSpcHrrrInterface.params
                labels = UtilityModelSpcHrrrInterface.labels
                sectors = UtilityModelSpcHrrrInterface.sectors
                defaultModel = "HRRR"
                timeTruncate = 2
                startStep = 1
                endStep = 14
            }
        }
        model = Utility.readPref(context, prefModel, defaultModel)
        prefSector = "MODEL_" + prefModel + numPanesStr + "_SECTOR_LAST_USED"
        prefParam = "MODEL_" + prefModel + numPanesStr + "_PARAM_LAST_USED"
        prefParamLabel = "MODEL_" + prefModel + numPanesStr + "_PARAM_LAST_USED_LABEL"
        prefRunPosn = "MODEL_" + prefModel + numPanesStr + "_RUN_POSN"
        modelProvider = "MODEL_$prefModel"
        prefModelIndex = "MODEL_$prefModel${numPanesStr}_INDEX"
        modelIndex = Utility.readPref(context, prefModelIndex, 0)
        setParams(modelIndex)
        sector = Utility.readPref(context, prefSector, sectors[0])
    }

    fun getImage(index: Int, overlayImg: List<String>): Bitmap {
        currentParam = displayData.param[index]
        return when (modelType) {
            ModelType.WPCGEFS -> UtilityModelWpcGefsInputOutput.getImage(this, time)
            ModelType.ESRL -> UtilityModelEsrlInputOutput.getImage(this, time)
            ModelType.NSSL -> UtilityModelNsslWrfInputOutput.getImage(context, this, time)
            ModelType.GLCFS -> UtilityModelGlcfsInputOutput.getImage(this, time)
            ModelType.NCEP -> UtilityModelNcepInputOutput.getImage(this, time)
            ModelType.SPCSREF -> UtilityModelSpcSrefInputOutput.getImage(context, this, time)
            ModelType.SPCHREF -> UtilityModelSpcHrefInputOutput.getImage(context, this, time)
            ModelType.SPCHRRR -> UtilityModelSpcHrrrInputOutput.getImage(context, this, time, overlayImg)
        }
    }

    fun getAnimate(index: Int, overlayImg: List<String>): AnimationDrawable {
        currentParam = displayData.param[index]
        return when (modelType) {
            ModelType.WPCGEFS -> UtilityModelWpcGefsInputOutput.getAnimation(context, this)
            ModelType.ESRL -> UtilityModelEsrlInputOutput.getAnimation(context, this)
            ModelType.NSSL -> UtilityModelNsslWrfInputOutput.getAnimation(context, this)
            ModelType.GLCFS -> UtilityModelGlcfsInputOutput.getAnimation(context, this)
            ModelType.NCEP -> UtilityModelNcepInputOutput.getAnimation(context, this)
            ModelType.SPCSREF -> UtilityModelSpcSrefInputOutput.getAnimation(context, this)
            ModelType.SPCHREF -> UtilityModelSpcHrefInputOutput.getAnimation(context, this)
            ModelType.SPCHRRR -> UtilityModelSpcHrrrInputOutput.getAnimation(context, this, overlayImg)
        }
    }

    fun getRunTime() = when (modelType) {
            ModelType.WPCGEFS -> UtilityModelWpcGefsInputOutput.runTime
            ModelType.ESRL -> UtilityModelEsrlInputOutput.getRunTime(model, displayData.param[0])
            ModelType.NSSL -> UtilityModelNsslWrfInputOutput.runTime
            ModelType.NCEP -> UtilityModelNcepInputOutput.getRunTime(model, displayData.param[0], sector)
            ModelType.SPCSREF -> UtilityModelSpcSrefInputOutput.runTime
            ModelType.SPCHREF -> UtilityModelSpcHrefInputOutput.runTime
            ModelType.SPCHRRR -> UtilityModelSpcHrrrInputOutput.runTime
            else -> RunTimeData()
        }

    fun setParams(selectedItemPosition: Int) {
        modelIndex = selectedItemPosition
        Utility.writePref(context, prefModelIndex, modelIndex)
        when (modelType) {
            ModelType.SPCHRRR -> {
                when (selectedItemPosition) {
                    1 -> {
                        model = "HRRR"
                        params = UtilityModelSpcHrrrInterface.models
                        labels = UtilityModelSpcHrrrInterface.labels
                        sectors = UtilityModelSpcHrrrInterface.sectors
                        startStep = 1
                        endStep = 14
                        stepAmount = 1
                        numberRuns = 4
                    }
                }
            }
            ModelType.NCEP -> {
                timeTruncate = 3
                when (selectedItemPosition) {
                    4 -> {
                        model = "GFS"
                        params = UtilityModelNcepInterface.paramsGfs
                        labels = UtilityModelNcepInterface.labelsGfs
                        sectors = UtilityModelNcepInterface.sectorsGfs
                        startStep = 0
                        endStep = 85
                        stepAmount = 3
                        numberRuns = 4
                    }
                    11 -> {
                        model = "NAM"
                        params = UtilityModelNcepInterface.paramsNam
                        labels = UtilityModelNcepInterface.labelsNam
                        sectors = UtilityModelNcepInterface.sectorsNam
                        startStep = 0
                        endStep = 85
                        stepAmount = 3
                        numberRuns = 4
                    }
                    15 -> {
                        model = "RAP"
                        params = UtilityModelNcepInterface.paramsRap
                        labels = UtilityModelNcepInterface.labelsRap
                        sectors = UtilityModelNcepInterface.sectorsRap
                        startStep = 0
                        endStep = 39
                        stepAmount = 1
                        numberRuns = 24
                    }
                    6 -> {
                        model = "HRRR"
                        params = UtilityModelNcepInterface.paramsHrrr
                        labels = UtilityModelNcepInterface.labelsHrrr
                        sectors = UtilityModelNcepInterface.sectorsHrrr
                        startStep = 0
                        endStep = 18
                        stepAmount = 1
                        numberRuns = 24
                    }
                    12 -> {
                        model = "NAM-HIRES"
                        params = UtilityModelNcepInterface.paramsNamHires
                        labels = UtilityModelNcepInterface.labelsNamHires
                        sectors = UtilityModelNcepInterface.sectorsNamHires
                        startStep = 1
                        endStep = 60
                        stepAmount = 1
                        numberRuns = 4
                    }
                    9 -> {
                        model = "HRW-FV3"
                        params = UtilityModelNcepInterface.modelHrwFv3Params
                        labels = UtilityModelNcepInterface.modelHrwFv3Labels
                        sectors = UtilityModelNcepInterface.sectorsHrwNmm
                        startStep = 1
                        endStep = 48
                        stepAmount = 1
                        numberRuns = 2
                    }
                    7 -> {
                        model = "HRW-ARW"
                        params = UtilityModelNcepInterface.paramsHrwNmm
                        labels = UtilityModelNcepInterface.labelsHrwNmm
                        sectors = UtilityModelNcepInterface.sectorsHrwNmm
                        startStep = 1
                        endStep = 48
                        stepAmount = 1
                        numberRuns = 2
                    }
                    3 -> {
                        model = "GEFS-SPAG"
                        params = UtilityModelNcepInterface.paramsGefsSpag
                        labels = UtilityModelNcepInterface.labelsGefsSpag
                        sectors = UtilityModelNcepInterface.sectorsGefsSpag
                        startStep = 0
                        endStep = 385
                        stepAmount = 6
                        numberRuns = 4
                    }
                    2 -> {
                        model = "GEFS-MEAN-SPRD"
                        params = UtilityModelNcepInterface.paramsGefsMnsprd
                        labels = UtilityModelNcepInterface.labelsGefsMnsprd
                        sectors = UtilityModelNcepInterface.sectorsGefsMnsprd
                        startStep = 0
                        endStep = 385
                        stepAmount = 6
                        numberRuns = 4
                    }
                    16 -> {
                        model = "SREF"
                        params = UtilityModelNcepInterface.paramsSref
                        labels = UtilityModelNcepInterface.labelsSref
                        sectors = UtilityModelNcepInterface.sectorsSref
                        startStep = 0
                        endStep = 88
                        stepAmount = 3
                        numberRuns = 5
                    }
                    10 -> {
                        model = "NAEFS"
                        params = UtilityModelNcepInterface.paramsNaefs
                        labels = UtilityModelNcepInterface.labelsNaefs
                        sectors = UtilityModelNcepInterface.sectorsNaefs
                        startStep = 6
                        endStep = 385
                        stepAmount = 6
                        numberRuns = 4
                    }
                    14 -> {
                        model = "POLAR"
                        params = UtilityModelNcepInterface.paramsPolar
                        labels = UtilityModelNcepInterface.labelsPolar
                        sectors = UtilityModelNcepInterface.sectorsPolar
                        startStep = 24
                        endStep = 385
                        stepAmount = 24
                        numberRuns = 1
                    }
                    17 -> {
                        model = "GFS-WAVE"
                        params = UtilityModelNcepInterface.paramsGfsWave
                        labels = UtilityModelNcepInterface.labelsGfsWave
                        sectors = UtilityModelNcepInterface.sectorsGfsWave
                        startStep = 0
                        endStep = 127
                        stepAmount = 6
                        numberRuns = 4
                    }
                    0 -> {
                        model = "ESTOFS"
                        params = UtilityModelNcepInterface.paramsEstofs
                        labels = UtilityModelNcepInterface.labelsEstofs
                        sectors = UtilityModelNcepInterface.sectorsEstofs
                        startStep = 0
                        endStep = 181
                        stepAmount = 1
                        numberRuns = 4
                    }
                    1 -> {
                        model = "FIREWX"
                        params = UtilityModelNcepInterface.paramsFirefx
                        labels = UtilityModelNcepInterface.labelsFirefx
                        sectors = UtilityModelNcepInterface.sectorsFirewx
                        startStep = 0
                        endStep = 37
                        stepAmount = 1
                        numberRuns = 4
                    }
                    8 -> {
                        model = "HRW-ARW2"
                        params = UtilityModelNcepInterface.paramsHrwArw2
                        labels = UtilityModelNcepInterface.labelsHrwArw2
                        sectors = UtilityModelNcepInterface.sectorsHrwArw2
                        startStep = 1
                        endStep = 48
                        stepAmount = 1
                        numberRuns = 2
                    }
                    5 -> {
                        model = "HREF"
                        params = UtilityModelNcepInterface.paramsHref
                        labels = UtilityModelNcepInterface.labelsHref
                        sectors = UtilityModelNcepInterface.sectorsHref
                        startStep = 0
                        endStep = 36
                        stepAmount = 1
                        numberRuns = 4
                    }
                    13 -> {
                        model = "NBM"
                        params = UtilityModelNcepInterface.paramsNbm
                        labels = UtilityModelNcepInterface.labelsNbm
                        sectors = UtilityModelNcepInterface.sectorsNbm
                        startStep = 0
                        endStep = 264
                        stepAmount = 3
                        numberRuns = 4
                    }

                }
            }
            ModelType.GLCFS -> {
                when (selectedItemPosition) {
                    0 -> {
                        model = "GLCFS"
                        labels = UtilityModelGlcfsInterface.labels
                        params = UtilityModelGlcfsInterface.params
                        sectors = UtilityModelGlcfsInterface.sectors
                        startStep = 1
                        endStep = 48
                        stepAmount = 1
                        numberRuns = 0
                        timeTruncate = 3
                    }
                }
            }
            ModelType.WPCGEFS -> {
                when (selectedItemPosition) {
                    0 -> {
                        model = "WPCGEFS"
                        labels = UtilityModelWpcGefsInterface.labels
                        params = UtilityModelWpcGefsInterface.params
                        sectors = UtilityModelWpcGefsInterface.sectors
                        startStep = 0
                        endStep = 241
                        stepAmount = 6
                        numberRuns = 0
                        format = "%03d"
                        timeTruncate = 3
                    }
                }
            }
            ModelType.NSSL -> {
                truncateTime = false
                when (selectedItemPosition) {
                    0 -> {
                        model = "WRF"
                        params = UtilityModelNsslWrfInterface.paramsNsslWrf
                        labels = UtilityModelNsslWrfInterface.labelsNsslWrf
                        sectors = UtilityModelNsslWrfInterface.sectorsLong
                        startStep = 1
                        endStep = 36
                        timeTruncate = 3
                    }
                    1 -> {
                        model = "FV3"
                        params = UtilityModelNsslWrfInterface.paramsNsslFv3
                        labels = UtilityModelNsslWrfInterface.labelsNsslFv3
                        sectors = UtilityModelNsslWrfInterface.sectorsLong
                        startStep = 1
                        endStep = 60
                        timeTruncate = 3
                    }
                    2 -> {
                        model = "HRRRV3"
                        params = UtilityModelNsslWrfInterface.paramsNsslHrrrv3
                        labels = UtilityModelNsslWrfInterface.labelsNsslHrrrv3
                        sectors = UtilityModelNsslWrfInterface.sectorsLong
                        startStep = 1
                        endStep = 36
                        timeTruncate = 3
                    }
                    3 -> {
                        model = "WRF_3KM"
                        params = UtilityModelNsslWrfInterface.paramsNsslWrf
                        labels = UtilityModelNsslWrfInterface.labelsNsslWrf
                        sectors = UtilityModelNsslWrfInterface.sectorsLong
                        startStep = 1
                        endStep = 36
                        timeTruncate = 3
                    }
                }
            }
            ModelType.ESRL -> {
                when (selectedItemPosition) {
                    1 -> {
                        model = "RAP"
                        params = UtilityModelEsrlInterface.paramsRap
                        labels = UtilityModelEsrlInterface.labelsRap
                        sectors = UtilityModelEsrlInterface.sectorsRap
                        startStep = 0
                        endStep = 39
                        stepAmount = 1
                        format = "%03d"
                        timeTruncate = 3
                    }
                    2 -> {
                        model = "RAP_NCEP"
                        params = UtilityModelEsrlInterface.paramsRap
                        labels = UtilityModelEsrlInterface.labelsRap
                        sectors = UtilityModelEsrlInterface.sectorsRap
                        startStep = 0
                        endStep = 39
                        stepAmount = 1
                        format = "%03d"
                        timeTruncate = 3
                    }
                    0 -> {
                        model = "HRRR_NCEP"
                        params = UtilityModelEsrlInterface.paramsHrrr
                        labels = UtilityModelEsrlInterface.labelsHrrr
                        sectors = UtilityModelEsrlInterface.sectorsHrrr
                        startStep = 0
                        endStep = 36
                        stepAmount = 1
                        format = "%03d"
                        timeTruncate = 3
                    }
                }
            }
            else -> {}
        }
    }

    fun getTimeLabel() = if (timeIndex > -1 && timeIndex < times.size) {
            times[timeIndex]
        } else {
            ""
        }

    fun setTimeIdx(timeIdx: Int) {
        if (timeIdx > -1 && timeIdx < times.size) {
            this.timeIndex = timeIdx
            this.time = this.times[timeIdx]
        }
    }

    private fun timeIdxIncr() {
        this.timeIndex += 1
        this.time = this.times.safeGet(timeIndex)
    }

    private fun timeIdxDecr() {
        this.timeIndex -= 1
        this.time = this.times.safeGet(timeIndex)
    }

    fun leftClick() {
        if (timeIndex == 0) {
            setTimeIdx(times.size - 1)
        } else {
            timeIdxDecr()
        }
        getContent()
    }

    fun rightClick() {
        if (timeIndex == times.size - 1) {
            setTimeIdx(0)
        } else {
            timeIdxIncr()
        }
        getContent()
    }
}


