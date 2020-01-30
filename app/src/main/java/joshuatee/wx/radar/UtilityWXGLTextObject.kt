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
//modded by ELY M.

package joshuatee.wx.radar

object UtilityWXGLTextObject {

    // FIXME naming and use more
    fun hideTV(numPanes: Int, textObjects: MutableList<WXGLTextObject>): Unit =
        (0 until numPanes).forEach { textObjects[it].hideTextLabels() }

    fun showTV(numPanes: Int, textObjects: MutableList<WXGLTextObject>): Unit =
        (0 until numPanes).forEach { textObjects[it].addTextLabels() }

    fun updateSpotterLabels(numPanes: Int, textObjects: MutableList<WXGLTextObject>) {
        (0 until numPanes).forEach {
	    ///textObjects[it].initTVSpottersLabels()  //is this needed????  test for bugs
            textObjects[it].addTextLabelsSpottersLabels()
        }
    }

    fun updateHailLabels(numPanes: Int, textObjects: MutableList<WXGLTextObject>) {
        (0 until numPanes).forEach {
            textObjects[it].initTVHailLabels()
            textObjects[it].addTVHailLabels()
        }
    }
    fun updateObs(numPanes: Int, textObjects: MutableList<WXGLTextObject>) {
        (0 until numPanes).forEach {
            textObjects[it].initializeTextLabelsObservations()
            textObjects[it].addTextLabelsObservations()
        }
    }

    fun updateWpcFronts(numPanes: Int, textObjects: MutableList<WXGLTextObject>) {
        (0 until numPanes).forEach {
            textObjects[it].addWpcPressureCenters()
        }
    }
}
