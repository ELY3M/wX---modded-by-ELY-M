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

package joshuatee.wx

//
// interface to JNI functions to be used by all Java files
// This file must stay in this location otherwise the underlying C source code files need
// to be updated
//

import java.nio.ByteBuffer

object Jni {

    //
    // Native code accessed via JNI was originally developed in 2014. At the time it was necessary for best performance.
    // years later gains in hardware and software make the gains negligible in most cases.
    // It is supported best effort and must be enabled in developer settings
    // This is used for Level3 only.
    //

    init {
        System.loadLibrary("radial")
    }

    external fun decode8BitAndGenRadials(
        src: String,
        seekStart: Long,
        length: Int,
        inputBuffer: ByteBuffer,
        outputBuffer: ByteBuffer,
        radarBuffer: ByteBuffer,
        colorBuffer: ByteBuffer,
        binSize: Float,
        bgColorRed: Byte,
        bgColorGreen: Byte,
        bgColorBlue: Byte,
        colorRedBuffer: ByteBuffer,
        colorGreenBuffer: ByteBuffer,
        colorBlueBuffer: ByteBuffer,
        productCode: Int
    ): Int

    external fun genIndex(indexBuffer: ByteBuffer, len: Int, breakSize: Int)
    external fun genIndexLine(indexBuffer: ByteBuffer, len: Int, breakSize: Int)
    external fun genMercator(
        inputBuffer: ByteBuffer,
        outputBuffer: ByteBuffer,
        centerX: Float,
        centerY: Float,
        xImageCenterPixels: Float,
        yImageCenterPixels: Float,
        oneDegreeScaleFactor: Float,
        count: Int
    )

    external fun genCircle(
        locationBuffer: ByteBuffer,
        indexBuffer: ByteBuffer,
        centerX: Float,
        centerY: Float,
        xImageCenterPixels: Float,
        yImageCenterPixels: Float,
        oneDegreeScaleFactor: Float,
        x: DoubleArray,
        y: DoubleArray,
        count: Int,
        len: Float,
        triangleAmount: Int,
        c: ByteBuffer,
        color: ByteArray
    )

    external fun genCircleWithColor(
        locationBuffer: ByteBuffer,
        indexBuffer: ByteBuffer,
        centerX: Float,
        centerY: Float,
        xImageCenterPixels: Float,
        yImageCenterPixels: Float,
        oneDegreeScaleFactor: Float,
        x: DoubleArray,
        y: DoubleArray,
        count: Int,
        len: Float,
        triangleAmount: Int,
        c: ByteBuffer,
        colorIntArray: IntArray
    )

    external fun genTriangle(
        locationBuffer: ByteBuffer,
        indexBuffer: ByteBuffer,
        centerX: Float,
        centerY: Float,
        xImageCenterPixels: Float,
        yImageCenterPixels: Float,
        oneDegreeScaleFactor: Float,
        x: DoubleArray,
        y: DoubleArray,
        count: Int,
        len: Float,
        c: ByteBuffer,
        color: ByteArray
    )

    external fun genTriangleUp(
        locationBuffer: ByteBuffer,
        indexBuffer: ByteBuffer,
        centerX: Float,
        centerY: Float,
        xImageCenterPixels: Float,
        yImageCenterPixels: Float,
        oneDegreeScaleFactor: Float,
        x: DoubleArray,
        y: DoubleArray,
        count: Int,
        len: Float,
        c: ByteBuffer,
        color: ByteArray
    )

    external fun colorGen(colorByteBuffer: ByteBuffer, len: Int, colorByteArray: ByteArray)
}
