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

//The following has chunks of code from Level2VolumeScan.java so using the license for that file
//This file is no longer in use but serves as a reference since the code used by OGL has been stripped of uneeded parts.

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Paint.Style;

import joshuatee.wx.JNI;
import joshuatee.wx.MyApplication;
import joshuatee.wx.util.UCARRandomAccessFile;
import joshuatee.wx.util.UtilityMath;

class UtilityNexradL2 {


	/**
	 * Size of the file header, aka title
	 */
	private static final int FILE_HEADER_SIZE = 24;

	private static UtilityNexradLevel2Record first;

	//private static UtilityNexradLevel2Record last;


	private static int vcp = 0; // Volume coverage pattern
	private static int max_radials = 0;
	private static int min_radials = Integer.MAX_VALUE;
	private static int max_radials_hr = 0;
	private static int min_radials_hr = Integer.MAX_VALUE;

	private static final boolean debugScans = false;
	private static final boolean  debugGroups2 = false;
	private static final boolean  debugRadials = false;
	private static final boolean runCheck = false;

	private static final String decomp_fn = "l2.decomp";
	private static ByteBuffer  bin_word = null;
	private static ByteBuffer radial_start_angle = null;


	public static void DecocodeAndPlotNexradL2(Context c, Bitmap bm1,String fn,String prod)
	{
		//UCARRandomAccessFile dis = null;
		//UCARRandomAccessFile dis2=null;

		Canvas canvas = new Canvas(bm1);

		int product_code = 153;
		if (prod.equals("L2VEL"))
			product_code = 154;

		int number_of_radials=720;
		int number_of_range_bins = 916;
		// 1832 vel 1192 vel



		//SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(c);

		String nws_radar_bg_black = MyApplication.preferences!!.getString("NWS_RADAR_BG_BLACK", "");
		int zero_color = -16777216;

		if ( ! nws_radar_bg_black.equals("true"))
			zero_color = -1;

	/*	try {

			//if ( ! already_downloaded )
			//{

			dis = new UCARRandomAccessFile(c.getFileStreamPath(fn).getAbsolutePath(), "r", 1024*256*10); // was c.getFileStreamPath(fn)
			dis.bigEndian = true;
			dis.close();

			ByteBuffer obuff = ByteBuffer.allocateDirect(2400000);
			ByteBuffer ibuff = ByteBuffer.allocateDirect(1200000);

			int ret = JNI.bzipwrapfull(c.getFileStreamPath(fn).getAbsolutePath(),c.getFileStreamPath(decomp_fn).getAbsolutePath(),ibuff,obuff,product_code,false);

			//dis2 = new UCARRandomAccessFile(c.getFileStreamPath(decomp_fn).getAbsolutePath(), "r");



		}	  catch (Exception e) {
			UtilityLog.HandleException(e);
		}*/

		radial_start_angle = ByteBuffer.allocateDirect(720*4);
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

	/*	try
		{
			dis = new UCARRandomAccessFile(c.getFileStreamPath(fn).getAbsolutePath(), "r");
			dis.bigEndian = true;
		}	  catch (Exception e) {
			UtilityLog.HandleException(e);
		}*/

	/*	try {

			dis.seek(0);
			dis.skipBytes(8);
			dis.skipBytes(4);
			dis.skipBytes(8);
			dis.skipBytes(4);
			dis.skipBytes(4);
			dis.skipBytes(2);
			dis2 = uncompress(c,dis, "l2.decomp");
			dis2.bigEndian = true;
			dis.close();
			dis2.seek(FILE_HEADER_SIZE);

		} catch (Exception e) {
			UtilityLog.HandleException(e);
		}*/


/*		List<UtilityNexradLevel2Record> highReflectivity = new ArrayList<>();
		List<UtilityNexradLevel2Record> highVelocity = new ArrayList<>();*/



		/*long message_offset31 = 0;
		int recno = 0;
		UtilityNexradLevel2Record r=null;

		while (true) {


			try{
				r = UtilityNexradLevel2Record.factory(dis2, recno++, message_offset31);
			} catch (Exception e) {
				UtilityLog.HandleException(e);
			}

			if (r == null) break;
			if (r.message_type == 31) {
				message_offset31 = message_offset31 + (r.message_size * 2 + 12 - 2432);
			}

			if (r.message_type != 1 && r.message_type != 31) {
				continue;
			}

			if (vcp == 0) vcp = r.vcp;
			if (first == null) first = r;

			if (runCheck && !r.checkOk()) {
				continue;
			}


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
*/





		// FIXME - needs to be test after comment out below

			/*if (highReflectivity.size() == 0) {
				reflectivityGroups = sortScans("reflect", reflectivity, 600);
				dopplerGroups = sortScans("doppler", doppler, 600);
			}
			if (highReflectivity.size() > 0)
				reflectivityHighResGroups = sortScans("reflect_HR", highReflectivity, 720);
			if (highVelocity.size() > 0)
				velocityHighResGroups = sortScans("velocity_HR", highVelocity, 720);
*/
		// FIXME - needs to be test after comment out above

			/*	if (highSpectrum.size() > 0)
				spectrumHighResGroups = sortScans("spectrum_HR", highSpectrum, 720);
			if (highDiffReflectivity.size() > 0)
				diffReflectHighResGroups = sortScans("diffReflect_HR", highDiffReflectivity, 720);
			if (highDiffPhase.size() > 0)
				diffPhaseHighResGroups = sortScans("diffPhase_HR", highDiffPhase, 720);
			if (highCorreCoefficient.size() > 0)
				coefficientHighResGroups = sortScans("coefficient_HR", highCorreCoefficient, 720);
			 */



		// FIXME start of loading data structures to parse by polygoin drawing
		// should remove this step and integrate steps #1 / #3 for perf reasons
		//Integer number_of_range_bins = 1832;

	/*	boolean velocity_prod = false;


		if ( prod.contains("L2VEL"))
		{
			//number_of_range_bins = 1192;
			velocity_prod = true;
		}*/

		/*int[][] bin_word = new int[number_of_radials][number_of_range_bins];
		float[] radial_start_angle = new float[number_of_radials];
		float[] radial_angle_delta = new float[number_of_radials];
		byte[] b = null;
		int rr;
		int tmp_int;
		int tmp_val;

		if ( ! velocity_prod )
		{
			for (  rr=0; rr<number_of_radials;rr++)
			{
				if (highReflectivity.get(rr).elevation_num==1)
				{

					Float tmp_float = (360.0f-highReflectivity.get(rr).azimuth)+90.0f;
					tmp_int = tmp_float.intValue();

					if ( (tmp_float - tmp_int) > 0.50)
						tmp_float = tmp_int + 0.75f;
					else
						tmp_float = tmp_int + 0.25f;

					radial_start_angle[rr] = tmp_float;
					radial_angle_delta[rr] = 0.5f;

					try{
					b = highReflectivity.get(rr).readData(dis2, REFLECTIVITY_HIGH);
					} catch (Exception e) {
						UtilityLog.HandleException(e);
					}
					int s;
					for ( s=0; s < number_of_range_bins ;s++) {
						tmp_val =  (byte)b[s] & 0xFF;
						bin_word[rr][s] = tmp_val; // was cast to int
					}
				}
			}
		} else{
			for ( rr=0; rr<number_of_radials;rr++)
			{
				if (highVelocity.get(rr).elevation_num==2) // change from 1
				{

					Float tmp_float = (360.0f-highVelocity.get(rr).azimuth)+90.0f;
					tmp_int = tmp_float.intValue();

					if ( (tmp_float - tmp_int) > 0.50)
						tmp_float = tmp_int + 0.75f;
					else
						tmp_float = tmp_int + 0.25f;

					radial_start_angle[rr] = tmp_float;
					radial_angle_delta[rr] = 0.5f;

					//Integer product_int = VELOCITY_HIGH;
					//byte[] b = highVelocity.get(r).readData(dis2, product_int);

					try{
						b = highVelocity.get(rr).readData(dis2, VELOCITY_HIGH);
					} catch (Exception e) {
						UtilityLog.HandleException(e);
					}
					int s;
					for ( s=0; s < number_of_range_bins ;s++) {
						tmp_val =  (byte)b[s] & 0xFF;
						bin_word[rr][s] = tmp_val; // was cast to int
					}
				}
			}
		}


		try{
			dis2.close();
		} catch (Exception e) {
			UtilityLog.HandleException(e);
		}
*/

		//if (debug_flag)
		//	Log.i("wx","data structure creation done");

		int center_x=500;
		int center_y=500;

		float bin_size = WXGLNexrad.GetBinSize(product_code);

		float[] XY1;
		float[] XY2;
		float[] XY3;
		float[] XY4;

		Paint wallpaint = new Paint();
		wallpaint.setStyle(Style.FILL);

		Path wallpath = new Path();

		int g;

		float angle;
		float angle_v;
		int level;
		int level_count;
		float bin_start;
		int bin;

		int num_range_bins = number_of_range_bins;

	//	int data_level_total=256;
	//	int[] color = UtilityNexradColors.GetColorArray(prod);

		ByteBuffer c_r;
		ByteBuffer c_g;
		ByteBuffer c_b;

		if (product_code==153)
		{
			c_r = MyApplication.color_map_94_r;
			c_g = MyApplication.color_map_94_g;
			c_b = MyApplication.color_map_94_b;

		} else
		{
			c_r = MyApplication.color_map_99_r;
			c_g = MyApplication.color_map_99_g;
			c_b = MyApplication.color_map_99_b;
		}

		int tmp_val;
		for (g=0;g<number_of_radials;g++)
		{
			angle   = radial_start_angle.getFloat();
			angle_v = 0.50f;

			bin_word.mark();
			level       = bin_word.get() & 0xFF;
			bin_word.reset();

			level_count = 0;
			bin_start   = bin_size;

			for  (bin=0;bin<num_range_bins;bin++ ) {

				tmp_val = bin_word.get() & 0xFF;
				if ( tmp_val == level  )
				{
					++level_count;
				} else {

					//UtilityLog.d("wx","wxogl share L2" + Integer.toString(level));


					XY1 = UtilityMath.toRect( bin_start, angle );
					XY2 = UtilityMath.toRect( bin_start + ( bin_size * level_count ), angle );
					XY3 = UtilityMath.toRect( bin_start + ( bin_size * level_count ), angle - angle_v );
					XY4 = UtilityMath.toRect( bin_start, angle - angle_v );

					XY1[0] += center_x;
					XY2[0] += center_x;
					XY3[0] += center_x;
					XY4[0] += center_x;
					XY1[1] = ( XY1[1] - center_y ) * -1;
					XY2[1] = ( XY2[1]- center_y ) * -1;
					XY3[1] = ( XY3[1]- center_y ) * -1;
					XY4[1] = ( XY4[1] - center_y ) * -1;


					if ( level == 0 )
					{
						wallpaint.setColor(zero_color);
					}

					else {

						wallpaint.setColor(Color.rgb(c_r.get(level)& 0xFF, c_g.get(level)& 0xFF, c_b.get(level)& 0xFF));

						/*if ( true )
						{
							Double tmp_d = 0.0;
							Double length_arr = new Double(color.length) - 1; // color was padded with extra value in additional slot at end
							if (prod.equals("EET"))
							{
								float level_f = level * 3.657143f; // needs 3.657143
								if ( level_f > 255.0f )
									level_f = 0.0f;
								tmp_d = Double.valueOf(level_f  / (data_level_total/ length_arr ));

							}  else
							{
								tmp_d = Double.valueOf(level  / (data_level_total/ length_arr ));
							}
							int lower_n =  tmp_d.intValue();
							int a = UtilityNexradColors.interpolateColor(color[lower_n],color[lower_n+1],tmp_d -lower_n);
							wallpaint.setColor(a);

						} else
						{
							wallpaint.setColor(color[level]);
						}*/


					}


					//wallpath.reset(); // only needed when reusing this path for a new build
					wallpath.rewind(); // only needed when reusing this path for a new build

					wallpath.moveTo(XY1[0], XY1[1]);
					wallpath.lineTo(XY2[0], XY2[1]);
					wallpath.lineTo(XY3[0], XY3[1]);
					wallpath.lineTo(XY4[0], XY4[1]);
					wallpath.lineTo(XY1[0], XY1[1]);

					canvas.drawPath(wallpath, wallpaint);

					// #Reset the level information
					//level       = bin_word[g][bin];

					level = tmp_val;
					bin_start   = bin * bin_size;
					level_count = 1;
				}
			}
		}
		/*} catch (Exception e) {
			UtilityLog.HandleException(e);
		}*/

		//UtilityLog.d("wx","made it 2");


	}

	private static List<List<UtilityNexradLevel2Record>> sortScans(String name, List<UtilityNexradLevel2Record> scans, int siz) {

		// now group by elevation_num
		Map<Short, List<UtilityNexradLevel2Record>> groupHash = new HashMap<>(siz);
		for (UtilityNexradLevel2Record record : scans) {
			List<UtilityNexradLevel2Record> group = groupHash.get(record.elevation_num);
			if (null == group) {
				group = new ArrayList<>();
				groupHash.put(record.elevation_num, group);
			}
			group.add(record);
		}

		// sort the groups by elevation_num
		List<List<UtilityNexradLevel2Record>> groups = new ArrayList<>(groupHash.values());
		Collections.sort(groups, new GroupComparator());

		// use the maximum radials
		for (int i = 0; i < groups.size(); i++) {
			ArrayList group = (ArrayList) groups.get(i);
			UtilityNexradLevel2Record r =(UtilityNexradLevel2Record) group.get(0);
			if(runCheck) testScan(name, group);
			if(r.getGateCount(UtilityNexradLevel2Record.REFLECTIVITY_HIGH) > 500 || r.getGateCount(UtilityNexradLevel2Record.VELOCITY_HIGH) > 1000) {
				if(group.size() <= 360) {
					max_radials = Math.max(max_radials, group.size());
					min_radials = Math.min(min_radials, group.size());
				} else {
					max_radials_hr = Math.max(max_radials_hr, group.size());
					min_radials_hr = Math.min(min_radials_hr, group.size());
				}
			}
			else {
				max_radials = Math.max(max_radials, group.size());
				min_radials = Math.min(min_radials, group.size());
			}
		}

		if (debugRadials) {
			System.out.println(name + " min_radials= " + min_radials + " max_radials= " + max_radials);
			for (int i = 0; i < groups.size(); i++) {
				ArrayList group = (ArrayList) groups.get(i);
				UtilityNexradLevel2Record lastr = (UtilityNexradLevel2Record) group.get(0);
				for (int j = 1; j < group.size(); j++) {
					UtilityNexradLevel2Record r = (UtilityNexradLevel2Record) group.get(j);
					if (r.data_msecs < lastr.data_msecs)
						System.out.println(" out of order " + j);
					lastr = r;
				}
			}
		}

		testVariable(name, groups);
		if (debugScans) System.out.println("-----------------------------");

		return groups;
	}

	// do we have same characteristics for all groups in a variable?
	private static boolean testVariable(String name, List scans) {
		int datatype = name.equals("reflect") ? UtilityNexradLevel2Record.REFLECTIVITY : UtilityNexradLevel2Record.VELOCITY_HI;
		if (scans.size() == 0) {
			//  log.warn(" No data for = " + name);
			return false;
		}

		boolean ok = true;
		List firstScan = (List) scans.get(0);
		UtilityNexradLevel2Record firstRecord = (UtilityNexradLevel2Record) firstScan.get(0);

		//dopplarResolution = firstRecord.resolution;

		if (debugGroups2)
			System.out.println("Group " + UtilityNexradLevel2Record.getDatatypeName(datatype) + " ngates = " + firstRecord.getGateCount(datatype) +
					" start = " + firstRecord.getGateStart(datatype) + " size = " + firstRecord.getGateSize(datatype));

		for (int i = 1; i < scans.size(); i++) {
			List scan = (List) scans.get(i);
			UtilityNexradLevel2Record record = (UtilityNexradLevel2Record) scan.get(0);

			if ((datatype == UtilityNexradLevel2Record.VELOCITY_HI) && (record.resolution != firstRecord.resolution)) { // do all velocity resolutions match ??
				//  log.warn(name + " scan " + i + " diff resolutions = " + record.resolution + ", " + firstRecord.resolution +
				//          " elev= " + record.elevation_num + " " + record.getElevation());
				ok = false;

				//hasDifferentDopplarResolutions = true;
			}

			if (record.getGateSize(datatype) != firstRecord.getGateSize(datatype)) {
				//   log.warn(name + " scan " + i + " diff gates size = " + record.getGateSize(datatype) + " " + firstRecord.getGateSize(datatype) +
				//           " elev= " + record.elevation_num + " " + record.getElevation());
				ok = false;

			} else if (debugGroups2)
				System.out.println(" ok gates size elev= " + record.elevation_num + " " + record.getElevation());

			if (record.getGateStart(datatype) != firstRecord.getGateStart(datatype)) {
				//    log.warn(name + " scan " + i + " diff gates start = " + record.getGateStart(datatype) + " " + firstRecord.getGateStart(datatype) +
				//            " elev= " + record.elevation_num + " " + record.getElevation());
				ok = false;

			} else if (debugGroups2)
				System.out.println(" ok gates start elev= " + record.elevation_num + " " + record.getElevation());


			//if (record.message_type == 31) {

			//hasHighResolutionData = true;

			//each data type

				/*if (record.hasHighResREFData)
					hasHighResolutionREF = true;
				if (record.hasHighResVELData)
					hasHighResolutionVEL = true;
				if (record.hasHighResSWData)
					hasHighResolutionSW = true;
				if (record.hasHighResZDRData)
					hasHighResolutionZDR = true;
				if (record.hasHighResPHIData)
					hasHighResolutionPHI = true;
				if (record.hasHighResRHOData)
					hasHighResolutionRHO = true;*/

			//}


		}

		return ok;
	}

	// do we have same characteristics for all records in a scan?
	private static final int MAX_RADIAL = 721;
	private static final int[] radial = new int[MAX_RADIAL];

	private static boolean testScan(String name, ArrayList group) {
		int datatype = name.equals("reflect") ? UtilityNexradLevel2Record.REFLECTIVITY : UtilityNexradLevel2Record.VELOCITY_HI;
		UtilityNexradLevel2Record first = (UtilityNexradLevel2Record) group.get(0);

		int n = group.size();
		if (debugScans) {
			boolean hasBoth = first.hasDopplerData && first.hasReflectData;
			System.out.println(name + " " + first + " has " + n + " radials resolution= " + first.resolution + " has both = " + hasBoth);
		}

		boolean ok = true;

		//double sum = 0.0;
		//double sum2 = 0.0;

		for (int i = 0; i < MAX_RADIAL; i++)
			radial[i] = 0;

		for (int i = 0; i < group.size(); i++) {
			UtilityNexradLevel2Record r = (UtilityNexradLevel2Record) group.get(i);

			/* this appears to be common - seems to be ok, we put missing values in
	      if (r.getGateCount(datatype) != first.getGateCount(datatype)) {
	        log.error(raf.getLocation()+" different number of gates ("+r.getGateCount(datatype)+
	                "!="+first.getGateCount(datatype)+") in record "+name+ " "+r);
	        ok = false;
	      } */

			if (r.getGateSize(datatype) != first.getGateSize(datatype)) {
				// log.warn(raf.getLocation() + " different gate size (" + r.getGateSize(datatype) + ") in record " + name + " " + r);
				ok = false;
			}
			if (r.getGateStart(datatype) != first.getGateStart(datatype)) {
				//  log.warn(raf.getLocation() + " different gate start (" + r.getGateStart(datatype) + ") in record " + name + " " + r);
				ok = false;
			}
			if (r.resolution != first.resolution) {
				//  log.warn(raf.getLocation() + " different resolution (" + r.resolution + ") in record " + name + " " + r);
				ok = false;
			}

			if ((r.radial_num < 0) || (r.radial_num >= MAX_RADIAL)) {
				//  log.info(raf.getLocation() + " radial out of range= " + r.radial_num + " in record " + name + " " + r);
				continue;
			}
			if (radial[r.radial_num] > 0) {
				//  log.warn(raf.getLocation() + " duplicate radial = " + r.radial_num + " in record " + name + " " + r);
				ok = false;
			}
			radial[r.radial_num] = r.recno + 1;

			//sum += r.getElevation();
			//sum2 += r.getElevation() * r.getElevation();

			// System.out.println("  elev="+r.getElevation()+" azi="+r.getAzimuth());
		}

		for (int i = 1; i < radial.length; i++) {
			if (0 == radial[i]) {
				if (n != (i - 1)) {
					//log.warn(" missing radial(s)");
					ok = false;
				}
				break;
			}
		}

		/* double avg = sum / n;
	    double sd = Math.sqrt((n * sum2 - sum * sum) / (n * (n - 1)));
	    System.out.println(" avg elev="+avg+" std.dev="+sd); */

		return ok;
	}

	private static class GroupComparator implements
			Comparator<List<UtilityNexradLevel2Record>> {

		public int compare(List<UtilityNexradLevel2Record> group1, List<UtilityNexradLevel2Record> group2) {
			UtilityNexradLevel2Record record1 = group1.get(0);
			UtilityNexradLevel2Record record2 = group2.get(0);

			//if (record1.elevation_num != record2.elevation_num)
			return record1.elevation_num - record2.elevation_num;
			//return record1.cut - record2.cut;
		}
	}

	/**
	 * Write equivilent uncompressed version of the file.
	 *
	 * @param inputRaf  file to uncompress
	 * @param ufilename write to this file
	 * @return raf of uncompressed file
	 * @throws IOException on read error
	 */
	private static UCARRandomAccessFile uncompress(Context c, UCARRandomAccessFile inputRaf, String ufilename) throws IOException {

		// FIXME


		// FIXME
		//RandomAccessFile outputRaf = new RandomAccessFile(c.getFilesDir().getPath() + "/"  + ufilename, "rw",1024);


		UCARRandomAccessFile outputRaf = new UCARRandomAccessFile(new File(c.getFilesDir(),ufilename).getAbsolutePath(), "rw");
		//RandomAccessFile outputRaf = new RandomAccessFile(ufilename, "rw");


		//Log.i("wx", "open rw file in decomp");

		//FileLock lock = null;

		/*  while (true) { // loop waiting for the lock
	      try {
	        lock = outputRaf.getRandomAccessFile().getChannel().lock(0, 1, false);
	        break;

	      } catch (OverlappingFileLockException oe) { // not sure why lock() doesnt block
	        try {
	          Thread.sleep(100); // msecs
	        } catch (InterruptedException e1) {
	        }
	      } catch (IOException e) {
	          outputRaf.close();
	          throw e;
	      }
	    }*/

		try {
			inputRaf.seek(0);
			byte[] header = new byte[UtilityNexradLevel2Record.FILE_HEADER_SIZE];
			int bytesRead = inputRaf.read(header);
			if (bytesRead != header.length)
			{
				throw new IOException("Error reading NEXRAD2 header -- got " +
						bytesRead + " rather than" + header.length);
			}
			outputRaf.write(header);

			boolean eof = false;
			int numCompBytes;
			//byte[] ubuff = new byte[40000];
			//byte[] obuff = new byte[40000];

			//CBZip2InputStream cbzip2 = new CBZip2InputStream();
			//Long byte_cnt = (long) 0;
			while (!eof) {
				try {
					numCompBytes = inputRaf.readInt();
					//byte_cnt++;
					if (numCompBytes == -1) {
						//  if (log.isDebugEnabled())
						//Log.i("wx","  done: numCompBytes=-1 ");
						break;
					}
				} catch (EOFException ee) {
					//Log.i("wx","got EOFException");
					break; // assume this is ok
				}

				// if (log.isDebugEnabled()) {
				//     log.debug("reading compressed bytes " + numCompBytes + " input starts at " + inputRaf.getFilePointer() + "; output starts at " + outputRaf.getFilePointer());



				//Log.i("wx","decomp bytes " + Long.toString(byte_cnt));


				/*
				 * For some stupid reason, the last block seems to
				 * have the number of bytes negated.  So, we just
				 * assume that any negative number (other than -1)
				 * is the last block and go on our merry little way.
				 */
				if (numCompBytes < 0) {
					// if (log.isDebugEnabled())
					//Log.i("wx","last block?" + Integer.toString(numCompBytes));
					numCompBytes = -numCompBytes;
					eof = true;
				}
				byte[] buf = new byte[numCompBytes];
				inputRaf.readFully(buf);
				ByteArrayInputStream bis = new ByteArrayInputStream(buf, 2,
						numCompBytes - 2);



				//CBZip2InputStream cbzip2 = new CBZip2InputStream(bis);
				//cbzip2.setStream(bis);

				//int total = 0;
				//int nread;



				/*	try {
					while ((nread = cbzip2.read(ubuff)) != -1) {
						if (total + nread > obuff.length) {
							byte[] temp = obuff;
							obuff = new byte[temp.length * 2];
							System.arraycopy(temp, 0, obuff, 0, temp.length);
						}
						System.arraycopy(ubuff, 0, obuff, total, nread);
						total += nread;
					}
					if (obuff.length >= 0) outputRaf.write(obuff, 0, total);
				} catch (BZip2ReadException ioe) {
					Log.i("wx","Nexrad2IOSP.uncompress ");
				}*/



				//float nrecords = (float) (total / 2432.0);

				//Log.i("wx","  unpacked " + Integer.toString(total) + " num bytes " + 
				//		Float.toString(nrecords) + " records; ouput ends at " + Long.toString(outputRaf.getFilePointer()));



			}
			//cbzip2.close();

			// outputRaf.flush();
		} catch (IOException e) {
			//  if (outputRaf != null) outputRaf.close();

			// dont leave bad files around
			// File ufile = new File(ufilename);
			// if (ufile.exists()) {
			//    if (!ufile.delete())
			// log.warn("failed to delete uncompressed file (IOException)" + ufilename);
			// }

			// throw e;
		} /*finally {
	      try {
	          if (lock != null) lock.release();
	      } catch (IOException e) {
	          if (outputRaf != null) outputRaf.close();
	          throw e;
	      }*/



		return outputRaf;
	}

}
