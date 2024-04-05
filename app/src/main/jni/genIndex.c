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

#include "genIndex.h"

JNIEXPORT void JNICALL Java_joshuatee_wx_Jni_genIndex(
    JNIEnv * env,
    jclass clazz,
    jobject indexBuffer,
    jint len,
    jint breakSize
) {
    jshort * iBuff = (*env)->GetDirectBufferAddress(env, indexBuffer);
    int i = 0;
    int incr = 0;
    int remainder = 0;
    int chunk_count = 1;
    int total_bins = len;
    if (total_bins < breakSize){
        breakSize = total_bins;
        remainder = breakSize;
    } else {
        chunk_count = total_bins / breakSize;
        remainder = total_bins - breakSize * chunk_count;
        chunk_count++;
    }
    int chunk_index = 0;
    for (chunk_index = 0; chunk_index < chunk_count; chunk_index++) {
        incr = 0;
        if (chunk_index == (chunk_count - 1)) {
            breakSize = remainder;
        }
        for (int j = 0; j < breakSize; j++) {
            iBuff[i++] = (short) (0 + incr);
            iBuff[i++] = (short) (1 + incr);
            iBuff[i++] = (short) (2 + incr);
            iBuff[i++] = (short) (0 + incr);
            iBuff[i++] = (short) (2 + incr);
            iBuff[i++] = (short) (3 + incr);
            incr += 4;
        }
    }
}

JNIEXPORT void JNICALL Java_joshuatee_wx_Jni_genIndexLine(
    JNIEnv * env,
    jclass clazz,
    jobject indexBuffer,
    jint len,
    jint breakSize
) {
    jshort * iBuff = (*env)->GetDirectBufferAddress(env, indexBuffer);
    int i = 0;
    int incr = 0;
    int remainder = 0;
    int chunk_count = 1;
    int total_bins = len / 4;
    if (total_bins < breakSize){
        breakSize = total_bins;
        remainder = breakSize;
    } else {
        chunk_count = total_bins / breakSize;
        remainder = total_bins - breakSize * chunk_count;
        chunk_count += 1;
    }
    int chunk_index = 0;
    for (chunk_index = 0; chunk_index < chunk_count; chunk_index++) {
        incr = 0;
        if (chunk_index == (chunk_count - 1)) {
            breakSize = remainder;
        }
        for (int j = 0; j < breakSize; j++) {
            iBuff[i++] = (short) (0 + incr);
            iBuff[i++] = (short) (1 + incr);
            incr += 2;
        }
    }
}
