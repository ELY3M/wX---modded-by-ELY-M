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

package joshuatee.wx;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.content.Context;
import android.util.Log;

public class UtilityHelper {

	final static String hw_file = "hwv2.bin";
	final static String state_file = "statev2.bin";
	final static String lakes_file = "lakesv2.bin";
	final static String county_file = "county.bin";

	final static String stateV2_file1 = "statev3_1.bin";
	final static String stateV2_file2 = "statev3_2.bin";


	public static void ConstructStateBinFile(Context c) {

		List<Float> state_line_list_bin = new ArrayList<Float>();
		String state = "conus";
		String state_list = UtilityCanvasStateLines.getBorderingStates("conus");

		if (state_list.equals("")) {
			state_list = UtilityCanvasStateLines.getBorderingStates(state);
		}

		String[] state_arr = state_list.split(":");

		int s = 0;
		for (s = 0; s < state_arr.length; s++) {

			state = state_arr[s];
			String sig_html_tmp = "";
			int res_id = UtilityCanvasStateLines.getStateID(state);
			InputStream is = c.getResources().openRawResource(res_id);

			try {

				StringBuilder out = new StringBuilder();
				BufferedReader br = new BufferedReader(new InputStreamReader(is));
				for (String line = br.readLine(); line != null; line = br.readLine())
					out.append(line);
				br.close();
				sig_html_tmp = out.toString();

			} catch (IOException e) {
				e.printStackTrace();
			}

			int j = 0;
			List<Double> x = new ArrayList<Double>();
			x.clear();

			String chunk = "";
			String[] chunk_arr;
			String[] XY;

			Pattern p2 = Pattern.compile("<coordinates>(.*?)</coordinates>");
			Matcher m2 = p2.matcher(sig_html_tmp);
			while (m2.find()) {
				chunk = m2.group(1);

				x.clear();

				chunk_arr = chunk.split(" ");
				int m = 0;
				for (m = 0; m < chunk_arr.length; m++) {

					XY = chunk_arr[m].split(",");
					XY[0] = XY[0].replaceAll("-", "");

					try {
						if (!state.equals("GU")) {

							x.add(Double.parseDouble(XY[1]));
							x.add(Double.parseDouble(XY[0]));
							x.add(Double.parseDouble(XY[1]));
							x.add(Double.parseDouble(XY[0]));
						} else {
							x.add(Double.parseDouble(XY[1]));
							x.add(Double.parseDouble(XY[0]) * -1);
							x.add(Double.parseDouble(XY[1]));
							x.add(Double.parseDouble(XY[0]) * -1);
							Log.i("wx", XY[1]);
							Log.i("wx", XY[0]);


						}

					} catch (Exception e) {
					}


				}


				if (x.size() > 0) {

					for (j = 2; j < x.size() - 2; j = j + 2) {


						state_line_list_bin.add(new Float(x.get(j)));
						state_line_list_bin.add(new Float(x.get(j + 1)));


					}

					state_line_list_bin.add(new Float(x.get(x.size() - 2)));
					state_line_list_bin.add(new Float(x.get(x.size() - 1)));


					state_line_list_bin.add(new Float(x.get(0)));
					state_line_list_bin.add(new Float(x.get(1)));


				} // end size check
			} // end loop over chunk
		} // end loop over state list

		try {
			//FileOutputStream fos = c.openFileOutput(state_file, Context.MODE_WORLD_READABLE);
			FileOutputStream fos = c.openFileOutput(state_file, Context.MODE_PRIVATE);
			DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(fos));
			Integer jj = 0;
			for (jj = 0; jj < state_line_list_bin.size(); jj++) {
				dos.writeFloat(state_line_list_bin.get(jj));
			}
			fos.flush();
			fos.close();
		} catch (IOException e) {
		}
		Log.i("wx", "state bin: " + Integer.toString(state_line_list_bin.size()));


	} // end method


	public static void ConstructHWBinFile(Context c) {

		List<Float> hw_line_list_bin = new ArrayList<Float>();
		String state = "conus";
		String state_list = UtilityCanvasStateLines.getBorderingStates("conus");

		if (state_list.equals("")) {
			state_list = UtilityCanvasStateLines.getBorderingStates(state);
		}

		String[] state_arr = state_list.split(":");


		int s = 0;
		for (s = 0; s < state_arr.length; s++) {

			state = state_arr[s];

			String sig_html_tmp = "";
			int res_id = UtilityCanvasHW.getStateID(state);
			InputStream is = c.getResources().openRawResource(res_id);

			try {

				StringBuilder out = new StringBuilder();
				BufferedReader br = new BufferedReader(new InputStreamReader(is));
				for (String line = br.readLine(); line != null; line = br.readLine())
					out.append(line);
				br.close();
				sig_html_tmp = out.toString();

			} catch (IOException e) {
				e.printStackTrace();
			}

			int j = 0;
			List<Double> x = new ArrayList<Double>();

			x.clear();

			String chunk = "";
			String[] chunk_arr;
			String[] XY;

			Pattern p2 = Pattern.compile("<coordinates>(.*?)</coordinates>");
			Matcher m2 = p2.matcher(sig_html_tmp);
			while (m2.find()) {
				chunk = m2.group(1);

				x.clear();

				chunk_arr = chunk.split(" ");
				int m = 0;
				for (m = 0; m < chunk_arr.length; m++) {
					XY = chunk_arr[m].split(",");
					XY[0] = XY[0].replaceAll("-", "");
					try {
						if (!state.equals("GU")) {
							x.add(Double.parseDouble(XY[1]));
							x.add(Double.parseDouble(XY[0]));
							x.add(Double.parseDouble(XY[1]));
							x.add(Double.parseDouble(XY[0]));
						} else {
							x.add(Double.parseDouble(XY[1]));
							x.add(Double.parseDouble(XY[0]) * -1);
							x.add(Double.parseDouble(XY[1]));
							x.add(Double.parseDouble(XY[0]) * -1);
							Log.i("wx", XY[1]);
							Log.i("wx", XY[0]);
						}
					} catch (Exception e) {
					}
				}

				if (x.size() > 0) {


					for (j = 2; j < x.size() - 2; j = j + 2) {

						hw_line_list_bin.add(new Float(x.get(j)));
						hw_line_list_bin.add(new Float(x.get(j + 1)));

					}

				} // end size check
			} // end loop over chunk
		} // end loop over state list


		// FIXME to write out to binary file
		try {
			FileOutputStream fos = c.openFileOutput(hw_file, Context.MODE_PRIVATE);
			DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(fos));
			Integer jj = 0;
			for (jj = 0; jj < hw_line_list_bin.size(); jj++) {
				dos.writeFloat(hw_line_list_bin.get(jj));
			}
			fos.flush();
			fos.close();
		} catch (IOException e) {
		}

		Log.i("wx", "hw bin: " + Integer.toString(hw_line_list_bin.size()));


	} // end method


	public static void ConstructLakesBinFile(Context c) {

		List<Float> hw_line_list_bin = new ArrayList<Float>();
		String state = "conus";
		String state_list = UtilityCanvasStateLines.getBorderingStates("conus");

		if (state_list.equals("")) {
			state_list = UtilityCanvasStateLines.getBorderingStates(state);
		}

		String[] state_arr = state_list.split(":");


		int s = 0;
		for (s = 0; s < state_arr.length; s++) {

			state = state_arr[s];

			String sig_html_tmp = "";
			int res_id = UtilityCanvasLakes.getStateID(state);
			if (res_id != 0) {
				InputStream is = c.getResources().openRawResource(res_id);

				try {

					StringBuilder out = new StringBuilder();
					BufferedReader br = new BufferedReader(new InputStreamReader(is));
					for (String line = br.readLine(); line != null; line = br.readLine())
						out.append(line);
					br.close();
					sig_html_tmp = out.toString();

				} catch (IOException e) {
					e.printStackTrace();
				}

				int j = 0;
				List<Double> x = new ArrayList<Double>();

				x.clear();

				String chunk = "";
				String[] chunk_arr;
				String[] XY;

				Pattern p2 = Pattern.compile("<coordinates>(.*?)</coordinates>");
				Matcher m2 = p2.matcher(sig_html_tmp);
				while (m2.find()) {
					chunk = m2.group(1);

					x.clear();

					chunk_arr = chunk.split(" ");
					int m = 0;
					for (m = 0; m < chunk_arr.length; m++) {
						XY = chunk_arr[m].split(",");
						XY[0] = XY[0].replaceAll("-", "");
						try {
							if (!state.equals("GU")) {
								x.add(Double.parseDouble(XY[1]));
								x.add(Double.parseDouble(XY[0]));
								x.add(Double.parseDouble(XY[1]));
								x.add(Double.parseDouble(XY[0]));
							} else {
								x.add(Double.parseDouble(XY[1]));
								x.add(Double.parseDouble(XY[0]) * -1);
								x.add(Double.parseDouble(XY[1]));
								x.add(Double.parseDouble(XY[0]) * -1);
								Log.i("wx", XY[1]);
								Log.i("wx", XY[0]);
							}
						} catch (Exception e) {
						}
					}

					if (x.size() > 0) {


						for (j = 2; j < x.size() - 2; j = j + 2) {

							hw_line_list_bin.add(new Float(x.get(j)));
							hw_line_list_bin.add(new Float(x.get(j + 1)));

						}

					} // end size check
				} // end loop over chunk
			} // end see if resid not 0
		} // end loop over state list


		// FIXME to write out to binary file
		try {
			FileOutputStream fos = c.openFileOutput(lakes_file, Context.MODE_PRIVATE);
			DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(fos));
			Integer jj = 0;
			for (jj = 0; jj < hw_line_list_bin.size(); jj++) {
				dos.writeFloat(hw_line_list_bin.get(jj));
			}
			fos.flush();
			fos.close();
		} catch (IOException e) {
		}

		Log.i("wx", "lakes bin: " + Integer.toString(hw_line_list_bin.size()));


	} // end method

	// for future use

	/*public static void ConstructCountyBinFile(Context c)
	{

		List<Float> hw_line_list_bin = new ArrayList<Float>();
		String sig_html_tmp = "";

		InputStream is = c.getResources().openRawResource(R.raw.cb_2014_us_county_20m);

		try {

			StringBuilder   out = new StringBuilder();
			BufferedReader     br = new BufferedReader(new InputStreamReader(is));
			for(String line = br.readLine(); line != null; line = br.readLine())
				out.append(line);
			br.close();
			sig_html_tmp = out.toString();

		} catch (IOException e) {
			e.printStackTrace();
		}

		int j=0;
		List<Double> x = new ArrayList<Double>();

		x.clear();

		String chunk="";
		String[] chunk_arr;
		String[] XY;

		Pattern p2 = Pattern.compile("<coordinates>(.*?)</coordinates>");
		Matcher m2 = p2.matcher(sig_html_tmp);
		while (m2.find()) {
			chunk = m2.group(1);

			x.clear();

			chunk_arr = chunk.split(" ");
			int m = 0;
			for  (m=0;m<chunk_arr.length;m++)
			{
				XY = chunk_arr[m].split(",");
				XY[0] = XY[0].replaceAll("-","");
				try
				{

					x.add(Double.parseDouble( XY[1]));
					x.add(Double.parseDouble( XY[0]));
					x.add(Double.parseDouble( XY[1]));
					x.add(Double.parseDouble( XY[0]));

				}   catch (Exception e) {
				}
			}

			if (  x.size() > 0 )
			{



				for ( j = 2; j < x.size()-2; j = j + 2)
				{

					hw_line_list_bin.add(new Float(x.get(j)));
					hw_line_list_bin.add(new Float(x.get(j+1)));

				}

			} // end size check
		} // end loop over chunk


		// FIXME to write out to binary file
		try {
			FileOutputStream fos = c.openFileOutput(county_file, Context.MODE_PRIVATE);
			DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(fos));
			Integer jj=0;
			for (jj=0;jj<hw_line_list_bin.size();jj++)
			{
				dos.writeFloat(hw_line_list_bin.get(jj));
			}
			fos.flush();
			fos.close();
		} catch (IOException e) {
		}

		Log.i("wx","county bin: " + Integer.toString(hw_line_list_bin.size()));



	} // end method

	// for future use 205748

	public static void ConstructStateV2BinFile1(Context c)
	{

		List<Float> hw_line_list_bin = new ArrayList<Float>();
		String sig_html_tmp = "";

		InputStream is = c.getResources().openRawResource(R.raw.gz_2010_us_040_00_20m_part1);

		try {

			StringBuilder   out = new StringBuilder();
			BufferedReader     br = new BufferedReader(new InputStreamReader(is));
			for(String line = br.readLine(); line != null; line = br.readLine())
				out.append(line);
			br.close();
			sig_html_tmp = out.toString();

		} catch (IOException e) {
			Log.i("wx", "error1");

			e.printStackTrace();
		}

		int j=0;
		List<Double> x = new ArrayList<Double>();

		x.clear();

		String chunk="";
		String[] chunk_arr;
		String[] XY;

		Pattern p2 = Pattern.compile("<coordinates>(.*?)</coordinates>");
		Matcher m2 = p2.matcher(sig_html_tmp);
		while (m2.find()) {
			chunk = m2.group(1);

			x.clear();

			chunk_arr = chunk.split(" ");
			int m = 0;
			for  (m=0;m<chunk_arr.length;m++)
			{
				XY = chunk_arr[m].split(",");
				XY[0] = XY[0].replaceAll("-","");

				try
				{

					x.add(Double.parseDouble( XY[1]));
					x.add(Double.parseDouble( XY[0]));
					x.add(Double.parseDouble(XY[1]));
					x.add(Double.parseDouble(XY[0]));
					//Log.i("wx", XY[1] + " " + XY[0]);

				}   catch (Exception e) {
					Log.i("wx", "error2");

				}
			}

			if (  x.size() > 0 )
			{



				for ( j = 2; j < x.size()-2; j = j + 2)
				//	for ( j = 0; j < x.size(); j++)
				{

					hw_line_list_bin.add(new Float(x.get(j)));
					hw_line_list_bin.add(new Float(x.get(j+1)));

				}
				//hw_line_list_bin.add(new Float(x.get(x.size()-2)));
				//hw_line_list_bin.add(new Float(x.get(x.size()-1)));


				//hw_line_list_bin.add(new Float(x.get(0)));
				//hw_line_list_bin.add(new Float(x.get(1)));

			} // end size check

		} // end loop over chunk


		// FIXME to write out to binary file
		try {
			FileOutputStream fos = c.openFileOutput(stateV2_file1, Context.MODE_PRIVATE);
			DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(fos));
			Integer jj=0;
			for (jj=0;jj<hw_line_list_bin.size();jj++)
			{
				dos.writeFloat(hw_line_list_bin.get(jj));
			}
			fos.flush();
			fos.close();
		} catch (IOException e) {
			Log.i("wx", "error3");

		}

		Log.i("wx","state bin: " + Integer.toString(hw_line_list_bin.size()));



	} // end method

	public static void ConstructStateV2BinFile2(Context c)
	{

		List<Integer> cod_hash = new ArrayList<Integer>();

		cod_hash.add(R.raw.state1);
		cod_hash.add(R.raw.state2);
		cod_hash.add(R.raw.state3);
		cod_hash.add(R.raw.state4);
		cod_hash.add(R.raw.state5);
		cod_hash.add(R.raw.state6);
		cod_hash.add(R.raw.state7);
		cod_hash.add(R.raw.state8);
		cod_hash.add(R.raw.state9);
		cod_hash.add(R.raw.state10);
		cod_hash.add(R.raw.state11);
		cod_hash.add(R.raw.state12);
		cod_hash.add(R.raw.state13);
		cod_hash.add(R.raw.state14);
		cod_hash.add(R.raw.state15);
		cod_hash.add(R.raw.state16);
		cod_hash.add(R.raw.state17);
		cod_hash.add(R.raw.state18);
		cod_hash.add(R.raw.state19);
		cod_hash.add(R.raw.state20);
		cod_hash.add(R.raw.state21);
		cod_hash.add(R.raw.state22);
		cod_hash.add(R.raw.state23);
		cod_hash.add(R.raw.state24);
		cod_hash.add(R.raw.state25);
		cod_hash.add(R.raw.state26);
		cod_hash.add(R.raw.state27);
		cod_hash.add(R.raw.state28);
		cod_hash.add(R.raw.state29);
		cod_hash.add(R.raw.state30);
		cod_hash.add(R.raw.state31);
		cod_hash.add(R.raw.state32);
		cod_hash.add(R.raw.state33);
		cod_hash.add(R.raw.state34);
		cod_hash.add(R.raw.state35);
		cod_hash.add(R.raw.state36);
		cod_hash.add(R.raw.state37);
		cod_hash.add(R.raw.state38);
		cod_hash.add(R.raw.state39);
		cod_hash.add(R.raw.state40);
		cod_hash.add(R.raw.state41);
		cod_hash.add(R.raw.state42);
		cod_hash.add(R.raw.state43);
		cod_hash.add(R.raw.state44);
		cod_hash.add(R.raw.state45);
		cod_hash.add(R.raw.state46);
		cod_hash.add(R.raw.state47);
		cod_hash.add(R.raw.state48);
		cod_hash.add(R.raw.state49);
		cod_hash.add(R.raw.state50);
		cod_hash.add(R.raw.state51);
		cod_hash.add(R.raw.state52);

		int total_cnt=0;
		String sig_html_tmp = "";


		int k=0;
		List<Float> hw_line_list_bin = new ArrayList<Float>();
		InputStream is = null;
		StringBuilder out = new StringBuilder();
		BufferedReader br = null;
		String line="";
		Pattern p2;
		Matcher m2;

		String chunk = "";
		String[] chunk_arr;
		String[] XY;
		List<Double> x = new ArrayList<Double>();
		int m = 0;

		int chunk_cnt_log=0;


		for (k=0;k<cod_hash.size();k++) {


			hw_line_list_bin.clear();
			is = c.getResources().openRawResource(cod_hash.get(k));

			try {

				//StringBuilder out = new StringBuilder();
				out.setLength(0);
				br = new BufferedReader(new InputStreamReader(is));
				for ( line = br.readLine(); line != null; line = br.readLine())
					out.append(line);
				br.close();
				sig_html_tmp = out.toString();

			} catch (IOException e) {
				Log.i("wx", "error1");

				e.printStackTrace();
			}

			int j = 0;

			x.clear();

			chunk_cnt_log=0;

			p2 = Pattern.compile("<coordinates>(.*?)</coordinates>");
			m2 = p2.matcher(sig_html_tmp);
			while (m2.find())
			{
				chunk = m2.group(1);

				x.clear();
				Log.i("wx", "state: " + Integer.toString(k) + " " + Integer.toString(chunk_cnt_log));
				chunk_cnt_log++;

				chunk_arr = chunk.split(" ");
				for (m = 0; m < chunk_arr.length; m++)
				{
					XY = chunk_arr[m].split(",");
					XY[0] = XY[0].replaceAll("-", "");
					try {

						x.add(Double.parseDouble(XY[1]));
						x.add(Double.parseDouble(XY[0]));
						x.add(Double.parseDouble(XY[1]));
						x.add(Double.parseDouble(XY[0]));
						//Log.i("wx", XY[1] + " " + XY[0]);

					} catch (Exception e) {
						Log.i("wx", "error2");

					}
				}

				if (x.size() > 0)
				{
					for (j = 2; j < x.size() - 2; j = j + 2)
					{
						hw_line_list_bin.add(new Float(x.get(j)));
						hw_line_list_bin.add(new Float(x.get(j + 1)));
					}

				} // end size check

			} // end loop over chunk

			try {
				//FileOutputStream fos = c.openFileOutput(stateV2_file2, Context.MODE_PRIVATE);

				//     DataInputStream in = new DataInputStream(openFileInput(FILENAME));
				// new DataOutputStream(openFileOutput(FILENAME, Context.MODE_PRIVATE));

				FileOutputStream fos = c.openFileOutput("s" + Integer.toString(k), Context.MODE_PRIVATE);
				DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(fos));
				Integer jj = 0;
				for (jj = 0; jj < hw_line_list_bin.size(); jj++) {
					dos.writeFloat(hw_line_list_bin.get(jj));
				}
				dos.close();
				fos.flush();
				fos.close();
			} catch (IOException e) {
				Log.i("wx", "error3");

			}

			Log.i("wx", Integer.toString(k) + " state bin: " + Integer.toString(hw_line_list_bin.size()));
			total_cnt = total_cnt +   hw_line_list_bin.size();
		}

		Log.i("wx", "Total state bin: " + Integer.toString(total_cnt));




	} // end method

}
*/
//./android-sdk-linux/platform-tools/adb pull /data/data/joshuatee.wx/files/statev3.bin

}