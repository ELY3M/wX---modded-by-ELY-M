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

package joshuatee.wx.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Paint.Style

import joshuatee.wx.objects.GeographyType
import joshuatee.wx.objects.ProjectionType
import java.nio.ByteBuffer

internal object UtilityCanvasGeneric {

    fun draw(
        context: Context,
        provider: ProjectionType,
        bitmap: Bitmap,
        rid: String,
        lineWidth: Int,
        type: GeographyType,
        genericByteBuffer: ByteBuffer
    ) {
        val mercato = provider.isMercator
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.style = Style.STROKE
        paint.strokeWidth = lineWidth.toFloat()
        if (provider.needsCanvasShift) {
            canvas.translate(UtilityCanvasMain.xOffset, UtilityCanvasMain.yOffset)
        }
        paint.color = type.color
        val wallpath = Path()
        wallpath.reset()
        val pn = ProjectionNumbers(context, rid, provider)
        genericByteBuffer.position(0)
        try {
            val tmpBuffer = ByteBuffer.allocateDirect(genericByteBuffer.capacity())
            if (mercato) {
                UtilityCanvasProjection.computeMercatorFloatToBuffer(
                    genericByteBuffer,
                    tmpBuffer,
                    pn
                )
            } else {
                UtilityCanvasProjection.compute4326NumbersFloatToBuffer(
                    genericByteBuffer,
                    tmpBuffer,
                    pn
                )
            }
            tmpBuffer.position(0)
            while (tmpBuffer.position() < tmpBuffer.capacity()) {
                wallpath.moveTo(tmpBuffer.float, tmpBuffer.float)
                wallpath.lineTo(tmpBuffer.float, tmpBuffer.float)
            }
            canvas.drawPath(wallpath, paint)
        } catch (e: OutOfMemoryError) {
            UtilityLog.HandleException(e)
        }
    }
}
