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

#include "colorGen.h"

JNIEXPORT void JNICALL Java_joshuatee_wx_Jni_colorGen(
	JNIEnv * env,
	jclass clazz,
	jobject colorByteBuffer,
	jint len,
	jbyteArray colorByteArray
) {
	jbyte* cBuff = (*env)->GetDirectBufferAddress(env, colorByteBuffer);
	jbyte* col_arr = (*env)->GetByteArrayElements(env, colorByteArray, 0);
	int count = 0;
	for (int index = 0; index < len; index++) {
		cBuff[count] = col_arr[0];
		cBuff[count + 1] = col_arr[1];
		cBuff[count + 2] = col_arr[2];
		count += 3;
	}
}
