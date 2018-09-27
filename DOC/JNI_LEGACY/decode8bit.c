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

#include "decode8bit.h"
#include "bzlib.h"

//#include <android/log.h>

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

JNIEXPORT jshort JNICALL Java_joshuatee_wx_JNI_decode8bit
(JNIEnv * env, jclass clazz, jstring src, jlong seek_start,jint length,jobject i_buff, jobject o_buff, jobject radial_start, jobject bin_word)

{

	const char *src_path = (*env)->GetStringUTFChars( env, src , NULL ) ;

	jbyte* oBuff =  (*env)-> GetDirectBufferAddress(env,o_buff);
	jbyte* iBuff =  (*env)-> GetDirectBufferAddress(env,i_buff);

	jfloat* rBuff =  (*env)-> GetDirectBufferAddress(env,radial_start);
	jbyte* bw =  (*env)-> GetDirectBufferAddress(env,bin_word);

	FILE *fp_src;
	fp_src = fopen(src_path,"r");
	int ret_size = 1000000;

	//__android_log_print(ANDROID_LOG_VERBOSE, "wx", "Made it %d", 0);

	int seek_ret = fseek(fp_src,seek_start,SEEK_SET);
	int bytesRead = fread(iBuff,sizeof(char),length,fp_src);
	//int a = BZ2_bzBuffToBuffDecompress( oBuff, &ret_size , iBuff, length , 1, 0 ); //  1 for small, 0 verbosity
	int a = BZ2_bzBuffToBuffDecompress( (char*)oBuff, (unsigned int*)&ret_size , (char*)iBuff, length , 1, 0 ); //  1 for small, 0 verbosity


	int r_idx=0;
	int bw_idx=0;
	int o_idx=20;

	//int  number_of_range_bins   = dis2.readUnsignedShort() ;
	//int packet_code = (oBuff[o_idx++] << 8 ) + oBuff[o_idx++];
	//int idx_first_bin = (oBuff[o_idx++] << 8 ) + oBuff[o_idx++];
	//unsigned short number_of_range_bins = ((uint16_t)oBuff[o_idx++] << 8 ) + (uint16_t)oBuff[o_idx++];

	char array[2];
	array[0] = oBuff[o_idx++];
	array[1] = oBuff[o_idx++];
	unsigned short number_of_range_bins = toShort(array);

	//__android_log_print(ANDROID_LOG_VERBOSE, "wx", "range bins: %d", number_of_range_bins);


	//int  i_center_of_sweep   = dis2.readShort() ;
	//int  j_center_of_sweep   = dis2.readShort() ;
	//int  scale_factor  = dis2.readUnsignedShort() ;
	//int  number_of_radials  = dis2.readUnsignedShort();

	int  number_of_radials  = 360;
	o_idx += 8; // skip 4 short or unsigned short

	int r = 1;
	unsigned short number_of_rle_halfwords = 0;
	int tn_mod10 = 0;
	int s = 0;
	unsigned short tn = 0;
	int tmp_int=0;

	for ( r=0; r<number_of_radials;r++)
	{
		//number_of_rle_halfwords = dis2.readUnsignedShort() ;

		array[0] = oBuff[o_idx++];
		array[1] = oBuff[o_idx++];
		number_of_rle_halfwords = toShort(array);

		//tn =dis2.readUnsignedShort();
		array[0] = oBuff[o_idx++];
		array[1] = oBuff[o_idx++];
		tn = toShort(array);

		//__android_log_print(ANDROID_LOG_VERBOSE, "wx", "Made it %d", tn);


		/*if (tn%2==1)
			tn++;

		tn_mod10 = tn % 10;
		if ( tn_mod10 > 0 && tn_mod10 < 5 )
			tn = tn - tn_mod10;
		else if (tn_mod10 > 6)
			tn = tn - tn_mod10+10;*/

		rBuff[r_idx++] = (float) (450 - (tn/10.0)); // was 10

		//__android_log_print(ANDROID_LOG_VERBOSE, "wx", "Made it %f", rBuff[r_idx-1]);


		//dis2.skipBytes(2);
		o_idx += 2;

		for ( s=0; s<number_of_rle_halfwords  ;s++)
		{
			//bin_word.put((byte)dis2.readUnsignedByte());
			bw[bw_idx++] = oBuff[o_idx++];
		}

	}

	int closev = fclose(fp_src);

	// support tilts above first
	if ( number_of_range_bins % 2 != 0 )
		number_of_range_bins++;

	return number_of_range_bins;

}






