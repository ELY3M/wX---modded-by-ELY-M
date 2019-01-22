/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019  joshua.tee@gmail.com

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
import joshuatee.wx.ui.ObjectFab
import joshuatee.wx.ui.ObjectSpinner

class ObjectModel(val context: Context, var prefModel: String, numPanesStr: String) {

    var run: String = "00Z"
    var time: String = "00"
    var sector: String = ""
    var currentParam: String = ""
    var numPanes: Int = 1
    var model: String = "WRF"
    var sectorInt: Int = 0
    var curImg: Int = 0
    var modelType: ModelType = ModelType.NSSL
    var startStep: Int = 0
    var endStep: Int = 0
    var stepAmount: Int = 1
    var numberRuns: Int = 4
    var timeTruncate: Int = 2
    var format: String = "%03d"
    var truncateTime: Boolean = true
    var prefSector: String = "MODEL_" + prefModel + numPanesStr + "_SECTOR_LAST_USED"
    var prefParam: String = "MODEL_" + prefModel + numPanesStr + "_PARAM_LAST_USED"
    var prefParamLabel: String = "MODEL_" + prefModel + numPanesStr + "_PARAM_LAST_USED_LABEL"
    var prefRunPosn: String = "MODEL_" + prefModel + numPanesStr + "_RUN_POSN"
    var modelProvider: String = "MODEL_$prefModel$numPanesStr"
    private var prefModelIndex: String = "MODEL_$prefModel${numPanesStr}_INDEX"
    private var modelIndex = 0
    var rtd: RunTimeData = RunTimeData()
    lateinit var displayData: DisplayData
    var sectors: List<String> = listOf("")
    var labels: List<String> = listOf("")
    var params: List<String> = listOf("")
    var models: List<String> = listOf("")
    private var defaultModel: String = ""
    var spinnerTimeValue: Int = 0
    lateinit var spTime: ObjectSpinner
    var animRan: Boolean = false
    var firstRun: Boolean = false
    var imageLoaded: Boolean = false

    lateinit var miStatusParam1: MenuItem
    lateinit var miStatusParam2: MenuItem
    var fab1: ObjectFab? = null
    var fab2: ObjectFab? = null
    lateinit var toolbar: Toolbar
    lateinit var spRun: ObjectSpinner
    lateinit var spSector: ObjectSpinner

    fun setUIElements(
        toolbar: Toolbar,
        fab1: ObjectFab?,
        fab2: ObjectFab?,
        miStatusParam1: MenuItem,
        miStatusParam2: MenuItem,
        spRun: ObjectSpinner,
        spSector: ObjectSpinner
    ) {
        this.miStatusParam1 = miStatusParam1
        this.miStatusParam2 = miStatusParam2
        this.fab1 = fab1
        this.fab2 = fab2
        this.toolbar = toolbar
        this.spRun = spRun
        this.spSector = spSector
    }

    init {
        numPanes = numPanesStr.toIntOrNull() ?: 0
        when (prefModel) {
            "WPCGEFS" -> {
                modelType = ModelType.WPCGEFS
                defaultModel = "WPCGEFS"
                models = UtilityModelWPCGEFSInterface.models
            }
            "NSSL" -> {
                modelType = ModelType.NSSL
                models = UtilityModelNSSLWRFInterface.models
                defaultModel = "WRF"
            }
            "ESRL" -> {
                modelType = ModelType.ESRL
                models = UtilityModelESRLInterface.models
            }
            "NCEP" -> {
                modelType = ModelType.NCEP
                models = UtilityModelNCEPInterface.models
                defaultModel = "GFS"
            }
            "GLCFS" -> {
                modelType = ModelType.GLCFS
                models = UtilityModelGLCFSInterface.models
                defaultModel = "GLCFS"
            }
            "SPCSREF" -> {
                modelType = ModelType.SPCSREF
                models = UtilityModelsSPCSREFInterface.models
                defaultModel = "SREF"
                timeTruncate = 4
                startStep = 0
                endStep = 88
                stepAmount = 3
            }
            "SPCHREF" -> {
                modelType = ModelType.SPCHREF
                models = UtilityModelSPCHREFInterface.models
                defaultModel = "HREF"
                timeTruncate = 2
            }
            "SPCHRRR" -> {
                modelType = ModelType.SPCHRRR
                models = UtilityModelSPCHRRRInterface.models
                params = UtilityModelSPCHRRRInterface.params
                labels = UtilityModelSPCHRRRInterface.labels
                defaultModel = "HRRR"
                timeTruncate = 2
                startStep = 2
                endStep = 16
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
            // FIXME remove time as it's part of om
            ModelType.WPCGEFS -> UtilityModelWPCGEFSInputOutput.getImage(this, time)
            ModelType.ESRL -> UtilityModelESRLInputOutput.getImage(this, time)
            ModelType.NSSL -> UtilityModelNSSLWRFInputOutput.getImage(context, this, time)
            ModelType.GLCFS -> UtilityModelGLCFSInputOutput.getImage(this, time)
            ModelType.NCEP -> UtilityModelNCEPInputOutput.getImage(this, time)
            ModelType.SPCSREF -> UtilityModelsSPCSREFInputOutput.getImage(context, this, time)
            ModelType.SPCHREF -> UtilityModelSPCHREFInputOutput.getImage(context, this, time)
            ModelType.SPCHRRR -> UtilityModelSPCHRRRInputOutput.getImage(
                context,
                this,
                time,
                overlayImg
            )
        }
    }

    fun getAnimate(index: Int, overlayImg: List<String>): AnimationDrawable {
        currentParam = displayData.param[index]
        spinnerTimeValue = spTime.selectedItemPosition
        return when (modelType) {
            ModelType.WPCGEFS -> UtilityModelWPCGEFSInputOutput.getAnimation(context, this)
            ModelType.ESRL -> UtilityModelESRLInputOutput.getAnimation(context, this)
            ModelType.NSSL -> UtilityModelNSSLWRFInputOutput.getAnimation(context, this)
            ModelType.GLCFS -> UtilityModelGLCFSInputOutput.getAnimation(context, this)
            ModelType.NCEP -> UtilityModelNCEPInputOutput.getAnimation(context, this)
            ModelType.SPCSREF -> UtilityModelsSPCSREFInputOutput.getAnimation(context, this)
            ModelType.SPCHREF -> UtilityModelSPCHREFInputOutput.getAnimation(context, this)
            ModelType.SPCHRRR -> UtilityModelSPCHRRRInputOutput.getAnimation(
                context,
                this,
                overlayImg
            )
        }
    }

    fun getRunTime(): RunTimeData {
        return when (modelType) {
            ModelType.WPCGEFS -> UtilityModelWPCGEFSInputOutput.runTime
            ModelType.ESRL -> UtilityModelESRLInputOutput.getRunTime(model, displayData.param[0])
            ModelType.NSSL -> UtilityModelNSSLWRFInputOutput.runTime
            ModelType.NCEP -> UtilityModelNCEPInputOutput.getRunTime(
                model,
                displayData.param[0],
                sector
            )
            ModelType.SPCSREF -> UtilityModelsSPCSREFInputOutput.runTime
            ModelType.SPCHREF -> UtilityModelSPCHREFInputOutput.runTime
            ModelType.SPCHRRR -> UtilityModelSPCHRRRInputOutput.runTime
            else -> RunTimeData()
        }
    }

    fun setParams(selectedItemPosition: Int) {
        modelIndex = selectedItemPosition
        Utility.writePref(context, prefModelIndex, modelIndex)
        when (modelType) {
            ModelType.SPCHRRR -> {
                when (selectedItemPosition) {
                    1 -> {
                        model = "HRRR"
                        params = UtilityModelSPCHRRRInterface.models
                        labels = UtilityModelSPCHRRRInterface.labels
                        sectors = UtilityModelSPCHRRRInterface.sectors
                        startStep = 2
                        endStep = 16
                        stepAmount = 1
                        numberRuns = 4
                    }
                }
            }
            ModelType.NCEP -> {
                timeTruncate = 3
                when (selectedItemPosition) {
                    1 -> {
                        model = "GFS"
                        params = UtilityModelNCEPInterface.paramsGfs
                        labels = UtilityModelNCEPInterface.labelsGfs
                        sectors = UtilityModelNCEPInterface.sectorsGfs
                        startStep = 0
                        endStep = 85
                        stepAmount = 3
                        numberRuns = 4
                    }
                    2 -> {
                        model = "NAM"
                        params = UtilityModelNCEPInterface.paramsNam
                        labels = UtilityModelNCEPInterface.labelsNam
                        sectors = UtilityModelNCEPInterface.sectorsNam
                        startStep = 0
                        endStep = 85
                        stepAmount = 3
                        numberRuns = 4
                    }
                    4 -> {
                        model = "RAP"
                        params = UtilityModelNCEPInterface.paramsRap
                        labels = UtilityModelNCEPInterface.labelsRap
                        sectors = UtilityModelNCEPInterface.sectorsRap
                        startStep = 0
                        endStep = 39
                        stepAmount = 1
                        numberRuns = 24
                    }
                    0 -> {
                        model = "HRRR"
                        params = UtilityModelNCEPInterface.paramsHrrr
                        labels = UtilityModelNCEPInterface.labelsHrrr
                        sectors = UtilityModelNCEPInterface.sectorsHrrr
                        startStep = 0
                        endStep = 18
                        stepAmount = 1
                        numberRuns = 24
                    }
                    3 -> {
                        model = "NAM-HIRES"
                        params = UtilityModelNCEPInterface.paramsNamHires
                        labels = UtilityModelNCEPInterface.labelsNamHires
                        sectors = UtilityModelNCEPInterface.sectorsNamHires
                        startStep = 0
                        endStep = 60
                        stepAmount = 1
                        numberRuns = 4
                    }
                    5 -> {
                        model = "HRW-NMMB"
                        params = UtilityModelNCEPInterface.paramsHrwNmm
                        labels = UtilityModelNCEPInterface.labelsHrwNmm
                        sectors = UtilityModelNCEPInterface.sectorsHrwNmm
                        startStep = 0
                        endStep = 49
                        stepAmount = 1
                        numberRuns = 2
                    }
                    6 -> {
                        model = "HRW-ARW"
                        params = UtilityModelNCEPInterface.paramsHrwNmm
                        labels = UtilityModelNCEPInterface.labelsHrwNmm
                        sectors = UtilityModelNCEPInterface.sectorsHrwNmm
                        startStep = 0
                        endStep = 49
                        stepAmount = 1
                        numberRuns = 2
                    }
                    7 -> {
                        model = "GEFS-SPAG"
                        params = UtilityModelNCEPInterface.paramsGefsSpag
                        labels = UtilityModelNCEPInterface.labelsGefsSpag
                        sectors = UtilityModelNCEPInterface.sectorsGefsSpag
                        startStep = 0
                        endStep = 385
                        stepAmount = 6
                        numberRuns = 4
                    }
                    8 -> {
                        model = "GEFS-MEAN-SPRD"
                        params = UtilityModelNCEPInterface.paramsGefsMnsprd
                        labels = UtilityModelNCEPInterface.labelsGefsMnsprd
                        sectors = UtilityModelNCEPInterface.sectorsGefsMnsprd
                        startStep = 0
                        endStep = 385
                        stepAmount = 6
                        numberRuns = 4
                    }
                    9 -> {
                        model = "SREF"
                        params = UtilityModelNCEPInterface.paramsSref
                        labels = UtilityModelNCEPInterface.labelsSref
                        sectors = UtilityModelNCEPInterface.sectorsSref
                        startStep = 0
                        endStep = 88
                        stepAmount = 3
                        numberRuns = 5
                    }
                    10 -> {
                        model = "NAEFS"
                        params = UtilityModelNCEPInterface.paramsNaefs
                        labels = UtilityModelNCEPInterface.labelsNaefs
                        sectors = UtilityModelNCEPInterface.sectorsNaefs
                        startStep = 6
                        endStep = 385
                        stepAmount = 6
                        numberRuns = 4
                    }
                    11 -> {
                        model = "POLAR"
                        params = UtilityModelNCEPInterface.paramsPolar
                        labels = UtilityModelNCEPInterface.labelsPolar
                        sectors = UtilityModelNCEPInterface.sectorsPolar
                        startStep = 24
                        endStep = 385
                        stepAmount = 24
                        numberRuns = 1
                    }
                    12 -> {
                        model = "WW3"
                        params = UtilityModelNCEPInterface.paramsWw3
                        labels = UtilityModelNCEPInterface.labelsWw3
                        sectors = UtilityModelNCEPInterface.sectorsWw3
                        startStep = 0
                        endStep = 127
                        stepAmount = 6
                        numberRuns = 4
                    }
                    13 -> {
                        model = "WW3-ENP"
                        params = UtilityModelNCEPInterface.paramsWw3Enp
                        labels = UtilityModelNCEPInterface.labelsWw3Enp
                        sectors = UtilityModelNCEPInterface.sectorsWw3Enp
                        startStep = 0
                        endStep = 127
                        stepAmount = 6
                        numberRuns = 4
                    }
                    14 -> {
                        model = "WW3-WNA"
                        params = UtilityModelNCEPInterface.paramsWw3Wna
                        labels = UtilityModelNCEPInterface.labelsWw3Wna
                        sectors = UtilityModelNCEPInterface.sectorsWw3Wna
                        startStep = 0
                        endStep = 127
                        stepAmount = 6
                        numberRuns = 4
                    }
                    15 -> {
                        model = "ESTOFS"
                        params = UtilityModelNCEPInterface.paramsEstofs
                        labels = UtilityModelNCEPInterface.labelsEstofs
                        sectors = UtilityModelNCEPInterface.sectorsEstofs
                        startStep = 0
                        endStep = 181
                        stepAmount = 1
                        numberRuns = 4
                    }
                    16 -> {
                        model = "FIREWX"
                        params = UtilityModelNCEPInterface.paramsFirefx
                        labels = UtilityModelNCEPInterface.labelsFirefx
                        sectors = UtilityModelNCEPInterface.sectorsFirewx
                        startStep = 0
                        endStep = 37
                        stepAmount = 1
                        numberRuns = 4
                    }

                }
            }
            ModelType.GLCFS -> {
                when (selectedItemPosition) {
                    0 -> {
                        model = "GLCFS"
                        labels = UtilityModelGLCFSInterface.labels
                        params = UtilityModelGLCFSInterface.params
                        sectors = UtilityModelGLCFSInterface.sectors
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
                        labels = UtilityModelWPCGEFSInterface.labels
                        params = UtilityModelWPCGEFSInterface.params
                        sectors = UtilityModelWPCGEFSInterface.sectors
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
                        params = UtilityModelNSSLWRFInterface.paramsNsslWrf
                        labels = UtilityModelNSSLWRFInterface.labelsNsslWrf
                        sectors = UtilityModelNSSLWRFInterface.sectorsLong
                        startStep = 1
                        endStep = 36
                        timeTruncate = 3
                    }
                    1 -> {
                        model = "FV3"
                        params = UtilityModelNSSLWRFInterface.paramsNsslFv3
                        labels = UtilityModelNSSLWRFInterface.labelsNsslFv3
                        sectors = UtilityModelNSSLWRFInterface.sectorsLong
                        startStep = 1
                        endStep = 60
                        timeTruncate = 3
                    }
                    2 -> {
                        model = "HRRRV3"
                        params = UtilityModelNSSLWRFInterface.paramsNsslHrrrv3
                        labels = UtilityModelNSSLWRFInterface.labelsNsslHrrrv3
                        sectors = UtilityModelNSSLWRFInterface.sectorsLong
                        startStep = 1
                        endStep = 36
                        timeTruncate = 3
                    }
                    3 -> {
                        model = "WRF_3KM"
                        model = "HRRRV3"
                        params = UtilityModelNSSLWRFInterface.paramsNsslWrf
                        labels = UtilityModelNSSLWRFInterface.labelsNsslWrf
                        sectors = UtilityModelNSSLWRFInterface.sectorsLong
                        startStep = 1
                        endStep = 36
                        timeTruncate = 3
                    }
                }
            }
            ModelType.ESRL -> {
                when (selectedItemPosition) {
                    3 -> {
                        model = "RAP"
                        params = UtilityModelESRLInterface.paramsRap
                        labels = UtilityModelESRLInterface.labelsRap
                        sectors = UtilityModelESRLInterface.sectorsRap
                        startStep = 0
                        endStep = 39
                        stepAmount = 1
                        format = "%02d"
                        timeTruncate = 2
                    }
                    4 -> {
                        model = "RAP_NCEP"
                        params = UtilityModelESRLInterface.paramsRap
                        labels = UtilityModelESRLInterface.labelsRap
                        sectors = UtilityModelESRLInterface.sectorsRap
                        startStep = 0
                        endStep = 39
                        stepAmount = 1
                        format = "%02d"
                        timeTruncate = 2
                    }
                    0 -> {
                        model = "HRRR"
                        params = UtilityModelESRLInterface.paramsHrrr
                        labels = UtilityModelESRLInterface.labelsHrrr
                        sectors = UtilityModelESRLInterface.sectorsHrrr
                        startStep = 0
                        endStep = 36
                        stepAmount = 1
                        format = "%02d"
                        timeTruncate = 2
                    }
                    1 -> {
                        model = "HRRR_AK"
                        params = UtilityModelESRLInterface.paramsHrrr
                        labels = UtilityModelESRLInterface.labelsHrrr
                        sectors = UtilityModelESRLInterface.sectorsHrrrAk
                        startStep = 0
                        endStep = 36
                        stepAmount = 1
                        format = "%02d"
                        timeTruncate = 2
                    }
                    2 -> {
                        model = "HRRR_NCEP"
                        params = UtilityModelESRLInterface.paramsHrrr
                        labels = UtilityModelESRLInterface.labelsHrrr
                        sectors = UtilityModelESRLInterface.sectorsHrrr
                        startStep = 0
                        endStep = 36
                        stepAmount = 1
                        format = "%02d"
                        timeTruncate = 2
                    }
                }
            }
            else -> {
            }
        }
    }
}


