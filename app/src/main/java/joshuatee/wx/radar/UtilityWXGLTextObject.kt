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
//modded by ELY M.

package joshuatee.wx.radar

object UtilityWXGLTextObject {

    fun hideLabels(numPanes: Int, textObjects: List<WXGLTextObject>) {
        (0 until numPanes).forEach {
            textObjects[it].hideLabels()
        }
    }

    fun showLabels(numPanes: Int, textObjects: List<WXGLTextObject>) {
        (0 until numPanes).forEach {
            textObjects[it].addLabels()
        }
    }

    fun updateSpotterLabels(numPanes: Int, textObjects: List<WXGLTextObject>) {
        (0 until numPanes).forEach {
            textObjects[it].addSpottersLabels()
        }
    }

    fun updateObservations(numPanes: Int, textObjects: List<WXGLTextObject>) {
        (0 until numPanes).forEach {
            textObjects[it].initializeObservations()
            textObjects[it].addObservations()
        }
    }
    //elys mod - not removing yet
    fun updateObservationsSinglePane(paneNumber: Int, textObjects: List<WXGLTextObject>) {
        textObjects[paneNumber].initializeObservations()
        textObjects[paneNumber].addObservations()
    }

    fun updateWpcFronts(numPanes: Int, textObjects: List<WXGLTextObject>) {
        (0 until numPanes).forEach {
            textObjects[it].addWpcPressureCenters()
        }
    }

    
    //elys mod    
    fun updateHailLabels(numPanes: Int, textObjects: List<WXGLTextObject>) {
        (0 until numPanes).forEach {
            textObjects[it].initializeHailLabels()
            textObjects[it].addHailLabels()
        }
    }
    
}
