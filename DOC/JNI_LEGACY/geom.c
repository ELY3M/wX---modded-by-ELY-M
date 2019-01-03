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

#include "geom.h"

JNIEXPORT jint JNICALL Java_joshuatee_wx_JNI_geom
(JNIEnv * env, jclass clazz, jobject in_buff, jobject out_buff,
		jfloat center_x, jfloat center_y, jfloat x_image_center_pixels,
		jfloat y_image_center_pixels, jfloat scale_factor,  jfloat one_degree_scale_factor, jint count)
{

	jfloat* iBuff =  (*env)-> GetDirectBufferAddress(env,in_buff);
	jfloat* oBuff =  (*env)-> GetDirectBufferAddress(env,out_buff);

	float PI = 3.1415926f;
	int i_count=0;

	for (i_count = 0; i_count < count; i_count = i_count + 2)
	{

		/*point_x = iBuff[i_count];
		point_y = iBuff[i_count+1];

		test1 = 180.0/PI * log(tan(PI/4.0+point_x*(PI/180.0)/2.0));
		test2 = 180.0/PI * log(tan(PI/4.0+center_x*(PI/180.0)/2.0));

		pix_y_d =  -((test1 - test2) *  one_degree_scale_factor ) + y_image_center_pixels;
		pix_x_d =  -((point_y - center_y ) * one_degree_scale_factor ) + x_image_center_pixels;

		oBuff[i_count] = pix_x_d;
		oBuff[i_count+1] = pix_y_d * -1.0f;*/

		//point_x = iBuff[i_count];
		//point_y = iBuff[i_count+1];

		//test1 = (180.0/PI * log(tan(PI/4.0+iBuff[i_count]*(PI/180.0)/2.0)));
		//test2 = (180.0/PI * log(tan(PI/4.0+center_x*(PI/180.0)/2.0)));

		oBuff[i_count+1] = -1.0f *( -(((180.0/PI * log(tan(PI/4.0+iBuff[i_count]*(PI/180.0)/2.0))) - (180.0/PI * log(tan(PI/4.0+center_x*(PI/180.0)/2.0)))) *  one_degree_scale_factor ) + y_image_center_pixels);
		oBuff[i_count] =  -((iBuff[i_count+1] - center_y ) * one_degree_scale_factor ) + x_image_center_pixels;

		//oBuff[i_count] = pix_x_d;
		//oBuff[i_count+1] = pix_y_d * -1.0f;

	}
	return 0;
}


