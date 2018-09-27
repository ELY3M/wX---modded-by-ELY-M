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

#include "genMercato.h"

JNIEXPORT void JNICALL Java_joshuatee_wx_JNI_genMercato(JNIEnv * env, jclass clazz, jobject in_buff, jobject out_buff, jfloat center_x, jfloat center_y, jfloat x_image_center_pixels, jfloat y_image_center_pixels,  jfloat one_degree_scale_factor, jint count){

	jfloat* iBuff = (*env)->GetDirectBufferAddress(env, in_buff);
	jfloat* oBuff = (*env)->GetDirectBufferAddress(env, out_buff);
	double W_180_DIV_PI = 180.0 / M_PI;
	double W_PI_DIV_360 = M_PI / 360.0;
	double W_PI_DIV_4 = M_PI / 4.0;
	int i_count = 0;
	for (i_count = 0; i_count < count; i_count = i_count + 2){
		oBuff[i_count+1] = -1.0f *( -(((W_180_DIV_PI * log(tan(W_PI_DIV_4+iBuff[i_count]*(W_PI_DIV_360))))
									   - (W_180_DIV_PI * log(tan(W_PI_DIV_4+center_x*(W_PI_DIV_360))))) *  one_degree_scale_factor ) + y_image_center_pixels);
		oBuff[i_count] =  -((iBuff[i_count+1] - center_y ) * one_degree_scale_factor ) + x_image_center_pixels;
	}
}

