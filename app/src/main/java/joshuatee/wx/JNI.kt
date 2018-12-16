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

package joshuatee.wx

// interface to JNI functions to be used by all Java files

import java.nio.ByteBuffer

object JNI {

    // Native code accessed via JNI was originally developed in 2014. At the time it was necessary for best performnace.
    // 2+ years later gains in hardware and software make the gains neglible in most cases.

    init {
        System.loadLibrary("radial")
    }

    external fun level2GenRadials(
        radar_float_buffer: ByteBuffer,
        radar_color_buffer: ByteBuffer,
        bin_word: ByteBuffer,
        radial_start_ang: ByteBuffer,
        radials: Int,
        range_bins: Int,
        bsize: Float,
        bg_color: Int,
        colormap_r: ByteBuffer,
        colormap_g: ByteBuffer,
        colormap_b: ByteBuffer,
        product_code: Int
    ): Int

    external fun decode8BitAndGenRadials(
        src: String,
        seek_start: Long,
        length: Int,
        i_buff: ByteBuffer,
        o_buff: ByteBuffer,
        rad_buff: ByteBuffer,
        color_buff: ByteBuffer,
        bin_size: Float,
        bg_color_red: Byte,
        bg_color_green: Byte,
        bg_color_blue: Byte,
        c_r: ByteBuffer,
        c_g: ByteBuffer,
        c_b: ByteBuffer
    ): Int

    // given a portion of a Level 2 radar file , decompress only what is needed for the lowest tilt of 153 ref or 154 vel
    external fun level2Decompress(
        src: String,
        dst: String,
        a: ByteBuffer,
        b: ByteBuffer,
        prod_code: Int
    )

    // given a decompressed Level 2 radar file decode the file and return radial / level data
    external fun level2Decode(
        src: String,
        a: ByteBuffer,
        b: ByteBuffer,
        prod_code: Int,
        bb_days: ByteBuffer,
        bb_msec: ByteBuffer
    )

    external fun genIndex(a: ByteBuffer, len: Int, bsize: Int)
    external fun genIndexLine(a: ByteBuffer, len: Int, bsize: Int)
    external fun genMercato(
        a: ByteBuffer,
        b: ByteBuffer,
        center_x: Float,
        center_y: Float,
        x_image_center_pixels: Float,
        y_image_center_pixels: Float,
        one_degree_scale_factor: Float,
        count: Int
    )

    external fun genCircle(
        a: ByteBuffer,
        b: ByteBuffer,
        center_x: Float,
        center_y: Float,
        x_image_center_pixels: Float,
        y_image_center_pixels: Float,
        one_degree_scale_factor: Float,
        x: DoubleArray,
        y: DoubleArray,
        count: Int,
        len: Float,
        triangleAmount: Int,
        c: ByteBuffer,
        color: ByteArray
    )

    external fun genCircleWithColor(
        a: ByteBuffer,
        b: ByteBuffer,
        center_x: Float,
        center_y: Float,
        x_image_center_pixels: Float,
        y_image_center_pixels: Float,
        one_degree_scale_factor: Float,
        x: DoubleArray,
        y: DoubleArray,
        count: Int,
        len: Float,
        triangleAmount: Int,
        c: ByteBuffer,
        color_arr: IntArray
    )

    external fun genTriangle(
        a: ByteBuffer,
        b: ByteBuffer,
        center_x: Float,
        center_y: Float,
        x_image_center_pixels: Float,
        y_image_center_pixels: Float,
        one_degree_scale_factor: Float,
        x: DoubleArray,
        y: DoubleArray,
        count: Int,
        len: Float,
        c: ByteBuffer,
        color: ByteArray
    )

    external fun genTriangleUp(
        a: ByteBuffer,
        b: ByteBuffer,
        center_x: Float,
        center_y: Float,
        x_image_center_pixels: Float,
        y_image_center_pixels: Float,
        one_degree_scale_factor: Float,
        x: DoubleArray,
        y: DoubleArray,
        count: Int,
        len: Float,
        c: ByteBuffer,
        color: ByteArray
    )

    external fun colorGen(a: ByteBuffer, len: Int, col: ByteArray)
}
