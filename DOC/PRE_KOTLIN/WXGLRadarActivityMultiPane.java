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

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import androidx.core.content.ContextCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener;
import android.os.Handler;

import joshuatee.wx.R;
import joshuatee.wx.activitiesmisc.ImageShowActivity;
import joshuatee.wx.activitiesmisc.USAlertsDetailActivity;
import joshuatee.wx.activitiesmisc.WebscreenABModels;
import joshuatee.wx.external.UtilityStringExternal;
import joshuatee.wx.settings.UtilityLocation;
import joshuatee.wx.ui.ObjectDialogue;
import joshuatee.wx.ui.UtilityToolbar;
import joshuatee.wx.util.ImageMap;
import joshuatee.wx.MyApplication;
import joshuatee.wx.util.Utility;
import joshuatee.wx.util.UtilityAlertDialog;
import joshuatee.wx.util.UtilityDownload;
import joshuatee.wx.util.UtilityFileManagement;
import joshuatee.wx.util.UtilityImageMap;
import joshuatee.wx.util.UtilityImg;
import joshuatee.wx.util.UtilityLog;
import joshuatee.wx.util.UtilityMath;
import joshuatee.wx.ui.UtilityUI;
import joshuatee.wx.settings.SettingsRadarActivity;
import joshuatee.wx.util.UtilityShare;
import joshuatee.wx.util.UtilityString;

import joshuatee.wx.util.UtilityArray;

public class WXGLRadarActivityMultiPane extends VideoRecordActivity implements OnMenuItemClickListener {

	// This activity is a general purpose viewer of nexrad and mosaic content
	// nexrad data is downloaded from NWS FTP, decoded and drawn using OpenGL ES
	// Unlike the traditional viewer this one shows multiple nexrad radars at the same time
	// nexrad sites, products, zoom and x/y are saved on stop and restored on start
	//
	// Arguments
	// 1: RID
	// 2: State
	// 3: number of panes

	public static final String RID = "";

	private int numPanes =4;
	private int[] numPanesArr;
	private Handler mHandler;
	private int mInterval = 180000; // 180 seconds by default
	private int loopCount =0;
	private boolean animRan = false;
	private boolean ridChanged =true;
	private boolean restartedZoom = false;
	private boolean inOglAnim = false;
	private boolean inOglAnimPaused = false;
	private String[] infoArr;
	private boolean oglInView =true;
	private WXGLRender[] OGLRArr;
	private WXGLSurfaceView[] glviewArr;
	private String tilt = "0";
	private String[] rid1Arr;
	private String[] oldRidArr;
	private ImageMap mImageMap;
	private boolean mapShown = false;
	private MenuItem star ;
	private MenuItem anim;
	private int delay;
	private String frameCntStrGlobal;
	private String locXCurrent;
	private String locYCurrent;
	private String[] infoAnim;
	private String[] tmpArr1;
	private String[] tmpArr2;
	private String[] tmpArr3;
	private String[] tmpArr4;
	private final String[] latlonArr = new String[2];
	private double latD =0.0;
	private double lonD =0.0;
	private LocationManager locationManager;
	private boolean animTriggerDownloads =false;
	private int curRadar =0;
	private String[] prodArr;
	private Activity a;
	private int idxIntG;
	private final ArrayList<String> alertDialogStatusAl = new ArrayList<>();
	private Context contextg;
	private int idxIntAl;
	private String prefPrefix ="";
	private RelativeLayout[] rlArr;
	private WXGLTextObject[] wxgltextArr;
	private Activity act;
    private ObjectDialogue diaTdwr;
    private ObjectDialogue diaStatus;

    @Override
	public void onCreate(Bundle savedInstanceState) {

        String[] turl = getIntent().getStringArrayExtra(RID);
        numPanes =Integer.parseInt(turl[2]);
        numPanesArr = UtilityArray.INSTANCE.stride(numPanes);
        UtilityFileManagement.INSTANCE.deleteCacheFiles(this);

        if (numPanes ==2) {
            if (MyApplication.Companion.getRadarImmersiveMode() || MyApplication.Companion.getRadarToolbarTransparent())
                super.onCreate(savedInstanceState, R.layout.activity_uswxoglmultipane_immersive, R.menu.uswxoglradarmultipane, true,true);
            else
                super.onCreate(savedInstanceState, R.layout.activity_uswxoglmultipane, R.menu.uswxoglradarmultipane, true,true);
        } else {
            if (MyApplication.Companion.getRadarImmersiveMode() || MyApplication.Companion.getRadarToolbarTransparent())
                super.onCreate(savedInstanceState, R.layout.activity_uswxoglmultipane_quad_immersive, R.menu.uswxoglradarmultipane, true,true);
            else
                super.onCreate(savedInstanceState, R.layout.activity_uswxoglmultipane_quad, R.menu.uswxoglradarmultipane, true,true);
        }
        getToolbarBottom().setOnMenuItemClickListener(this);

		act=this;

		UtilityUI.INSTANCE.immersiveMode(this);

		rlArr = new RelativeLayout[numPanes];
		wxgltextArr = new WXGLTextObject[numPanes];

        locXCurrent = Utility.INSTANCE.getX();
        locYCurrent = Utility.INSTANCE.getY();

		prodArr = new String[numPanes];
		infoAnim = new String[numPanes];
		rid1Arr = new String[numPanes];
		oldRidArr = new String[numPanes];
		infoArr = new String[numPanes];
		OGLRArr = new WXGLRender[numPanes];
		glviewArr = new WXGLSurfaceView[numPanes];

		int width_divider;
		if (numPanes ==4) {
			width_divider =2;
			prefPrefix ="WXOGL_QUADPANE";
		} else {
			width_divider =1;
			prefPrefix ="WXOGL_DUALPANE";
		}

		contextg=this;
		alertDialogStatus();
		alertDialogTDWR();

		UtilityToolbar.INSTANCE.transparentToolbars(getToolbar(), getToolbarBottom());

		a = this;

		double[] latlonArrD = UtilityLocation.INSTANCE.getGPS(this);
		latD = latlonArrD[0];
		lonD = latlonArrD[1];

		Menu menu = getToolbarBottom().getMenu();
		star = menu.findItem(R.id.action_fav);
		anim = menu.findItem(R.id.action_a);
		MenuItem rad3 = menu.findItem(R.id.action_radar3);
		MenuItem rad4 = menu.findItem(R.id.action_radar4);
		if (numPanes ==2) {
			rad3.setVisible(false);
			rad4.setVisible(false);
		}

		if ( !MyApplication.Companion.getRadarImmersiveMode()) {
			MenuItem blank = menu.findItem(R.id.action_blank);
			blank.setVisible(false);
			menu.findItem(R.id.action_level3_blank).setVisible(false);
			menu.findItem(R.id.action_level2_blank).setVisible(false);
			menu.findItem(R.id.action_animate_blank).setVisible(false);
			menu.findItem(R.id.action_tilt_blank).setVisible(false);
			menu.findItem(R.id.action_tools_blank).setVisible(false);
		}

		if (android.os.Build.VERSION.SDK_INT<21)
			menu.findItem(R.id.action_share).setTitle("Share");

		mImageMap = (ImageMap) findViewById(R.id.map);
		mImageMap.setVisibility(View.GONE);

		delay = UtilityImg.INSTANCE.getAnimInterval();

		for ( int z : numPanesArr) {
			glviewArr[z] = new WXGLSurfaceView(this, width_divider, numPanes);
		}

		for ( int z : numPanesArr) {
			glviewArr[z].setIdxInt(z);
		}

		for ( int z : numPanesArr) {
			OGLRArr[z] = new WXGLRender(this);
		}

		for ( int z : numPanesArr) {
			OGLRArr[z].setRadarStatus(Integer.toString(z+1));
			OGLRArr[z].setIdxStr(Integer.toString(z+1));
		}

		if (numPanes ==4) {
			rlArr[0] = (RelativeLayout) findViewById(R.id.rl1);
			rlArr[1] = (RelativeLayout) findViewById(R.id.rl2);
			rlArr[2]= (RelativeLayout) findViewById(R.id.rl3);
			rlArr[3] = (RelativeLayout) findViewById(R.id.rl4);
			rlArr[0].addView(glviewArr[0]);
			rlArr[1].addView(glviewArr[1]);
			rlArr[2].addView(glviewArr[2]);
			rlArr[3].addView(glviewArr[3]);

			for ( int z : numPanesArr) {
				LayoutParams params = rlArr[z].getLayoutParams();

				if (Build.VERSION.SDK_INT >=19 && (MyApplication.Companion.getRadarImmersiveMode() || MyApplication.Companion.getRadarToolbarTransparent()))
					params.height = MyApplication.Companion.getDm().heightPixels/2 + UtilityUI.INSTANCE.getStatusBarHeight();
				else
					params.height = MyApplication.Companion.getDm().heightPixels/2- MyApplication.Companion.getActionBarHeight();

				if (Build.VERSION.SDK_INT >=19
						&& MyApplication.Companion.getRadarToolbarTransparent()
						&& !MyApplication.Companion.getRadarImmersiveMode()
						&& numPanes ==4)
					params.height = MyApplication.Companion.getDm().heightPixels/2 - UtilityUI.INSTANCE.getStatusBarHeight()/2;

				params.width = MyApplication.Companion.getDm().widthPixels/2;
			}

		} else if (numPanes ==2) {

			rlArr[0] = (RelativeLayout) findViewById(R.id.rl1);
			rlArr[1] = (RelativeLayout) findViewById(R.id.rl2);
			rlArr[0].addView(glviewArr[0]);
			rlArr[1].addView(glviewArr[1]);

			LayoutParams params = rlArr[0].getLayoutParams();
			params.height = MyApplication.Companion.getDm().heightPixels/2- MyApplication.Companion.getActionBarHeight();
			params.width = MyApplication.Companion.getDm().widthPixels;

			LayoutParams params2 = rlArr[1].getLayoutParams();
			params2.height = MyApplication.Companion.getDm().heightPixels/2- MyApplication.Companion.getActionBarHeight();
			params2.width = MyApplication.Companion.getDm().widthPixels;

		}

		for ( int z : numPanesArr) {
			initGLVIEW(glviewArr[z], OGLRArr[z]);
		}

		oglInView =true;

		for ( int z : numPanesArr) {
			rid1Arr[z] = MyApplication.Companion.getPreferences().getString(prefPrefix + "_RID" + Integer.toString(z + 1), turl[0]);
		}

		if (MyApplication.Companion.getDualpaneshareposn()) {

			for (int z : UtilityArray.INSTANCE.stride(1,numPanes)) {
				rid1Arr[z] = rid1Arr[0];
			}
		}

		for ( int z : numPanesArr) {
			oldRidArr[z] = "";
		}

		if (numPanes ==4) {
			prodArr[0] = MyApplication.Companion.getPreferences().getString(prefPrefix + "_PROD1", "N0Q");
			prodArr[1] = MyApplication.Companion.getPreferences().getString(prefPrefix + "_PROD2", "N0U");
			prodArr[2] = MyApplication.Companion.getPreferences().getString(prefPrefix + "_PROD3", "N0C");
			prodArr[3] = MyApplication.Companion.getPreferences().getString(prefPrefix + "_PROD4", "DVL");
		} else if (numPanes ==2) {
			prodArr[0] = MyApplication.Companion.getPreferences().getString(prefPrefix + "_PROD1", "N0Q");
			prodArr[1] = MyApplication.Companion.getPreferences().getString(prefPrefix + "_PROD2", "N0U");
		}

		OGLRArr[0].setZoom(MyApplication.Companion.getPreferences().getFloat(prefPrefix + "_ZOOM1", ((float) MyApplication.Companion.getWxoglSize()) / 10.0f));
		glviewArr[0].setScaleFactor(MyApplication.Companion.getPreferences().getFloat(prefPrefix + "_ZOOM1", ((float) MyApplication.Companion.getWxoglSize()) / 10.0f));
		OGLRArr[0].setX(MyApplication.Companion.getPreferences().getFloat(prefPrefix + "_X1", 0.0f));
		OGLRArr[0].setY(MyApplication.Companion.getPreferences().getFloat(prefPrefix + "_Y1", 0.0f));

		if (MyApplication.Companion.getDualpaneshareposn()) {

			for (int z : UtilityArray.INSTANCE.stride(1,numPanes)) {
				OGLRArr[z].setZoom(MyApplication.Companion.getPreferences().getFloat(prefPrefix + "_ZOOM1", ((float) MyApplication.Companion.getWxoglSize()) / 10.0f));
				glviewArr[z].setScaleFactor(glviewArr[0].getScaleFactor());
				OGLRArr[z].setX(OGLRArr[0].getX());
				OGLRArr[z].setY(OGLRArr[0].getY());
			}

		} else {

			for (int z : UtilityArray.INSTANCE.stride(1,numPanes)) {
				OGLRArr[z].setZoom(MyApplication.Companion.getPreferences().getFloat(prefPrefix + "_ZOOM" + Integer.toString(z+1), ((float) MyApplication.Companion.getWxoglSize()) / 10.0f));
				glviewArr[z].setScaleFactor(MyApplication.Companion.getPreferences().getFloat(prefPrefix + "_ZOOM"+ Integer.toString(z+1), ((float) MyApplication.Companion.getWxoglSize()) / 10.0f));
				OGLRArr[z].setX(MyApplication.Companion.getPreferences().getFloat(prefPrefix + "_X"+ Integer.toString(z+1), 0.0f));
				OGLRArr[z].setY(MyApplication.Companion.getPreferences().getFloat(prefPrefix + "_Y"+ Integer.toString(z+1), 0.0f));
			}
		}

		for ( int z : numPanesArr) {
			wxgltextArr[z] = new WXGLTextObject(this, rlArr[z], rid1Arr[z], glviewArr[z],
					OGLRArr[z], numPanes);
		}

		for ( int z : numPanesArr) {
			glviewArr[z].setWxgltextArr(wxgltextArr);
			wxgltextArr[z].initTV();
		}

		if (MyApplication.Companion.getWxoglSpotters() || MyApplication.Companion.getWxoglSpottersLabel())
			getContentSerial();
		else
			getContentParallel();

		if (MyApplication.Companion.getWxoglRadarAutorefresh()) {
			// 180000 is 3 min
			mInterval = 60000 * MyApplication.Companion.getPreferences().getInt("RADAR_REFRESH_INTERVAL", 3);

			// Acquire a reference to the system Location Manager
			locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
			// Register the listener with the Location Manager to receive location updates
			if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
					|| ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
				locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 20000, 30, locationListener);
			// Was GPS_PROVIDER
			// had tried 60000 and 60, changing to 0 0

			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			mHandler = new Handler();
			startRepeatingTask();
		}
	}

	protected void onRestart() {

		delay = UtilityImg.INSTANCE.getAnimInterval();

		inOglAnim = false;
		inOglAnimPaused = false;

		anim.setIcon(MyApplication.Companion.getICON_PLAY());
		restartedZoom = true;

		for ( int z : numPanesArr) {
			wxgltextArr[z].initTV();
			wxgltextArr[z].addTV();
		}

		// spotter code is serialized for now
		if (MyApplication.Companion.getWxoglSpotters() || MyApplication.Companion.getWxoglSpottersLabel())
			getContentSerial();
		else
			getContentParallel();

		if (MyApplication.Companion.getWxoglRadarAutorefresh()) {
			// 180000 is 3 min
			mInterval = 60000 * MyApplication.Companion.getPreferences().getInt("RADAR_REFRESH_INTERVAL", 3);

			// Acquire a reference to the system Location Manager
			locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
			// Register the listener with the Location Manager to receive location updates
			if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
					|| ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
				locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 20000, 30, locationListener);
			// Was GPS_PROVIDER
			// had tried 60000 and 60, changing to 0 0

			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			mHandler = new Handler();
			startRepeatingTask();
		}
		super.onRestart();
	}

	private class GetContent extends AsyncTask<String, String, String> {

		final WXGLSurfaceView glv;
		final WXGLRender ogl;
		final int z;

		public GetContent(WXGLSurfaceView glvg, WXGLRender OGLRg, int zee){
			this.glv = glvg;
			this.ogl = OGLRg;
			this.z = zee;
		}

		@Override
		protected void onPreExecute() {

			if ( (prodArr[z].equals("N0Q")|| prodArr[z].equals("N1Q")|| prodArr[z].equals("N2Q")|| prodArr[z].equals("N3Q")|| prodArr[z].equals("L2REF"))
					&& WXGLNexrad.INSTANCE.isRIDTDWR(rid1Arr[z]))
				prodArr[z]="TZL";
			if ( prodArr[z].equals("TZL") && !WXGLNexrad.INSTANCE.isRIDTDWR(rid1Arr[z]))
				prodArr[z]="N0Q";

			if ( (prodArr[z].equals("N0U")|| prodArr[z].equals("N1U")|| prodArr[z].equals("N2U")|| prodArr[z].equals("N3U")|| prodArr[z].equals("L2VEL"))
					&& WXGLNexrad.INSTANCE.isRIDTDWR(rid1Arr[z]))
				prodArr[z]="TV0";
			if ( prodArr[z].equals("TV0") && !WXGLNexrad.INSTANCE.isRIDTDWR(rid1Arr[z]))
				prodArr[z]="N0U";

			getToolbar().setSubtitle("");
			setToolbarTitle();

			initWXOGLGeom(rid1Arr[z], glv,ogl, z);
		}

		@Override
		protected String doInBackground(String... params) {

			ogl.ConstructPolygons(rid1Arr[z], prodArr[z], "", "",true);

			if ( MyApplication.Companion.getWxoglSpotters() || MyApplication.Companion.getWxoglSpottersLabel()) {
				ogl.constructSpotters();
			} else {
				ogl.deconstructSpotters();
			}

			if (MyApplication.Companion.getStiDefault())
				ogl.constructSTILines();
			else
				ogl.deconstructSTILines();

			if (MyApplication.Companion.getHiDefault())
				ogl.constructHI();
			else
				ogl.deconstructHI();

			if (MyApplication.Companion.getTvsDefault())
				ogl.constructTVS();
			else
				ogl.deconstructTVS();

			if (MyApplication.Companion.getLocdotFollowsGps()) {
				getGPSFromDouble();
				locXCurrent = latlonArr[0];
				locYCurrent = latlonArr[1];
			}

			if ( MyApplication.Companion.getCodLocdotDefault() || MyApplication.Companion.getLocdotFollowsGps())
				ogl.constructLocationDot(locXCurrent, locYCurrent,false);
			else
				ogl.deconstructLocationDot();

			return "Executed";
		}

		@Override
		protected void onPostExecute(String result) {

			if ( !oglInView) {
				mImageMap.setVisibility(View.GONE);
				GLVIEWShow();
				oglInView = true;
			}

			if ( ridChanged && !restartedZoom)
				ridChanged =false;

			if (restartedZoom) {
				restartedZoom = false;
				ridChanged =false;
			}

			if (MyApplication.Companion.getWxoglSpottersLabel())
				UtilityWXGLTextObject.INSTANCE.updateSpotterLabels(numPanes, wxgltextArr);

			glv.requestRender();
			setSubTitle();

			animRan = false;
		}
	}

	private class AnimateRadar extends AsyncTask<String, String, String> {

		@Override
		protected void onPreExecute() {

			if ( !oglInView) {
				mImageMap.setVisibility(View.GONE);
				GLVIEWShow();
				oglInView = true;
			}
			inOglAnim = true;
			animRan = true;
		}

		@Override
		protected String doInBackground(String... params) {

			File fh;
			long timeMilli;
			long priorTime;

			String frameCntStr = params[0];
			frameCntStrGlobal =frameCntStr;

			String[][] animArray = new String[numPanes][Integer.parseInt(frameCntStr)];

			for ( int z : numPanesArr) {
				animArray[z] = OGLRArr[z].getRdDownload().getRadarByFTPAnimation(getBaseContext(), frameCntStr);
				for ( int r : UtilityArray.INSTANCE.stride(animArray[z].length)) {
					fh = new File(getBaseContext().getFilesDir(), animArray[z][r]);
					getBaseContext().deleteFile(Integer.toString(z+1) + prodArr[z] + "nexrad_anim" + Integer.toString(r));
					if ( ! fh.renameTo(new File(getBaseContext().getFilesDir(), Integer.toString(z+1) +
							prodArr[z] + "nexrad_anim" + Integer.toString(r))))
						UtilityLog.INSTANCE.d("wx","Problem moving to " + Integer.toString(z+1) + prodArr[z] + "nexrad_anim" + Integer.toString(r));
				}
			}

			int loopCnt=0;
			while (inOglAnim) {
				if (animTriggerDownloads) {
					for ( int z : numPanesArr) {
						animArray[z] = OGLRArr[z].getRdDownload().getRadarByFTPAnimation(getBaseContext(), frameCntStr);
						for ( int r : UtilityArray.INSTANCE.stride(animArray[z].length)) {
							fh = new File(getBaseContext().getFilesDir(), animArray[z][r]);
							getBaseContext().deleteFile(Integer.toString(z+1) + prodArr[z] + "nexrad_anim" + Integer.toString(r));
							if ( ! fh.renameTo(new File(getBaseContext().getFilesDir(), Integer.toString(z+1) +
									prodArr[z] + "nexrad_anim" + Integer.toString(r))))
								UtilityLog.INSTANCE.d("wx","Problem moving to " + Integer.toString(z+1) + prodArr[z] + "nexrad_anim" + Integer.toString(r));
						}
					}
					animTriggerDownloads =false;
				}

				for ( int r : UtilityArray.INSTANCE.stride(animArray[0].length)) {
					while (inOglAnimPaused)
						SystemClock.sleep(delay);

					// formerly priorTime was set at the end but that is goofed up with pause
					priorTime = System.currentTimeMillis();

					// added because if paused and then another icon life vel/ref it won't load correctly, likely
					// timing issue
					if  (!inOglAnim)
						break;

					if (loopCnt>0) {
						for ( int z : numPanesArr) {
							OGLRArr[z].ConstructPolygons(rid1Arr[z], prodArr[z], Integer.toString(z + 1) +
									prodArr[z] + "nexrad_anim" + Integer.toString(r), "", false);
						}
					} else {
						for ( int z : numPanesArr) {
							OGLRArr[z].ConstructPolygons(rid1Arr[z], prodArr[z], Integer.toString(z + 1) +
									prodArr[z] + "nexrad_anim" + Integer.toString(r), "", true);
						}
					}

					publishProgress(Integer.toString(r + 1), Integer.toString(animArray[0].length));

					for ( int z : numPanesArr) {
						glviewArr[z].requestRender();
					}

					timeMilli = System.currentTimeMillis();

					if ( (timeMilli-priorTime) < delay)
						SystemClock.sleep( delay - ((timeMilli-priorTime)));

					if  (!inOglAnim)
						break;

					if ( r == (animArray[0].length-1))
						SystemClock.sleep(delay*2);
				}
				loopCnt++;
			}
			return "Executed";
		}

		protected void onProgressUpdate(String... progress) {
			//This method runs on the UI thread, it receives progress updates
			//from the background thread and publishes them to the status bar
			//mNotificationHelper.progressUpdate(progress[0]);

			if (Integer.parseInt(progress[1]) > 1) {
				setSubTitle(progress[0], progress[1]);
			} else {
				getToolbar().setSubtitle("Problem downloading");
			}
		}

		@Override
		protected void onPostExecute(String result) {

			UtilityFileManagement.INSTANCE.deleteCacheFiles(getBaseContext());
		}
	}

	public void onWindowFocusChanged(boolean hasFocus) {

		super.onWindowFocusChanged(hasFocus);
		UtilityUI.INSTANCE.immersiveMode(this);
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {

		UtilityUI.INSTANCE.immersiveMode(this);

		if (inOglAnim && (item.getItemId()!=R.id.action_fav)
				&& (item.getItemId()!=R.id.action_share)
				&& (item.getItemId()!=R.id.action_tools)
				) {
			inOglAnim = false;
			inOglAnimPaused = false;

			// if an L2 anim is in process sleep for 1 second to let the current decode/render finish
			// otherwise the new selection might overwrite in the OGLR object - hack

			// (revert) 2016_08 have this apply to Level 3 in addition to Level 2
			if (prodArr[0].contains("L2") || prodArr[1].contains("L2"))
				SystemClock.sleep(2000);

			anim.setIcon(MyApplication.Companion.getICON_PLAY());
			if (item.getItemId()==R.id.action_a)
				return true;
		}

		switch (item.getItemId()) {
			case R.id.action_help:
				UtilityAlertDialog.INSTANCE.showHelpText(MyApplication.Companion.getRes().getString(R.string.help_radar)
								+ MyApplication.Companion.getNewline() + MyApplication.Companion.getNewline()
								+ MyApplication.Companion.getRes().getString(R.string.help_radar_drawingtools)
								+ MyApplication.Companion.getNewline() + MyApplication.Companion.getNewline()
								+ MyApplication.Companion.getRes().getString(R.string.help_radar_recording)
								+ MyApplication.Companion.getNewline() + MyApplication.Companion.getNewline()
						,this);
				return true;
			case R.id.action_share:
				if (android.os.Build.VERSION.SDK_INT>20) {
					if (isStoragePermissionGranted()) {
						if (android.os.Build.VERSION.SDK_INT > 22)
							checkDrawOverlayPermission();
						else
							fireScreenCaptureIntent();
					}
				} else {

					if (animRan) {
						AnimationDrawable animDrawable = UtilityUSImgWX.INSTANCE.animationFromFiles(this, rid1Arr[curRadar], prodArr[curRadar],
								frameCntStrGlobal, Integer.toString(curRadar + 1));

						UtilityShare.shareAnimGif(this,
								rid1Arr[curRadar] + " (" +
										MyApplication.Companion.getPreferences().getString("RID_LOC_" + rid1Arr[curRadar], "")
										+ ") " + prodArr[curRadar], animDrawable);
					} else {
						UtilityShare.shareBitmap(this, rid1Arr[curRadar]  +
										" (" + MyApplication.Companion.getPreferences().getString("RID_LOC_"+ rid1Arr[curRadar] ,"") + ") "
										+ prodArr[curRadar], UtilityUSImgWX.INSTANCE.LayeredImgFromFile(getApplicationContext(), rid1Arr[curRadar], prodArr[curRadar],"0"));
					}
				}
				return true;
			case R.id.action_settings:
				Intent intent=new Intent(this,SettingsRadarActivity.class);
				startActivity(intent);
				return true;
            case R.id.action_radar_markers:
                intent = new Intent(this, ImageShowActivity.class);
                intent.putExtra(ImageShowActivity.Companion.getURL(),
                        new String[] {"raw:radar_legend", "Radar Markers", "false"});
                startActivity(intent);
                return true;
			case R.id.action_radar_site_status_l3:
				Intent i_radar_status = new Intent(this, WebscreenABModels.class);
				i_radar_status.putExtra(WebscreenABModels.Companion.getURL(),
						new String[] {	"http://radar3pub.ncep.noaa.gov",
								MyApplication.Companion.getRes().getString(R.string.action_radar_site_status_l3)});
				startActivity(i_radar_status);
				return true;
			case R.id.action_radar_site_status_l2:
				i_radar_status = new Intent(this, WebscreenABModels.class);
				i_radar_status.putExtra(WebscreenABModels.Companion.getURL(),
						new String[] {	"http://radar2pub.ncep.noaa.gov",
								MyApplication.Companion.getRes().getString(R.string.action_radar_site_status_l2)});
				startActivity(i_radar_status);
				return true;
			case R.id.action_radar1:
				curRadar =0;
				idxIntAl =0;
				setToolbarTitle();
				return true;
			case R.id.action_radar2:
				curRadar =1;
				idxIntAl =1;
				setToolbarTitle();
				return true;
			case R.id.action_radar3:
				curRadar =2;
				idxIntAl =2;
				setToolbarTitle();
				return true;
			case R.id.action_radar4:
				curRadar =3;
				idxIntAl =3;
				setToolbarTitle();
				return true;
			case R.id.action_n0q:
				if(!WXGLNexrad.INSTANCE.isRIDTDWR(rid1Arr[curRadar])) {
					prodArr[curRadar] = "N" + tilt + "Q";
				} else {
					prodArr[curRadar] = "TZL";
				}
				getContentIntelligent();
				return true;
			case R.id.action_n0u:
				if(!WXGLNexrad.INSTANCE.isRIDTDWR(rid1Arr[curRadar])) {
					prodArr[curRadar]  = "N" + tilt + "U";
				} else{
					prodArr[curRadar]  = "TV" + tilt;
				}
				getContentIntelligent();
				return true;
			case R.id.action_n0s:
				prodArr[curRadar] = "N" + tilt + "S";
				getContentIntelligent();
				return true;
			case R.id.action_net:
				prodArr[curRadar] = "EET";
				getContentIntelligent();
				return true;
			case R.id.action_N0X:
				prodArr[curRadar] = "N" + tilt + "X";
				getContentIntelligent();
				return true;
			case R.id.action_N0C:
				prodArr[curRadar] = "N" + tilt + "C";
				getContentIntelligent();
				return true;
			case R.id.action_N0K:
				prodArr[curRadar] = "N" + tilt + "K";
				getContentIntelligent();
				return true;
			case R.id.action_H0C:
				prodArr[curRadar] = "H" + tilt + "C";
				getContentIntelligent();
				return true;
			case R.id.action_about:
				showRadarScanInfo();
				return true;
			case R.id.action_vil:
				prodArr[curRadar] = "DVL";
				getContentIntelligent();
				return true;
			case R.id.action_dsp:
				prodArr[curRadar] = "DSA";
				getContentIntelligent();
				return true;
			case R.id.action_daa:
				prodArr[curRadar] = "DAA";
				getContentIntelligent();
				return true;
			case R.id.action_l2vel:
				prodArr[curRadar] = "L2VEL";
				new GetContent(glviewArr[curRadar], OGLRArr[curRadar], curRadar).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				return true;
			case R.id.action_l2ref:
				prodArr[curRadar] = "L2REF";
				getContentIntelligent();
				return true;
			case R.id.action_tilt1:
				tilt = "0";
				prodArr[curRadar] = prodArr[curRadar].replaceAll("N[0-3]", "N" + tilt);
				setTitle(prodArr[curRadar]);
				new GetContent(glviewArr[curRadar], OGLRArr[curRadar], curRadar).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				return true;
			case R.id.action_tilt2:
				tilt = "1";
				prodArr[curRadar] = prodArr[curRadar].replaceAll("N[0-3]", "N" + tilt);
				setTitle(prodArr[curRadar]);
				new GetContent(glviewArr[curRadar], OGLRArr[curRadar], curRadar).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				return true;
			case R.id.action_tilt3:
				tilt = "2";
				prodArr[curRadar] = prodArr[curRadar].replaceAll("N[0-3]", "N" + tilt);
				setTitle(prodArr[curRadar]);
				new GetContent(glviewArr[curRadar], OGLRArr[curRadar], curRadar).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				return true;
			case R.id.action_tilt4:
				tilt = "3";
				prodArr[curRadar] = prodArr[curRadar].replaceAll("N[0-3]", "N" + tilt);
				setTitle(prodArr[curRadar]);
				new GetContent(glviewArr[curRadar], OGLRArr[curRadar], curRadar).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				return true;
			case R.id.action_a12:
				setTitle(prodArr[curRadar]);
				anim.setIcon(MyApplication.Companion.getICON_STOP());
				star.setIcon(MyApplication.Companion.getICON_PAUSE());
				new AnimateRadar().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"12", prodArr[curRadar]);
				return true;
			case R.id.action_a18:
				anim.setIcon(MyApplication.Companion.getICON_STOP());
				star.setIcon(MyApplication.Companion.getICON_PAUSE());
				new AnimateRadar().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"18", prodArr[curRadar]);
				return true;
			case R.id.action_a6_sm:
				anim.setIcon(MyApplication.Companion.getICON_STOP());
				star.setIcon(MyApplication.Companion.getICON_PAUSE());
				new AnimateRadar().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"6", prodArr[curRadar]);
				return true;
			case R.id.action_a:
				anim.setIcon(MyApplication.Companion.getICON_STOP());
				star.setIcon(MyApplication.Companion.getICON_PAUSE());
				new AnimateRadar().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, MyApplication.Companion.getUiAnimIconFrames(), prodArr[curRadar]);
				return true;
			case R.id.action_a36:
				anim.setIcon(MyApplication.Companion.getICON_STOP());
				star.setIcon(MyApplication.Companion.getICON_PAUSE());
				new AnimateRadar().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"36", prodArr[curRadar]);
				return true;
			case R.id.action_a72:
				anim.setIcon(MyApplication.Companion.getICON_STOP());
				star.setIcon(MyApplication.Companion.getICON_PAUSE());
				new AnimateRadar().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"72", prodArr[curRadar]);
				return true;
			case R.id.action_a144:
				anim.setIcon(MyApplication.Companion.getICON_STOP());
				star.setIcon(MyApplication.Companion.getICON_PAUSE());
				new AnimateRadar().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"144", prodArr[curRadar]);
				return true;
			case R.id.action_a3:
				anim.setIcon(MyApplication.Companion.getICON_STOP());
				star.setIcon(MyApplication.Companion.getICON_PAUSE());
				new AnimateRadar().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"3", prodArr[curRadar]);
				return true;
			case R.id.action_fav:
				if (inOglAnim) {
					if (!inOglAnimPaused) {
						// set icon to play
						star.setIcon(MyApplication.Companion.getICON_PLAY());
						inOglAnimPaused = true;
					} else {
						// set icon to pause
						star.setIcon(MyApplication.Companion.getICON_PAUSE());
						inOglAnimPaused = false;
					}
				}
				return true;
			case R.id.action_TDWR:
				diaTdwr.show();
				return true;
			case R.id.action_ridmap:
				LayoutParams params_iv = mImageMap.getLayoutParams();
				params_iv.height= MyApplication.Companion.getDm().heightPixels- getToolbar().getHeight() - getToolbarBottom().getHeight() - getStatusBarHeight();
				params_iv.width= MyApplication.Companion.getDm().widthPixels;
				mImageMap.setLayoutParams(params_iv);
				if(!mapShown) {
					mapShown =true;
					UtilityWXGLTextObject.INSTANCE.hideTV(numPanes, wxgltextArr);
					GLVIEWHide();
					mImageMap.setVisibility(View.VISIBLE);
					oglInView = false;
					mImageMap.addOnImageMapClickedHandler(new ImageMap.OnImageMapClickedHandler() {
						@Override
						public void onImageMapClicked(int id,ImageMap im2 ) {

							RIDMapSwitch(UtilityImageMap.INSTANCE.maptoRid(id));
						}

						@Override
						public void onBubbleClicked(int id) {
						}
					});
				} else {
					mapShown =false;
					mImageMap.setVisibility(View.GONE);
					UtilityWXGLTextObject.INSTANCE.showTV(numPanes, wxgltextArr);
					GLVIEWShow();
					oglInView =true;
				}
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void RIDMapSwitch(String r) {
		mapShown =false;

		UtilityWXGLTextObject.INSTANCE.showTV(numPanes, wxgltextArr);

		if (inOglAnim) {
			inOglAnim = false;
			inOglAnimPaused = false;

			// if an L2 anim is in process sleep for 1 second to let the current decode/render finish
			// otherwise the new selection might overwrite in the OGLR object - hack
			if (prodArr[0].contains("L2") || prodArr[1].contains("L2"))
				SystemClock.sleep(2000);

			anim.setIcon(MyApplication.Companion.getICON_PLAY());

		}

		if (MyApplication.Companion.getDualpaneshareposn()) {

			// if one long presses change the currently active radar as well
			curRadar = idxIntAl;

			for ( int z : numPanesArr) {
				rid1Arr[z] = r;
				OGLRArr[z].setZoom(((float) MyApplication.Companion.getWxoglSize()) / 10.0f);
				glviewArr[z].setScaleFactor(((float) MyApplication.Companion.getWxoglSize() / 10.0f));
				OGLRArr[z].setX(0.0f);
				OGLRArr[z].setY(0.0f);
			}
		} else {

			// if one long presses change the currently active radar as well
			curRadar = idxIntAl;

			rid1Arr[idxIntAl] = r;
			OGLRArr[idxIntAl].setZoom(((float) MyApplication.Companion.getWxoglSize()) / 10.0f);
			glviewArr[idxIntAl].setScaleFactor(((float) MyApplication.Companion.getWxoglSize() / 10.0f));
			OGLRArr[idxIntAl].setX(0.0f);
			OGLRArr[idxIntAl].setY(0.0f);
		}

		if (MyApplication.Companion.getWxoglSpotters() || MyApplication.Companion.getWxoglSpottersLabel())
			getContentSerial();
		else
			getContentParallel();
	}

	private void showRadarScanInfo() {
		String scanInfo="";
		for ( int z : numPanesArr) {
			infoArr[z] = MyApplication.Companion.getPreferences().getString("WX_RADAR_CURRENT_INFO" + Integer.toString(z + 1), "");
			scanInfo=scanInfo+ infoArr[z]+ MyApplication.Companion.getNewline() + MyApplication.Companion.getNewline();
		}
		UtilityAlertDialog.INSTANCE.showHelpText(scanInfo,this);
	}

	@Override
	public void onStop() {

		super.onStop();

		for ( int z : numPanesArr) {
			MyApplication.Companion.getEditor().putString(prefPrefix + "_RID" + Integer.toString(z+1), rid1Arr[z]);
			MyApplication.Companion.getEditor().putString(prefPrefix + "_PROD" + Integer.toString(z+1), prodArr[z]);
			MyApplication.Companion.getEditor().putFloat(prefPrefix + "_ZOOM" + Integer.toString(z+1), OGLRArr[z].getZoom());
			MyApplication.Companion.getEditor().putFloat(prefPrefix + "_X" + Integer.toString(z+1), OGLRArr[z].getX());
			MyApplication.Companion.getEditor().putFloat(prefPrefix + "_Y" + Integer.toString(z+1), OGLRArr[z].getY());
		}

		MyApplication.Companion.getEditor().apply();

		// otherwise cpu will spin with no fix but to kill app

		inOglAnim = false;

		if ( mHandler != null )
			stopRepeatingTask();

		if (locationManager != null) {
			if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
					|| ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
				locationManager.removeUpdates(locationListener);
		}
	}

	private final WXGLSurfaceView.OnProgressChangeListener changeListener = new WXGLSurfaceView.OnProgressChangeListener() {

		public void onProgressChanged(int code, int idx, int idx_int) { // remove 1st arg View v,

			idxIntAl =idx_int;

			if ( code != 50000) {

				alertDialogStatusAl.clear();
				double dist =0.0;
				double distRid =0.0;
				double locX, locY, pointX,pointY, ridX, ridY;

				try {
					locX = Double.parseDouble(locXCurrent);
					locY = Double.parseDouble(locYCurrent);
					pointX = (double) glviewArr[idx_int].getNewY();
					pointY = (double) (glviewArr[idx_int].getNewX() * -1);
					dist = UtilityMath.INSTANCE.distance(locX, locY, pointX, pointY, 'M');

					ridX = Double.parseDouble(MyApplication.Companion.getPreferences().getString("RID_" + rid1Arr[idxIntAl]+ "_X", "0.0"));
					ridY =  -1 * Double.parseDouble(MyApplication.Companion.getPreferences().getString("RID_" + rid1Arr[idxIntAl] + "_Y", "0.0"));
					distRid = UtilityMath.INSTANCE.distance(ridX, ridY, pointX, pointY, 'M');
				} catch (Exception e) {
					UtilityLog.INSTANCE.HandleException(e);
				}

                diaStatus.setTitle(UtilityStringExternal.INSTANCE.truncate(Double.toString(glviewArr[idx_int].getNewX()),6)
						+ ",-" + UtilityStringExternal.INSTANCE.truncate(Double.toString(glviewArr[idx_int].getNewY()),6));

				alertDialogStatusAl.add( UtilityStringExternal.INSTANCE.truncate(Double.toString(dist),6) + " miles from location");
				alertDialogStatusAl.add(UtilityStringExternal.INSTANCE.truncate(Double.toString(distRid), 6) + " miles from " + rid1Arr[idxIntAl]);

				for (RID ridAl : OGLRArr[idxIntAl].getRidNewList()) {
					alertDialogStatusAl.add("Radar: (" + Integer.toString(ridAl.getDistance())
							+ " mi) " + ridAl.getName() + " " + MyApplication.Companion.getPreferences().getString("RID_LOC_" + ridAl.getName(), ""));
				}

				alertDialogStatusAl.add("Show warning text");
				alertDialogStatusAl.add("Show nearest observation");
				alertDialogStatusAl.add("Show nearest meteogram");
				alertDialogStatusAl.add("Show radar status message");
                diaStatus.show();
			} else {
				for ( int z : numPanesArr) {
					wxgltextArr[z].addTV();
				}
			}
		}
	};

	private void initGLVIEW(WXGLSurfaceView glv, WXGLRender ogl) {
		glv.setEGLContextClientVersion(2);

		//glv.setEGLConfigChooser(8, 8, 8, 8, 16, 0); // a test to see if android emulator will now work

		glv.setRenderer(ogl);
		glv.setRenderVar(ogl, OGLRArr, glviewArr,act);
		glv.setFullScreen(false);
		glv.setOnProgressChangeListener(changeListener);
		glv.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		glv.setToolbar(getToolbar());
		glv.setToolbarBottom(getToolbarBottom());
	}

	private void initWXOGLGeom(String rid, WXGLSurfaceView glv, WXGLRender ogl, int z) {
		ogl.initGEOM(rid1Arr[z], prodArr[z]);

		final WXGLRender ogll = ogl;
		final WXGLSurfaceView glvv = glv;

		if (! oldRidArr[z].equals(rid1Arr[z])) // was rid for last arg, causing bug
		{
			ogl.setChunkCount(0);
			ogl.setChunkCountSti(0);
			ogl.setHiInit(false);
			ogl.setTvsInit(false);

			new Thread(new Runnable() {
				public void run(){
					ogll.constructStateLines();
					glvv.requestRender();
				}
			}).start();

			new Thread(new Runnable() {
				public void run(){
					if (MyApplication.Companion.getCodLakesDefault())
						ogll.constructLakes();
					else
						ogll.deconstructLakes();
				}
			}).start();

			new Thread(new Runnable() {
				public void run(){
					if (MyApplication.Companion.getCountyDefault()) {
						ogll.constructCounty();
						glvv.requestRender();
					}
					else
						ogll.deconstructCounty();
				}
			}).start();

			new Thread(new Runnable() {
				public void run(){
					if (MyApplication.Companion.getCodHwDefault()) {
						ogll.constructHWLines();
						glvv.requestRender();
					}
					else
						ogll.deconstructHWLines();
				}
			}).start();

			new Thread(new Runnable() {
				public void run(){
					if (MyApplication.Companion.getRadarHwEnhExt()) {
						ogll.constructHWEXTLines();
						glvv.requestRender();
					}
					else
						ogll.deconstructHWEXTLines();
				}
			}).start();

			wxgltextArr[z].setRid(rid);
			wxgltextArr[z].addTV();

			oldRidArr[z]=rid;
		}

		new Thread(new Runnable() {
			public void run(){
				if (MyApplication.Companion.getCodWarningsDefault())
					ogll.constructWarningFFWLines();
				else
					ogll.deconstructWarningFFWLines();

				if (MyApplication.Companion.getWatmcdDefault())
					ogll.constructWATMCDLines();
				else
					ogll.deconstructWATMCDLines();

				if (MyApplication.Companion.getMpdDefault())
					ogll.constructMPDLines();
				else
					ogll.deconstructMPDLines();

				glvv.requestRender();

			}
		}).start();

		if (MyApplication.Companion.getLocdotFollowsGps()) {
			getGPSFromDouble();
			locXCurrent = latlonArr[0];
			locYCurrent = latlonArr[1];
		}

		if ( MyApplication.Companion.getCodLocdotDefault() || MyApplication.Companion.getLocdotFollowsGps())
			ogl.constructLocationDot(locXCurrent, locYCurrent,false);
		else
			ogl.deconstructLocationDot();

		if (!mapShown) {
			mImageMap.setVisibility(View.GONE);

			if (numPanes ==4) {
				glviewArr[0].setVisibility(View.VISIBLE);
				glviewArr[1].setVisibility(View.VISIBLE);
				glviewArr[2].setVisibility(View.VISIBLE);
				glviewArr[3].setVisibility(View.VISIBLE);
			} else {
				glviewArr[0].setVisibility(View.VISIBLE);
				glviewArr[1].setVisibility(View.VISIBLE);
			}
		}
	}

	// inazaruk thanks http://stackoverflow.com/questions/6242268/repeat-a-task-with-a-time-delay

	private final Runnable mStatusChecker = new Runnable() {
		@Override
		public void run() {
			if ( loopCount > 0 ) {
				if (inOglAnim) {
					animTriggerDownloads =true;
				} else {
					for ( int z : numPanesArr) {
						new GetContent(glviewArr[z], OGLRArr[z], z).execute();
					}
				}
			}
			loopCount++;
			mHandler.postDelayed(mStatusChecker, mInterval);
		}
	};

	private void startRepeatingTask() {
		mStatusChecker.run();
	}

	private void stopRepeatingTask() {
		mHandler.removeCallbacks(mStatusChecker);
	}

	// considered best practive for glsurfaceview do the following

	public void  onPause() {
		for ( int z : numPanesArr) {
			glviewArr[z].onPause();
		}
		super.onPause();
	}

	public void  onResume() {
		for ( int z : numPanesArr) {
			glviewArr[z].onResume();
		}
		super.onResume();
	}

	// thanks Ben Clayton
	// http://stackoverflow.com/questions/3407256/height-of-status-bar-in-android

	private int getStatusBarHeight() {
		int result = 0;
		int resourceId = MyApplication.Companion.getRes().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			result = MyApplication.Companion.getRes().getDimensionPixelSize(resourceId);
		}
		return result;
	}

	private void setToolbarTitle() {
		if (numPanes ==4) {
			if (MyApplication.Companion.getDualpaneshareposn()) {

				setTitle(Integer.toString(curRadar + 1) + ":"
						+ rid1Arr[0] + "(" + prodArr[0] +
						"," + prodArr[1] +
						"," + prodArr[2] +
						"," + prodArr[3] + ")"
				);
			} else {
				setTitle(Integer.toString(curRadar + 1) + ": "
						+ rid1Arr[0] + "(" + prodArr[0] + ") "
						+ rid1Arr[1] + "(" + prodArr[1] + ") "
						+ rid1Arr[2] + "(" + prodArr[2] + ") "
						+ rid1Arr[3] + "(" + prodArr[3] + ")"
				);
			}
		} else if (numPanes ==2) {
			if (MyApplication.Companion.getDualpaneshareposn()) {

				setTitle(Integer.toString(curRadar + 1) + ":"
						+ rid1Arr[0] + "(" + prodArr[0] +
						"," + prodArr[1] + ")"
				);
			} else {
				setTitle(Integer.toString(curRadar + 1) + ": "
						+ rid1Arr[0] + "(" + prodArr[0] + ") "
						+ rid1Arr[1] + "(" + prodArr[1] + ") "
				);
			}
		}
	}

	// Define a listener that responds to location updates
	private final LocationListener locationListener = new LocationListener() {
		public void onLocationChanged(Location location) {
			// Called when a new location is found by the network location provider.
			makeUseOfNewLocation(location);
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {}

		public void onProviderEnabled(String provider) {}

		public void onProviderDisabled(String provider) {}
	};

	private void makeUseOfNewLocation(Location location) {
		latD = location.getLatitude();
		lonD = location.getLongitude();

		getGPSFromDouble();
		locXCurrent = latlonArr[0];
		locYCurrent = latlonArr[1];

		for ( int z : numPanesArr) {
			OGLRArr[z].constructLocationDot(locXCurrent, locYCurrent, false);
			glviewArr[z].requestRender();
		}
	}

	private void getGPSFromDouble() {
		try {
			latlonArr[0] = Double.toString(latD);
			latlonArr[1] = Double.toString(lonD);
		}   catch (Exception e) {
			UtilityLog.INSTANCE.HandleException(e);
		}
	}


	private void alertDialogStatus() {
		//alertDialogStatus = new AlertDialog.Builder(contextg);
		//final ArrayAdapter<String> arrayAdapterRadar = new ArrayAdapter<>(contextg, R.layout.simple_spinner_item, alertDialogStatusAl); // was simple_spinner_item
		//arrayAdapterRadar.setDropDownViewResource(MyApplication.spinnerLayout);

        diaStatus = new ObjectDialogue(contextg,alertDialogStatusAl);

        diaStatus.setNegativeButton( new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						UtilityUI.INSTANCE.immersiveMode(act);

					}
				});

        diaStatus.setSingleChoiceItems( new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String strName = alertDialogStatusAl.get(which);
						dialog.dismiss();
						if(strName.contains("Radar:"))
						{
							String ridNew = UtilityString.INSTANCE.parse(strName,"\\) ([A-Z]{3,4}) ");
							if (MyApplication.Companion.getDualpaneshareposn()) {
								for ( int z : numPanesArr) {
									rid1Arr[z] = ridNew;
									OGLRArr[z].setRid(ridNew);
								}
								ridChanged = true;
								RIDMapSwitch(rid1Arr[curRadar]);
							} else {
								rid1Arr[idxIntAl] = ridNew;
								OGLRArr[idxIntAl].setRid(ridNew);
								ridChanged = true;
								RIDMapSwitch(rid1Arr[idxIntAl]);
							}
						}  else if(strName.contains("Show warning text")) {
                            //UtilityWXOGL.showTextProducts(a, glviewArr[idxIntAl].newY, glviewArr[idxIntAl].newX * -1);
                            String polygonUrl = UtilityWXOGL.INSTANCE.showTextProducts( glviewArr[idxIntAl].getNewY(), glviewArr[idxIntAl].getNewX() * -1);
                            if(!polygonUrl.equals("")){
                                Intent intent = new Intent(getBaseContext(), USAlertsDetailActivity.class);
                                intent.putExtra(USAlertsDetailActivity.Companion.getURL(), new String[]{polygonUrl, ""});
                                startActivity(intent);
                            }
                        } else if(strName.contains("Show nearest observation")) {
							idxIntG = idxIntAl;
							new GetMetar().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
						} else if(strName.contains("Show nearest meteogram")) {
							// http://www.nws.noaa.gov/mdl/gfslamp/meteoform.php
							// http://www.nws.noaa.gov/mdl/gfslamp/meteo.php?BackHour=0&TempBox=Y&DewBox=Y&SkyBox=Y&WindSpdBox=Y&WindDirBox=Y&WindGustBox=Y&CigBox=Y&VisBox=Y&ObvBox=Y&PtypeBox=N&PopoBox=Y&LightningBox=Y&ConvBox=Y&sta=KTEW

							idxIntG = idxIntAl;
							String obsSite = UtilityMetar.INSTANCE.findClosestObs(glviewArr[idxIntG].getNewY(), glviewArr[idxIntG].getNewX() *-1);
							Intent intent = new Intent(getBaseContext(), ImageShowActivity.class);
							intent.putExtra(ImageShowActivity.Companion.getURL(), new String[] {
									"http://www.nws.noaa.gov/mdl/gfslamp/meteo.php?BackHour=0&TempBox=Y&DewBox=Y&SkyBox=Y&WindSpdBox=Y&WindDirBox=Y&WindGustBox=Y&CigBox=Y&VisBox=Y&ObvBox=Y&PtypeBox=N&PopoBox=Y&LightningBox=Y&ConvBox=Y&sta="
											+ obsSite,
									obsSite  + " Meteogram"});
							startActivity(intent);
						} else if(strName.contains("Show radar status message"))
							new GetRadarStatus().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
					}
				});

	}

	private void alertDialogTDWR() {
		//alertDialogTdwr = new AlertDialog.Builder(contextg);
		//final ArrayAdapter<String> arrayAdapterRadarTdwr = new ArrayAdapter<>(contextg, R.layout.simple_spinner_item, MyApplication.TDWR_RIDS); // was simple_spinner_item
		//arrayAdapterRadarTdwr.setDropDownViewResource(MyApplication.spinnerLayout);

        diaTdwr = new ObjectDialogue(contextg, MyApplication.Companion.getTDWR_RIDS());

        diaTdwr.setNegativeButton( new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						UtilityUI.INSTANCE.immersiveMode(act);
					}
				});

        diaTdwr.setSingleChoiceItems( new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String strName = MyApplication.Companion.getTDWR_RIDS()[which];
						rid1Arr[curRadar]= MyApplication.Companion.getSpace().split(strName)[0];

						if (prodArr[curRadar].equals("N0Q"))
							prodArr[curRadar] = "TZL";
						else
							prodArr[curRadar] = "TV0";
						RIDMapSwitch(rid1Arr[curRadar]);
						new GetContent(glviewArr[curRadar], OGLRArr[curRadar], curRadar).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
						dialog.dismiss();
					}
				});

	}

	private class GetRadarStatus extends AsyncTask<String, String, String> {

		String radarStatus ="";

		@Override
		protected String doInBackground(String... params) {
			radarStatus = UtilityDownload.INSTANCE.getRadarStatusMessage(rid1Arr[idxIntAl]);
			return "Executed";
		}

		@Override
		protected void onPostExecute(String result) {
			UtilityAlertDialog.INSTANCE.showHelpText(Utility.INSTANCE.fromHtml(radarStatus),a);
		}
	}

	private void getContentSerial() {
		for ( int z : numPanesArr) {
			new GetContent(glviewArr[z], OGLRArr[z], z).execute();
		}
	}

	private void getContentParallel() {
		for ( int z : numPanesArr) {
			new GetContent(glviewArr[z], OGLRArr[z], z).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
	}

	private void GLVIEWShow() {
		for ( int z : numPanesArr) {
			glviewArr[z].setVisibility(View.VISIBLE);
		}

		for ( int z : numPanesArr) {
			rlArr[z].setVisibility(View.VISIBLE);
		}
	}

	private void GLVIEWHide() {
		for ( int z : numPanesArr) {
			glviewArr[z].setVisibility(View.GONE);
		}

		for ( int z : numPanesArr) {
			rlArr[z].setVisibility(View.GONE);
		}
	}

	private void getContentIntelligent() {
		if (MyApplication.Companion.getDualpaneshareposn()) {
			if (MyApplication.Companion.getWxoglSpotters() || MyApplication.Companion.getWxoglSpottersLabel())
				getContentSerial();
			else
				getContentParallel();
		} else {
			new GetContent(glviewArr[curRadar], OGLRArr[curRadar], curRadar).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
	}

	private void setSubTitle() {
		if (numPanes ==4) {
			for ( int z : numPanesArr) {
				infoArr[z] = MyApplication.Companion.getPreferences().getString("WX_RADAR_CURRENT_INFO" + Integer.toString(z + 1), "");
			}

			tmpArr1 = MyApplication.Companion.getSpace().split(infoArr[0]);
			tmpArr2 = MyApplication.Companion.getSpace().split(infoArr[1]);
			tmpArr3 = MyApplication.Companion.getSpace().split(infoArr[2]);
			tmpArr4 = MyApplication.Companion.getSpace().split(infoArr[3]);

			if (tmpArr1.length > 3 && tmpArr2.length > 3 && tmpArr3.length > 3 && tmpArr4.length > 3)
				getToolbar().setSubtitle(tmpArr1[3] + "/" + tmpArr2[3] + "/" + tmpArr3[3] + "/" + tmpArr4[3]);
			else
				getToolbar().setSubtitle("");
		} else if (numPanes ==2) {
			for ( int z : numPanesArr) {
				infoArr[z] = MyApplication.Companion.getPreferences().getString("WX_RADAR_CURRENT_INFO" + Integer.toString(z + 1), "");
			}

			tmpArr1 = MyApplication.Companion.getSpace().split(infoArr[0]);
			tmpArr2 = MyApplication.Companion.getSpace().split(infoArr[1]);

			if (tmpArr1.length > 3 && tmpArr2.length > 3 )
				getToolbar().setSubtitle(tmpArr1[3] + "/" + tmpArr2[3]);
			else
				getToolbar().setSubtitle("");
		}
	}

	// used for animations - BUG: code is not used as animations are not working across all 4 ( commented out code not working )
	private void setSubTitle(String a, String b) {
		if (numPanes ==4) {
			infoAnim[0] = MyApplication.Companion.getPreferences().getString("WX_RADAR_CURRENT_INFO1", "");
			infoAnim[1] = MyApplication.Companion.getPreferences().getString("WX_RADAR_CURRENT_INFO2", "");
			infoAnim[2] = MyApplication.Companion.getPreferences().getString("WX_RADAR_CURRENT_INFO3", "");
			infoAnim[3] = MyApplication.Companion.getPreferences().getString("WX_RADAR_CURRENT_INFO4", "");

			tmpArr1 = MyApplication.Companion.getSpace().split(infoAnim[0]);
			tmpArr2 = MyApplication.Companion.getSpace().split(infoAnim[1]);
			tmpArr3 = MyApplication.Companion.getSpace().split(infoAnim[2]);
			tmpArr4 = MyApplication.Companion.getSpace().split(infoAnim[3]);

			if (tmpArr1.length > 3 && tmpArr2.length > 3 && tmpArr3.length > 3 && tmpArr4.length > 3)
				getToolbar().setSubtitle(tmpArr1[3] + "/" + tmpArr2[3] + "/"
						+ tmpArr3[3] + "/" + tmpArr4[3] + "(" + a + "/" + b + ")");
			else
				getToolbar().setSubtitle("");
		} else if (numPanes ==2) {
			infoAnim[0] = MyApplication.Companion.getPreferences().getString("WX_RADAR_CURRENT_INFO1", "");
			infoAnim[1] = MyApplication.Companion.getPreferences().getString("WX_RADAR_CURRENT_INFO2", "");

			tmpArr1 = MyApplication.Companion.getSpace().split(infoAnim[0]);
			tmpArr2 = MyApplication.Companion.getSpace().split(infoAnim[1]);

			if (tmpArr1.length > 3 && tmpArr2.length > 3 )
				getToolbar().setSubtitle(tmpArr1[3] + "/" + tmpArr2[3] + "/"
						+ "(" + a + "/" + b + ")");
			else
				getToolbar().setSubtitle("");
		}
	}

	private class GetMetar extends AsyncTask<String, String, String> {

		String txt;

		@Override
		protected String doInBackground(String... params) {

			txt = UtilityMetar.INSTANCE.findClosestMetar(glviewArr[idxIntG].getNewY(), glviewArr[idxIntG].getNewX() *-1);
			return "Executed";
		}

		@Override
		protected void onPostExecute(String result) {

			UtilityAlertDialog.INSTANCE.showHelpText(txt,a);
		}
	}
}

