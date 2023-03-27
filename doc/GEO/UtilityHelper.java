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

package joshuatee.wx.util;

class UtilityHelper {

	/*public static void ConstructCACities()
	{

		String a;
		String[] prov_arr = new String[] {"AB","BC","MB","NB","NL","NT","NS","NU","ON","PE","QC","SK","YT"};

		//String[] prov_arr = new String[] {"YT"};

		int i = 0;

		for ( String s : prov_arr) {
			a = UtilityDownload.getStringFromURL("http://weather.gc.ca/forecast/canada/index_e.html?id=" + s);
			//List<String> al = UtilityString.parseColumn(a,"<a href=\"/city/pages/.*?_metric_e.html\">(.*?)</a>");
			List<String> al = UtilityString.parseColumn(a,"<a href=\"/city/pages/.*?-(.*?)_metric_e.html\">.*?</a>");


			ArrayList<String> tmp_al = new ArrayList<>(al.subList(0,al.size()/2));

			for (String j : tmp_al) {

				*//*String city_str = j + ", " + s;
				String[] str_arr = UtilityLocation.getXYFromAddressOSM(city_str);
				if (str_arr[0] == null || str_arr[1] == null )
				{
					str_arr[0]= "0.0";
					str_arr[1]= "0.0";
				}

				str_arr[1]=str_arr[1].replace("-","");
				SystemClock.sleep(3000);*//*

				UtilityLog.d("Wx", "code[" + i + "]=\"" + j + "\";");

				i++;
			}

		}

	}*/

	//private final static String hw_file = "hwv4ext.bin";

	// ./Android/Sdk/platform-tools/adb pull /data/data/joshuatee.wx/files/hwv3.bin


	/*final static String hw_file = "hwv2.bin";
	final static String state_file = "statev2.bin";
	final static String lakes_file = "lakesv2.bin";
	final static String county_file = "county.bin";

	final static String stateV2_file1 = "statev3_1.bin";
	final static String stateV2_file2 = "statev3_2.bin";*/

	//private final static String county_file = "countyv2.bin";
	//private final static String rivers_files ="rivers.bin";



/*	public static void ConstructStateBinFile(Context c) {

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


	} // end method*/


/*
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
			// FIXME
			int res_id = 0
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
*/


/*	public static void ConstructLakesBinFile(Context c) {

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
			int res_id = 0;
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


	} // end method*/

	// for future use

	/*
	public static void ConstructCountyBinFile(Context c)
	{

		List<Float> hw_line_list_bin = new ArrayList<>();
		String sig_html_tmp = "";

		// download from https://www.census.gov/geo/maps-data/data/kml/kml_counties.html

		//InputStream is = c.getResources().openRawResource(R.raw.cb_2014_us_county_20m);
		// InputStream is = c.getResources().openRawResource(R.raw.cb_2015_us_county_5m);
		InputStream is =null;

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

		int j;
		List<Double> x = new ArrayList<>();

		x.clear();

		String chunk;
		String[] chunk_arr;
		String[] XY;

		Pattern p2 = Pattern.compile("<coordinates>(.*?)</coordinates>");
		Matcher m2 = p2.matcher(sig_html_tmp);
		while (m2.find()) {
			chunk = m2.group(1);

			x.clear();

			chunk_arr = chunk.split(" ");
			int m;
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
					UtilityLog.HandleException(e);
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
			Integer jj;
			for (jj=0;jj<hw_line_list_bin.size();jj++)
			{
				dos.writeFloat(hw_line_list_bin.get(jj));
			}
			fos.flush();
			fos.close();
		} catch (IOException e) {
			UtilityLog.HandleException(e);
		}

		Log.i("wx","county bin: " + Integer.toString(hw_line_list_bin.size()));



	} // end method
	*/



	// for future use 205748

	//private final static String stateV2_file1 = "ca.bin";


	/* public static void ConstructStateBinFile1(Context c)
	{

		List<Float> hw_line_list_bin = new ArrayList<>();
		String sig_html_tmp = "";

		//InputStream is = c.getResources().openRawResource(R.raw.cb_2015_us_state_500k);
		InputStream is = null;
		//InputStream is = c.getResources().openRawResource(R.raw.mx);



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

		int j;
		List<Double> x = new ArrayList<>();

		x.clear();

		String chunk;
		String[] chunk_arr;
		String[] XY;

		Pattern p2 = Pattern.compile("<coordinates>(.*?)</coordinates>");
		Matcher m2 = p2.matcher(sig_html_tmp);
		while (m2.find()) {
			chunk = m2.group(1);

			x.clear();

			chunk_arr = chunk.split(" ");
			int m;
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
			Integer jj;
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
	*/



	/* public static void ConstructRiversBinFile(Context c)
	{


		List<Float> hw_line_list_bin = new ArrayList<>();
		String sig_html_tmp = "";

		//InputStream is = c.getResources().openRawResource(R.raw.nwsrivers);
		InputStream is = null;


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

		int j;
		List<Double> x = new ArrayList<>();

		x.clear();

		String chunk;
		String[] chunk_arr;
		String[] XY;

		Pattern p2 = Pattern.compile("<coordinates>(.*?)</coordinates>");
		Matcher m2 = p2.matcher(sig_html_tmp);
		while (m2.find()) {
			chunk = m2.group(1);

			x.clear();

			chunk_arr = chunk.split(" ");
			int m;
			for  (m=0;m<chunk_arr.length;m++)
			{
				XY = chunk_arr[m].split(",");
				//XY[0] = XY[0].replaceAll("-","");

				try
				{

					// lat 20-60
					// lon 60-130


					//if ( Double.parseDouble(XY[1])>20 && Double.parseDouble(XY[1])<60 && Double.parseDouble(XY[0]) < -60 &&  Double.parseDouble(XY[0]) > -130 ) {

					if ( Double.parseDouble(XY[1])>20 && Double.parseDouble(XY[1])<75 && Double.parseDouble(XY[0]) < -60 &&  Double.parseDouble(XY[0]) > -170 ) {


						//UtilityLog.d("wx", XY[0] + " " + XY[1]);

						XY[0] = XY[0].replaceAll("-","");
						x.add(Double.parseDouble(XY[1]));
						x.add(Double.parseDouble(XY[0]));
						x.add(Double.parseDouble(XY[1]));
						x.add(Double.parseDouble(XY[0]));
					}
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
			FileOutputStream fos = c.openFileOutput(rivers_files, Context.MODE_PRIVATE);
			DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(fos));
			Integer jj;
			for (jj=0;jj<hw_line_list_bin.size();jj++)
			{
				dos.writeFloat(hw_line_list_bin.get(jj));
			}
			fos.flush();
			fos.close();
		} catch (IOException e) {
			Log.i("wx", "error3");

		}

		Log.i("wx","rivers bin: " + Integer.toString(hw_line_list_bin.size()));



	} // end method
	*/

	/*

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

	/*public static void ConstructHWBinFile(Context c) {

		List<Float> hw_line_list_bin = new ArrayList<>();
		String state = "conus";
		//String state_list = UtilityCanvasStateLines.getBorderingStates("conus");


			String sig_html_tmp = "";
			//int res_id = UtilityCanvasHW.getStateID(state);
			InputStream is = c.getResources().openRawResource(R.raw.us_metar3); // FIXME set to KML

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

			int j;
			List<Double> x = new ArrayList<>();

			x.clear();

			String chunk;
			String[] chunk_arr;
			String[] XY;

			Pattern p2 = Pattern.compile("<coordinates>(.*?)</coordinates>");
			Matcher m2 = p2.matcher(sig_html_tmp);
			while (m2.find()) {
				chunk = m2.group(1);

				x.clear();

				chunk_arr = chunk.split(" ");
				int m;
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
							//Log.i("wx", XY[1]);
							//Log.i("wx", XY[0]);
						}
					} catch (Exception e) {
						UtilityLog.HandleException(e);
					}
				}

				if (x.size() > 0) {


					for (j = 2; j < x.size() - 2; j = j + 2) {

						hw_line_list_bin.add(new Float(x.get(j)));
						hw_line_list_bin.add(new Float(x.get(j + 1)));

					}

				} // end size check
			} // end loop over chunk
		//} // end loop over state list


		// FIXME to write out to binary file
		try {
			FileOutputStream fos = c.openFileOutput(hw_file, Context.MODE_PRIVATE);
			DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(fos));
			Integer jj;
			for (jj = 0; jj < hw_line_list_bin.size(); jj++) {
				dos.writeFloat(hw_line_list_bin.get(jj));
			}
			fos.flush();
			fos.close();
		} catch (IOException e) {
			UtilityLog.HandleException(e);
		}

		UtilityLog.d("wx", "hw bin: " + Integer.toString(hw_line_list_bin.size()));


	} // end method*/

	/*public static void WriteColorMaps(Context c, String prod, ByteBuffer map_r, ByteBuffer map_g, ByteBuffer map_b)
	{

		try {
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(c.openFileOutput("UtilityColorPalette" + prod + ".swift", Context.MODE_PRIVATE));


			String data="";

			data += "UtilityColorPalette" + prod + " {" + MyApplication.newline;
			data += MyApplication.newline;
			data += "class func Gen" + prod + "() {" + MyApplication.newline;
			data += "MyApplication.color_map_" + prod + "_r.position(position:0);" + MyApplication.newline;
			data += "MyApplication.color_map_" + prod + "_r.position(position:0);" + MyApplication.newline;
			data += "MyApplication.color_map_" + prod + "_r.position(position:0);" + MyApplication.newline;


			for (int i=0;i<256;i++)
			{
				data += "MyApplication.color_map_" + prod + "_r.put(byte:" + Integer.toString(map_r.get(i) & 0xff) +  ");\n";
			}
			for (int i=0;i<256;i++)
			{
				data += "MyApplication.color_map_" + prod + "_g.put(byte:" + Integer.toString(map_g.get(i)& 0xff) +  ");\n";
			}
			for (int i=0;i<256;i++)
			{
				data += "MyApplication.color_map_" + prod + "_b.put(byte:" + Integer.toString(map_b.get(i)& 0xff) +  ");\n";
			}

			data += "}\n";
			data += "}\n";

			outputStreamWriter.write(data);
			outputStreamWriter.close();
		}
		catch (IOException e) {
			Log.e("Exception", "File write failed: " + e.toString());
		}


		//FileOutputStream fos = c.openFileOutput(hw_file, Context.MODE_PRIVATE);
		//DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(fos));
		//Integer jj;
		////for (jj = 0; jj < hw_line_list_bin.size(); jj++) {
		//dos.writeFloat(hw_line_list_bin.get(jj));
		//}

		//fos.write();

		//fos.flush();
		//fos.close();

	} // end method
	*/

}