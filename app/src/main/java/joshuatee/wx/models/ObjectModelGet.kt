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

package joshuatee.wx.models

import android.graphics.Bitmap
import android.graphics.drawable.AnimationDrawable

object ObjectModelGet {

    fun image(objectModel: ObjectModel, index: Int, overlayImg: List<String>): Bitmap {
        objectModel.currentParam = objectModel.displayData.param[index]
        return when (objectModel.modelType) {
            ModelType.WPCGEFS -> UtilityModelWpcGefsInputOutput.getImage(objectModel.activity, objectModel, objectModel.time)
            ModelType.ESRL -> UtilityModelEsrlInputOutput.getImage(objectModel.activity, objectModel, objectModel.time)
            ModelType.NSSL -> UtilityModelNsslWrfInputOutput.getImage(objectModel.activity, objectModel, objectModel.time)
            ModelType.NCEP -> UtilityModelNcepInputOutput.getImage(objectModel.activity, objectModel, objectModel.time)
            ModelType.SPCSREF -> UtilityModelSpcSrefInputOutput.getImage(objectModel.activity, objectModel, objectModel.time)
            ModelType.SPCHREF -> UtilityModelSpcHrefInputOutput.getImage(objectModel.activity, objectModel, objectModel.time)
            ModelType.SPCHRRR -> UtilityModelSpcHrrrInputOutput.getImage(objectModel.activity, objectModel, objectModel.time, overlayImg)
        }
    }

    fun animate(objectModel: ObjectModel, index: Int, overlayImg: List<String>): AnimationDrawable {
        objectModel.currentParam = objectModel.displayData.param[index]
        return when (objectModel.modelType) {
            ModelType.WPCGEFS -> UtilityModels.getAnimation(objectModel.activity, objectModel, UtilityModelWpcGefsInputOutput::getImage)
            ModelType.ESRL -> UtilityModels.getAnimation(objectModel.activity, objectModel, UtilityModelEsrlInputOutput::getImage)
            ModelType.NSSL -> UtilityModels.getAnimation(objectModel.activity, objectModel, UtilityModelNsslWrfInputOutput::getImage)
            ModelType.NCEP -> UtilityModels.getAnimation(objectModel.activity, objectModel, UtilityModelNcepInputOutput::getImage)
            ModelType.SPCSREF -> UtilityModels.getAnimation(objectModel.activity, objectModel, UtilityModelSpcSrefInputOutput::getImage)
            ModelType.SPCHREF -> UtilityModels.getAnimation(objectModel.activity, objectModel, UtilityModelSpcHrefInputOutput::getImage)
            ModelType.SPCHRRR -> UtilityModelSpcHrrrInputOutput.getAnimation(objectModel.activity, objectModel, overlayImg)
        }
    }

    fun runTime(objectModel: ObjectModel) = when (objectModel.modelType) {
        ModelType.WPCGEFS -> UtilityModelWpcGefsInputOutput.runTime
        ModelType.ESRL -> UtilityModelEsrlInputOutput.getRunTime(objectModel.model, objectModel.displayData.param[0])
        ModelType.NSSL -> UtilityModelNsslWrfInputOutput.runTime(objectModel)
        ModelType.NCEP -> UtilityModelNcepInputOutput.getRunTime(objectModel.model, objectModel.displayData.param[0], objectModel.sector)
        ModelType.SPCSREF -> UtilityModelSpcSrefInputOutput.runTime
        ModelType.SPCHREF -> UtilityModelSpcHrefInputOutput.runTime
        ModelType.SPCHRRR -> UtilityModelSpcHrrrInputOutput.runTime
    }
}
