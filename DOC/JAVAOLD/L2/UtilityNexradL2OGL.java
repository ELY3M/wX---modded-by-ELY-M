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




package joshuatee.wx.radar;

// The following has chunks of code from Level2VolumeScan.java so using the license for that file
// This file has now been extensively modified from the original

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.Context;

import joshuatee.wx.util.UCARRandomAccessFile;
import joshuatee.wx.util.UtilityLog;

public class UtilityNexradL2OGL {


/*	private static native int bzipwrapfull (String src, String dst,ByteBuffer a,ByteBuffer b, int prod_code, boolean afb_site);
	private static native int nexradl2ogl (String src, String dst,ByteBuffer a,ByteBuffer b, int prod_code, boolean afb_site,
										   ByteBuffer bb_days,ByteBuffer bb_msec);*/


	/*private static ByteBuffer  bin_word = null;
	private static ByteBuffer radial_start_angle = null;*/

	/**
	 * Reflectivity moment identifier
	 */
	private static final int REFLECTIVITY = 1;

	/**
	 * Radial Velocity moment identifier
	 */
	private static final int VELOCITY_HI = 2;

	/**
	 * Radial Velocity moment identifier
	 */
	private static final int VELOCITY_LOW = 4;

	/**
	 * Sprectrum Width moment identifier
	 */
	private static final int SPECTRUM_WIDTH = 3;

	/**
	 * Low doppler resolution code
	 */
	//public static final int DOPPLER_RESOLUTION_LOW_CODE = 4;

	/**
	 * High doppler resolution code
	 */
	//public static final int DOPPLER_RESOLUTION_HIGH_CODE = 2;

	/**
	 * Horizontal beam width
	 */
	//public static final float HORIZONTAL_BEAM_WIDTH = (float) 1.5;         // LOOK  always true ??

	/* added for high resolution message type 31 */

	private static final int REFLECTIVITY_HIGH = 5;

	/**
	 * High Resolution Radial Velocity moment identifier
	 */
	private static final int VELOCITY_HIGH = 6;

	/**
	 * High Resolution Sprectrum Width moment identifier
	 */
	//public static final int SPECTRUM_WIDTH_HIGH = 7;

	/**
	 * High Resolution Radial Velocity moment identifier
	 */
	//public static final int DIFF_REFLECTIVITY_HIGH = 8;

	/**
	 * High Resolution Radial Velocity moment identifier
	 */
	//public static final int DIFF_PHASE = 9;

	/**
	 * High Resolution Sprectrum Width moment identifier
	 */
	//public static final int CORRELATION_COEFFICIENT = 10;
	//public static final byte MISSING_DATA = (byte) 1;
	//public static final byte BELOW_THRESHOLD = (byte) 0;

	/**
	 * Size of the file header, aka title
	 */

	/**
	 * Size of the CTM record header
	 */
	private static final int CTM_HEADER_SIZE = 12;

	/**
	 * Size of the the message header, to start of the data message
	 */
	private static final int MESSAGE_HEADER_SIZE = 28;

	/**
	 * Size of the entire message, if its a radar data message
	 */
	private static final int RADAR_DATA_SIZE = 2432;

	/**
	 * Size of the file header, aka title
	 */
	private static final int FILE_HEADER_SIZE = 24;

	private static UtilityNexradLevel2Record first;

	//private static UtilityNexradLevel2Record last;

	private static int vcp = 0; // Volume coverage pattern

	/*private static int max_radials = 0;
	private static int min_radials = Integer.MAX_VALUE;
	private static int max_radials_hr = 0;
	private static int min_radials_hr = Integer.MAX_VALUE;

	private static boolean hasHighResolutionREF;
	private static boolean hasHighResolutionVEL;

	private static boolean showMessages = false;
	private static boolean showData = false;
	private static boolean debugScans = false;
	private static boolean  debugGroups2 = false;
	private static boolean  debugRadials = false;
	private static boolean debugStats = false;
	private static boolean runCheck = false;*/

	//private static  String decomp_fn = "l2.decomp";

	//public static WXGLNexrad.RadarDataL2 DecocodeAndPlotNexradL2(Context c, String fn,String prod, boolean already_downloaded, String rid1,String rid_loc )
	//public static WXGLNexrad.RadarDataL2 DecocodeAndPlotNexradL2(Context c, String fn, String prod, String rid_loc, String radar_status_str, String idx_str )
	public static WXGLNexrad.RadarDataL2 DecocodeAndPlotNexradL2(Context c, String fn,ByteBuffer bin_word ,
																 ByteBuffer radial_start_angle, int prod , ByteBuffer days, ByteBuffer msecs) {

		UCARRandomAccessFile dis2 = null;
		WXGLNexrad.RadarDataL2 rd = null;

		int ref_alloc_list = 1440;
		int vel_alloc_list = 1440;
		boolean velocity_prod = false;

		int product_code = 153;
		if (prod == 154) {
			product_code = 154;
			velocity_prod = true;
			//ref_alloc_list = 1440;
			//vel_alloc_list = 720;
		}

	/*	try {

			dis = new UCARRandomAccessFile(c.getFileStreamPath(fn).getAbsolutePath(), "r", 1024*256*10); // was c.getFileStreamPath(fn)
			dis.bigEndian = true;
			dis.close();

			ByteBuffer obuff = ByteBuffer.allocateDirect(2400000);
			ByteBuffer ibuff = ByteBuffer.allocateDirect(1200000);

			JNI.bzipwrapfull(c.getFileStreamPath(fn).getAbsolutePath(),c.getFileStreamPath(decomp_fn).getAbsolutePath(),ibuff,obuff,product_code);

		}	  catch (Exception e) {
			UtilityLog.HandleException(e);
		}*/

		try {

			dis2 = new UCARRandomAccessFile(c.getFileStreamPath(fn).getAbsolutePath(), "r", 1024 * 256 * 10); // was c.getFileStreamPath(fn)
			dis2.bigEndian = true;


		} catch (Exception e) {

			UtilityLog.HandleException(e);
		}

		int number_of_range_bins = 916;


		/*radial_start_angle = ByteBuffer.allocateDirect(720*4);
		radial_start_angle.order(ByteOrder.nativeOrder());
		radial_start_angle.position(0);

		bin_word = ByteBuffer.allocateDirect(720*number_of_range_bins); // was 720*1832
		bin_word.order(ByteOrder.nativeOrder());
		bin_word.position(0);

		ByteBuffer days = ByteBuffer.allocateDirect(2);
		days.order(ByteOrder.nativeOrder());
		days.position(0);

		ByteBuffer msecs = ByteBuffer.allocateDirect(4);
		msecs.order(ByteOrder.nativeOrder());
		msecs.position(0);

		JNI.nexradl2ogl(c.getFileStreamPath(decomp_fn).getAbsolutePath(),"",bin_word,radial_start_angle,product_code,days,msecs);

		msecs.position(0);
		days.position(0);


		final short days2 = days.getShort();
		final int msecs2 =  msecs.getInt();
		final long sec = ((long) (days2 - 1)) * 24 * 3600 * 1000 + msecs2;
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(sec);
		java.util.Date d = cal.getTime();

		final String radar_info = d.toString()  + MyApplication.newline +
				//"VCP: " + Integer.toString(highReflectivity.get(r).vcp) + MyApplication.newline +
				"Product Code: " + Integer.toString(product_code);


		MyApplication.editor!!.putString("WX_RADAR_CURRENT_INFO" + radar_status_str , radar_info);
		MyApplication.editor!!.apply();*/

		float angle_delta = 0.5f;

/*
		// AFB appears to have 360 with boundary of .50
		if (highReflectivity.get(361).elevation_num != 1)
		{
			number_of_radials=360;
			rad720=false;
			angle_delta=1.0f;
		}*/


		/*if (product_code==153) {
			//number_of_range_bins = 1832;
			//number_of_range_bins = 916; // test to reduce raw data to see impact on perf


			rd = new WXGLNexrad.RadarDataL2(radial_start_angle, angle_delta, WXGLNexrad.GetBinSize(product_code),
					number_of_range_bins, (short) 0, bin_word);
		}
		else {
			//number_of_range_bins = 1192;
			//number_of_range_bins = 916;
			rd = new WXGLNexrad.RadarDataL2(radial_start_angle, angle_delta, WXGLNexrad.GetBinSize(product_code),
					number_of_range_bins, (short) 0, bin_word);
		}*/

		try {
			if (dis2 != null) {
				dis2.setBufferSize(2621440); // 1024*256*10
				dis2.bigEndian = true;
				dis2.seek(FILE_HEADER_SIZE);
			}
		} catch (Exception e) {
		}


			List<UtilityNexradLevel2Record> highReflectivity = new ArrayList<>(ref_alloc_list);
			List<UtilityNexradLevel2Record> highVelocity = new ArrayList<>(vel_alloc_list);

			long message_offset31 = 0;
			int recno = 0;



			while (true) {


				UtilityNexradLevel2Record r=null;
				try {
					 r = UtilityNexradLevel2Record.factory(dis2, recno++, message_offset31);
				} catch (IOException e)
				{

				}
				//UtilityLog.d("wx","MADE IT");


				if (r == null) break;
				if (r.message_type == 31) {
					message_offset31 = message_offset31 + (r.message_size * 2 + 12 - 2432);
				}

				if (r.message_type != 1 && r.message_type != 31) {
					continue;
				}



				if (vcp == 0) vcp = r.vcp;
				if (first == null) first = r;

				//last = r;

				if (r.message_type == 31) {
					if (r.hasHighResREFData)
					{
						highReflectivity.add(r);
					}
					if (r.hasHighResVELData)
					{
						highVelocity.add(r);
					}

				}

			}

			int number_of_radials=720;

try{

		/*	boolean rad720=true;

			// AFB appears to have 360 with boundary of .50
			if (highReflectivity.get(361).elevation_num != 1)
			{
				number_of_radials=360;
				rad720=false;
				angle_delta=1.0f;
			}*/

			/*boolean velocity_prod = false;
			if ( prod.contains("L2VEL"))
			{
				//number_of_range_bins = 1192;
				velocity_prod = true;
			}
*/
			int r = 1;

			days.position(0);
			days.putShort(highReflectivity.get(r).data_julian_date);
			msecs.position(0);
			msecs.putInt(highReflectivity.get(r).data_msecs);

			/*final short days = highReflectivity.get(r).data_julian_date;
			final int msecs =  highReflectivity.get(r).data_msecs;
			final long sec = ((long) (days - 1)) * 24 * 3600 * 1000 + msecs;
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(sec);
			java.util.Date d = cal.getTime();

			final String radar_info = d.toString()  + MyApplication.newline +
					"VCP: " + Integer.toString(highReflectivity.get(r).vcp) + MyApplication.newline +
					"Product Code: " + Integer.toString(product_code);

			MyApplication.editor!!.putString("WX_RADAR_CURRENT_INFO" + radar_status_str , radar_info);
			MyApplication.editor!!.apply();*/

			//radial_start_angle = ByteBuffer.allocateDirect(number_of_radials*4);
			//radial_start_angle.order(ByteOrder.nativeOrder());

			//radial_start_angle.position(0);


			//int tmp_int;

			float tmp_float;

			if ( ! velocity_prod )
			{

				final int product_int = REFLECTIVITY_HIGH;
				//number_of_range_bins = highReflectivity.get(0).getGateCount(REFLECTIVITY_HIGH);
				//bin_word = ByteBuffer.allocateDirect(number_of_radials*number_of_range_bins);
				//bin_word.order(ByteOrder.nativeOrder());

				for ( r=0; r<number_of_radials;r++)
				{
					if (highReflectivity.get(r).elevation_num==1)
					{
						tmp_float = 450.0f-highReflectivity.get(r).azimuth;
						radial_start_angle.putFloat(tmp_float);

						//UtilityLog.d("wx","radial " + Float.toString(tmp_float));

						/*tmp_int = (int)tmp_float;
						if (rad720)
						{
							if ( (tmp_float - tmp_int) > 0.50)
							{
								tmp_float = tmp_int + 0.75f;
							} else
							{
								tmp_float = tmp_int + 0.25f;
							}
							radial_start_angle.putFloat(tmp_float);
						} else {
							radial_start_angle.putFloat(tmp_int+0.50f);
						}*/

						highReflectivity.get(r).readData(dis2, product_int,bin_word);

					}
				}
			} else {

				final int product_int = VELOCITY_HIGH;
				//number_of_range_bins = highVelocity.get(0).getGateCount(product_int);
				//bin_word = ByteBuffer.allocateDirect(number_of_radials*number_of_range_bins);
				//bin_word.order(ByteOrder.nativeOrder());

				for ( r=0; r<number_of_radials;r++)
				{
					if (highVelocity.get(r).elevation_num==2) // change from 1
					{
						tmp_float = 450.0f-highVelocity.get(r).azimuth;
						radial_start_angle.putFloat(tmp_float);


						/*tmp_int = (int)tmp_float;
						if (rad720)
						{
							if ( (tmp_float - tmp_int) > 0.50)
							{
								tmp_float = tmp_int + 0.75f;
							} else
							{
								tmp_float = tmp_int + 0.25f;
							}
							radial_start_angle.putFloat(tmp_float);

						} else {
							radial_start_angle.putFloat(tmp_int+0.50f);
						}*/

						highVelocity.get(r).readData(dis2, product_int,bin_word);

					}
				}
			}

			dis2.close();

			rd = new WXGLNexrad.RadarDataL2(radial_start_angle, angle_delta,WXGLNexrad.GetBinSize(product_code),
					number_of_range_bins,(short)0 ,bin_word );

			//rd = new WXGLNexrad.RadarDataL2( radial_start_angle,angle_delta,WXGLNexrad.GetBinSize(prod,product_code),
			//		number_of_range_bins,(short)0 );

		} catch (Exception e) {
			UtilityLog.HandleException(e);
		}


		return rd;

	}


	/**
	 * This class reads one record (radial) in an NEXRAD level II file.
	 * File must be uncompressed.
	 * Not handling messages yet, only data.
	 * <p/>
	 * 10/16/05: Now returns data as a byte, so use scale and offset.
	 * <p/>
	 * Adapted with permission from the Java Iras software developed by David Priegnitz at NSSL.
	 *
	 * @author caron
	 * @author David Priegnitz
	 */

	public static class UtilityNexradLevel2Record {

	/*	static public String getDatatypeName(int datatype) {
			switch (datatype) {
				case REFLECTIVITY:
					return "Reflectivity";
				case VELOCITY_HI:
				case VELOCITY_LOW:
					return "RadialVelocity";
				case SPECTRUM_WIDTH:
					return "SpectrumWidth";
				case REFLECTIVITY_HIGH:
					return "Reflectivity_HI";
				case VELOCITY_HIGH:
					return "RadialVelocity_HI";
				case SPECTRUM_WIDTH_HIGH:
					return "SpectrumWidth_HI";
				case DIFF_REFLECTIVITY_HIGH:
					return "Reflectivity_DIFF";
				case DIFF_PHASE:
					return "Phase";
				case CORRELATION_COEFFICIENT:
					return "RHO";


				default:
					throw new IllegalArgumentException();
			}
		}*/

	/*	static public String getDatatypeUnits(int datatype) {
			switch (datatype) {
				case REFLECTIVITY:
					return "dBz";

				case VELOCITY_HI:
				case VELOCITY_LOW:
				case SPECTRUM_WIDTH:
					return "m/s";

				case REFLECTIVITY_HIGH:
					return "dBz";
				case DIFF_REFLECTIVITY_HIGH:
					return "dBz";

				case VELOCITY_HIGH:
				case SPECTRUM_WIDTH_HIGH:
					return "m/s";

				case DIFF_PHASE:
					return "deg";

				case CORRELATION_COEFFICIENT:
					return "N/A";
			}
			throw new IllegalArgumentException();
		}*/

	/*	public short getDatatypeSNRThreshhold(int datatype) {
			switch (datatype) {
				case REFLECTIVITY_HIGH:
					return ref_snr_threshold;
				case VELOCITY_HIGH:
					return vel_snr_threshold;

				*//*	case SPECTRUM_WIDTH_HIGH:
				return sw_snr_threshold;
			case DIFF_REFLECTIVITY_HIGH:
				return zdrHR_snr_threshold;
			case DIFF_PHASE:
				return phiHR_snr_threshold;
			case CORRELATION_COEFFICIENT:
				return rhoHR_snr_threshold;*//*

				default:
					throw new IllegalArgumentException();
			}
		}*/

	/*	public short getDatatypeRangeFoldingThreshhold(int datatype) {
			switch (datatype) {
				case REFLECTIVITY_HIGH:
					return ref_rf_threshold;
				case VELOCITY_HIGH:
					return vel_rf_threshold;

				*//*case SPECTRUM_WIDTH_HIGH:
				return sw_rf_threshold;*//*

				case REFLECTIVITY:
				case VELOCITY_LOW:
				case VELOCITY_HI:
				case SPECTRUM_WIDTH:
					return threshhold;

				*//*	case DIFF_REFLECTIVITY_HIGH:
				return zdrHR_rf_threshold;
			case DIFF_PHASE:
				return phiHR_rf_threshold;
			case CORRELATION_COEFFICIENT:
				return rhoHR_rf_threshold;*//*

				default:
					throw new IllegalArgumentException();
			}
		}*/

	/*	public float getDatatypeScaleFactor(int datatype) {
			switch (datatype) {
				case REFLECTIVITY:
					return 0.5f;
				case VELOCITY_LOW:
					return 1.0f;
				case VELOCITY_HI:
				case SPECTRUM_WIDTH:
					return 0.5f;
				case REFLECTIVITY_HIGH:
					return 1 / reflectHR_scale;
				case VELOCITY_HIGH:
					return 1 / velocityHR_scale;

				*//*	case SPECTRUM_WIDTH_HIGH:
				return 1 / spectrumHR_scale;
			case DIFF_REFLECTIVITY_HIGH:
				return 1.0f / zdrHR_scale;
			case DIFF_PHASE:
				return 1.0f / phiHR_scale;
			case CORRELATION_COEFFICIENT:
				return 1.0f / rhoHR_scale;*//*

				default:
					throw new IllegalArgumentException();
			}
		}*/

		/*public float getDatatypeAddOffset(int datatype) {
			switch (datatype) {
				case REFLECTIVITY:
					return -33.0f;
				case VELOCITY_LOW:
					return -129.0f;
				case VELOCITY_HI:
				case SPECTRUM_WIDTH:
					return -64.5f;
				case REFLECTIVITY_HIGH:
					return reflectHR_addoffset * (-1) / reflectHR_scale;
				case VELOCITY_HIGH:
					return velocityHR_addoffset * (-1) / velocityHR_scale;

				*//*case SPECTRUM_WIDTH_HIGH:
				return spectrumHR_addoffset * (-1) / spectrumHR_scale;
			case DIFF_REFLECTIVITY_HIGH:
				return zdrHR_addoffset * (-1) / zdrHR_scale;
			case DIFF_PHASE:
				return phiHR_addoffset * (-1) / phiHR_scale;
			case CORRELATION_COEFFICIENT:
				return rhoHR_addoffset * (-1) / rhoHR_scale;*//*

				default:
					throw new IllegalArgumentException();
			}
		}*/

	/*	static public String getMessageTypeName(int code) {
			switch (code) {
				case 1:
					return "digital radar data";
				case 2:
					return "RDA status data";
				case 3:
					return "performance/maintainence data";
				case 4:
					return "console message - RDA to RPG";
				case 5:
					return "maintainence log data";
				case 6:
					return "RDA control ocmmands";
				case 7:
					return "volume coverage pattern";
				case 8:
					return "clutter censor zones";
				case 9:
					return "request for data";
				case 10:
					return "console message - RPG to RDA";
				case 11:
					return "loop back test - RDA to RPG";
				case 12:
					return "loop back test - RPG to RDA";
				case 13:
					return "clutter filter bypass map - RDA to RPG";
				case 14:
					return "edited clutter filter bypass map - RDA to RPG";
				case 15:
					return "Notchwidth Map";
				case 18:
					return "RDA Adaptation data";
				case 31:
					return "Digitail Radar Data Generic Format";
				default:
					return "unknown " + code;
			}
		}*/

		/*static public String getRadialStatusName(int code) {
			switch (code) {
				case 0:
					return "start of new elevation";
				case 1:
					return "intermediate radial";
				case 2:
					return "end of elevation";
				case 3:
					return "begin volume scan";
				case 4:
					return "end volume scan";
				default:
					return "unknown " + code;
			}
		}*/

	/*	static public String getVolumeCoveragePatternName(int code) {
			switch (code) {
				case 11:
					return "16 elevation scans every 5 mins";
				case 12:
					return "14 elevation scan every 4.1 mins";
				case 21:
					return "11 elevation scans every 6 mins";
				case 31:
					return "8 elevation scans every 10 mins";
				case 32:
					return "7 elevation scans every 10 mins";
				case 121:
					return "9 elevations, 20 scans every 5 minutes";
				case 211:
					return "14 elevations, 16 scans every 5 mins";
				case 212:
					return "14 elevations, 17 scans every 4 mins";
				case 221:
					return "9 elevations, 11 scans every 5 minutes";
				default:
					return "unknown " + code;
			}
		}*/

	/*	static public java.util.Date getDate(int julianDays, int msecs) {
			long total = ((long) (julianDays - 1)) * 24 * 3600 * 1000 + msecs;
			return new Date(total);
		}*/

		/////////////////////////////////////////////////////////////////////////////////////////////////////////////

		final int recno;        //  record number within the file
		final long message_offset; // offset of start of message

		//boolean hasReflectData, hasDopplerData;
		boolean hasHighResREFData;
		boolean hasHighResVELData;

		/*boolean hasHighResSWData;
		boolean hasHighResZDRData;
		boolean hasHighResPHIData;
		boolean hasHighResRHOData;*/

		// message header
		short message_size = 0;
		byte id_channel = 0;
		public byte message_type = 0;
		short id_sequence = 0;
		short mess_julian_date = 0;
		int mess_msecs = 0;
		short seg_count = 0;
		short seg_number = 0;

		// radar data header
		int data_msecs = 0;
		short data_julian_date = 0;
		short unamb_range = 0;
		int azimuth_ang = 0;
		short radial_num = 0; // radial number within the elevation : starts with one
		short radial_status = 0;
		short elevation_ang = 0;
		short elevation_num = 0;

		short reflect_first_gate = 0; // distance to first reflectivity gate (m)
		short reflect_gate_size = 0; //  reflectivity gate size (m)
		short reflect_gate_count = 0; //  number of reflectivity gates

		short doppler_first_gate = 0; // distance to first reflectivity gate (m)
		short doppler_gate_size = 0; //  reflectivity gate size (m)
		short doppler_gate_count = 0; //  number of reflectivity gates

		short cut = 0;
		float calibration = 0; // system gain calibration constant (db biased)
		short resolution = 0; // dopplar velocity resolution
		short vcp = 0;        // volume coverage pattern

		short nyquist_vel; // nyquist velocity
		short attenuation; // atmospheric attenuation factor
		short threshhold; // threshhold paramter for minimum difference
		short ref_snr_threshold; // reflectivity signal to noise threshhold
		short vel_snr_threshold;

		//short sw_snr_threshold;
		//short zdrHR_snr_threshold;
		//short phiHR_snr_threshold;
		//short rhoHR_snr_threshold;

		short ref_rf_threshold; // reflectivity range folding threshhold
		short vel_rf_threshold;
		//short sw_rf_threshold;
		//short zdrHR_rf_threshold;
		//short phiHR_rf_threshold;
		//short rhoHR_rf_threshold;

		private short reflect_offset; // reflectivity data pointer (byte number from start of message)
		private short velocity_offset; // velocity data pointer (byte number from start of message)
		private short spectWidth_offset; // spectrum-width data pointer (byte number from start of message)
		// new addition for message type 31
		short rlength = 0;

		// FIXME
		//String id;
		float azimuth;
		byte compressIdx;
		byte sp;
		byte ars;
		byte rs;
		float elevation;
		byte rsbs;
		byte aim;
		short dcount;

		int dbp1;
		int dbp2;
		int dbp3;
		int dbp4;
		int dbp5;
		int dbp6;
		int dbp7;
		int dbp8;
		int dbp9;
		// FIXME
		public short reflectHR_gate_count = 0;
		short velocityHR_gate_count = 0;

		//short spectrumHR_gate_count = 0;

		float reflectHR_scale = 0;
		float velocityHR_scale = 0;

		//float spectrumHR_scale = 0;

		//float zdrHR_scale = 0;
		//float phiHR_scale = 0;
		//float rhoHR_scale = 0;

		float reflectHR_addoffset = 0;
		float velocityHR_addoffset = 0;

		/*		float spectrumHR_addoffset = 0;
		float zdrHR_addoffset = 0;
		float phiHR_addoffset = 0;
		float rhoHR_addoffset = 0;*/

		short reflectHR_offset = 0;
		short velocityHR_offset = 0;

		/*short spectrumHR_offset = 0;
		short zdrHR_offset = 0;
		short phiHR_offset = 0;
		short rhoHR_offset = 0;
		short zdrHR_gate_count = 0;
		short phiHR_gate_count = 0;
		short rhoHR_gate_count = 0;*/

		short reflectHR_gate_size = 0;
		short velocityHR_gate_size = 0;

		//short spectrumHR_gate_size = 0;
		//short zdrHR_gate_size = 0;
		//short phiHR_gate_size = 0;
		//short rhoHR_gate_size = 0;

		short reflectHR_first_gate = 0;
		short velocityHR_first_gate = 0;

		//short spectrumHR_first_gate = 0;
		//short zdrHR_first_gate = 0;
		//short phiHR_first_gate = 0;
		//short rhoHR_first_gate = 0;


		public static UtilityNexradLevel2Record factory(UCARRandomAccessFile din, int record, long message_offset31) throws IOException {
			long offset = record * RADAR_DATA_SIZE + FILE_HEADER_SIZE + message_offset31;

			//UtilityLog.d("wx","LEVEL 2 record makrer" + Long.toString(offset) + " " + Long.toString(din.length()));


			if (offset >= din.length())
				return null;
			else
				return new UtilityNexradLevel2Record(din, record, message_offset31);
		}

		public UtilityNexradLevel2Record(UCARRandomAccessFile din, int record, long message_offset31) throws IOException {

			this.recno = record;
			message_offset = record * RADAR_DATA_SIZE + FILE_HEADER_SIZE + message_offset31;

			//UtilityLog.d("wx","LEVEL 2 record makrer");

			din.seek(message_offset);
			din.skipBytes(CTM_HEADER_SIZE);

			// Message Header
			// int size = din.readInt();
			message_size = din.readShort(); // size in "halfwords" = 2 bytes
			id_channel = din.readByte(); // channel id
			message_type = din.readByte();
			id_sequence = din.readShort();
			mess_julian_date = din.readShort(); // from 1/1/70; prob "message generation time"
			mess_msecs = din.readInt();   // message generation time
			seg_count = din.readShort(); // number of message segments
			seg_number = din.readShort(); // this segment

			// if (message_type != 1 ) return;
			if (message_type == 1) {
				// data header
				data_msecs = din.readInt();   // collection time for this radial, msecs since midnight
				data_julian_date = din.readShort(); // prob "collection time"
				unamb_range = din.readShort(); // unambiguous range
				azimuth_ang = din.readUnsignedShort(); // LOOK why unsigned ??
				radial_num = din.readShort(); // radial number within the elevation
				radial_status = din.readShort();
				elevation_ang = din.readShort();
				elevation_num = din.readShort(); // RDA elevation number
				reflect_first_gate = din.readShort(); // range to first gate of reflectivity (m) may be negetive
				doppler_first_gate = din.readShort(); // range to first gate of dopplar (m) may be negetive
				reflect_gate_size = din.readShort(); // reflectivity data gate size (m)
				doppler_gate_size = din.readShort(); // dopplar data gate size (m)
				reflect_gate_count = din.readShort(); // number of reflectivity gates
				doppler_gate_count = din.readShort(); // number of velocity or spectrum width gates
				cut = din.readShort(); // sector number within cut
				calibration = din.readFloat(); // system gain calibration constant (db biased)
				reflect_offset = din.readShort(); // reflectivity data pointer (byte number from start of message)
				velocity_offset = din.readShort(); // velocity data pointer (byte number from start of message)
				spectWidth_offset = din.readShort(); // spectrum-width data pointer (byte number from start of message)
				resolution = din.readShort(); // dopplar velocity resolution
				vcp = din.readShort(); // volume coverage pattern

				din.skipBytes(14);

				nyquist_vel = din.readShort(); // nyquist velocity
				attenuation = din.readShort(); // atmospheric attenuation factor
				threshhold = din.readShort(); // threshhold paramter for minimum difference

				//hasReflectData = (reflect_gate_count > 0);
				//hasDopplerData = (doppler_gate_count > 0);

			} else if (message_type == 31) {
				// data header

				// FIXME

				//  id = din.readString(4);

				//Byte ba = din.readByte();
				//ba = din.readByte();
				//ba = din.readByte();
				//ba = din.readByte();

				din.skipBytes(4);

				data_msecs = din.readInt();   // collection time for this radial, msecs since midnight
				data_julian_date = din.readShort(); // prob "collection time"
				radial_num = din.readShort(); // radial number within the elevation
				azimuth = din.readFloat(); // LOOK why unsigned ??
				compressIdx = din.readByte();
				sp = din.readByte();
				rlength = din.readShort();
				ars = din.readByte();
				rs = din.readByte();
				elevation_num = din.readByte(); // RDA elevation number
				cut = din.readByte(); // sector number within cut
				elevation = din.readFloat();
				rsbs = din.readByte();
				aim = din.readByte();
				dcount = din.readShort();

				dbp1 = din.readInt();
				dbp2 = din.readInt();
				dbp3 = din.readInt();
				dbp4 = din.readInt();
				dbp5 = din.readInt();
				dbp6 = din.readInt();
				dbp7 = din.readInt();
				dbp8 = din.readInt();
				dbp9 = din.readInt();

				vcp = getDataBlockValue(din, (short) dbp1, 40);
				int dbpp4 = 0;
				int dbpp5 = 0;
				//int dbpp6 = 0;
				//int dbpp7 = 0;
				//int dbpp8 = 0;
				//int dbpp9 = 0;

				if (dbp4 > 0) {
					String tname = getDataBlockStringValue(din, (short) dbp4, 1, 3);
					if (tname.startsWith("REF")) {
						hasHighResREFData = true;
						dbpp4 = dbp4;
					} else if (tname.startsWith("VEL")) {
						hasHighResVELData = true;
						dbpp5 = dbp4;
					} /*else if (tname.startsWith("SW")) {
						hasHighResSWData = true;
						dbpp6 = dbp4;
					} else if (tname.startsWith("ZDR")) {
						hasHighResZDRData = true;
						dbpp7 = dbp4;
					} else if (tname.startsWith("PHI")) {
						hasHighResPHIData = true;
						dbpp8 = dbp4;
					} else if (tname.startsWith("RHO")) {
						hasHighResRHOData = true;
						dbpp9 = dbp4;
					} else {
						// logger.warn("Missing radial product dbp4={} tname={}", dbp4, tname);
					}*/

				}
				if (dbp5 > 0) {

					String tname = getDataBlockStringValue(din, (short) dbp5, 1, 3);
					if (tname.startsWith("REF")) {
						hasHighResREFData = true;
						dbpp4 = dbp5;
					} else if (tname.startsWith("VEL")) {
						hasHighResVELData = true;
						dbpp5 = dbp5;
					} /*else if (tname.startsWith("SW")) {
						hasHighResSWData = true;
						dbpp6 = dbp5;
					} else if (tname.startsWith("ZDR")) {
						hasHighResZDRData = true;
						dbpp7 = dbp5;
					} else if (tname.startsWith("PHI")) {
						hasHighResPHIData = true;
						dbpp8 = dbp5;
					} else if (tname.startsWith("RHO")) {
						hasHighResRHOData = true;
						dbpp9 = dbp5;
					} else {
						//logger.warn("Missing radial product dbp5={} tname={}", dbp5, tname);
					}*/
				}
				if (dbp6 > 0) {

					String tname = getDataBlockStringValue(din, (short) dbp6, 1, 3);
					if (tname.startsWith("REF")) {
						hasHighResREFData = true;
						dbpp4 = dbp6;
					} else if (tname.startsWith("VEL")) {
						hasHighResVELData = true;
						dbpp5 = dbp6;
					} /*else if (tname.startsWith("SW")) {
						hasHighResSWData = true;
						dbpp6 = dbp6;
					} else if (tname.startsWith("ZDR")) {
						hasHighResZDRData = true;
						dbpp7 = dbp6;
					} else if (tname.startsWith("PHI")) {
						hasHighResPHIData = true;
						dbpp8 = dbp6;
					} else if (tname.startsWith("RHO")) {
						hasHighResRHOData = true;
						dbpp9 = dbp6;
					} else {
						//logger.warn("Missing radial product dbp6={} tname={}", dbp6, tname);
					}*/
				}

				if (dbp7 > 0) {

					String tname = getDataBlockStringValue(din, (short) dbp7, 1, 3);
					if (tname.startsWith("REF")) {
						hasHighResREFData = true;
						dbpp4 = dbp7;
					} else if (tname.startsWith("VEL")) {
						hasHighResVELData = true;
						dbpp5 = dbp7;
					} /*else if (tname.startsWith("SW")) {
						hasHighResSWData = true;
						dbpp6 = dbp7;
					} else if (tname.startsWith("ZDR")) {
						hasHighResZDRData = true;
						dbpp7 = dbp7;
					} else if (tname.startsWith("PHI")) {
						hasHighResPHIData = true;
						dbpp8 = dbp7;
					} else if (tname.startsWith("RHO")) {
						hasHighResRHOData = true;
						dbpp9 = dbp7;
					} else {
						// logger.warn("Missing radial product dbp7={} tname={}", dbp7, tname);
					}*/
				}

				if (dbp8 > 0) {

					String tname = getDataBlockStringValue(din, (short) dbp8, 1, 3);
					if (tname.startsWith("REF")) {
						hasHighResREFData = true;
						dbpp4 = dbp8;
					} else if (tname.startsWith("VEL")) {
						hasHighResVELData = true;
						dbpp5 = dbp8;
					} /*else if (tname.startsWith("SW")) {
						hasHighResSWData = true;
						dbpp6 = dbp8;
					} else if (tname.startsWith("ZDR")) {
						hasHighResZDRData = true;
						dbpp7 = dbp8;
					} else if (tname.startsWith("PHI")) {
						hasHighResPHIData = true;
						dbpp8 = dbp8;
					} else if (tname.startsWith("RHO")) {
						hasHighResRHOData = true;
						dbpp9 = dbp8;
					} else {
						// logger.warn("Missing radial product dbp8={} tname={}", dbp8, tname);
					}*/
				}

				if (dbp9 > 0) {

					String tname = getDataBlockStringValue(din, (short) dbp9, 1, 3);
					if (tname.startsWith("REF")) {
						hasHighResREFData = true;
						dbpp4 = dbp9;
					} else if (tname.startsWith("VEL")) {
						hasHighResVELData = true;
						dbpp5 = dbp9;
					} /*else if (tname.startsWith("SW")) {
						hasHighResSWData = true;
						dbpp6 = dbp9;
					} else if (tname.startsWith("ZDR")) {
						hasHighResZDRData = true;
						dbpp7 = dbp9;
					} else if (tname.startsWith("PHI")) {
						hasHighResPHIData = true;
						dbpp8 = dbp9;
					} else if (tname.startsWith("RHO")) {
						hasHighResRHOData = true;
						dbpp9 = dbp9;
					} else {
						// logger.warn("Missing radial product dbp9={} tname={}", dbp9, tname);
					}*/
				}
				//hasHighResREFData = (dbp4 > 0);

				if (hasHighResREFData) {
					reflectHR_gate_count = getDataBlockValue(din, (short) dbpp4, 8);
					reflectHR_first_gate = getDataBlockValue(din, (short) dbpp4, 10);
					reflectHR_gate_size = getDataBlockValue(din, (short) dbpp4, 12);
					ref_rf_threshold = getDataBlockValue(din, (short) dbpp4, 14);
					ref_snr_threshold = getDataBlockValue(din, (short) dbpp4, 16);
					reflectHR_scale = getDataBlockValue1(din, (short) dbpp4, 20);
					reflectHR_addoffset = getDataBlockValue1(din, (short) dbpp4, 24);
					reflectHR_offset = (short) (dbpp4 + 28);

				}
				//hasHighResVELData = (dbp5 > 0);
				if (hasHighResVELData) {
					velocityHR_gate_count = getDataBlockValue(din, (short) dbpp5, 8);
					velocityHR_first_gate = getDataBlockValue(din, (short) dbpp5, 10);
					velocityHR_gate_size = getDataBlockValue(din, (short) dbpp5, 12);
					vel_rf_threshold = getDataBlockValue(din, (short) dbpp5, 14);
					vel_snr_threshold = getDataBlockValue(din, (short) dbpp5, 16);
					velocityHR_scale = getDataBlockValue1(din, (short) dbpp5, 20);
					velocityHR_addoffset = getDataBlockValue1(din, (short) dbpp5, 24);
					velocityHR_offset = (short) (dbpp5 + 28);

				}


			}

		}

		/*	public void dumpMessage(PrintStream out) {
			out.println(recno + " ---------------------");
			out.println(" message type = " + getMessageTypeName(message_type) + " (" + message_type + ")");
			out.println(" message size = " + message_size + " segment=" + seg_number + "/" + seg_count);
		}

		public void dump(PrintStream out) {
			out.println(recno + " ------------------------------------------" + message_offset);
			out.println(" message type = " + getMessageTypeName(message_type));
			out.println(" data date = " + data_julian_date + " : " + data_msecs);
			out.println(" elevation = " + getElevation() + " (" + elevation_num + ")");
			out.println(" azimuth = " + getAzimuth());
			out.println(" radial = " + radial_num + " status= " + getRadialStatusName(radial_status) +
					" ratio = " + getAzimuth() / radial_num);
			out.println(" reflectivity first= " + reflect_first_gate + " size= " + reflect_gate_size + " count= " + reflect_gate_count);
			out.println(" doppler first= " + doppler_first_gate + " size= " + doppler_gate_size + " count= " + doppler_gate_count);
			out.println(" offset: reflect= " + reflect_offset + " velocity= " + velocity_offset + " spWidth= " + spectWidth_offset);
			out.println(" pattern = " + vcp + " cut= " + cut);
		}*/

		/*public void dump2(PrintStream out) {
			out.println("recno= " + recno + " massType= " + message_type + " massSize = " + message_size);
		}*/

		/*public boolean checkOk() {
			boolean ok = true;

			if (Float.isNaN(getAzimuth())) {
				//  logger.warn("****" + recno + " HAS bad azimuth value = " + azimuth_ang);
				ok = false;
			}

			if (message_type != 1) return ok;

			if ((seg_count != 1) || (seg_number != 1)) {
				//  logger.warn("*** segment = " + seg_number + "/" + seg_count + who());
			}

			if ((reflect_offset < 0) || (reflect_offset > RADAR_DATA_SIZE)) {
				// logger.warn("****" + recno + " HAS bad reflect offset= " + reflect_offset + who());
				ok = false;
			}

			if ((velocity_offset < 0) || (velocity_offset > RADAR_DATA_SIZE)) {
				//  logger.warn("****" + recno + " HAS bad velocity offset= " + velocity_offset + who());
				ok = false;
			}

			if ((spectWidth_offset < 0) || (spectWidth_offset > RADAR_DATA_SIZE)) {
				//  logger.warn("****" + recno + " HAS bad spwidth offset= " +
				//         spectWidth_offset + who());
				ok = false;
			}

			if ((velocity_offset > 0) && (spectWidth_offset <= 0)) {
				//  logger.warn("****" + recno + " HAS velocity NOT spectWidth!!" + who());
				ok = false;
			}

			if ((velocity_offset <= 0) && (spectWidth_offset > 0)) {
				//  logger.warn("****" + recno + " HAS spectWidth AND NOT velocity!!" + who());
				ok = false;
			}

			if (mess_julian_date != data_julian_date) {
				//  logger.warn("*** message date = " + mess_julian_date + " : " + mess_msecs + who() + "\n" +
				//          " data date = " + data_julian_date + " : " + data_msecs);
				ok = false;
			}

			if (!hasReflectData && !hasDopplerData) {
				//  logger.info("*** no reflect or dopplar = " + who());
			}

			return ok;
		}*/

		/* private String who() {
    return " message(" + recno + " " + message_offset + ")";
  }*/


		/**
		 * Get the azimuth in degrees
		 *
		 * @return azimuth angle in degrees 0 = true north, 90 = east
		 */

		/*public float getAzimuth() {
			if (message_type == 31)
				return azimuth;
			else if (message_type == 1)
				return 180.0f * azimuth_ang / 32768.0f;
			else
				return -1.0f;
		}*/

		/**
		 * Get the elevation angle in degrees
		 *
		 * @return elevation angle in degrees 0 = parellel to pedestal base, 90 = perpendicular
		 */
		/*	public float getElevation() {
			if (message_type == 31)
				return elevation;
			else if (message_type == 1)
				return 180.0f * elevation_ang / 32768.0f;
			else
				return -1.0f;
		}*/


		/**
		 * This method returns the gate size in meters
		 *
		 * @param datatype which type of data : REFLECTIVITY, VELOCITY_HI, VELOCITY_LO, SPECTRUM_WIDTH
		 * @return the gate size in meters
		 */
		/*public int getGateSize(int datatype) {
			switch (datatype) {
			case REFLECTIVITY:
				return ((int) reflect_gate_size);

			case VELOCITY_HI:
			case VELOCITY_LOW:
			case SPECTRUM_WIDTH:
				return ((int) doppler_gate_size);
				//high resolution
			case REFLECTIVITY_HIGH:
				return ((int) reflectHR_gate_size);
			case VELOCITY_HIGH:
				return ((int) velocityHR_gate_size);
			case SPECTRUM_WIDTH_HIGH:
				return ((int) spectrumHR_gate_size);
			case DIFF_REFLECTIVITY_HIGH:
				return ((int) zdrHR_gate_size);
			case DIFF_PHASE:
				return ((int) phiHR_gate_size);
			case CORRELATION_COEFFICIENT:
				return ((int) rhoHR_gate_size);

			}
			return -1;
		}*/

		/**
		 * This method returns the starting gate in meters
		 *
		 * @param datatype which type of data : REFLECTIVITY, VELOCITY_HI, VELOCITY_LO, SPECTRUM_WIDTH
		 * @return the starting gate in meters
		 */
		/*	public int getGateStart(int datatype) {
			switch (datatype) {
			case REFLECTIVITY:
				return ((int) reflect_first_gate);

			case VELOCITY_HI:
			case VELOCITY_LOW:
			case SPECTRUM_WIDTH:
				return ((int) doppler_first_gate);
				//high resolution
			case REFLECTIVITY_HIGH:
				return ((int) reflectHR_first_gate);
			case VELOCITY_HIGH:
				return ((int) velocityHR_first_gate);
			case SPECTRUM_WIDTH_HIGH:
				return ((int) spectrumHR_first_gate);
			case DIFF_REFLECTIVITY_HIGH:
				return ((int) zdrHR_first_gate);
			case DIFF_PHASE:
				return ((int) phiHR_first_gate);
			case CORRELATION_COEFFICIENT:
				return ((int) rhoHR_first_gate);

			}
			return -1;
		}*/

		/**
		 * This method returns the number of gates
		 *
		 * @param datatype which type of data : REFLECTIVITY, VELOCITY_HI, VELOCITY_LO, SPECTRUM_WIDTH
		 * @return the number of gates
		 */
		public int getGateCount(int datatype) {
			switch (datatype) {
				case REFLECTIVITY:
					return ((int) reflect_gate_count);

				case VELOCITY_HI:
				case VELOCITY_LOW:
				case SPECTRUM_WIDTH:
					return ((int) doppler_gate_count);
				// hight resolution
				case REFLECTIVITY_HIGH:
					return ((int) reflectHR_gate_count);
				case VELOCITY_HIGH:
					return ((int) velocityHR_gate_count);

				/*	case SPECTRUM_WIDTH_HIGH:
				return ((int) spectrumHR_gate_count);
			case DIFF_REFLECTIVITY_HIGH:
				return ((int) zdrHR_gate_count);
			case DIFF_PHASE:
				return ((int) phiHR_gate_count);
			case CORRELATION_COEFFICIENT:
				return ((int) rhoHR_gate_count);*/
			}
			return 0;
		}

		private short getDataOffset(int datatype) {
			switch (datatype) {
				case REFLECTIVITY:
					return reflect_offset;
				case VELOCITY_HI:
				case VELOCITY_LOW:
					return velocity_offset;
				case SPECTRUM_WIDTH:
					return spectWidth_offset;
				case REFLECTIVITY_HIGH:
					return reflectHR_offset;
				case VELOCITY_HIGH:
					return velocityHR_offset;

				/*case SPECTRUM_WIDTH_HIGH:
				return spectrumHR_offset;
			case DIFF_REFLECTIVITY_HIGH:
				return zdrHR_offset;
			case DIFF_PHASE:
				return phiHR_offset;
			case CORRELATION_COEFFICIENT:
				return rhoHR_offset;*/

			}
			return Short.MIN_VALUE;
		}

		private short getDataBlockValue(UCARRandomAccessFile raf, short offset, int skip) throws IOException {
			long off = offset + message_offset + MESSAGE_HEADER_SIZE;
			raf.seek(off);
			raf.skipBytes(skip);
			return raf.readShort();
		}

		private String getDataBlockStringValue(UCARRandomAccessFile raf, short offset, int skip, int size) throws IOException {
			long off = offset + message_offset + MESSAGE_HEADER_SIZE;
			raf.seek(off);
			raf.skipBytes(skip);

			byte[] b = new byte[size];
			//Integer i=0;
			for (int i =0; i < size;i++)
			{
				b[i] = raf.readByte();
			}

			//String s = new String(b);
			//return s;

			return new String(b);

			// return raf.readString(size);

		}

		private float getDataBlockValue1(UCARRandomAccessFile raf, short offset, int skip) throws IOException {
			long off = offset + message_offset + MESSAGE_HEADER_SIZE;
			raf.seek(off);
			raf.skipBytes(skip);
			return raf.readFloat();
		}

		/*public java.util.Date getDate() {
			return getDate(data_julian_date, data_msecs);
		}*/




		//moved to JNI ( and brought back )

		public void readData(UCARRandomAccessFile raf, int datatype, ByteBuffer bin_word) throws IOException {

			long offset = message_offset;
			offset += MESSAGE_HEADER_SIZE; // offset is from "start of digital radar data message header"
			offset += getDataOffset(datatype);
			raf.seek(offset);
			//int dataCount = getGateCount(datatype);
			int dataCount = 916;

			int i;
			for ( i=0;i<dataCount;i++)
			{
				bin_word.put((byte)raf.readUnsignedByte());
			}

		}

		/*public short[] convertunsignedByte2Short(byte[] inb) {
			int len = inb.length;
			short[] outs = new short[len];
			int i = 0;
			for (byte b : inb) {
				outs[i++] = convertunsignedByte2Short(b);
			}
			return outs;
		}*/

		/*public short convertunsignedByte2Short(byte b) {
			return (short) ((b < 0) ? (short) b + 256 : (short) b);
		}

		public String toString() {
			return "elev= " + elevation_num + " radial_num = " + radial_num;
		}*/

	}


}


