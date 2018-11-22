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

package joshuatee.wx.models

import joshuatee.wx.util.Utility
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.AnimationDrawable
import joshuatee.wx.objects.ModelType
import joshuatee.wx.util.UtilityImg

class ObjectModel(val context: Context, var prefModel: String, var numPanesStr: String) {

    var run: String = "00Z"
    var time: String = "00"
    var sector: String = ""
    var numPanes: Int = 1
    //var numPanesStr: String = "1"
    var model: String = "WRF"
    var sectorInt: Int = 0
    var sectorOrig: String = ""
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
    var rtd: RunTimeData = RunTimeData()
    lateinit var displayData: DisplayData
    var sectors: List<String> = listOf("")
    var labels: List<String> = listOf("")
    var params: List<String> = listOf("")
    var models: List<String> = listOf("")
    private var defaultModel: String = ""

    init {
        numPanes = numPanesStr.toIntOrNull() ?: 0
        when (prefModel) {
            "WPCGEFS" -> {
                modelType = ModelType.WPCGEFS
                defaultModel = "WPCGEFS"
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
                models = UtilityModelNCEPInterface.MODELS_ARR
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
            }
            "SPCHREF" -> {
                modelType = ModelType.SPCHREF
                models = UtilityModelSPCHREFInterface.models
                defaultModel = "HREF"
                timeTruncate = 4
            }
            "SPCHRRR" -> {
                modelType = ModelType.SPCHRRR
                models = UtilityModelSPCHRRRInterface.models
                defaultModel = "HRRR"
                timeTruncate = 2
            }
        }

        //prefModel += numPanesStr
        model = Utility.readPref(context, prefModel, defaultModel)
        prefSector = "MODEL_" + prefModel + numPanesStr + "_SECTOR_LAST_USED"
        prefParam = "MODEL_" + prefModel + numPanesStr + "_PARAM_LAST_USED"
        prefParamLabel = "MODEL_" + prefModel + numPanesStr + "_PARAM_LAST_USED_LABEL"
        prefRunPosn = "MODEL_" + prefModel + numPanesStr + "_RUN_POSN"
        modelProvider = "MODEL_$prefModel"

        //sectors = UtilityModelWPCGEFSInterface.sectors
        //labels = UtilityModelWPCGEFSInterface.LABELS
        //params = UtilityModelWPCGEFSInterface.PARAMS
        //models = UtilityModelWPCGEFSInterface.models
    }

    fun getImage(index: Int): Bitmap {
        return when (modelType) {
            // FIXME remove params already part of ObjectModel and update aux methods to use object
            ModelType.WPCGEFS -> UtilityModelWPCGEFSInputOutput.getImage(sector, displayData.param[index], run, time)
            ModelType.ESRL -> UtilityModelESRLInputOutput.getImage(model, sectorOrig, sectorInt, displayData.param[index], run, time)
            ModelType.NSSL -> UtilityModelNSSLWRFInputOutput.getImage(context, model, sector, displayData.param[index], run, time)
            ModelType.GLCFS -> UtilityModelGLCFSInputOutput.getImage(sector, displayData.param[index], time)
            ModelType.NCEP -> UtilityModelNCEPInputOutput.getImage(model, sector, displayData.param[index], run, time)
            ModelType.SPCSREF -> UtilityModelsSPCSREFInputOutput.getImage(context, displayData.param[index], run, time)
            ModelType.SPCHREF -> UtilityModelSPCHREFInputOutput.getImage(context, sector, run, time, displayData.param[index])
            else -> UtilityImg.getBlankBitmap()
        }
    }

    fun getAnimate(index: Int, spinnerTimeValue: Int, timeList: List<String>): AnimationDrawable {
        return when (modelType) {
            ModelType.WPCGEFS -> UtilityModelWPCGEFSInputOutput.getAnimation(context, sector, displayData.param[index], run, spinnerTimeValue, timeList)
            ModelType.ESRL -> UtilityModelESRLInputOutput.getAnimation(context, model, sectorOrig, sectorInt, displayData.param[index], run, spinnerTimeValue, timeList)
            ModelType.NSSL -> UtilityModelNSSLWRFInputOutput.getAnimation(context, model, sector, displayData.param[index], run, spinnerTimeValue, timeList)
            ModelType.GLCFS -> UtilityModelGLCFSInputOutput.getAnimation(context, sector, displayData.param[index], spinnerTimeValue, timeList)
            ModelType.NCEP -> UtilityModelNCEPInputOutput.getAnimation(context, model, sector, displayData.param[index], run, spinnerTimeValue, timeList)
            ModelType.SPCSREF -> UtilityModelsSPCSREFInputOutput.getAnimation(context, displayData.param[index], run, spinnerTimeValue, timeList)
            ModelType.SPCHREF -> UtilityModelSPCHREFInputOutput.getAnimation(context, sector, run, spinnerTimeValue, timeList, displayData.param[index])
            else -> AnimationDrawable()
        }
    }

    fun getRunTime(): RunTimeData {
        return when (modelType) {
            ModelType.WPCGEFS -> UtilityModelWPCGEFSInputOutput.runTime
            ModelType.ESRL -> UtilityModelESRLInputOutput.getRunTime(model)
            ModelType.NSSL -> UtilityModelNSSLWRFInputOutput.runTime
            ModelType.NCEP -> UtilityModelNCEPInputOutput.getRunTime(model, displayData.param[0], sector)
            ModelType.SPCSREF -> UtilityModelsSPCSREFInputOutput.runTime
            ModelType.SPCHREF -> UtilityModelSPCHREFInputOutput.runTime
            else -> RunTimeData()
        }
    }

    fun setParams(selectedItemPosition: Int) {
        when (modelType) {
            ModelType.NCEP -> {
                timeTruncate = 3
                when (selectedItemPosition) {
                    1 -> {
                        model = "GFS"
                        params = UtilityModelNCEPInterface.MODEL_GFS_PARAMS
                        labels = UtilityModelNCEPInterface.MODEL_GFS_PARAMS_LABELS
                        sectors = UtilityModelNCEPInterface.LIST_SECTOR_ARR_GFS
                        startStep = 0
                        endStep = 85
                        stepAmount = 3
                        numberRuns = 4
                    }
                    2 -> {
                        model = "NAM"
                        params = UtilityModelNCEPInterface.MODEL_NAM_PARAMS
                        labels = UtilityModelNCEPInterface.MODEL_NAM_PARAMS_LABELS
                        sectors = UtilityModelNCEPInterface.LIST_SECTOR_ARR_NAM
                        startStep = 0
                        endStep = 85
                        stepAmount = 3
                        numberRuns = 4
                    }
                    4 -> {
                        model = "RAP"
                        params = UtilityModelNCEPInterface.MODEL_RAP_PARAMS
                        labels = UtilityModelNCEPInterface.MODEL_RAP_PARAMS_LABELS
                        sectors = UtilityModelNCEPInterface.LIST_SECTOR_ARR_RAP
                        startStep = 0
                        endStep = 39
                        stepAmount = 1
                        numberRuns = 24
                    }
                    0 -> {
                        model = "HRRR"
                        params = UtilityModelNCEPInterface.MODEL_HRRR_PARAMS
                        labels = UtilityModelNCEPInterface.MODEL_HRRR_PARAMS_LABELS
                        sectors = UtilityModelNCEPInterface.LIST_SECTOR_ARR_HRRR
                        startStep = 0
                        endStep = 18
                        stepAmount = 1
                        numberRuns = 24
                    }
                    3 -> {
                        model = "NAM-HIRES"
                        params = UtilityModelNCEPInterface.MODEL_NAM_4_KM_PARAMS
                        labels = UtilityModelNCEPInterface.MODEL_NAM_4_KM_PARAMS_LABELS
                        sectors = UtilityModelNCEPInterface.LIST_SECTOR_ARR_NAM_4_KM
                        startStep = 0
                        endStep = 60
                        stepAmount = 1
                        numberRuns = 4
                    }
                    5 -> {
                        model = "HRW-NMMB"
                        params = UtilityModelNCEPInterface.MODEL_HRW_NMM_PARAMS
                        labels = UtilityModelNCEPInterface.MODEL_HRW_NMM_PARAMS_LABELS
                        sectors = UtilityModelNCEPInterface.LIST_SECTOR_ARR_HRW_NMM
                        startStep = 0
                        endStep = 49
                        stepAmount = 1
                        numberRuns = 2
                    }
                    6 -> {
                        model = "HRW-ARW"
                        params = UtilityModelNCEPInterface.MODEL_HRW_NMM_PARAMS
                        labels = UtilityModelNCEPInterface.MODEL_HRW_NMM_PARAMS_LABELS
                        sectors = UtilityModelNCEPInterface.LIST_SECTOR_ARR_HRW_NMM
                        startStep = 0
                        endStep = 49
                        stepAmount = 1
                        numberRuns = 2
                    }
                    7 -> {
                        model = "GEFS-SPAG"
                        params = UtilityModelNCEPInterface.MODEL_GEFS_SPAG_PARAMS
                        labels = UtilityModelNCEPInterface.MODEL_GEFS_SPAG_PARAMS_LABELS
                        sectors = UtilityModelNCEPInterface.LIST_SECTOR_ARR_GEFS_SPAG
                        startStep = 0
                        endStep = 385
                        stepAmount = 6
                        numberRuns = 4
                    }
                    8 -> {
                        model = "GEFS-MEAN-SPRD"
                        params = UtilityModelNCEPInterface.MODEL_GEFS_MNSPRD_PARAMS
                        labels = UtilityModelNCEPInterface.MODEL_GEFS_MNSPRD_PARAMS_LABELS
                        sectors = UtilityModelNCEPInterface.LIST_SECTOR_ARR_GEFS_MNSPRD
                        startStep = 0
                        endStep = 385
                        stepAmount = 6
                        numberRuns = 4
                    }
                    9 -> {
                        model = "SREF"
                        params = UtilityModelNCEPInterface.MODEL_SREF_PARAMS
                        labels = UtilityModelNCEPInterface.MODEL_SREF_PARAMS_LABELS
                        sectors = UtilityModelNCEPInterface.LIST_SECTOR_ARR_SREF
                        startStep = 0
                        endStep = 88
                        stepAmount = 3
                        numberRuns = 5
                    }
                    10 -> {
                        model = "NAEFS"
                        params = UtilityModelNCEPInterface.MODEL_NAEFS_PARAMS
                        labels = UtilityModelNCEPInterface.MODEL_NAEFS_PARAMS_LABELS
                        sectors = UtilityModelNCEPInterface.LIST_SECTOR_ARR_NAEFS
                        startStep = 6
                        endStep = 385
                        stepAmount = 6
                        numberRuns = 4
                    }
                    11 -> {
                        model = "POLAR"
                        params = UtilityModelNCEPInterface.MODEL_POLAR_PARAMS
                        labels = UtilityModelNCEPInterface.MODEL_POLAR_PARAMS_LABELS
                        sectors = UtilityModelNCEPInterface.LIST_SECTOR_ARR_POLAR
                        startStep = 24
                        endStep = 385
                        stepAmount = 24
                        numberRuns = 1
                    }
                    12 -> {
                        model = "WW3"
                        params = UtilityModelNCEPInterface.MODEL_WW_3_PARAMS
                        labels = UtilityModelNCEPInterface.MODEL_WW_3_PARAMS_LABELS
                        sectors = UtilityModelNCEPInterface.LIST_SECTOR_ARR_WW_3
                        startStep = 0
                        endStep = 127
                        stepAmount = 6
                        numberRuns = 4
                    }
                    13 -> {
                        model = "WW3-ENP"
                        params = UtilityModelNCEPInterface.MODEL_WW_3_ENP_PARAMS
                        labels = UtilityModelNCEPInterface.MODEL_WW_3_ENP_PARAMS_LABELS
                        sectors = UtilityModelNCEPInterface.LIST_SECTOR_ARR_WW_3_ENP
                        startStep = 0
                        endStep = 127
                        stepAmount = 6
                        numberRuns = 4
                    }
                    14 -> {
                        model = "WW3-WNA"
                        params = UtilityModelNCEPInterface.MODEL_WW_3_WNA_PARAMS
                        labels = UtilityModelNCEPInterface.MODEL_WW_3_WNA_PARAMS_LABELS
                        sectors = UtilityModelNCEPInterface.LIST_SECTOR_ARR_WW_3_WNA
                        startStep = 0
                        endStep = 127
                        stepAmount = 6
                        numberRuns = 4
                    }
                    15 -> {
                        model = "ESTOFS"
                        params = UtilityModelNCEPInterface.MODEL_ESTOFS_PARAMS
                        labels = UtilityModelNCEPInterface.MODEL_ESTOFS_PARAMS_LABELS
                        sectors = UtilityModelNCEPInterface.LIST_SECTOR_ARR_ESTOFS
                        startStep = 0
                        endStep = 181
                        stepAmount = 1
                        numberRuns = 4
                    }
                    16 -> {
                        model = "FIREWX"
                        params = UtilityModelNCEPInterface.MODEL_FIREWX_PARAMS
                        labels = UtilityModelNCEPInterface.MODEL_FIREWX_PARAMS_LABELS
                        sectors = UtilityModelNCEPInterface.LIST_SECTOR_ARR_FIREWX
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
                        labels = UtilityModelWPCGEFSInterface.LABELS
                        params = UtilityModelWPCGEFSInterface.PARAMS
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
        }

    }
}


