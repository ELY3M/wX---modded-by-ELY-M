/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022, 2023  joshua.tee@gmail.com

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

#include "level2GenRadials.h"

JNIEXPORT jint JNICALL Java_joshuatee_wx_Jni_level2GenRadials(
    JNIEnv * env,
    jobject clazz, // was jclass
    jobject radarFloatBuffer,
    jobject radarColorBuffer,
    jobject binWord,
    jobject radialStartAngle,
    jint number_of_radials,
    jint numberOfRangeBins,
    jfloat bin_size,
    jbyte bgColorRed,
    jbyte bgColorGreen,
    jbyte bgColorBlue,
    jobject colormapRed,
    jobject colormapGreen,
    jobject colormapBlue,
    int productCode
) {
    unsigned char* c_r = (*env)->GetDirectBufferAddress(env, colormapRed);
    unsigned char* c_g = (*env)->GetDirectBufferAddress(env, colormapGreen);
    unsigned char* c_b = (*env)->GetDirectBufferAddress(env, colormapBlue);
    c_r[0] = bgColorRed;
    c_g[0] = bgColorGreen;
    c_b[0] = bgColorBlue;
    int total_bins = 0;
    int g = 0;
    float angle;
    float angle_v;
    int level = 0;
    int level_count = 0;
    float bin_start;
    int bin = 0;
    int color_for = 0;
    double W_180_DIV_PI = 180.0 / M_PI;
    jfloat* rBuff = (*env)->GetDirectBufferAddress(env, radarFloatBuffer);
    jbyte* cBuff = (*env)->GetDirectBufferAddress(env, radarColorBuffer);
    jbyte* bBuff = (*env)->GetDirectBufferAddress(env, binWord);
    jfloat* radial_start_angle = (*env)->GetDirectBufferAddress(env, radialStartAngle);
    int r_i = 0;
    int c_i = 0;
    int b_i = 0;
    int cur_level;
    float bin_size_times_level_count;
    for (g = 0; g < number_of_radials; g++) {
        angle = radial_start_angle[g];
        level = (unsigned char)bBuff[b_i];
        level_count = 0;
        bin_start = 4.0f;
        if ( g < (number_of_radials - 1)) {
            angle_v = radial_start_angle[g + 1];
        } else {
            angle_v = radial_start_angle[0];
        }
        for (bin = 0; bin < numberOfRangeBins; bin++) {
            cur_level = (unsigned char)bBuff[b_i];
            b_i++;
            if (cur_level == level) {
                level_count++;
            } else {
                bin_size_times_level_count = bin_size * level_count;

                rBuff[r_i++] = bin_start * cos(angle_v / W_180_DIV_PI);
                rBuff[r_i++] = bin_start * sin(angle_v / W_180_DIV_PI);

                rBuff[r_i++] = (bin_start + (bin_size_times_level_count )) * cos((angle_v) / (W_180_DIV_PI));
                rBuff[r_i++] = (bin_start + (bin_size_times_level_count )) * sin((angle_v) / (W_180_DIV_PI));

                rBuff[r_i++] = (bin_start + (bin_size_times_level_count)) * cos(angle / (W_180_DIV_PI));
                rBuff[r_i++] = (bin_start + (bin_size_times_level_count)) * sin(angle / (W_180_DIV_PI));

                rBuff[r_i++] = bin_start * cos(angle / (W_180_DIV_PI));
                rBuff[r_i++] = bin_start * sin(angle / (W_180_DIV_PI));

                for (color_for = 0; color_for < 4; color_for++) {
                    cBuff[c_i++] = (jbyte)c_r[level];
                    cBuff[c_i++] = (jbyte)c_g[level];
                    cBuff[c_i++] = (jbyte)c_b[level];
                }
                total_bins++;
                level = cur_level;
                bin_start = bin * bin_size + 4.0f;
                level_count = 1;
            }
        }
    }
    return total_bins;
}
