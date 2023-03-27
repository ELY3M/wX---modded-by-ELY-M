/*
 * Copyright 1998-2009 University Corporation for Atmospheric Research/Unidata
 *
 * Portions of this software were developed by the Unidata Program at the
 * University Corporation for Atmospheric Research.
 *
 * Access and use of this software shall impose the following obligations
 * and understandings on the user. The user is granted the right, without
 * any fee or cost, to use, copy, modify, alter, enhance and distribute
 * this software, and any derivative works thereof, and its supporting
 * documentation for any purpose whatsoever, provided that this entire
 * notice appears in all copies of the software, derivative works and
 * supporting documentation.  Further, UCAR requests that the user credit
 * UCAR/Unidata in any publications that result from the use of this
 * software or in any product that includes this software. The names UCAR
 * and/or Unidata, however, may not be used in any advertising or publicity
 * to endorse or promote any products or commercial entity unless specific
 * written permission is obtained from UCAR/Unidata. The user also
 * understands that UCAR/Unidata is not obligated to provide the user with
 * any support, consulting, training or assistance of any kind with regard
 * to the use, operation and performance of this software nor to provide
 * the user with any updates, revisions, new versions or "bug fixes."
 *
 * THIS SOFTWARE IS PROVIDED BY UCAR/UNIDATA "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL UCAR/UNIDATA BE LIABLE FOR ANY SPECIAL,
 * INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING
 * FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT,
 * NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION
 * WITH THE ACCESS, USE OR PERFORMANCE OF THIS SOFTWARE.
 */

#include "level2Decode.h"

// This modules is responsible for decoding a file of uncompressed level 2 data
// and construcuting the necessary byte buffers with radial angles and level data for each radial
//

#define  CTM_HEADER_SIZE 12

/**
 * Size of the the message header, to start of the data message
 */
#define  MESSAGE_HEADER_SIZE 28

/**
 * Size of the entire message, if its a radar data message
 */
#define  RADAR_DATA_SIZE  2432

/**
 * Size of the file header, aka title
 */
#define  FILE_HEADER_SIZE 24

long bin_word_idx;
FILE *fp_src;

struct Level2Record {
	bool hasHighResREFData;
	bool hasHighResVELData;
	short message_size;
	char message_type;
	bool eof;
	char elevation_num;
	jfloat azimuth;
	long message_offset;
	short velocityHR_gate_count;
	short reflectHR_gate_count;
	short reflectHR_offset;
	short velocityHR_offset;
	int data_msecs;   // collection time for this radial, msecs since midnight
	short data_julian_date; // prob "collection time"
};

int readInt() {
	char c1;
	char c2;
	char c3;
	char c4;
	fread(&c1,sizeof(char), 1, fp_src);
	fread(&c2,sizeof(char), 1, fp_src);
	fread(&c3,sizeof(char), 1, fp_src);
	fread(&c4,sizeof(char), 1, fp_src);
	return ((c1 << 24) + (c2 << 16) + (c3 << 8) + (c4));
}

jfloat readFloatAz() {
	union {
		float f;  // assuming 32-bit IEEE 754 single-precision
		int i;    // assuming 32-bit 2's complement int
	} u;
	u.i = readInt();
	return (jfloat)u.f;
}

short readShort() {
	char c1;
	char c2;
	fread(&c1, sizeof(char), 1, fp_src);
	fread(&c2, sizeof(char), 1, fp_src);
	return (short)((c1 << 8) + (c2));
}

short readUnsignedShort() {
	char c1;
	char c2;
	fread(&c1, sizeof(char), 1, fp_src);
	fread(&c2, sizeof(char), 1, fp_src);
	return ((c1 << 8) + (c2));
}

char readByte() {
	char myInt;
	fread(&myInt, sizeof(char), 1, fp_src);
	return myInt;
}

char* getDataBlockStringValue(short offset, int skip, int size, long m_o, char* b){
	long off = offset + m_o + MESSAGE_HEADER_SIZE;
	fseek(fp_src, off, SEEK_SET);
	fseek(fp_src, skip, SEEK_CUR);
	int i;
	for (i =0; i < size; i++){
		b[i] = readByte();
	}
	return b;
}

short getDataBlockValue(short offset, int skip, long m_o){
	long off = offset + m_o + MESSAGE_HEADER_SIZE;
	fseek(fp_src, off, SEEK_SET);
	fseek(fp_src, skip, SEEK_CUR);
	return readShort();
}

struct Level2Record UtilityNexradLevel2Record(FILE* fp_src, int record, long message_offset31){

	struct Level2Record st;
	st.message_offset = record * RADAR_DATA_SIZE + FILE_HEADER_SIZE + message_offset31;

	fseek(fp_src, 0L, SEEK_END);
	long sz = ftell(fp_src);
	if (st.message_offset >= sz) {
		st.eof=true;
		return st;
	} else {
	    st.eof=false;
	}

	fseek(fp_src, st.message_offset, SEEK_SET);
	fseek(fp_src, CTM_HEADER_SIZE, SEEK_CUR);

	// Message Header
	// int size = din.readInt();
	st.message_size = readShort(); // size in "halfwords" = 2 bytes
	char id_channel = readByte(); // channel id
	st.message_type = readByte();
	fseek(fp_src, 12, SEEK_CUR);

	if (st.message_type == 1) {
		// data header
		fseek(fp_src, 46, SEEK_CUR);
		fseek(fp_src, 14, SEEK_CUR);
		fseek(fp_src, 6, SEEK_CUR);

	} else if (st.message_type == 31) {

		//skipBytes(4);
		//sk = fseek(fp_src,4,SEEK_CUR);
		int testt = readInt();
		st.data_msecs = readInt();   // collection time for this radial, msecs since midnight
		st.data_julian_date = readShort(); // prob "collection time"
		short radial_num = readShort(); // radial number within the elevation
		st.azimuth = readFloatAz(); // LOOK why unsigned ??
		fseek(fp_src, 6, SEEK_CUR);
		st.elevation_num = readByte(); // RDA elevation number
		fseek(fp_src, 21, SEEK_CUR);
		int dbp4 = readInt();
		int dbp5 = readInt();
		int dbp6 = readInt();
		int dbp7 = readInt();
		int dbp8 = readInt();
		int dbp9 = readInt();
		int dbpp4 = 0;
		int dbpp5 = 0;
		const char* ref_str = "REF";
		const char* vel_str = "VEL";
		char sting_storage[3];
		if (dbp4 > 0) {
			char* tname = getDataBlockStringValue((short) dbp4, 1, 3, st.message_offset, sting_storage);
			if ( strncmp(tname, ref_str, 3)==0) {
				st.hasHighResREFData = true;
				dbpp4 = dbp4;
			} else if (strncmp(tname, vel_str, 3)==0) {
				st.hasHighResVELData = true;
				dbpp5 = dbp4;
			}
		}
		if (dbp5 > 0) {
			// VEL data seems to be here
			char* tname = getDataBlockStringValue((short) dbp5, 1, 3, st.message_offset, sting_storage);
			if (strncmp(tname, ref_str, 3)==0) {
				st.hasHighResREFData = true;
				dbpp4 = dbp5;
			} else if (strncmp(tname, vel_str, 3)==0) {
				st.hasHighResVELData = true;
				dbpp5 = dbp5;
			}
		}
		if (dbp6 > 0) {
			char* tname = getDataBlockStringValue((short)dbp6, 1, 3, st.message_offset, sting_storage);
			if (strncmp(tname, ref_str, 3)==0) {
				st.hasHighResREFData = true;
				dbpp4 = dbp6;
			} else if (strncmp(tname, vel_str, 3)==0) {
				st.hasHighResVELData = true;
				dbpp5 = dbp6;
			}
		}
		if (dbp7 > 0) {
			char* tname = getDataBlockStringValue((short)dbp7, 1, 3, st.message_offset,sting_storage);
			if (strncmp(tname, ref_str, 3)==0) {
				st.hasHighResREFData = true;
				dbpp4 = dbp7;
			} else if (strncmp(tname, vel_str, 3)==0) {
				st.hasHighResVELData = true;
				dbpp5 = dbp7;
			}
		}
		if (dbp8 > 0) {
			char* tname = getDataBlockStringValue((short)dbp8, 1, 3, st.message_offset, sting_storage);
			if (strncmp(tname, ref_str, 3)==0) {
				st.hasHighResREFData = true;
				dbpp4 = dbp8;
			} else if (strncmp(tname, vel_str, 3)==0) {
				st.hasHighResVELData = true;
				dbpp5 = dbp8;
			}
		}
		if (dbp9 > 0) {
			char* tname = getDataBlockStringValue((short)dbp9, 1, 3, st.message_offset, sting_storage);
			if (strncmp(tname, ref_str, 3)==0) {
				st.hasHighResREFData = true;
				dbpp4 = dbp9;
			} else if (strncmp(tname, vel_str, 3)==0) {
				st.hasHighResVELData = true;
				dbpp5 = dbp9;
			}
		}
		if (st.hasHighResREFData) {
			st.reflectHR_gate_count = getDataBlockValue((short)dbpp4, 8, st.message_offset);
			st.reflectHR_offset = (short)(dbpp4 + 28);
		}
		if (st.hasHighResVELData) {
			st.velocityHR_gate_count = getDataBlockValue((short)dbpp5, 8, st.message_offset);
			st.velocityHR_offset = (short)(dbpp5 + 28);
		}
	}
	return st;
}

void readData(jbyte* bin_word, long offset, int cnt, short s) {
	offset += MESSAGE_HEADER_SIZE; // offset is from "start of digital radar data message header"
	offset += s;
	fseek(fp_src, offset, SEEK_SET);
	// test for reduction in data size ref
	//if (cnt==1832)
	cnt=916; // similar to other radar programs only how 1/2 the bins for performance reasons
	fread(bin_word + bin_word_idx, sizeof(char), cnt, fp_src);
	bin_word_idx = bin_word_idx + cnt;
}

JNIEXPORT void JNICALL Java_joshuatee_wx_Jni_level2Decode(JNIEnv * env, jclass clazz, jstring src, jobject bin_word_g, jobject radial_start_angle_g, jint product_code, jobject bb_days, jobject bb_msecs) {
	jbyte* bin_word = (*env)->GetDirectBufferAddress(env, bin_word_g);
	jfloat* radial_start_angle = (*env)->GetDirectBufferAddress(env, radial_start_angle_g);
	jshort* days = (*env)-> GetDirectBufferAddress(env, bb_days);
	jint* msecs = (*env)-> GetDirectBufferAddress(env, bb_msecs);
	days[0] = 0;
	msecs[0] = 0;
	bin_word_idx = 0;
	const char *src_path = (*env)->GetStringUTFChars( env, src , NULL ) ;
	fp_src = fopen(src_path, "r");
	if (fp_src == NULL) {
    	return;
    }
	int ref_alloc_list = 720;
	int vel_alloc_list = 0;
	if (product_code == 154) {
		vel_alloc_list = 1440;
	}
	// skip the header
	char header[FILE_HEADER_SIZE];
	size_t fread_return = fread(header, sizeof(char), FILE_HEADER_SIZE, fp_src);
	if(fread_return != FILE_HEADER_SIZE ) {
	    return;
    }
	struct Level2Record highReflectivity[ref_alloc_list];
	struct Level2Record highVelocity[vel_alloc_list];
	struct Level2Record level2_tmp;
	int highReflectivity_cnt = 0;
	int highVelocity_cnt = 0;
	long message_offset31 = 0;
	int recno = 0;
	while (true) {
		level2_tmp = UtilityNexradLevel2Record(fp_src, recno++, message_offset31);
		if (level2_tmp.eof) {
		    break;
		}
		if (level2_tmp.message_type == 31) {
			message_offset31 = message_offset31 + (level2_tmp.message_size * 2 + 12 - 2432);
		}
		if (level2_tmp.message_type != 1 && level2_tmp.message_type != 31) {
			continue;
		}
		if (level2_tmp.message_type == 31) {
			if (level2_tmp.hasHighResREFData && product_code == 153 && level2_tmp.elevation_num == 1) {
				highReflectivity[highReflectivity_cnt] = level2_tmp;
				highReflectivity_cnt++;
			}
			if (level2_tmp.hasHighResVELData && product_code == 154 &&  level2_tmp.velocityHR_gate_count == 1192 && level2_tmp.elevation_num == 2){
				highVelocity[highVelocity_cnt] = level2_tmp;
				highVelocity_cnt++;
			}
		}
	}
	if (product_code == 153) {
		days[0] = highReflectivity[1].data_julian_date;
		msecs[0] = highReflectivity[1].data_msecs;
	} else {
		days[0] = highVelocity[1].data_julian_date;
		msecs[0] = highVelocity[1].data_msecs;
	}
	int number_of_radials = 720;
	bool velocity_prod = false;
	if (product_code == 154) {
		velocity_prod = true;
	}
	int r;
	int radial_start_angle_cnt = 0;
	if (!velocity_prod) {
		for (r = 0; r < number_of_radials; r++) {
			if (highReflectivity[r].elevation_num == 1) {
				radial_start_angle[radial_start_angle_cnt] = 450.0f - highReflectivity[r].azimuth;
				radial_start_angle_cnt++;
				readData(bin_word, highReflectivity[r].message_offset, highReflectivity[r].reflectHR_gate_count, highReflectivity[r].reflectHR_offset);
			}
		}
	} else {
		for (r = 0; r < number_of_radials; r++) {
			if (highVelocity[r].elevation_num == 2) {
				radial_start_angle[radial_start_angle_cnt] = 450.0f - highVelocity[r].azimuth;
				radial_start_angle_cnt++;
				readData(bin_word,highVelocity[r].message_offset, highVelocity[r].velocityHR_gate_count, highVelocity[r].velocityHR_offset);
			}
		}
	}
	fclose(fp_src);
}

