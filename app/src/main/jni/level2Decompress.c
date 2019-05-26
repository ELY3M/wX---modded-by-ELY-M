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

#include "level2Decompress.h"

// This module is responsible for taking raw Level 2 data and decompressing it
// In addition, it intelligently detects when enough "chunks" have been downloaded to
// assemble the lowest tilt for base reflectivity or base velocity

JNIEXPORT void JNICALL Java_joshuatee_wx_Jni_level2Decompress(JNIEnv * env, jclass clazz, jstring src, jstring dst,jobject i_buff,jobject o_buff, jint product_code){

	const char *src_path = (*env)->GetStringUTFChars(env, src, NULL );
	const char *dst_path = (*env)->GetStringUTFChars(env, dst, NULL );

	jbyte* iBuff = (*env)->GetDirectBufferAddress(env, i_buff);
	jbyte* oBuff = (*env)->GetDirectBufferAddress(env, o_buff);

	FILE *fp_src;
	FILE *fp_dst;
	fp_src = fopen(src_path, "r");
	fp_dst = fopen(dst_path, "w");
	if ( fp_src == NULL || fp_dst == NULL ){
		return;
	}
	int FILE_HEADER_SIZE = 24;
	int o_size = 829472;
	int eof = 1;
	char header[FILE_HEADER_SIZE];
	size_t fread_return = fread(header, sizeof(char), FILE_HEADER_SIZE, fp_src);
	if(fread_return != FILE_HEADER_SIZE ) {
    	    return;
    }
	int bytesWritten = fwrite(header, sizeof(char), FILE_HEADER_SIZE, fp_dst);
	int bytesWritten2 = 0;
	int bytesRead2 = 0;
	int numCompBytes = 0;
	char bytes[4];
	unsigned int ret_size;
	int loop_cnt = 0;
	int loop_cnt_break = 0;
	int ref_decomp_size = 0;
	int vel_decomp_size = 0;

	if ( product_code == 153 ){
		loop_cnt_break = 5;
	} else {
		loop_cnt_break = 11;
	}

	ref_decomp_size = 827040;
	vel_decomp_size = 460800;

	while ( eof != 0 ){
		ret_size = (unsigned int)o_size;
		fread(bytes, sizeof(char), 4, fp_src);

		numCompBytes = ((uint32_t)bytes[0] << 24) + ((uint32_t)bytes[1] << 16) + ((uint32_t)bytes[2] << 8) + bytes[3]; // big end

		if (numCompBytes == -1 || numCompBytes == 0) {
			break;
		}

		if (numCompBytes < 0) {
			numCompBytes = -numCompBytes;
			eof = 0;
		}

		bytesRead2 = fread(iBuff, sizeof(char), numCompBytes, fp_src);
		if (bytesRead2 != numCompBytes){
			break;
		}

		//__android_log_print(ANDROID_LOG_VERBOSE, "wx", "input size %d", bytesRead2);

		BZ2_bzBuffToBuffDecompress((char*)oBuff, &ret_size , (char*)iBuff, numCompBytes, 1, 0); //  1 for small, 0 verbosity
		bytesWritten2 = fwrite(oBuff, sizeof(char), (int)ret_size, fp_dst);

		bytesWritten = bytesWritten2 + bytesWritten;

		if ( bytesWritten2 == ref_decomp_size || bytesWritten2 == vel_decomp_size)
			loop_cnt++;

		if (loop_cnt>loop_cnt_break){
			break;
		}
	}

	// 1 325888 message data
	// 6 827040 ( speculate ref ) or 829472(one case observed lead chunk was thus big and as a consequence have some elevation 2 for ref )
	// 6 460800 ( speculate vel )

	fclose(fp_src);
	fclose(fp_dst);
}
