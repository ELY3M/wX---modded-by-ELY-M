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

import androidx.appcompat.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener;
import android.util.TypedValue;

import joshuatee.wx.R;
import joshuatee.wx.util.ImageMap;
import joshuatee.wx.MyApplication;
import joshuatee.wx.ui.TouchImageView2;
import joshuatee.wx.util.UtilityImgAnim;
import joshuatee.wx.util.UtilityLog;
import joshuatee.wx.settings.FavAddActivity;
import joshuatee.wx.settings.FavRemoveActivity;
import joshuatee.wx.settings.SettingsRadarActivity;
import joshuatee.wx.ui.UtilityToolbar;
import joshuatee.wx.util.UtilityDownload;
import joshuatee.wx.util.UtilityImageMap;
import joshuatee.wx.util.UtilityImg;
import joshuatee.wx.util.UtilityShare;

public class USWXRadarActivity extends AppCompatActivity   implements OnClickListener,OnItemSelectedListener,OnMenuItemClickListener {

	// This activity is a general purpose viewer of nexrad and mosaic content
	// with NWS overlays and nexrad images for Iowa Mesonet ( to gain access to higher resolution 
	// base reflectivity/velocity
	// It's accessible from the action bar
	//

	private static final String RID = "";
	private boolean anim_ran = false;
	private Spinner spinner1;
	private final int spinner_layout = R.layout.spinner_row_blue;
	private ArrayAdapter<String> dataAdapter;
	private TouchImageView2 img ;
	private int actionBarHeight;
	private Toolbar toolbar;
	private Toolbar toolbar_bottom;
	private final Float init_zoom = 1.00f; // was 1.50f
	private String tilt = "0";
	private String[] turl;
	private String img_url = "";
	private String prod = "";
	private AnimationDrawable animDrawable;
	private LayerDrawable layerDrawable ;
	private String[] rid_arr;
	private String[] rid_arr_loc;
	private String rid1="";
	private String state="";
	private String sector = "";
	private String onek = "";
	private Boolean spotters = false;
	private Integer zoom_level_wv = 1;
	private Integer zoom_level_ir = 1;
	private Integer zoom_level_vis = 1;
	private Integer zoom_level_radar = 0;
	private ImageMap mImageMap;
	private MenuItem star ;
	private MenuItem tdwr;
	private MenuItem anim;
	private String rid_fav="";

	@Override
	public void onCreate(Bundle savedInstanceState) {


		setTheme(MyApplication.theme_int);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_uswxradar);

		toolbar = (Toolbar) findViewById(R.id.toolbar_top);
		setSupportActionBar(toolbar);
		assert getSupportActionBar() != null;
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		toolbar_bottom = (Toolbar) findViewById(R.id.toolbar_bottom);
		if (MyApplication.icons_even_spaced)
			UtilityToolbar.setupEvenlyDistributedToolbar(this,toolbar_bottom, R.menu.uswxradar);
		else
			toolbar_bottom.inflateMenu(R.menu.uswxradar);
		toolbar_bottom.setOnMenuItemClickListener(this);

		Menu menu = toolbar_bottom.getMenu();

		star = menu.findItem(R.id.action_fav);
		tdwr = menu.findItem(R.id.action_tdwr);
		anim = menu.findItem(R.id.action_animate);

		// Calculate ActionBar height
		TypedValue tv = new TypedValue();
		if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
		{
			actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
		}

		img = (TouchImageView2) findViewById(R.id.iv);
		img.setOnClickListener(this);

		img.setMaxZoom(6.0f);
		img.setZoom(init_zoom);

		turl = getIntent().getStringArrayExtra(RID);

		prod = "N0Q";

		rid1 = turl[0];
		state = turl[1];
		if ( turl.length > 2 )
		{
			prod =  turl[2];
			if (prod.equals("N0R"))
			{
				prod = "N0Q";
			}
		}

		rid_fav = MyApplication.preferences!!.getString("RID_FAV"," : : :");
		rid_arr = rid_fav.split(":");

		boolean fromOrientation = MyApplication.preferences!!.getBoolean("fromOrient", false);
		if(fromOrientation) {
			String nexrad_last = MyApplication.preferences!!.getString("NEXRAD_LAST",rid1);
			rid_arr[0] = nexrad_last;
		} else {
			rid_arr[0] = rid1;

		}

		rid_arr[0] = turl[0];
		rid_arr[1] = "Add";
		rid_arr[2] = "Remove";

		rid_arr_loc = new String[rid_arr.length];

		int k;
		for (k=0;k<rid_arr.length;k++)
		{
			String rid_loc = MyApplication.preferences!!.getString("RID_LOC_"+rid_arr[k],"");
			rid_arr_loc[k]=rid_arr[k]+" "+rid_loc;
		}

		sector = MyApplication.preferences!!.getString("COD_SECTOR_"+state,"");
		state = MyApplication.preferences!!.getString("STATE_CODE_"+state,"");
		onek = MyApplication.preferences!!.getString("COD_1KM_"+rid1,"");


		setTitle( prod);

		mImageMap = (ImageMap) findViewById(R.id.map);
		mImageMap.setVisibility(View.GONE);

		spinner1 = (Spinner) findViewById(R.id.spinner1);
		dataAdapter = new ArrayAdapter<String>(this,R.layout.simple_spinner_item, rid_arr_loc);
		dataAdapter.setDropDownViewResource(spinner_layout);
		spinner1.setAdapter(dataAdapter);
		spinner1.setOnItemSelectedListener(this);

	}

	protected void onRestart() {

		if (! prod.contains("L2"))
		{

			rid_fav = MyApplication.preferences!!.getString("RID_FAV"," : : :");

			String nexrad_last = MyApplication.preferences!!.getString("NEXRAD_LAST","ZZZ");
			int pos=0;

			rid_arr = rid_fav.split(":");

			rid_arr[0] = nexrad_last;
			rid_arr[1] = "Add";
			rid_arr[2] = "Remove";

			rid_arr_loc = new String[rid_arr.length];

			int k;
			for (k=0;k<rid_arr.length;k++)
			{
				String rid_loc = MyApplication.preferences!!.getString("RID_LOC_"+rid_arr[k],"");

				if (nexrad_last.equals(rid_arr[k]))
				{
					pos=k;
				}

				if (nexrad_last.equals("Add") || nexrad_last.equals("Remove")  )
				{
					pos=0;
				}

				rid_arr_loc[k]=rid_arr[k]+" "+rid_loc;

			}

			dataAdapter = new ArrayAdapter<String>(this,R.layout.simple_spinner_item, rid_arr_loc);
			dataAdapter.setDropDownViewResource(spinner_layout);
			spinner1.setAdapter(dataAdapter);


			spinner1.setSelection(pos);


		}

		super.onRestart();

	}

	private class GetContent extends AsyncTask<String, String, String> {

		Bitmap bitmap;

		@Override
		protected String doInBackground(String... params) {


			if ( ! prod.equals("2k"))
			{
				layerDrawable = UtilityUSImgWX.LayeredImg(getApplicationContext(), rid1, prod);
			} else {
				bitmap = UtilityDownload.getBitmapFromURL(img_url);
			}


			return "Executed";
		}

		@Override
		protected void onPostExecute(String result) {

			mImageMap.setVisibility(View.GONE);
			img.setVisibility(View.VISIBLE);

			if ( ! prod.equals("2k"))
			{

				img.setImageDrawable(layerDrawable);
				String info = MyApplication.preferences!!.getString("WX_RADAR_CURRENT_INFO","");
				String[] tmp_arr = info.split(" ");
				if (tmp_arr.length>3)
				{
					toolbar.setSubtitle(tmp_arr[3]);
				} else
				{
					toolbar.setSubtitle("");
				}
			} else {
				img.setImageBitmap(bitmap);
				toolbar.setSubtitle("");

			}

			if ( rid_fav.contains(":" + rid1 + ":"))
			{
				star.setIcon(MyApplication.star_icon);

			} else
			{
				star.setIcon(MyApplication.star_outline_icon);
			}

			anim_ran = false;

		}

		@Override
		protected void onPreExecute() {

		}

		@Override
		protected void onProgressUpdate(String... values) {}
	}


	private class AnimateRadar extends AsyncTask<String, String, String> {


		String frame_cnt_str = "";

		@Override
		protected String doInBackground(String... params) {
			frame_cnt_str = params[0];

			try {
				animDrawable = UtilityUSImgWX.Animation(getApplicationContext(),  rid1,  prod, frame_cnt_str);
			}  catch (Exception e) {
				UtilityLog.HandleException(e);
			}

			return "Executed";
		}

		@Override
		protected void onPostExecute(String result) {


			mImageMap.setVisibility(View.GONE);
			img.setVisibility(View.VISIBLE);

			if ( rid_fav.contains(":" + rid1 + ":"))
			{
				star.setIcon(MyApplication.star_icon);

			} else
			{
				star.setIcon(MyApplication.star_outline_icon);
			}

			img.setImageDrawable(animDrawable);
			animDrawable.setOneShot(false);
			animDrawable.start();
			anim_ran = true;

		}

		@Override
		protected void onPreExecute() {

		}

		@Override
		protected void onProgressUpdate(String... values) {}
	}



	@Override
	public boolean onMenuItemClick(MenuItem item) {

		switch (item.getItemId()) {
			case R.id.action_share:

				if ( anim_ran)
				{
					UtilityShare.ShareAnimGif(this,
							rid1 + " (" + MyApplication.preferences!!.getString("RID_LOC_"+rid1,"") + ") " + prod, animDrawable);

				} else
				{
					UtilityShare.ShareBitmap(this,
							rid1 + " (" + MyApplication.preferences!!.getString("RID_LOC_"+rid1,"") + ") " + prod, UtilityImg.LayerDrawableToBitmap(layerDrawable));
				}

				return true;
			case R.id.action_n0q:
				prod = "N" + tilt + "Q";
				setTitle(prod);
				new GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				return true;
			case R.id.action_n0u:
				prod = "N" + tilt + "U";
				setTitle( prod);
				new GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				return true;
			case R.id.action_tv0:
				prod = "TV0";
				setTitle( prod);
				new GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				return true;
			case R.id.action_tzl:
				prod = "TZL";
				setTitle( prod);
				new GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				return true;

			case R.id.action_n0s:
				prod = "N" + tilt + "S";
				setTitle( prod);
				new GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				return true;
			case R.id.action_net:
				prod = "EET";
				setTitle( prod);
				new GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				return true;
			case R.id.action_DVL:
				prod = "DVL";
				setTitle( prod);
				new GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				return true;

			case R.id.action_N0X:
				prod = "N" + tilt + "X";
				setTitle(prod);
				new GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				return true;
			case R.id.action_N0C:
				prod = "N" + tilt + "C";
				setTitle(prod);
				new GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				return true;
			case R.id.action_N0K:
				prod = "N" + tilt + "K";
				setTitle(prod);
				new GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				return true;

			case R.id.action_about:
				ShowRadarScanInfo();
				return true;
			case R.id.action_settings:
				Intent intent=new Intent(this,SettingsRadarActivity.class);
				startActivity(intent);
				return true;


			case R.id.action_vil:
				prod = "DVL";
				setTitle( prod);
				new GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				return true;

			case R.id.action_dsp:
				prod = "DSA";
				setTitle( prod);
				new GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				return true;
			case R.id.action_h0c:
				prod = "H" + tilt + "C";
				setTitle( prod);
				new GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				return true;

			case R.id.action_n0r:
				prod = "N0R";
				setTitle( prod);
				new GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				return true;
			case R.id.action_n0v:
				prod = "N0V";
				setTitle( prod);
				new GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				return true;

			case R.id.action_l2vel:
				prod = "L2VEL";
				setTitle( prod);
				new GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				return true;

			case R.id.action_l2ref:
				prod = "L2REF";
				setTitle( prod);
				new GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				return true;



			case R.id.action_tilt1:
				tilt = "0";
				prod = prod.replaceAll("N[0-3]","N" + tilt);
				setTitle(prod);
				new GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				return true;
			case R.id.action_tilt2:
				tilt = "1";
				prod = prod.replaceAll("N[0-3]","N" +tilt);
				setTitle(prod);
				new GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				return true;
			case R.id.action_tilt3:
				tilt = "2";
				prod = prod.replaceAll("N[0-3]","N" +tilt);
				setTitle(prod);
				new GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				return true;
			case R.id.action_tilt4:
				tilt = "3";
				prod = prod.replaceAll("N[0-3]","N" +tilt);
				setTitle(prod);
				new GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				return true;

			case R.id.action_a12:
				setTitle( prod);
				if (  prod.equals("2k"))
				{
					new AnimateRadarMosaic().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"12",prod);

				} else {
					new AnimateRadar().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"12",prod);
				}
				return true;
			case R.id.action_a18:
				setTitle(prod);
				if (  prod.equals("2k"))
				{
					new AnimateRadarMosaic().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"18",prod);

				} else {
					new AnimateRadar().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"18",prod);
				}
				return true;
			case R.id.action_a6:
				setTitle( prod);
				if (  prod.equals("2k"))
				{
					new AnimateRadarMosaic().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"6",prod);

				} else {
					new AnimateRadar().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"6",prod);
				}
				return true;


			case R.id.action_a36:
				setTitle(prod);
				if (  prod.equals("2k"))
				{
					new AnimateRadarMosaic().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"36",prod);

				} else {
					new AnimateRadar().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"36",prod);
				}
				return true;

			case R.id.action_a3:
				setTitle(prod);
				if (  prod.equals("2k"))
				{
					new AnimateRadarMosaic().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"3",prod);

				} else {
					new AnimateRadar().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"3",prod);
				}
				return true;



			case R.id.action_zoomout:
				setTitle(state + " Radar");
				img.resetZoom();
				if ( zoom_level_radar == 0 )
				{
					img_url = "http://climate.cod.edu/data/satellite/1km/" + onek + "/current/" + onek + ".rad.gif" ;
					zoom_level_radar++;
				} else if ( zoom_level_radar == 1 )
				{
					img_url = "http://climate.cod.edu/data/satellite/2km/" + state + "/current/" + state + ".rad.gif" ;
					zoom_level_radar++;
				} else if ( zoom_level_radar == 2)
				{
					img_url = "http://climate.cod.edu/data/satellite/regional/" + sector + "/current/" + sector + ".rad.gif";
					zoom_level_radar++;
				} else if ( zoom_level_radar == 3 )
				{
					img_url = "http://climate.cod.edu/data/satellite/regional/usa/current/usa.rad.gif";
					zoom_level_radar = 0;
				}
				prod = "2k";
				new GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				return true;
			case R.id.action_vis:
				setTitle(state + " Vis");
				img.resetZoom();
				if ( state.equals("HI") || state.equals("AK") )
				{
					zoom_level_vis = 3;
				}

				if ( zoom_level_vis == 1 )
				{
					img_url = "http://climate.cod.edu/data/satellite/1km/" + onek + "/current/" + onek + ".vis.gif" ;
					zoom_level_vis++;
				} else	if ( zoom_level_vis == 2 )
				{
					img_url = "http://climate.cod.edu/data/satellite/2km/" + state + "/current/" + state + ".vis.gif" ;
					zoom_level_vis++;
				} else if ( zoom_level_vis == 3 )
				{
					img_url = "http://climate.cod.edu/data/satellite/regional/" + sector + "/current/" + sector + ".vis.gif";
					zoom_level_vis++;
				} else if ( zoom_level_vis == 4 )
				{
					img_url = "http://climate.cod.edu/data/satellite/regional/usa/current/usa.vis.gif";
					zoom_level_vis = 1;
				}
				prod = "2k";
				new GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				return true;
			case R.id.action_ir:
				setTitle(state+ " IR");
				img.resetZoom();
				if ( state.equals("HI") || state.equals("AK") )
				{
					zoom_level_ir = 2;
				}
				if ( zoom_level_ir == 1 )
				{
					img_url = "http://climate.cod.edu/data/satellite/2km/" + state + "/current/" + state + ".ir.gif" ;
					zoom_level_ir++;
				} else if ( zoom_level_ir == 2 )
				{
					img_url = "http://climate.cod.edu/data/satellite/regional/" + sector + "/current/" + sector + ".ir.gif";
					zoom_level_ir++;
				} else if ( zoom_level_ir == 3 )
				{
					img_url = "http://climate.cod.edu/data/satellite/regional/usa/current/usa.ir.gif";
					zoom_level_ir = 1;
				}
				prod = "2k";
				new GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				return true;
			case R.id.action_wv:
				setTitle(state+ " WV");
				img.resetZoom();
				if ( state.equals("HI") || state.equals("AK") )
				{
					zoom_level_wv = 2;
				}
				if ( zoom_level_wv == 1 )
				{
					img_url = "http://climate.cod.edu/data/satellite/2km/" + state + "/current/" + state + ".wv.gif" ;
					zoom_level_wv++;
				} else if ( zoom_level_wv == 2 )
				{
					img_url = "http://climate.cod.edu/data/satellite/regional/" + sector + "/current/" + sector + ".wv.gif";
					zoom_level_wv++;
				} else if ( zoom_level_wv == 3 )
				{
					img_url = "http://climate.cod.edu/data/satellite/regional/usa/current/usa.wv.gif";
					zoom_level_wv = 1;
				}
				prod = "2k";
				new GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				return true;

			case R.id.action_fav:
				ToggleFavorite();
				return true;

			case R.id.action_CMH:
				rid1="CMH";
				prod="TR0";
				setTitle( prod);
				RIDMapSwitch(rid1);
				new GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				return true;
			case R.id.action_CVG:
				rid1="CVG";
				prod="TR0";
				setTitle( prod);
				RIDMapSwitch(rid1);
				new GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				return true;
			case R.id.action_DAL:
				rid1="DAL";
				prod="TR0";
				setTitle( prod);
				RIDMapSwitch(rid1);
				new GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				return true;
			case R.id.action_DAY:
				rid1="DAY";
				prod="TR0";
				setTitle( prod);
				new GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				return true;
			case R.id.action_EWR:
				rid1="EWR";
				prod="TR0";
				RIDMapSwitch(rid1);
				setTitle( prod);
				new GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				return true;
			case R.id.action_FLL:
				rid1="FLL";
				prod="TR0";
				RIDMapSwitch(rid1);
				new GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				return true;
			case R.id.action_IAD:
				rid1="IAD";
				prod="TR0";
				RIDMapSwitch(rid1);
				setTitle( prod);
				new GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				return true;

			case R.id.action_IAH:
				rid1="IAH";
				prod="TR0";
				RIDMapSwitch(rid1);
				setTitle( prod);
				new GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				return true;
			case R.id.action_MDW:
				rid1="MDW";
				prod="TR0";
				RIDMapSwitch(rid1);
				setTitle( prod);
				new GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				return true;
			case R.id.action_PBI:
				rid1="PBI";
				prod="TR0";
				setTitle( prod);
				new GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				return true;

			case R.id.action_spotters:
				if ( spotters )
				{
					spotters=false;
					new GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				} else
				{
					spotters=true;
					new GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				}
				return true;

			case R.id.action_ridmap:

				LayoutParams params_iv =  mImageMap.getLayoutParams();
				params_iv.height=MyApplication.dm.heightPixels-actionBarHeight;
				params_iv.width=MyApplication.dm.widthPixels;
				mImageMap.setLayoutParams(params_iv);

				mImageMap.setVisibility(View.VISIBLE);
				img.setVisibility(View.GONE);

				mImageMap.addOnImageMapClickedHandler(new ImageMap.OnImageMapClickedHandler() {
					@Override
					public void onImageMapClicked(int id,ImageMap im2 ) {

						RIDMapSwitch(UtilityImageMap.MaptoRid(id));



					}

					@Override
					public void onBubbleClicked(int id) {
						switch(id){

							case R.id.BOX:
								rid1="BOX";
								new GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

						}
					}
				});


				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	} // end onOptionsItemSelected


	private class AnimateRadarMosaic extends AsyncTask<String, String, String> {


		@Override
		protected String doInBackground(String... params) {

			animDrawable = UtilityImgAnim.GetCODMosaicAnim ( img_url, "rad", params[0]);

			return "Executed";
		}

		@Override
		protected void onPostExecute(String result) {


			if ( rid_fav.contains(":" + rid1 + ":"))
			{
				star.setIcon(MyApplication.star_icon);

			} else
			{
				star.setIcon(MyApplication.star_outline_icon);
			}

			mImageMap.setVisibility(View.GONE);
			img.setVisibility(View.VISIBLE);


			img.setImageDrawable(animDrawable);
			animDrawable.setOneShot(false);
			animDrawable.start();
			anim_ran = true;

		}

		@Override
		protected void onPreExecute() {

		}

		@Override
		protected void onProgressUpdate(String... values) {}
	}


	private void RIDMapSwitch(String r)
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

		rid1=r;

		rid_arr[0] = rid1;
		String rid_loc = preferences.getString("RID_LOC_"+rid_arr[0],"");
		rid_arr_loc[0]=rid_arr[0]+" "+rid_loc;


		dataAdapter = new ArrayAdapter<String>(this,R.layout.simple_spinner_item, rid_arr_loc);
		dataAdapter.setDropDownViewResource(spinner_layout);
		spinner1.setAdapter(dataAdapter);
		spinner1.setSelection(0);

	}

	private void ToggleFavorite()
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = preferences.edit();
		rid_fav = preferences.getString("RID_FAV"," : : :");

		if ( rid_fav.contains(rid1))
		{
			rid_fav = rid_fav.replaceAll(rid1  + ":", "");
			star.setIcon(MyApplication.star_outline_icon);


		} else
		{
			rid_fav = rid_fav + rid1 + ":";
			star.setIcon(MyApplication.star_icon);

		}

		editor.putString("RID_FAV", rid_fav);
		editor.apply();

		String nexrad_last = preferences.getString("NEXRAD_LAST","ZZZ");
		int pos=0;

		rid_arr = rid_fav.split(":");

		rid_arr[0] = turl[0];
		rid_arr[1] = "Add";
		rid_arr[2] = "Remove";

		rid_arr_loc = new String[rid_arr.length];

		int k;
		for (k=0;k<rid_arr.length;k++)
		{
			String rid_loc = preferences.getString("RID_LOC_"+rid_arr[k],"");

			if (nexrad_last.equals(rid_arr[k]))
			{
				pos=k;
			}

			if (nexrad_last.equals("Add") || nexrad_last.equals("Remove")  )
			{
				pos=0;
			}

			rid_arr_loc[k]=rid_arr[k]+" "+rid_loc;

		}

		dataAdapter = new ArrayAdapter<String>(this,R.layout.simple_spinner_item, rid_arr_loc);
		dataAdapter.setDropDownViewResource(spinner_layout);
		spinner1.setAdapter(dataAdapter);

		spinner1.setSelection(pos);

	}


	private void ShowRadarScanInfo()
	{
		String info = MyApplication.preferences!!.getString("WX_RADAR_CURRENT_INFO","");
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder
				.setMessage(info)
				.setIcon(R.drawable.wx)
				.setCancelable(false)
				.setNegativeButton("OK",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						dialog.cancel();
					}
				});
		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();

	}




	public void onItemSelected(AdapterView<?> parent, View view,
							   int pos, long id) {

		String rid_loc = MyApplication.preferences!!.getString("RID_LOC_"+rid_arr[pos],"");

		if (pos==1 )
		{
			Intent i = new Intent(getApplicationContext(),  FavAddActivity.class);
			i.putExtra(FavAddActivity.TYPE,
					new String[] {"RID"});
			startActivity(i);
		}else if (pos==2 )
		{
			Intent i = new Intent(getApplicationContext(),  FavRemoveActivity.class);
			i.putExtra(FavRemoveActivity.TYPE,
					new String[] {"RID"});
			startActivity(i);
		} else {
			MyApplication.editor!!.putString("NEXRAD_LAST", rid_arr[pos]);
			MyApplication.editor!!.commit();
			String old_state = state;
			String old_sector = sector;
			String old_onek = onek;
			String[] nws_location_arr = rid_loc.split(",");
			state = nws_location_arr[0];
			sector = MyApplication.preferences!!.getString("COD_SECTOR_"+state,"");
			state = MyApplication.preferences!!.getString("STATE_CODE_"+state,"");
			onek = MyApplication.preferences!!.getString("COD_1KM_"+rid_arr[pos],"");

			if ( prod.equals("2k"))
			{
				img_url = img_url.replaceAll(old_sector,sector);
				img_url = img_url.replaceAll(old_state, state);
				img_url = img_url.replaceAll(old_onek, onek);
			}
			rid1 = rid_arr[pos];
			String tdwr_s = WXGLNexrad.GetTDWRFromRID(rid1);

			if ( tdwr_s.equals("") )
			{
				tdwr.setVisible(false);

			} else
			{
				tdwr.setVisible(true);
			}

			if ( prod.contains("L2") )
			{
				anim.setVisible(false);
			} else
			{
				anim.setVisible(true);
			}
			img.resetZoom();
			img.setZoom(init_zoom);

			new GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}


	}

	public void onNothingSelected(AdapterView<?> parent) {
	}


	@Override
	public void onClick(View v) {
		switch(v.getId()){
			case R.id.iv:
				UtilityToolbar.ShowHide( toolbar, toolbar_bottom);

				break;
		}
	}


}

