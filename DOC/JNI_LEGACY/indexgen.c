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

#include "indexgen.h"

JNIEXPORT jint JNICALL Java_joshuatee_wx_JNI_indexgen
(JNIEnv * env, jclass clazz,  jobject index_buff, jint len, jint break_size)
{

	jshort* iBuff =  (*env)-> GetDirectBufferAddress(env,index_buff);

	int i=0;
	int incr = 0;
	int remainder = 0;
	int chunk_count = 1;
	int total_bins = len;

	if (total_bins<break_size)
	{
		break_size=total_bins;
		remainder = break_size;
	}
	else
	{
		chunk_count = total_bins/break_size;
		remainder = total_bins - break_size*chunk_count;
		chunk_count++;
	}

	int chunk_index = 0;
	int j=0;
	for (chunk_index=0;chunk_index<chunk_count;chunk_index++)
	{

		incr=0;

		if (chunk_index == (chunk_count-1))
			break_size = remainder;

		for (j=0;j<break_size;j++)
		{

			iBuff[i]=(short) (0 +incr);
			iBuff[i+1]=(short) (1 +incr);
			iBuff[i+2]=(short) (2 +incr);
			iBuff[i+3]=(short) (0 +incr);
			iBuff[i+4]=(short) (2 +incr);
			iBuff[i+5]=(short) (3 +incr);

			incr += 4;
			i += 6;

		}
	}
	return remainder;
}


JNIEXPORT jint JNICALL Java_joshuatee_wx_JNI_indexgenline
(JNIEnv * env, jclass clazz,  jobject index_buff, jint len, jint break_size)
{

	jshort* iBuff =  (*env)-> GetDirectBufferAddress(env,index_buff);

	int i=0;
	int incr = 0;
	int remainder = 0;
	int chunk_count = 1;
	int total_bins = len/4;

	if (total_bins<break_size)
	{
		break_size=total_bins;
		remainder = break_size;
	}
	else
	{
		chunk_count = total_bins/break_size;
		remainder = total_bins - break_size*chunk_count;
		chunk_count++;
	}

	int chunk_index = 0;
	int j=0;
	for (chunk_index=0;chunk_index<chunk_count;chunk_index++)
	{

		incr=0;

		if (chunk_index == (chunk_count-1))
			break_size = remainder;

		for (j=0;j<break_size;j++)
		{

			iBuff[i]=(short) (0 +incr);
			iBuff[i+1]=(short) (1 +incr);

			incr += 2;
			i += 2;

		}
	}
	return remainder;
}


