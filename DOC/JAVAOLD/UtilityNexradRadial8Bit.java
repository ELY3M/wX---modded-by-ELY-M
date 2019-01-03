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

package joshuatee.wx.radar;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
import joshuatee.wx.util.UCARRandomAccessFile;
import joshuatee.wx.util.UtilityLog;

class UtilityNexradRadial8Bit {

	// big thanks to gottipati, harpchad
	// http://sourceforge.net/projects/nexrad-perl/files/nexrad-perl/
	// below is mostly a java port of this code

	// this is overloaded below with the only change being a canvas instead of a bitmap
	// ultimately the code below is used for nexrad widget and for notification that shows radar

	public static void DecocodeAndPlotNexradDigital(Context c,Bitmap bm1, String fn, String prod )
	{
		ByteBuffer bin_word;
		ByteBuffer radial_start;
		ByteBuffer r_Buff;

		Canvas canvas = new Canvas(bm1);
		int zero_color = ContextCompat.getColor(c,R.color.black);

		if ( ! MyApplication.preferences!!.getString("NWS_RADAR_BG_BLACK", "").startsWith("t"))
			zero_color = ContextCompat.getColor(c,R.color.white);

		UCARRandomAccessFile dis = null;

		try {
			dis = new UCARRandomAccessFile(c.getFileStreamPath(fn).getAbsolutePath(), "r");
			dis.bigEndian = true;

		} catch (Exception e) {
			UtilityLog.HandleException(e);
		}

		try {

			if (dis!=null) {

				// ADVANCE PAST WMO HEADER
				while (dis.readShort() != -1) {
					// while (dis.readUnsignedShort() != 16) {
				}

				// the following chunk was added to analyze the header so that status info could be extracted
				// index 4 is radar height
				// index 0,1 is lat as Int
				// index 2,3 is long as Int
				final double latitude_of_radar = dis.readInt() / 1000.0;
				final double longitude_of_radar = dis.readInt() / 1000.0;
				final short height_of_radar = (short) dis.readUnsignedShort();
				final short product_code = (short) dis.readUnsignedShort();
				final short operational_mode = (short) dis.readUnsignedShort();

			/*final short        volume_scan_pattern =  (short) dis.readUnsignedShort();
			final short        sequence_number     = (short) dis.readUnsignedShort();
			final short        volume_scan_number  = (short) dis.readUnsignedShort();*/
				dis.skipBytes(6);

				final short volume_scan_date = (short) dis.readUnsignedShort();
				final Integer volume_scan_time = dis.readInt();
				final long sec = ((volume_scan_date - 1) * 60 * 60 * 24) + (volume_scan_time); // removed *1000
				final long milli = sec * 1000;
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(milli);
				java.util.Date d = cal.getTime();
				final String radar_info = d.toString() + MyApplication.newline +
						"Radar Mode: " + Integer.toString(operational_mode) + MyApplication.newline +
						"Product Code: " + Integer.toString(product_code) + MyApplication.newline +
						"Radar height: " + Integer.toString(height_of_radar) + MyApplication.newline +
						"Radar Lat: " + Double.toString(latitude_of_radar) + MyApplication.newline +
						"Radar Lon: " + Double.toString(longitude_of_radar) + MyApplication.newline;

				MyApplication.editor!!.putString("WX_RADAR_CURRENT_INFO", radar_info);
				MyApplication.editor!!.apply();

				dis.skipBytes(74);

				final int range_bin_alloc = 1390; // 460 for reflect, set to max possible for velocity - was 1200 for velocity, TZL requires 1390
				final int number_of_radials = 360;
				radial_start = ByteBuffer.allocateDirect(4 * number_of_radials);
				radial_start.position(0);
				radial_start.order(ByteOrder.nativeOrder());

				//final int compressedFileSize = (int)( dis.length()-dis.getFilePointer());

				dis.close();

				bin_word = ByteBuffer.allocateDirect(number_of_radials * (range_bin_alloc));
				bin_word.order(ByteOrder.nativeOrder());

				r_Buff = ByteBuffer.allocateDirect(32);  // 4bytes * 8 floats
				r_Buff.order(ByteOrder.nativeOrder());
				r_Buff.position(0);

				short number_of_range_bins = UtilityWXOGLPerf.Decode8bitWX(c, fn, radial_start, bin_word);

				final float bin_size = WXGLNexrad.GetBinSize(product_code);
				final int center_x = 500;
				final int center_y = 500;

				Paint wallpaint = new Paint();
				wallpaint.setStyle(Style.FILL);

				Path wallpath = new Path();
				int g;
				float angle;
				float angle_v = 1.0f;
				int level;
				int level_count;
				float bin_start;
				int bin;
				int tmp_val;
				float x1, y1;
				int red, green, blue;
				byte b;
				int col_rgb;
				ByteBuffer buf_r, buf_g, buf_b;

				switch (prod) {
					case "L2REF":
					case "N0Q":
						buf_r = MyApplication.color_map_94_r;
						buf_g = MyApplication.color_map_94_g;
						buf_b = MyApplication.color_map_94_b;
						break;
					case "L2VEL":
					case "N0U":
						buf_r = MyApplication.color_map_99_r;
						buf_g = MyApplication.color_map_99_g;
						buf_b = MyApplication.color_map_99_b;
						break;
					case "EET":
						buf_r = MyApplication.color_map_135_r;
						buf_g = MyApplication.color_map_135_g;
						buf_b = MyApplication.color_map_135_b;
						break;
					case "DVL":
						buf_r = MyApplication.color_map_134_r;
						buf_g = MyApplication.color_map_134_g;
						buf_b = MyApplication.color_map_134_b;
						break;
					case "N0X":
						buf_r = MyApplication.color_map_159_r;
						buf_g = MyApplication.color_map_159_g;
						buf_b = MyApplication.color_map_159_b;
						break;
					case "N0C":
						buf_r = MyApplication.color_map_161_r;
						buf_g = MyApplication.color_map_161_g;
						buf_b = MyApplication.color_map_161_b;
						break;
					case "N0K":
						buf_r = MyApplication.color_map_163_r;
						buf_g = MyApplication.color_map_163_g;
						buf_b = MyApplication.color_map_163_b;
						break;
					case "H0C":
						buf_r = MyApplication.color_map_165_r;
						buf_g = MyApplication.color_map_165_g;
						buf_b = MyApplication.color_map_165_b;
						break;
					case "N0S":
						buf_r = MyApplication.color_map_56_r;
						buf_g = MyApplication.color_map_56_g;
						buf_b = MyApplication.color_map_56_b;
						break;
					case "DAA":
					case "DSA":
						buf_r = MyApplication.color_map_172_r;
						buf_g = MyApplication.color_map_172_g;
						buf_b = MyApplication.color_map_172_b;
						break;
					default:
						buf_r = MyApplication.color_map_94_r;
						buf_g = MyApplication.color_map_94_g;
						buf_b = MyApplication.color_map_94_b;
						break;
				}

				for (g = 0; g < number_of_radials; g++) {
					angle = radial_start.getFloat();
					bin_word.mark();
					level = bin_word.get() & 0xFF;
					bin_word.reset();
					level_count = 0;
					bin_start = bin_size;

					for (bin = 0; bin < number_of_range_bins; bin++) {

						tmp_val = bin_word.get() & 0xFF;
						if (tmp_val == level)
							++level_count;
						else {
							UtilityWXOGLPerf.rect8bitwx(r_Buff, bin_start, bin_size, level_count, angle, angle_v, center_x, center_y);

							if (level == 0)
								wallpaint.setColor(zero_color);
							else {
								b = buf_r.get(level);
								red = b & 0xFF;
								b = buf_g.get(level);
								green = b & 0xFF;
								b = buf_b.get(level);
								blue = b & 0xFF;
								col_rgb = Color.rgb(red, green, blue);
								wallpaint.setColor(col_rgb);
							}

							wallpath.rewind(); // only needed when reusing this path for a new build

							r_Buff.position(0);
							x1 = r_Buff.getFloat();
							y1 = r_Buff.getFloat();
							wallpath.moveTo(x1, y1);
							wallpath.lineTo(r_Buff.getFloat(), r_Buff.getFloat());
							wallpath.lineTo(r_Buff.getFloat(), r_Buff.getFloat());
							wallpath.lineTo(r_Buff.getFloat(), r_Buff.getFloat());
							wallpath.lineTo(x1, y1);

							canvas.drawPath(wallpath, wallpaint);

							level = tmp_val;
							bin_start = bin * bin_size;
							level_count = 1;
						}
					}
					if (number_of_range_bins % 2 != 0)
						bin_word.position(bin_word.position() + 4);
				}
			} // end dis null check
		} catch (IOException e) {
			UtilityLog.HandleException(e);
		}
	}
}
