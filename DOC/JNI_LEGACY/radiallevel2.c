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

#include "radiallevel2.h"

JNIEXPORT jint JNICALL Java_joshuatee_wx_JNI_radiallevel2
		(JNIEnv * env, jclass clazz, jobject rad_buff, jobject color_buff, jobject bin_buff, jobject radial_start,
		 jint number_of_radials, jint num_range_bins, jfloat bin_size, jshort product_code, jboolean bg_color,
		 jint use_map, jobject colormap_r, jobject colormap_g, jobject colormap_b,
		 jint use_map99, jobject colormap99_r, jobject colormap99_g, jobject colormap99_b)
{

	unsigned char* color_r;
	unsigned char* color_g;
	unsigned char* color_b;

	unsigned char* c_r =  (*env)-> GetDirectBufferAddress(env,colormap_r);
	unsigned char* c_g =  (*env)-> GetDirectBufferAddress(env,colormap_g);
	unsigned char* c_b =  (*env)-> GetDirectBufferAddress(env,colormap_b);

	unsigned char* c99_r =  (*env)-> GetDirectBufferAddress(env,colormap99_r);
	unsigned char* c99_g =  (*env)-> GetDirectBufferAddress(env,colormap99_g);
	unsigned char* c99_b =  (*env)-> GetDirectBufferAddress(env,colormap99_b);

	switch ( product_code )
	{
		case 153:

				color_r = c_r;
				color_g = c_g;
				color_b = c_b;

			break;
		case 154:
				color_r = c99_r;
				color_g = c99_g;
				color_b = c99_b;
			break;
		default:
			break;
	}

	color_r[0] = bg_color;
	color_g[0] = bg_color;
	color_b[0] = bg_color;

	int total_bins =0;
	int g=0;
	float angle;  // this and one below were init assigned 0.0f
	float angle_v;
	int level       = 0;
	int level_count = 0;
	float bin_start;  // was init assigned 0.0f
	int bin = 0;
	//int data_level_total=256;
	//unsigned char colorf[3];

	int color_for = 0;
	float PI = 3.1415926f;

	jfloat* rBuff =  (*env)-> GetDirectBufferAddress(env,rad_buff);
	jbyte* cBuff =  (*env)-> GetDirectBufferAddress(env,color_buff);
	jbyte* bBuff =  (*env)-> GetDirectBufferAddress(env,bin_buff);
	jfloat* radial_start_angle =  (*env)-> GetDirectBufferAddress(env,radial_start);

	int r_i=0;
	int c_i=0;
	int b_i=0;

	// uncomment to backout change below
	//angle_v = radial_delta;

	int cur_level;

	for (g=0;g<number_of_radials;g++)
	{
		angle   = radial_start_angle[g];
		level =  (unsigned char )bBuff[b_i];
		level_count = 0;
		bin_start   = bin_size;

		// the below conditional is the only change from radialv2 minus removal of color palette data for non prod 153/154

        if ( g < (number_of_radials-1))
        {
		    angle_v = radial_start_angle[g+1];
		} else
		{
	        angle_v = radial_start_angle[0];
		}

		for  (bin=0;bin<num_range_bins;bin++ ) {

			cur_level =(unsigned char)bBuff[b_i];
			b_i++;

			if ( cur_level == level  )
			{
				++level_count;
			} else {


				rBuff[r_i] = bin_start * cos(( angle_v)/(180.00f / PI));
				rBuff[r_i+1] = bin_start * sin(( angle_v)/(180.00f / PI));


				rBuff[r_i+2] = (bin_start + ( bin_size * level_count )) * cos((angle_v)/(180.00f / PI));
				rBuff[r_i+3] = (bin_start + ( bin_size * level_count )) * sin(( angle_v)/(180.00f / PI));

			/*	rBuff[r_i] = bin_start * cos((angle - angle_v)/(180.00f / PI));
				rBuff[r_i+1] = bin_start * sin((angle - angle_v)/(180.00f / PI));


				rBuff[r_i+2] = (bin_start + ( bin_size * level_count )) * cos((angle - angle_v)/(180.00f / PI));
				rBuff[r_i+3] = (bin_start + ( bin_size * level_count )) * sin((angle - angle_v)/(180.00f / PI));*/

				rBuff[r_i+4] = (bin_start + ( bin_size * level_count)) * cos(angle/(180.00f / PI));
				rBuff[r_i+5] = (bin_start + ( bin_size * level_count)) * sin(angle/(180.00f / PI));

				rBuff[r_i+6] = bin_start * cos(angle/(180.00f / PI));
				rBuff[r_i+7] = bin_start * sin(angle/(180.00f / PI));

				r_i += 8;

				for ( color_for = 0; color_for<4;color_for++ )
				{

					cBuff[c_i]=(jbyte)color_r[level];
					cBuff[c_i+1]=(jbyte)color_g[level];
					cBuff[c_i+2]=(jbyte)color_b[level];
					//cBuff[c_i+3]=255;

					c_i += 3;
				}

				//total_bins++;
				++total_bins;

				level = cur_level;
				bin_start   = bin * bin_size;
				level_count = 1;
			}
		}
	}
	return total_bins;
}










