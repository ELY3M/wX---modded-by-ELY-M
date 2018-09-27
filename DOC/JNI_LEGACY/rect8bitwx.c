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

#include "rect8bitwx.h"
//#include <android/log.h>

JNIEXPORT jshort JNICALL Java_joshuatee_wx_JNI_rect8bitwx
(JNIEnv * env, jclass clazz,  jobject r_buff, jfloat bin_start, jfloat bin_size, jint level_count ,jfloat angle, jfloat angle_v, jint center_x, jint center_y)

{


	jfloat* rBuff =  (*env)-> GetDirectBufferAddress(env,r_buff);
	float PI = 3.1415926f;


	rBuff[0] =   (bin_start * cos(angle/(180.00f / PI))) + center_x;
	rBuff[1] = ((bin_start * sin(angle/(180.00f / PI)))- center_y ) * -1;
	rBuff[2] = ((bin_start + ( bin_size * level_count )) * cos(angle/(180.00f / PI))) + center_x ;
	rBuff[3] = (((bin_start + ( bin_size * level_count )) * sin(angle/(180.00f / PI)))- center_y ) * -1;
	rBuff[4] = ((bin_start + ( bin_size * level_count )) * cos((angle - angle_v )/(180.00f / PI))) + center_x ;
	rBuff[5] = (((bin_start + ( bin_size * level_count )) * sin((angle - angle_v )/(180.00f / PI)))- center_y ) * -1;
	rBuff[6] = (bin_start * cos((angle - angle_v)/(180.00f / PI)))+ center_x;
	rBuff[7] = ((bin_start * sin((angle - angle_v)/(180.00f / PI)))- center_y ) * -1;

	return 0;

}






