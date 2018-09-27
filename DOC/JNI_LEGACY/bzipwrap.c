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

#include "bzipwrap.h"
#include "bzlib.h"

//#include <android/log.h>

// This module is responsible for taking raw Level 2 data and decompressing it
// In addition, it intelligently detects when enough "chunks" have been downloaded to
// assemble the lowest tilt for base reflectivity or base velocity


JNIEXPORT jint JNICALL Java_joshuatee_wx_JNI_bzipwrapfull
		(JNIEnv * env, jclass clazz, jstring src, jstring dst,jobject i_buff,jobject o_buff, jint product_code)

{

	const char *src_path = (*env)->GetStringUTFChars( env, src , NULL ) ;
	const char *dst_path = (*env)->GetStringUTFChars( env, dst , NULL ) ;

	jbyte* iBuff =  (*env)-> GetDirectBufferAddress(env,i_buff);
	jbyte* oBuff =  (*env)-> GetDirectBufferAddress(env,o_buff);

	FILE *fp_src;
	FILE *fp_dst;
	fp_src = fopen(src_path,"r");
	fp_dst = fopen(dst_path,"w");

	int FILE_HEADER_SIZE = 24;
	int i_size = 1200000;
	int o_size = 2400000;
	int eof = 1;

	char header[FILE_HEADER_SIZE];
	int bytesRead = fread(header,sizeof(char),FILE_HEADER_SIZE,fp_src);
	int bytesWritten = fwrite(header,sizeof(char),FILE_HEADER_SIZE,fp_dst);
	int bytesWritten2 = 0;
	int bytesRead2 = 0;
	int numCompBytes = 0;
	char bytes[4];
	unsigned int ret_size;
	int loop_cnt = 0;
	int loop_cnt_break = 0;
	int i=0;
	int ref_decomp_size = 0;
	int vel_decomp_size = 0;

	int full_read = -1;

	if ( product_code == 153 )
	{
		loop_cnt_break = 5; // was 6 when msg data was 1
	} else
	{
		loop_cnt_break = 11; // was 12
	}

	ref_decomp_size = 827040;
	vel_decomp_size = 460800;

	while ( eof != 0 )
	{

		ret_size=(unsigned int)o_size;
		bytesRead = fread(bytes,sizeof(char),4,fp_src);

		//numCompBytes = bytes[3] + ((uint32_t)bytes[2] << 8)
		//    		+ ((uint32_t)bytes[1] << 16) + ((uint32_t)bytes[0] << 24); // little end - not needed

		numCompBytes = ((uint32_t)bytes[0] << 24) + ((uint32_t)bytes[1] << 16) + ((uint32_t)bytes[2] << 8) + bytes[3]; // big end

		if (numCompBytes == -1 || numCompBytes == 0) {
			break;
		}

		if (numCompBytes < 0) {
			numCompBytes = -numCompBytes;
			eof = 0;
		}

		bytesRead2 = fread(iBuff,sizeof(char),numCompBytes,fp_src);
		if (bytesRead2!=numCompBytes)
		{
			break;
		}

		//int a = BZ2_bzBuffToBuffDecompress( oBuff, &ret_size , iBuff, numCompBytes , 1, 0 ); //  1 for small, 0 verbosity
		int a = BZ2_bzBuffToBuffDecompress( (char*)oBuff, (unsigned int*)&ret_size , (char*)iBuff, numCompBytes , 1, 0 ); //  1 for small, 0 verbosity

		bytesWritten2 = fwrite(oBuff,sizeof(char),(int)ret_size,fp_dst);

		bytesWritten = bytesWritten2 + bytesWritten;

		if ( bytesWritten2 == ref_decomp_size || bytesWritten2 == vel_decomp_size)
			loop_cnt++;

		if (loop_cnt>loop_cnt_break)  // 12 break if velocity has been decomp
		{
			full_read = 0;
			break;
		}

	}

	// 1 325888 message data
	// 6 827040 ( speculate ref ) or 829472(one case observed lead chunk was thus big and as a consequence have some elevation 2 for ref )
	// 6 460800 ( speculate vel )

	int closev = fclose(fp_src);
	closev = fclose(fp_dst);

	return full_read;
}


JNIEXPORT jint JNICALL Java_joshuatee_wx_JNI_bzipwrap
		(JNIEnv * env, jclass clazz, jobject i_buff, jint i_len, jobject o_buff, jint dest_len)

{

	jbyte* iBuff =  (*env)-> GetDirectBufferAddress(env,i_buff);
	jbyte* oBuff =  (*env)-> GetDirectBufferAddress(env,o_buff);

	unsigned int ret_size = (unsigned int )dest_len;
	//int a = BZ2_bzBuffToBuffDecompress( oBuff, &ret_size , iBuff, i_len , 1, 0 ); // 0 verbosity, 1 for small
	int a = BZ2_bzBuffToBuffDecompress( (char*)oBuff, (unsigned int*)&ret_size , (char*)iBuff, i_len , 1, 0 ); // 0 verbosity, 1 for small

	return ret_size;
}


