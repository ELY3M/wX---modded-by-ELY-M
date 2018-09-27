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

package joshuatee.wx.radar;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Calendar;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Paint.Style;
import androidx.core.content.ContextCompat;

import joshuatee.wx.MyApplication;
import joshuatee.wx.R;
import joshuatee.wx.util.UtilityLog;
import joshuatee.wx.util.UtilityMath;

class UtilityNexradRadial4Bit {

	// big thanks to gottipati, harpchad
	// http://sourceforge.net/projects/nexrad-perl/files/nexrad-perl/
	// below is mostly a java port of this code

	public static void DecocodeAndPlotNexrad(Context c,Bitmap bm1, String fn, String prod )
	{
		Canvas canvas = new Canvas(bm1);
		String nws_radar_bg_black = MyApplication.preferences!!.getString("NWS_RADAR_BG_BLACK", "");
		int zero_color = ContextCompat.getColor(c, R.color.black);

		if ( ! nws_radar_bg_black.equals("true"))
			zero_color = ContextCompat.getColor(c,R.color.white);

		boolean is_velocity = false;
		if ( prod.contains("S") ||prod.contains("V")||prod.contains("U"))
			is_velocity = true;

		DataInputStream dis = null;
		try
		{
			FileInputStream fis = c.openFileInput(fn);
			dis = new DataInputStream(new BufferedInputStream(fis));

		}	  catch (Exception e) {
			UtilityLog.HandleException(e);
		}

		try {

			if (dis!=null) {
			/*int init_read = 30;
			byte[] by = new byte[init_read]; // 21 and 9 -- 50 for digital?
			int t = dis.read(by);
			short	message_code      = (short) dis.readUnsignedShort();
			short    date_of_message  = (short) dis.readUnsignedShort();
			Integer   time_of_message  = dis.readInt();
			Integer   length_of_message  = dis.readInt();
			short    source_id         = (short) dis.readUnsignedShort();
			short   destination_id   = (short) dis.readUnsignedShort();
			short  number_of_blocks = (short) dis.readUnsignedShort();
			short  header2 = (short) dis.readUnsignedShort();*/
				dis.skipBytes(50);

				double latitude_of_radar = dis.readInt() / 1000.0;
				double longitude_of_radar = dis.readInt() / 1000.0;
				short height_of_radar = (short) dis.readUnsignedShort();
				short product_code = (short) dis.readUnsignedShort();
				short operational_mode = (short) dis.readUnsignedShort();

			/*short        volume_scan_pattern =  (short) dis.readUnsignedShort();
			short        sequence_number     = (short) dis.readUnsignedShort();
			short        volume_scan_number  = (short) dis.readUnsignedShort();*/
				dis.skipBytes(6);

				short volume_scan_date = (short) dis.readUnsignedShort();
				int volume_scan_time = dis.readInt();
				long sec = (long) (((volume_scan_date - 1) * 60 * 60 * 24) + (volume_scan_time)); // removed *1000
				long milli = sec * 1000;
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(milli);
				java.util.Date d = cal.getTime();
				//short        product_generation_date = (short) dis.readUnsignedShort();
				dis.skipBytes(2);
				//int        product_generation_time    = dis.readInt();
				dis.skipBytes(4);

				String newline = System.getProperty("line.separator");
				String radar_info = d.toString() + newline +
						"Radar Mode: " + Integer.toString(operational_mode) + newline +
						"Product Code: " + Integer.toString(product_code) + newline +
						"Radar height: " + Integer.toString(height_of_radar) + newline +
						"Radar Lat: " + Double.toString(latitude_of_radar) + newline +
						"Radar Lon: " + Double.toString(longitude_of_radar) + newline;

				MyApplication.editor!!.putString("WX_RADAR_CURRENT_INFO", radar_info);
				MyApplication.editor!!.apply();

		/*	short  p1                        = (short) dis.readUnsignedShort();
			short        p2                        = (short) dis.readUnsignedShort();
			short        elevation_number         = (short) dis.readUnsignedShort();
			short        p3                        = (short) dis.readUnsignedShort();
			short        data_threshold1           = (short) dis.readUnsignedShort();
			short        data_threshold2          = (short) dis.readUnsignedShort();
			short        data_threshold3          = (short) dis.readUnsignedShort();
			short        data_threshold4           = (short) dis.readUnsignedShort();
			short        data_threshold5           = (short) dis.readUnsignedShort();
			short        data_threshold6          = (short) dis.readUnsignedShort();
			short        data_threshold7           = (short) dis.readUnsignedShort();
			short        data_threshold8           = (short) dis.readUnsignedShort();
			short        data_threshold9           = (short) dis.readUnsignedShort();
			short        data_threshold10          = (short) dis.readUnsignedShort();
			short        data_threshold11          = (short) dis.readUnsignedShort();
			short        data_threshold12           = (short) dis.readUnsignedShort();
			short        data_threshold13         = (short) dis.readUnsignedShort();
			short        data_threshold14          = (short) dis.readUnsignedShort();
			short        data_threshold15          = (short) dis.readUnsignedShort();
			short        data_threshold16           = (short) dis.readUnsignedShort();
			short        p4                        = (short) dis.readUnsignedShort();
			short        p5                        = (short) dis.readUnsignedShort();
			short        p6                        = (short) dis.readUnsignedShort();
			short        p7                        = (short) dis.readUnsignedShort();
			short        p8                        = (short) dis.readUnsignedShort();
			short        p9                        = (short) dis.readUnsignedShort();
			short        p10                        = (short) dis.readUnsignedShort();
			short        number_of_maps             = (short) dis.readUnsignedShort();
			int  offset_to_symbology_block =  dis.readInt() ;
			int  offset_to_graphic_block = dis.readInt() ;
			int  offset_to_tabular_block = dis.readInt() ;*/
				dis.skipBytes(68);

				//   #Product Symbology Block
			/*short  h1   = (short) dis.readUnsignedShort();
			short  h1b   = (short) dis.readUnsignedShort();
			int  length_of_block   = dis.readInt() ;
			short  number_of_layers   = (short) dis.readUnsignedShort();
			short  h2   = (short) dis.readUnsignedShort();
			int  smbology_length  = dis.readInt() ;
			// RDP
			int  header_before_radial  = dis.readUnsignedShort() ;
			int  index_of_first_range_bin  = dis.readUnsignedShort();*/
				dis.skipBytes(20);

				int number_of_range_bins = dis.readUnsignedShort();

		/*	int   i_center_of_sweep   = dis.readShort() ;
			int  j_center_of_sweep   = dis.readShort() ;
			int  scale_factor  = dis.readUnsignedShort() ;*/
				dis.skipBytes(6);

				int number_of_radials = dis.readUnsignedShort();

				int r;
				int[] number_of_rle_halfwords = new int[number_of_radials];
				float[] radial_start_angle = new float[number_of_radials];
				float[] radial_angle_delta = new float[number_of_radials];
				int[][] bin_word = new int[number_of_radials][number_of_range_bins];
				int tn_mod10;

				for (r = 0; r < number_of_radials; r++) {
					number_of_rle_halfwords[r] = dis.readUnsignedShort();
					int tn = dis.readUnsignedShort();
					if (tn % 2 == 1)
						tn++;

					tn_mod10 = tn % 10;
					if (tn_mod10 > 0 && tn_mod10 < 5)
						tn = tn - tn_mod10;
					else if (tn_mod10 > 6)
						tn = tn - tn_mod10 + 10;

					radial_start_angle[r] = (float) (450 - (tn / 10));
					radial_angle_delta[r] = dis.readUnsignedShort();
					radial_angle_delta[r] = 1.0f;

					int bin_count = 0; // was 1
					int s;

					for (s = 0; s < number_of_rle_halfwords[r] * 2; s++) {
						// old 4 bit
						int bin = dis.readUnsignedByte();
						int num_of_bins = (bin >> 4);

						for (int u = 0; u < num_of_bins; u++) {
							bin_word[r][bin_count] = bin % 16;
							++bin_count;
						}
					}
				}
				dis.close();


				int[] graph_color = new int[16];
				graph_color[0]  = Color.parseColor("#000000");
				graph_color[1]  = Color.parseColor("#00ECEC");
				graph_color[2]  = Color.parseColor("#01A0F6");
				graph_color[3]  = Color.parseColor("#0000F6");
				graph_color[4]  = Color.parseColor("#00FF00");
				graph_color[5]  = Color.parseColor("#00C800");
				graph_color[6]  = Color.parseColor("#009000");
				graph_color[7]  = Color.parseColor("#FFFF00");
				graph_color[8]  = Color.parseColor("#E7C000");
				graph_color[9]  = Color.parseColor("#FF9000");
				graph_color[10] = Color.parseColor("#FF0000");
				graph_color[11] = Color.parseColor("#D60000");
				graph_color[12] = Color.parseColor("#C00000");
				graph_color[13] = Color.parseColor("#FF00FF");
				graph_color[14] = Color.parseColor("#9955C9");
				graph_color[15] = Color.parseColor("#FFFFFF");

				int[] graph_color2 = new int[16];
				graph_color2[0]  = Color.parseColor("#000000");
				graph_color2[1]  = Color.parseColor("#02FC02");
				graph_color2[2]  = Color.parseColor("#01E401");
				graph_color2[3]  = Color.parseColor("#01C501");
				graph_color2[4]  = Color.parseColor("#07AC04");
				graph_color2[5]  = Color.parseColor("#068F03");
				graph_color2[6]  = Color.parseColor("#047202");
				graph_color2[7]  = Color.parseColor("#7C977B");
				graph_color2[8]  = Color.parseColor("#987777");
				graph_color2[9]  = Color.parseColor("#890000");
				graph_color2[10] = Color.parseColor("#A20000");
				graph_color2[11] = Color.parseColor("#B90000");
				graph_color2[12] = Color.parseColor("#D80000");
				graph_color2[13] = Color.parseColor("#EF0000");
				graph_color2[14] = Color.parseColor("#FE0000");
				graph_color2[15] = Color.parseColor("#9000A0");

				//int num_range_bins = number_of_range_bins;
				float bin_size = WXGLNexrad.GetBinSize(product_code);

				int center_x=500;
				int center_y=500;

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

				for (g=0;g<number_of_radials;g++)
				{
					angle   = radial_start_angle[g];
					angle_v = radial_angle_delta[g];
					level       = bin_word[g][0];
					level_count = 0;
					bin_start   = bin_size;

					for  (bin=0;bin<number_of_range_bins;bin++ ) {
						if (( bin_word[g][bin] == level ) && ( bin != (number_of_range_bins-1) ) )
						{
							++level_count;
						} else {

							XY1 = UtilityMath.toRect( bin_start, angle );
							XY2 = UtilityMath.toRect( bin_start + ( bin_size * level_count ), angle );
							XY3 =  UtilityMath.toRect( bin_start + ( bin_size * level_count ), angle - angle_v );
							XY4  = UtilityMath.toRect( bin_start, angle - angle_v );

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
							} else {
								if ( is_velocity )
								{
									wallpaint.setColor(graph_color2[level] );
								}else{
									wallpaint.setColor(graph_color[level] );

								}
							}

							wallpath.rewind();
							wallpath.moveTo(XY1[0], XY1[1]);
							wallpath.lineTo(XY2[0], XY2[1]);
							wallpath.lineTo(XY3[0], XY3[1]);
							wallpath.lineTo(XY4[0], XY4[1]);
							wallpath.lineTo(XY1[0], XY1[1]);

							canvas.drawPath(wallpath, wallpaint);

							level       = bin_word[g][bin];
							bin_start   = bin * bin_size;
							level_count = 1;
						}
					}
				}

			} // end dis null check

		} catch (IOException e) {
			UtilityLog.HandleException(e);
		}
	}
}
