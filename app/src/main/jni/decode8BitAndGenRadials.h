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

#include <jni.h>
#include <string.h>
#include <math.h>
#include <stdlib.h>
#include <stdio.h>
#include "bzlib.h"
//#include <android/log.h>

#ifndef _Included_joshuatee_wX_Jni_decode8BitAndGenRadials
#define _Included_joshuatee_wX_Jni_decode8BitAndGenRadials
#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jint JNICALL Java_joshuatee_wx_Jni_decode8BitAndGenRadials
  (JNIEnv * , jclass, jstring , jlong ,jint ,jobject , jobject  ,jobject , jobject , jfloat, jbyte, jbyte, jbyte, jobject, jobject , jobject );

#ifdef __cplusplus
}
#endif
#endif
