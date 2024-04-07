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

#include "genCircleWithColor.h"

jbyte red(int color) {
    return (color >> 16) & 0xFF;
}

jbyte green(int color) {
    return (color >> 8) & 0xFF;
}

jbyte blue(int color) {
    return color & 0xFF;
}

JNIEXPORT void JNICALL Java_joshuatee_wx_Jni_genCircleWithColor(
    JNIEnv * env,
    jclass clazz,
    jobject locationBuffer,
    jobject indexBuffer,
    jfloat centerX,
    jfloat centerY,
    jfloat xImageCenterPixels,
    jfloat yImageCenterPixels,
    jfloat oneDegreeScaleFactor,
    jdoubleArray x,
    jdoubleArray y,
    jint count,
    jfloat len,
    int triangleAmount,
    jobject color_buff,
    jintArray colorIntArray
) {
    jfloat * lBuff = (*env)->GetDirectBufferAddress(env, locationBuffer);
    jshort * iBuff = (*env)->GetDirectBufferAddress(env, indexBuffer);
    jbyte * cBuff = (*env)->GetDirectBufferAddress(env, color_buff);
    jdouble * x_arr = (*env)->GetDoubleArrayElements(env, x, 0);
    jdouble * y_arr = (*env)->GetDoubleArrayElements(env, y, 0);
    jint * col_arrInt = (*env)->GetIntArrayElements(env, colorIntArray, 0);
    jbyte col_arr[3];
    int l_count = 0;
    int c_count = 0;
    int ix_count = 0;
    float twicePi = 2.0f * M_PI;
    double W_180_DIV_PI = 180.0 / M_PI;
    double W_PI_DIV_360 = M_PI / 360.0 ;
    double W_PI_DIV_4 = M_PI / 4.0;
    len *= 0.50;
    for (int i_count = 0; i_count < count; i_count++) {
        col_arr[0] = red(col_arrInt[i_count]);
        col_arr[1] = green(col_arrInt[i_count]);
        col_arr[2] = blue(col_arrInt[i_count]);
        float test1 = W_180_DIV_PI * log(tan(W_PI_DIV_4 + x_arr[i_count] * W_PI_DIV_360));
        float test2 = W_180_DIV_PI * log(tan(W_PI_DIV_4 + centerX * W_PI_DIV_360));
        float pix_y_d = -1.0 * ((test1 - test2) *  oneDegreeScaleFactor) + yImageCenterPixels;
        float pix_x_d = -1.0 * ((y_arr[i_count] - centerY) * oneDegreeScaleFactor) + xImageCenterPixels;
        for (int i = 0; i < triangleAmount; i++) {
            lBuff[l_count] = pix_x_d;
            lBuff[l_count + 1] = -1.0 * pix_y_d;
            lBuff[l_count + 2] = pix_x_d + (len * cos(i *  twicePi / triangleAmount));
            lBuff[l_count + 3] = -1.0 * pix_y_d + (len * sin(i * twicePi / triangleAmount));
            lBuff[l_count + 4] = pix_x_d + (len * cos((i + 1) *  twicePi / triangleAmount));
            lBuff[l_count + 5] = -1.0 * pix_y_d + (len * sin((i + 1) * twicePi / triangleAmount));
            l_count += 6;
            iBuff[ix_count] = ix_count;
            iBuff[ix_count + 1] = ix_count + 1;
            iBuff[ix_count + 2] = ix_count + 2;
            ix_count += 3;
            cBuff[c_count] = col_arr[0];
            cBuff[c_count + 1] = col_arr[1];
            cBuff[c_count + 2] = col_arr[2];
            cBuff[c_count + 3] = col_arr[0];
            cBuff[c_count + 4] = col_arr[1];
            cBuff[c_count + 5] = col_arr[2];
            cBuff[c_count + 6] = col_arr[0];
            cBuff[c_count + 7] = col_arr[1];
            cBuff[c_count + 8] = col_arr[2];
            c_count += 9;
        }
    }
}
