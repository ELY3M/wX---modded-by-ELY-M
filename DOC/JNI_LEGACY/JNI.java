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

package joshuatee.wx;

// interface to JNI functions to be used by all Java files

public class JNI {

    // Native code accessed via JNI was originally developed in 2014. At the time it was necessary for best performnace.
    // 2+ years later gains in hardware and software make the gains neglible in most cases. Plan is to migrate away from
    // native code to ease maintenance of wX codebase.

    //static { System.loadLibrary("radial"); }

    // handles transformation of radaril/bin to x,y coords - color palette assignmentes and polygon reductions
    /*public static native int radialv2( ByteBuffer radar_float_buffer,ByteBuffer radar_color_buffer,ByteBuffer bin_word,
                                 ByteBuffer radial_start_ang, int radials, int range_bins, float bsize, int prod_code, int bg_color,
                                 int use_map, ByteBuffer colormap_r, ByteBuffer colormap_g, ByteBuffer colormap_b,
                                 int use_map99, ByteBuffer colormap99_r, ByteBuffer colormap99_g, ByteBuffer colormap99_b);

    public static native int radiallevel2( ByteBuffer radar_float_buffer,ByteBuffer radar_color_buffer,ByteBuffer bin_word,
                                       ByteBuffer radial_start_ang, int radials, int range_bins, float bsize, int prod_code, int bg_color,
                                       int use_map, ByteBuffer colormap_r, ByteBuffer colormap_g, ByteBuffer colormap_b,
                                       int use_map99, ByteBuffer colormap99_r, ByteBuffer colormap99_g, ByteBuffer colormap99_b);*/

   /* public static native int geom (ByteBuffer a, ByteBuffer b,
                             float center_x, float center_y, float x_image_center_pixels,
                             float y_image_center_pixels, float scale_factor,  float one_degree_scale_factor, int count);*/

   /* public static native int city (ByteBuffer a, ByteBuffer b, ByteBuffer c,
                             float center_x, float center_y, float x_image_center_pixels,
                             float y_image_center_pixels, float scale_factor,  float one_degree_scale_factor,double[] x,double[] y,int count,float len, byte[] col);*/

    /*public static native int spotter (ByteBuffer a, ByteBuffer b, ByteBuffer c,
                                float center_x, float center_y, float x_image_center_pixels,
                                float y_image_center_pixels, float scale_factor,  float one_degree_scale_factor,double[] x,double[] y,int count,float len, byte[] col, int triangleAmount);*/


/*    public static native int colorgen (ByteBuffer a, int len, byte[] col);
    public static native int indexgen (ByteBuffer a, int len, int bsize);
    public static native int indexgenline (ByteBuffer a, int len, int bsize);*/

    //public static native short decode8bit (String src, long seek_start,int length,ByteBuffer i_buff,
    //                                       ByteBuffer o_buff, ByteBuffer radial_start, ByteBuffer bin_word);

    // given a portion of a Level 2 radar file , decompress only what is needed for the lowest tilt of 153 ref or 154 vel
    //public static native int bzipwrapfull (String src, String dst, ByteBuffer a, ByteBuffer b, int prod_code);

    // given a decompressed Level 2 radar file decode the file and return radial / level data
    //public static native int nexradl2ogl (String src, String dst,ByteBuffer a,ByteBuffer b, int prod_code,
    //                                       ByteBuffer bb_days,ByteBuffer bb_msec);

    // used by UtilityNexrad8Bit.java for sharing in wxogl
    //public static native short decode8bitwx(String src, long seek_start,int length,ByteBuffer i_buff, ByteBuffer o_buff, ByteBuffer radial_start, ByteBuffer bin_word);
    //public static native short rect8bitwx(ByteBuffer r_buff, float bin_start2, float bin_size2, int level_count2 ,
    //                                      float angle2, float angle_v2, int center_x2, int center_y2);

}
