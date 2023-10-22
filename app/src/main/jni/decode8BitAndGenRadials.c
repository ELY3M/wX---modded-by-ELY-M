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

#include "decode8BitAndGenRadials.h"

union CharToStruct {
	char charArray[2];
	unsigned short value;
};

unsigned short toShort(char* value){
	union CharToStruct cs;
	cs.charArray[0] = value[1]; // most significant bit of short is not first bit of char array
	cs.charArray[1] = value[0];
	return cs.value;
}

JNIEXPORT jint JNICALL Java_joshuatee_wx_Jni_decode8BitAndGenRadials (
	JNIEnv * env,
	jobject clazz, // was jclass
	jstring src,
	jlong seekStart,
	jint length,
	jobject inputBuffer,
	jobject outputBuffer,
	jobject radarBuffer,
	jobject colorBuffer,
	jfloat binSize,
	jbyte bgColorRed,
	jbyte bgColorGreen,
	jbyte bgColorBlue,
	jobject colorRedBuffer,
	jobject colorGreenBuffer,
	jobject colorBlueBuffer,
	jint productCode
 ) {
	jbyte* color_r = (*env)->GetDirectBufferAddress(env, colorRedBuffer);
	jbyte* color_g = (*env)->GetDirectBufferAddress(env, colorGreenBuffer);
	jbyte* color_b = (*env)->GetDirectBufferAddress(env, colorBlueBuffer);
	color_r[0] = bgColorRed;
	color_g[0] = bgColorGreen;
	color_b[0] = bgColorBlue;
	int total_bins = 0;
	float angle;
	float angle_v;
	int level = 0;
	int level_count = 0;
	float bin_start;
	int bin = 0;
	int color_for = 0;
	float bin_size_times_level_count;
	double W_180_DIV_PI = 180.0 / M_PI;
	jfloat* rBuff = (*env)->GetDirectBufferAddress(env, radarBuffer);
	jbyte* cBuff = (*env)->GetDirectBufferAddress(env, colorBuffer);
	int r_i = 0;
	int c_i = 0;
	int cur_level;
	const char *src_path = (*env)->GetStringUTFChars(env, src, NULL);
	jbyte* oBuff = (*env)->GetDirectBufferAddress(env, outputBuffer);
	jbyte* iBuff = (*env)->GetDirectBufferAddress(env, inputBuffer);
	FILE* fp_src = fopen(src_path, "r");
	if (fp_src == NULL) {
		return -1;
	}
	// int ret_size = 1000000;
	// Feb 22, increase for L3 super-res
	int ret_size = 2000000;
	int seek_return = fseek(fp_src, seekStart, SEEK_SET);
	if (seek_return != 0) {
    	return -1;
    }
	size_t fread_return = fread(iBuff, sizeof(char), length, fp_src);
	if (fread_return != length) {
	    return -1;
        //} else {
        //   if (feof(fp_src))
        //      printf("error reading file: unexpected end of file\n");
        //   else if (ferror(fp_src)) {
        //      perror("error reading file");
        //}
    }
	BZ2_bzBuffToBuffDecompress((char*)oBuff, (unsigned int*)&ret_size, (char*)iBuff, length, 1, 0); // 1 for small, 0 verbosity
	int o_idx=20;
	char array[2];
	array[0] = oBuff[o_idx++];
	array[1] = oBuff[o_idx++];
	int number_of_radials  = 360;
	if (productCode == 2153 || productCode == 2154) {
	    number_of_radials = 720;
	}
	o_idx += 8; // skip 4 short or unsigned short
	// int r;
	unsigned short number_of_rle_halfwords = 0;
	unsigned short tn = 0;
	unsigned short tn_next = 0;
	float angle_0 = 0.0f;
	float angle_next;
	for (int r = 0; r < number_of_radials; r++) {
		array[0] = oBuff[o_idx++];
		array[1] = oBuff[o_idx++];
		number_of_rle_halfwords = toShort(array);
		array[0] = oBuff[o_idx++];
		array[1] = oBuff[o_idx++];
		tn = toShort(array);
		o_idx += 2;
		array[0] = oBuff[o_idx + number_of_rle_halfwords + 2];
		array[1] = oBuff[o_idx + number_of_rle_halfwords + 3];
		tn_next = toShort(array);
		angle_next = (float)(450 - (tn_next / 10.0));
		angle = (float)(450 - (tn / 10.0));
		if (r==0)
			angle_0 = angle;

		level = 0;
		level_count = 0;
		bin_start = binSize;
		if (r < 359) {
			angle_v = angle_next;
		} else {
			angle_v = angle_0;
		}
        double angleVCos = cos(angle_v / W_180_DIV_PI);
        double angleVSin = sin(angle_v / W_180_DIV_PI);
        double angleCos = cos(angle / W_180_DIV_PI);
        double angleSin = sin(angle / W_180_DIV_PI);
		for (bin = 0; bin < number_of_rle_halfwords; bin++) {
			cur_level = (unsigned char)oBuff[o_idx++];
			if (cur_level == level){
				level_count++;
			} else {
				bin_size_times_level_count = binSize * level_count;

				rBuff[r_i++] = bin_start * angleVCos;
				rBuff[r_i++] = bin_start * angleVSin;

				rBuff[r_i++] = (bin_start + bin_size_times_level_count) * angleVCos;
				rBuff[r_i++] = (bin_start + bin_size_times_level_count) * angleVSin;

				rBuff[r_i++] = (bin_start + bin_size_times_level_count) * angleCos;
				rBuff[r_i++] = (bin_start + bin_size_times_level_count) * angleSin;

				rBuff[r_i++] = bin_start * angleCos;
				rBuff[r_i++] = bin_start * angleSin;

				for (color_for = 0; color_for < 4; color_for++) {
					cBuff[c_i++] = (jbyte)color_r[level];
					cBuff[c_i++] = (jbyte)color_g[level];
					cBuff[c_i++] = (jbyte)color_b[level];
				}
				total_bins++;
				level = cur_level;
				bin_start = bin * binSize;
				level_count = 1;
			}
		} // end looping over bins in one radial
	}
	fclose(fp_src);
	return total_bins;
}
