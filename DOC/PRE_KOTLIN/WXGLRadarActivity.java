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
import android.os.Bundle;
import android.os.SystemClock;
import androidx.core.app.NavUtils;
import androidx.core.app.TaskStackBuilder;
import androidx.core.content.ContextCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.RelativeLayout;
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener;
import android.os.Handler;

import joshuatee.wx.R;
import joshuatee.wx.activitiesmisc.ImageShowActivity;
import joshuatee.wx.activitiesmisc.TextScreenActivity;
import joshuatee.wx.activitiesmisc.USAlertsDetailActivity;
import joshuatee.wx.activitiesmisc.WebscreenABModels;
import joshuatee.wx.external.UtilityStringExternal;
import joshuatee.wx.settings.UtilityLocation;
import joshuatee.wx.telecine.TelecineService;
import joshuatee.wx.ui.ObjectDialogue;
import joshuatee.wx.ui.ObjectSpinner;
import joshuatee.wx.ui.UtilityToolbar;
import joshuatee.wx.util.ImageMap;
import joshuatee.wx.MyApplication;
import joshuatee.wx.ui.TouchImageView2;
import joshuatee.wx.util.Utility;
import joshuatee.wx.util.UtilityAlertDialog;
import joshuatee.wx.util.UtilityDownload;
import joshuatee.wx.util.UtilityFavorites;
import joshuatee.wx.util.UtilityFileManagement;
import joshuatee.wx.util.UtilityImageMap;
import joshuatee.wx.util.UtilityImg;
import joshuatee.wx.util.UtilityLog;
import joshuatee.wx.util.UtilityMath;
import joshuatee.wx.ui.UtilityUI;
import joshuatee.wx.settings.FavAddActivity;
import joshuatee.wx.settings.FavRemoveActivity;
import joshuatee.wx.settings.SettingsRadarActivity;
import joshuatee.wx.util.UtilityShare;
import joshuatee.wx.util.UtilityString;

import joshuatee.wx.util.UtilityArray;

public class WXGLRadarActivity extends VideoRecordActivity implements OnItemSelectedListener,OnMenuItemClickListener {

	// This activity is a general purpose viewer of nexrad and mosaic content
	// nexrad data is downloaded from NWS FTP, decoded and drawn using OpenGL ES
	//
	//
	// Arguments
	// 1: RID
	// 2: State
	// 3: Product ( optional )
	// 4: Fixed site ( simply having a 4th arg will prevent remember location from working )
	// 4: URL String ( optional, archive )
	// 5: X ( optional, archive )
	// 6: Y ( optional, archive )
	//

	public static final String RID = "";
	private String oldProd ="";
	private boolean firstRun =true;
	private String oldRid ="";
	private Handler mHandler;
	private int mInterval = 180000; // 180 seconds by default
	private int loopCount =0;
	private boolean animRan = false;
	private boolean archiveMode =false;
	private boolean ridChanged =true;
	private boolean restartedZoom = false;
	private TouchImageView2 img ;
	private boolean firstTime = true;
	private boolean inOglAnim = false;
	private boolean inOglAnimPaused = false;
	private boolean oglInView =true;
	private static WXGLRender OGLR;
	private WXGLRender[] OGLRArr;
	private WXGLSurfaceView[] glviewArr;
	private boolean restarted = false;
	private boolean tiltOption = true;
	private WXGLSurfaceView glview;
    private String tilt = "0";
	private String imgUrl = "";
	private String prod = "";
	private String[] ridArrLoc;
	public static String rid1="";
	private String state="";
	private String sector = "";
	private String onek = "";
	private ImageMap mImageMap;
	private boolean mapShown = false;
	private MenuItem star ;
	private MenuItem anim;
	private MenuItem tiltMenu;
	private MenuItem l3Menu;
	private MenuItem l2Menu;
	private int delay;
	private final String prefTokenLocation = "RID_LOC_";
	private String frameCntStrGlobal;
	private String locXCurrent;
	private String locYCurrent;
	private String urlStr ="";
	private boolean fixedSite = false;
	private RelativeLayout rl;
	private final String[] latlonArr = new String[2];
	private double latD =0.0;
	private double lonD =0.0;
	private LocationManager locationManager;
	private boolean animTriggerDownloads =false;
	private Activity a;
	public static String spotterId;
	public static boolean spotterShowSelected = false;
	private final ArrayList<String> alertDialogStatusAl = new ArrayList<>();
	private Context contextg;
	private boolean legendShown =false;
	public static float dspLegendMax;
	private final int numPanes =1;
	private int[] numPanesArr;
	private WXGLTextObject[] wxgltextArr;
	private Activity act;
    private ObjectSpinner sp;
    private ObjectDialogue diaTdwr;
    private ObjectDialogue diaStatus;

    @Override
	public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState, R.layout.activity_uswxogl, R.menu.uswxoglradar, true,true);
        getToolbarBottom().setOnMenuItemClickListener(this);

        UtilityUI.INSTANCE.immersiveMode(this);

        act=this;
        spotterShowSelected = false;

        locXCurrent = Utility.INSTANCE.getX();
        locYCurrent = Utility.INSTANCE.getY();

        String[] turl = getIntent().getStringArrayExtra(RID);
		numPanesArr = UtilityArray.INSTANCE.stride(numPanes);

		UtilityFileManagement.INSTANCE.deleteCacheFiles(this);

		// for L2 archive called from storm reports

		if ( turl.length > 6 ) {
			urlStr =  turl[4];
			locXCurrent = turl[5];
			locYCurrent = turl[6];
			archiveMode = true;
		} else if (turl.length > 4) {
			spotterId = turl[4];
			spotterShowSelected = true;
		}

		if ( turl.length > 3 )
			fixedSite = true;

		contextg=this;
		alertDialogStatus();
		alertDialogTDWR();

		UtilityToolbar.INSTANCE.transparentToolbars(getToolbar(), getToolbarBottom());

		a=this;

		if ( archiveMode && !spotterShowSelected)
			getToolbarBottom().setVisibility(View.GONE);

		double[] latlon_arr_d = UtilityLocation.INSTANCE.getGPS(this);
		latD = latlon_arr_d[0];
		lonD = latlon_arr_d[1];

		Menu menu = getToolbarBottom().getMenu();
		star = menu.findItem(R.id.action_fav);
		anim = menu.findItem(R.id.action_a);
		tiltMenu = menu.findItem(R.id.action_tilt);
		l3Menu = menu.findItem(R.id.action_l3);
		l2Menu = menu.findItem(R.id.action_l2);

		if ( !MyApplication.Companion.getRadarImmersiveMode()) {
			MenuItem blank = menu.findItem(R.id.action_blank);
			blank.setVisible(false);
			menu.findItem(R.id.action_level3_blank).setVisible(false);
			menu.findItem(R.id.action_level2_blank).setVisible(false);
			menu.findItem(R.id.action_animate_blank).setVisible(false);
			menu.findItem(R.id.action_tilt_blank).setVisible(false);
			menu.findItem(R.id.action_tools_blank).setVisible(false);
		}

		if (android.os.Build.VERSION.SDK_INT>20)
			menu.findItem(R.id.action_jellybean_drawtools).setVisible(false);
		else
			menu.findItem(R.id.action_share).setTitle("Share");

		mImageMap = (ImageMap) findViewById(R.id.map);
		mImageMap.setVisibility(View.GONE);

		delay = UtilityImg.INSTANCE.getAnimInterval();

		img = (TouchImageView2) findViewById(R.id.iv);
		img.setMaxZoom(6.0f);

		prod = "N0Q";

		glview =  new WXGLSurfaceView(this,1, numPanes);

		rl = (RelativeLayout) findViewById(R.id.rl);
		rl.addView(glview);
		RelativeLayout[] rlArr = new RelativeLayout[1];
		rlArr[0]=rl;
		initGLVIEW();

		oglInView =true;

		rid1 = turl[0];

		// hack, in rare cases a user will save a location that doesn't pick up RID
		if (rid1.equals("") || rid1==null)
			rid1 = "TLX";

		state = turl[1];
		if ( turl.length > 2 ) {
			prod =  turl[2];
			if (prod.equals("N0R")) {
				prod = "N0Q";
			}
		}

		wxgltextArr = new WXGLTextObject[1];
		for ( int z : numPanesArr) {
			wxgltextArr[z] = new WXGLTextObject(this, rlArr[z], rid1, glviewArr[z],
					OGLRArr[z], numPanes);
		}

		for ( int z : numPanesArr) {
			glviewArr[z].setWxgltextArr(wxgltextArr);
			wxgltextArr[z].initTV();
		}

		if ( MyApplication.Companion.getWxoglRememberLocation() && !archiveMode && !fixedSite) {
			OGLR.setZoom(MyApplication.Companion.getWxoglZoom());
			glview.setScaleFactor(MyApplication.Companion.getWxoglZoom());
			if ( ! MyApplication.Companion.getWxoglRid().equals("") )
				rid1 = MyApplication.Companion.getWxoglRid();
			prod = MyApplication.Companion.getWxoglProd();
			OGLR.setX(MyApplication.Companion.getWxoglX());
			OGLR.setY(MyApplication.Companion.getWxoglY());
		}

		if(MyApplication.Companion.getRadarShowLegend())
			showLegend();

		setTitle(prod);
		ridArrLoc = UtilityFavorites.INSTANCE.setupFavMenu(MyApplication.Companion.getRidFav(),rid1, prefTokenLocation);
        sp = new ObjectSpinner(this,this,R.id.spinner1,ridArrLoc);
        sp.setOnItemSelectedListener(this);

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

		if ( MyApplication.Companion.getRidFav().contains(":" + rid1 + ":"))
			star.setIcon(MyApplication.Companion.getSTAR_ICON());
		else
			star.setIcon(MyApplication.Companion.getSTAR_OUTLINE_ICON());

		anim.setIcon(MyApplication.Companion.getICON_PLAY());

		restarted = true;
		restartedZoom = true;

		for ( int z : numPanesArr) {
			wxgltextArr[z].initTV();
			wxgltextArr[z].addTV();
		}

		// if the top toolbar is not showing then neither are showing and the only restart
		// is from an app switch or resume from sleep, therefore get content directly

		if( glview.getToolbarsHidden()) {
			new GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

		} else {

			ridArrLoc = UtilityFavorites.INSTANCE.setupFavMenu(MyApplication.Companion.getRidFav(), rid1, prefTokenLocation);
            sp.refreshData(getBaseContext(),ridArrLoc);
		}

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

		boolean ridIsTdwr;

		@Override
		protected void onPreExecute() {

			ridIsTdwr = WXGLNexrad.INSTANCE.isRIDTDWR(rid1);

			if (ridIsTdwr) {
				l3Menu.setVisible(false);
				l2Menu.setVisible(false);
			} else {
				l3Menu.setVisible(true);
				l2Menu.setVisible(true);
			}

			if ( (prod.equals("N0Q")||prod.equals("N1Q")||prod.equals("N2Q")||prod.equals("N3Q")||prod.equals("L2REF"))
					&& ridIsTdwr)
				prod="TZL";
			if ( prod.equals("TZL") && !ridIsTdwr)
				prod="N0Q";

			if ( (prod.equals("N0U")||prod.equals("N1U")||prod.equals("N2U")||prod.equals("N3U")||prod.equals("L2VEL"))
					&& ridIsTdwr)
				prod="TV0";
			if ( prod.equals("TV0") && !ridIsTdwr)
				prod="N0U";

			setTitle(prod);

			if ( MyApplication.Companion.getRidFav().contains(":" + rid1 + ":"))
				star.setIcon(MyApplication.Companion.getSTAR_ICON());
			else
				star.setIcon(MyApplication.Companion.getSTAR_OUTLINE_ICON());

			getToolbar().setSubtitle("");

			if ( ! prod.startsWith("2"))
				initWXOGLGeom(rid1);
		}

		@Override
		protected String doInBackground(String... params) {

			OGLR.ConstructPolygons(rid1, prod, "", urlStr,true);

            if ( (MyApplication.Companion.getWxoglSpotters() || MyApplication.Companion.getWxoglSpottersLabel()) && !archiveMode)
                OGLR.constructSpotters();
			else
				OGLR.deconstructSpotters();

			if ( MyApplication.Companion.getStiDefault() && !archiveMode)
				OGLR.constructSTILines();
			else
				OGLR.deconstructSTILines();

			if ( MyApplication.Companion.getHiDefault() && !archiveMode)
				OGLR.constructHI();
			else
				OGLR.deconstructHI();

			if ( MyApplication.Companion.getTvsDefault() && !archiveMode)
				OGLR.constructTVS();
			else
				OGLR.deconstructTVS();

			if (MyApplication.Companion.getLocdotFollowsGps() && !archiveMode) {
				getGPSFromDouble();
				locXCurrent = latlonArr[0];
				locYCurrent = latlonArr[1];
			}

			if ( MyApplication.Companion.getCodLocdotDefault() || archiveMode || MyApplication.Companion.getLocdotFollowsGps()) {
				OGLR.constructLocationDot(locXCurrent, locYCurrent, archiveMode);
			} else {
				OGLR.deconstructLocationDot();
			}

			if ( (MyApplication.Companion.getWxoglObs() || MyApplication.Companion.getWxoglObsWindbarbs())  && !archiveMode) {
				UtilityMetar.INSTANCE.getStateMetarArrayForWXOGL(rid1);
			}

			if ( MyApplication.Companion.getWxoglObsWindbarbs() && !archiveMode) {
				OGLR.constructWBLines();
			} else {
				OGLR.deconstructWBLines();
			}

			if ( MyApplication.Companion.getWxoglSwo() && !archiveMode) {
				UtilitySWOD1.INSTANCE.getSWO();
				OGLR.constructSWOLines();
			} else {
				OGLR.deconstructSWOLines();
			}

			return "Executed";
		}

		@Override
		protected void onPostExecute(String result) {

			if ( !oglInView) {
				mImageMap.setVisibility(View.GONE);
				img.setVisibility(View.GONE);
				glview.setVisibility(View.VISIBLE);
				oglInView = true;
			}

			if ( ridChanged && !restartedZoom) {
				ridChanged = false;
			}

			if (restartedZoom) {
				restartedZoom = false;
				ridChanged =false;
			}

			if (MyApplication.Companion.getWxoglSpottersLabel() && !archiveMode) {
				UtilityWXGLTextObject.INSTANCE.updateSpotterLabels(numPanes, wxgltextArr);
			}

			if ( (MyApplication.Companion.getWxoglObs() || MyApplication.Companion.getWxoglObsWindbarbs())  && !archiveMode) {
				UtilityWXGLTextObject.INSTANCE.updateObs(numPanes, wxgltextArr);
			}

			glview.requestRender();

			if ( legendShown && ! prod.equals(oldProd) && ! prod.equals("DSA") && ! prod.equals("DAA")) {
				updateLegend();
			}

			if (legendShown && (prod.equals("DSA")||prod.equals("DAA")) ) {
				dspLegendMax = (255f/OGLR.getRadarL3Object().getHalfword3132())*0.01f;
				updateLegend();
			}

			oldProd = prod;

			String info = MyApplication.Companion.getPreferences().getString("WX_RADAR_CURRENT_INFO", "");
			String[] tmp_arr = MyApplication.Companion.getSpace().split(info);

			if (tmp_arr.length>3)
				getToolbar().setSubtitle(tmp_arr[3]);
			else
				getToolbar().setSubtitle("");

			animRan = false;
			firstRun =false;
		}
	}

	private class AnimateRadar extends AsyncTask<String, String, String> {

		@Override
		protected void onPreExecute() {

			if ( !oglInView) {
				mImageMap.setVisibility(View.GONE);
				img.setVisibility(View.GONE);
				glview.setVisibility(View.VISIBLE);
				oglInView = true;
			}

			inOglAnim = true;
			animRan = true;
		}

		@Override
		protected String doInBackground(String... params) {

			String frameCntStr = params[0];
			frameCntStrGlobal =frameCntStr;
			String[] animArray = OGLR.getRdDownload().getRadarByFTPAnimation(getBaseContext(), frameCntStr);

			File fh;

			long timeMilli;
			long priorTime;

			for ( int r : UtilityArray.INSTANCE.stride(animArray.length)) {
				fh = new File(getBaseContext().getFilesDir(), animArray[r]);
				getBaseContext().deleteFile("nexrad_anim" + Integer.toString(r));
				if ( ! fh.renameTo(new File(getBaseContext().getFilesDir(),"nexrad_anim" + Integer.toString(r))))
					UtilityLog.INSTANCE.d("wx","Problem moving to " + "nexrad_anim" + Integer.toString(r));
			}

			int loop_cnt=0;
			while (inOglAnim) {
				if (animTriggerDownloads) {
					animArray = OGLR.getRdDownload().getRadarByFTPAnimation(getBaseContext(),frameCntStr);
					for ( int r : UtilityArray.INSTANCE.stride(animArray.length)) {
						fh = new File(getBaseContext().getFilesDir(), animArray[r]);
						getBaseContext().deleteFile("nexrad_anim" + Integer.toString(r));
						if ( ! fh.renameTo(new File(getBaseContext().getFilesDir(),"nexrad_anim" + Integer.toString(r))))
							UtilityLog.INSTANCE.d("wx","Problem moving to " + "nexrad_anim" + Integer.toString(r));
					}
					animTriggerDownloads =false;
				}

				for ( int r : UtilityArray.INSTANCE.stride(animArray.length)) {
					while (inOglAnimPaused)
						SystemClock.sleep(delay);

					// formerly priorTime was set at the end but that is goofed up with pause
					priorTime = System.currentTimeMillis();

					// added because if paused and then another icon life vel/ref it won't load correctly, likely
					// timing issue
					if  (!inOglAnim)
						break;

					// if the first pass has completed, for L2 no longer uncompress, use the existing decomp files
					if (loop_cnt>0)
						OGLR.ConstructPolygons(rid1, prod, "nexrad_anim" + Integer.toString(r), urlStr,false);
					else
						OGLR.ConstructPolygons(rid1, prod, "nexrad_anim" + Integer.toString(r), urlStr,true);


					publishProgress(Integer.toString(r+1), Integer.toString(animArray.length));

					glview.requestRender();

					timeMilli = System.currentTimeMillis();

					if ( (timeMilli-priorTime) < delay)
						SystemClock.sleep(delay - ((timeMilli - priorTime)));

					if  (!inOglAnim)
						break;

					if ( r == (animArray.length-1))
						SystemClock.sleep(delay*2);
				}
				loop_cnt++;
			}
			return "Executed";
		}

		protected void onProgressUpdate(String... progress) {
			//This method runs on the UI thread, it receives progress updates
			//from the background thread and publishes them to the status bar
			//mNotificationHelper.progressUpdate(progress[0]);

			if (Integer.parseInt(progress[1]) > 1) {
				String[] tmpArrAnim = MyApplication.Companion.getSpace().split(MyApplication.Companion.getPreferences().getString("WX_RADAR_CURRENT_INFO", ""));
				if (tmpArrAnim.length > 3)
					getToolbar().setSubtitle(tmpArrAnim[3] + " (" + progress[0] + "/" + progress[1] + ")");
				else
					getToolbar().setSubtitle("");
			}  else {
				getToolbar().setSubtitle("Problem downloading");
			}
		}
	}

	public void onWindowFocusChanged(boolean hasFocus) {

		super.onWindowFocusChanged(hasFocus);
		UtilityUI.INSTANCE.immersiveMode(this);
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {

		UtilityUI.INSTANCE.immersiveMode(this);

		if (inOglAnim && (item.getItemId() != R.id.action_fav)
				&& (item.getItemId() != R.id.action_share)
				&& (item.getItemId() != R.id.action_tools)
				) {
			inOglAnim = false;
			inOglAnimPaused = false;

			// if an L2 anim is in process sleep for 1 second to let the current decode/render finish
			// otherwise the new selection might overwrite in the OGLR object - hack

			// (revert) 2016_08 have this apply to Level 3 in addition to Level 2
			if (prod.contains("L2"))
				SystemClock.sleep(2000);

			if (MyApplication.Companion.getRidFav().contains(":" + rid1 + ":"))
				star.setIcon(MyApplication.Companion.getSTAR_ICON());
			else
				star.setIcon(MyApplication.Companion.getSTAR_OUTLINE_ICON());

			anim.setIcon(MyApplication.Companion.getICON_PLAY());
			if (item.getItemId() == R.id.action_a)
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
						, this);
				return true;
			case R.id.action_jellybean_drawtools:
				Intent tI = TelecineService.newIntent(this, 1, new Intent());
				tI.putExtra("show_distance_tool", getShow_distance_tool());
				tI.putExtra("show_recording_tools", "false");
				startService(tI);
				return true;
			case R.id.action_share:
				if (android.os.Build.VERSION.SDK_INT > 20) {

					setShow_distance_tool("true");
					if (isStoragePermissionGranted()) {
						if (android.os.Build.VERSION.SDK_INT > 22)
							checkDrawOverlayPermission();
						else
							fireScreenCaptureIntent();
					}
				} else {
					if (animRan) {
						AnimationDrawable animDrawable = UtilityUSImgWX.INSTANCE.animationFromFiles(this, rid1, prod, frameCntStrGlobal, "");

						UtilityShare.shareAnimGif(this,
								rid1 + " (" + MyApplication.Companion.getPreferences().getString("RID_LOC_" + rid1, "") + ") " + prod, animDrawable);
					} else {
						UtilityShare.shareBitmap(this, rid1 + " (" + MyApplication.Companion.getPreferences().getString("RID_LOC_" + rid1, "") + ") " + prod,
								UtilityUSImgWX.INSTANCE.LayeredImgFromFile(getApplicationContext(), rid1, prod, "0"));
					}
				}
				return true;
			case R.id.action_settings:
				Intent intent = new Intent(this, SettingsRadarActivity.class);
				startActivity(intent);
				return true;
            case R.id.action_radar_markers:
                intent = new Intent(this, ImageShowActivity.class);
                intent.putExtra(ImageShowActivity.Companion.getURL(),
                        new String[] {"raw:radar_legend", "Radar Markers", "false"});
                startActivity(intent);
                return true;
			case R.id.action_radar_site_status_l3:
				Intent iRadarStatus = new Intent(this, WebscreenABModels.class);
				iRadarStatus.putExtra(WebscreenABModels.Companion.getURL(),
						new String[]{"http://radar3pub.ncep.noaa.gov",
								MyApplication.Companion.getRes().getString(R.string.action_radar_site_status_l3)});
				startActivity(iRadarStatus);
				return true;
			case R.id.action_radar_site_status_l2:
				iRadarStatus = new Intent(this, WebscreenABModels.class);
				iRadarStatus.putExtra(WebscreenABModels.Companion.getURL(),
						new String[]{"http://radar2pub.ncep.noaa.gov",
								MyApplication.Companion.getRes().getString(R.string.action_radar_site_status_l2)});
				startActivity(iRadarStatus);
				return true;
			case R.id.action_n0q:
				if (MyApplication.Companion.getRadarIconsLevel2() && prod.matches("N[0-3]Q")) {
					prod = "L2REF";
					setTitle( prod);
					tiltOption = false;
				} else {
					if (!WXGLNexrad.INSTANCE.isRIDTDWR(rid1)) {
						prod = "N" + tilt + "Q";
						setTitle(prod);
						tiltOption = true;
					} else {
						prod = "TZL";
						setTitle(prod);
						tiltOption = false;
					}
				}
				new GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				return true;
			case R.id.action_n0u:
				if (MyApplication.Companion.getRadarIconsLevel2() &&  prod.matches("N[0-3]U")) {
					prod = "L2VEL";
					setTitle( prod);
					tiltOption = false;
				} else {
					if (!WXGLNexrad.INSTANCE.isRIDTDWR(rid1)) {
						prod = "N" + tilt + "U";
						setTitle(prod);
						tiltOption = true;
					} else {
						prod = "TV" + tilt;
						setTitle(prod);
						tiltOption = true;
					}
				}
				new GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				return true;
			case R.id.action_n0s:
				prod = "N" + tilt + "S";
				setTitle( prod);
				tiltOption = false;
				new GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				return true;
			case R.id.action_net:
				prod = "EET";
				setTitle( prod);
				tiltOption = false;
				new GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				return true;
			case R.id.action_N0X:
				prod = "N" + tilt + "X";
				setTitle(prod);
				tiltOption = true;
				new GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				return true;
			case R.id.action_N0C:
				prod = "N" + tilt + "C";
				setTitle(prod);
				tiltOption = true;
				new GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				return true;
			case R.id.action_N0K:
				prod = "N" + tilt + "K";
				setTitle(prod);
				tiltOption = true;
				new GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				return true;
			case R.id.action_H0C:
				prod = "H" + tilt + "C";
				setTitle(prod);
				tiltOption = true;
				new GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				return true;
			case R.id.action_legend:
				showLegend();
				return true;
			case R.id.action_about:
				showRadarScanInfo();
				return true;
			case R.id.action_vil:
				prod = "DVL";
				setTitle( prod);
				tiltOption = false;
				new GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				return true;
			case R.id.action_dsp:
				prod = "DSA";
				setTitle( "DSA");
				tiltOption = false;
				new GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				return true;
			case R.id.action_daa:
				prod = "DAA";
				setTitle( prod);
				tiltOption = false;
				new GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				return true;
			case R.id.action_l2vel:
				prod = "L2VEL";
				setTitle( prod);
				tiltOption = false;
				new GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				return true;
			case R.id.action_l2ref:
				prod = "L2REF";
				setTitle( prod);
				tiltOption = false;
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
				setTitle(prod);
				anim.setIcon(MyApplication.Companion.getICON_STOP());
				star.setIcon(MyApplication.Companion.getICON_PAUSE());
				new AnimateRadar().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"12",prod);
				return true;
			case R.id.action_a18:
				setTitle(prod);
				anim.setIcon(MyApplication.Companion.getICON_STOP());
				star.setIcon(MyApplication.Companion.getICON_PAUSE());
				new AnimateRadar().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"18",prod);
				return true;
			case R.id.action_a6_sm:
				setTitle( prod);
				anim.setIcon(MyApplication.Companion.getICON_STOP());
				star.setIcon(MyApplication.Companion.getICON_PAUSE());
				new AnimateRadar().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"6",prod);
				return true;
			case R.id.action_a:
				setTitle( prod);
				anim.setIcon(MyApplication.Companion.getICON_STOP());
				star.setIcon(MyApplication.Companion.getICON_PAUSE());
				new AnimateRadar().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, MyApplication.Companion.getUiAnimIconFrames(),prod);
				return true;
			case R.id.action_a36:
				setTitle(prod);
				anim.setIcon(MyApplication.Companion.getICON_STOP());
				star.setIcon(MyApplication.Companion.getICON_PAUSE());
				new AnimateRadar().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"36",prod);
				return true;
			case R.id.action_a72:
				setTitle(prod);
				anim.setIcon(MyApplication.Companion.getICON_STOP());
				star.setIcon(MyApplication.Companion.getICON_PAUSE());
				new AnimateRadar().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"72",prod);
				return true;
			case R.id.action_a144:
				setTitle(prod);
				anim.setIcon(MyApplication.Companion.getICON_STOP());
				star.setIcon(MyApplication.Companion.getICON_PAUSE());
				new AnimateRadar().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"144",prod);
				return true;
			case R.id.action_a3:
				setTitle(prod);
				anim.setIcon(MyApplication.Companion.getICON_STOP());
				star.setIcon(MyApplication.Companion.getICON_PAUSE());
				new AnimateRadar().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"3",prod);
				return true;
			case R.id.action_NVW:
				new GetContentVWP().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
				} else
					toggleFavorite();
				return true;
			case R.id.action_TDWR:
                diaTdwr.show();
				return true;
			case R.id.action_ridmap:
				LayoutParams paramsIv = mImageMap.getLayoutParams();
				paramsIv.height= MyApplication.Companion.getDm().heightPixels- getToolbar().getHeight() - getToolbarBottom().getHeight() - getStatusBarHeight();
				paramsIv.width= MyApplication.Companion.getDm().widthPixels;
				mImageMap.setLayoutParams(paramsIv);
				if(!mapShown) {
					mapShown =true;
					glview.setVisibility(View.GONE);
					img.setVisibility(View.GONE);
					mImageMap.setVisibility(View.VISIBLE);
					UtilityWXGLTextObject.INSTANCE.hideTV(numPanes, wxgltextArr);
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
					img.setVisibility(View.GONE);
					UtilityWXGLTextObject.INSTANCE.showTV(numPanes, wxgltextArr);
					glview.setVisibility(View.VISIBLE);
					oglInView =true;
				}
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void RIDMapSwitch(String r) {
		rid1=r;
		mapShown =false;
		ridArrLoc = UtilityFavorites.INSTANCE.setupFavMenu(MyApplication.Companion.getRidFav(),rid1, prefTokenLocation);
        sp.refreshData(getBaseContext(),ridArrLoc);
    }

	private void toggleFavorite() {
		String rid_fav = UtilityFavorites.INSTANCE.toggleFavoriteString(rid1, star, "RID_FAV");
		ridArrLoc = UtilityFavorites.INSTANCE.setupFavMenu(rid_fav,rid1, prefTokenLocation);
        sp.refreshData(getBaseContext(),ridArrLoc);
    }

	private void showRadarScanInfo() {
		String info = MyApplication.Companion.getPreferences().getString("WX_RADAR_CURRENT_INFO", "");
		UtilityAlertDialog.INSTANCE.showHelpText(info, this);

	}

	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

		if (ridArrLoc.length>0) {

			inOglAnim = false;
			inOglAnimPaused = false;
			anim.setIcon(MyApplication.Companion.getICON_PLAY());

			if (pos == 0 || pos > 2) {
				rid1 = MyApplication.Companion.getSpace().split(ridArrLoc[pos])[0];

				if (tiltOption)
					tiltMenu.setVisible(true);
				else
					tiltMenu.setVisible(false);

				String oldState = state;
				String oldSector = sector;
				String oldOnek = onek;
				state = MyApplication.Companion.getComma().split(MyApplication.Companion.getPreferences().getString("RID_LOC_" + rid1, ""))[0];
				sector = MyApplication.Companion.getPreferences().getString("COD_SECTOR_" + state, "");
				state = MyApplication.Companion.getPreferences().getString("STATE_CODE_" + state, "");
				onek = MyApplication.Companion.getPreferences().getString("COD_1KM_" + rid1, "");

				if (prod.equals("2k")) {
					imgUrl = imgUrl.replace(oldSector, sector);
					imgUrl = imgUrl.replace(oldState, state);
					imgUrl = imgUrl.replace(oldOnek, onek);
				}
				if (!restarted && !(MyApplication.Companion.getWxoglRememberLocation() && firstRun)) {
					img.resetZoom();
					img.setZoom(1.0f);
					OGLR.setZoom(((float) MyApplication.Companion.getWxoglSize()) / 10.0f);
					glview.setScaleFactor((float) MyApplication.Companion.getWxoglSize() / 10.0f);

					OGLR.setX(0.0f);
					OGLR.setY(0.0f);
				}
				restarted = false;
				ridChanged = true;
				new GetContent().execute();
			} else if (pos == 1) {

				Intent i = new Intent(getApplicationContext(),  FavAddActivity.class);
				i.putExtra(FavAddActivity.Companion.getTYPE(),
						new String[] {"RID"});
				startActivity(i);
			} else if (pos == 2) {

				Intent i = new Intent(getApplicationContext(),  FavRemoveActivity.class);
				i.putExtra(FavRemoveActivity.Companion.getTYPE(),
						new String[] {"RID"});
				startActivity(i);
			}

			if (firstTime) {
				UtilityToolbar.INSTANCE.fullScreenMode(getToolbar(), getToolbarBottom());
				firstTime = false;
			}
		} // end check if ridArr is zero size


		UtilityUI.INSTANCE.immersiveMode(this);

	}

	public void onNothingSelected(AdapterView<?> parent) {

	}

	@Override
	public void onStop() {

		super.onStop();

		if (!archiveMode && !fixedSite) {
			MyApplication.Companion.getEditor().putString("WXOGL_RID", rid1);
			MyApplication.Companion.getEditor().putString("WXOGL_PROD", prod);
			MyApplication.Companion.getEditor().putFloat("WXOGL_ZOOM", OGLR.getZoom());
			MyApplication.Companion.getEditor().putFloat("WXOGL_X", OGLR.getX());
			MyApplication.Companion.getEditor().putFloat("WXOGL_Y", OGLR.getY());
			MyApplication.Companion.getEditor().apply();

			MyApplication.Companion.setWxoglRid(rid1);
			MyApplication.Companion.setWxoglProd(prod);
			MyApplication.Companion.setWxoglZoom(OGLR.getZoom());
			MyApplication.Companion.setWxoglX(OGLR.getX());
			MyApplication.Companion.setWxoglY(OGLR.getY());

		}

		// otherwise cpu will spin with no fix but to kill app

		inOglAnim = false;

		if ( mHandler != null )
			stopRepeatingTask();

		if (  locationManager != null) {
			if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
					|| ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
				locationManager.removeUpdates(locationListener);
		}

	}

	private final WXGLSurfaceView.OnProgressChangeListener changeListener = new WXGLSurfaceView.OnProgressChangeListener() {

		public void onProgressChanged(int code, int idx, int idx_int) {

			if ( code != 50000) {

				alertDialogStatusAl.clear();

				double dist =0.0;
				double distRid =0.0;
				double locX, locY, pointX,pointY, ridX, ridY;

				try {
					locX = Double.parseDouble(locXCurrent);
					locY = Double.parseDouble(locYCurrent);

					pointX = (double) glview.getNewY();
					pointY = (double) (glview.getNewX() * -1);

					dist = UtilityMath.INSTANCE.distance(locX, locY, pointX, pointY, 'M');

					ridX = Double.parseDouble(MyApplication.Companion.getPreferences().getString("RID_" + rid1 + "_X", "0.0"));
					ridY =  -1 * Double.parseDouble(MyApplication.Companion.getPreferences().getString("RID_" + rid1 + "_Y", "0.0"));
					distRid = UtilityMath.INSTANCE.distance(ridX, ridY, pointX, pointY, 'M');

				} catch (Exception e) {
					UtilityLog.INSTANCE.HandleException(e);
				}

                diaStatus.setTitle(UtilityStringExternal.INSTANCE.truncate(Double.toString(glview.getNewY()), 6)
						+ ",-" + UtilityStringExternal.INSTANCE.truncate(Double.toString(glview.getNewX()), 6));
				alertDialogStatusAl.add(UtilityStringExternal.INSTANCE.truncate(Double.toString(dist), 6) + " miles from location");
				alertDialogStatusAl.add( UtilityStringExternal.INSTANCE.truncate(Double.toString(distRid),6) + " miles from " + rid1);

				for (RID ridAl : OGLR.getRidNewList()) {
					alertDialogStatusAl.add("Radar: (" + Integer.toString(ridAl.getDistance())
							+ " mi) "
							+ ridAl.getName() + " "
							+ MyApplication.Companion.getPreferences().getString("RID_LOC_" + ridAl.getName(), ""));
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

	private void initGLVIEW() {
		glview.setEGLContextClientVersion(2);
		OGLR = new WXGLRender(this);

		OGLRArr = new WXGLRender[1];
		OGLRArr[0]=OGLR;
		glviewArr = new WXGLSurfaceView[1];
		glviewArr[0]=glview;

		//glview.setEGLConfigChooser(8, 8, 8, 8, 16, 0); // a test to see if android emulator will now work

		glview.setRenderer(OGLR);
		glview.setRenderVar(OGLR, OGLRArr, glviewArr, act);
		glview.setFullScreen(true);
		glview.setOnProgressChangeListener(changeListener);
		glview.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY); // RENDERMODE_CONTINUOUSLY
		//glview.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
		glview.setToolbar(getToolbar());
		glview.setToolbarBottom(getToolbarBottom());
		glview.setArchiveMode(archiveMode);
	}

	private void initWXOGLGeom(String rid) {
		OGLR.initGEOM(rid1, prod);

		if (! oldRid.equals(rid)) {
			OGLR.setChunkCount(0);
			OGLR.setChunkCountSti(0);
			OGLR.setHiInit(false);
			OGLR.setTvsInit(false);

			new Thread(new Runnable() {
				public void run(){
					OGLR.constructStateLines();
					glview.requestRender();
				}
			}).start();

			new Thread(new Runnable() {
				public void run(){
					if (MyApplication.Companion.getCodLakesDefault())
						OGLR.constructLakes();
					else
						OGLR.deconstructLakes();
				}
			}).start();

			new Thread(new Runnable() {
				public void run(){
					if (MyApplication.Companion.getCountyDefault()) {
						OGLR.constructCounty();
						glview.requestRender();
					}
					else
						OGLR.deconstructCounty();
				}
			}).start();

			new Thread(new Runnable() {
				public void run(){
					if (MyApplication.Companion.getCodHwDefault()) {
						OGLR.constructHWLines();
						glview.requestRender();
					}
					else
						OGLR.deconstructHWLines();
				}
			}).start();

			new Thread(new Runnable() {
				public void run(){
					if (MyApplication.Companion.getRadarHwEnhExt()) {
						OGLR.constructHWEXTLines();
						glview.requestRender();
					}
					else
						OGLR.deconstructHWEXTLines();
				}
			}).start();

			wxgltextArr[0].setRid(rid1);
			wxgltextArr[0].addTV();
			oldRid =rid;
		}

		new Thread(new Runnable() {
			public void run(){
				if ( MyApplication.Companion.getCodWarningsDefault() && !archiveMode)
					OGLR.constructWarningFFWLines();
				else
					OGLR.deconstructWarningFFWLines();

				if ( MyApplication.Companion.getWatmcdDefault() && !archiveMode)
					OGLR.constructWATMCDLines();
				else
					OGLR.deconstructWATMCDLines();

				if ( MyApplication.Companion.getMpdDefault() && !archiveMode)
					OGLR.constructMPDLines();
				else
					OGLR.deconstructMPDLines();



				glview.requestRender();
			}
		}).start();

		if (MyApplication.Companion.getLocdotFollowsGps() && !archiveMode) {
			getGPSFromDouble();
			locXCurrent = latlonArr[0];
			locYCurrent = latlonArr[1];
		}

		if ( MyApplication.Companion.getCodLocdotDefault() || MyApplication.Companion.getLocdotFollowsGps()) // added locdot gps apr 2016
			OGLR.constructLocationDot(locXCurrent, locYCurrent, archiveMode);
		else
			OGLR.deconstructLocationDot();

		img.setVisibility(View.GONE);
		mImageMap.setVisibility(View.GONE);
		glview.setVisibility(View.VISIBLE);
	}

	// inazaruk thanks http://stackoverflow.com/questions/6242268/repeat-a-task-with-a-time-delay

	private final Runnable mStatusChecker = new Runnable() {
		@Override
		public void run() {
			if ( loopCount > 0 ) {
				if (inOglAnim)
					animTriggerDownloads =true;
				else
					new GetContent().execute();  // change from parallel to serial to prevent onrestart storm spotter crash
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
		glview.onPause();
		super.onPause();
	}

	public void  onResume() {
		glview.onResume();
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

	// Define a listener that responds to location updates
	private final LocationListener locationListener = new LocationListener() {
		public void onLocationChanged(Location location) {
			// Called when a new location is found by the network location provider.
			if (MyApplication.Companion.getLocdotFollowsGps() && !archiveMode)
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
		OGLR.constructLocationDot(locXCurrent, locYCurrent, archiveMode);
		glview.requestRender();
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
						if(strName.contains("Radar:"))
						{
							String ridNew = UtilityString.INSTANCE.parse(strName,"\\) ([A-Z]{3,4}) ");
							rid1 = ridNew;
							OGLR.setRid(ridNew);
							ridChanged = true;
							RIDMapSwitch(rid1);
						}  else if(strName.contains("Show warning text")) {
                            //UtilityWXOGL.showTextProducts(a, glview.newY, glview.newX * -1);

                            String polygonUrl = UtilityWXOGL.INSTANCE.showTextProducts( glview.getNewY(), glview.getNewX() * -1);
                            if(!polygonUrl.equals("")){
                                Intent intent = new Intent(getBaseContext(), USAlertsDetailActivity.class);
                                intent.putExtra(USAlertsDetailActivity.Companion.getURL(), new String[]{polygonUrl, ""});
                                startActivity(intent);
                            }
                        } else if(strName.contains("Show nearest observation"))
							new GetMetar().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
						else if(strName.contains("Show nearest meteogram")) {
							// http://www.nws.noaa.gov/mdl/gfslamp/meteoform.php
							// http://www.nws.noaa.gov/mdl/gfslamp/meteo.php?BackHour=0&TempBox=Y&DewBox=Y&SkyBox=Y&WindSpdBox=Y&WindDirBox=Y&WindGustBox=Y&CigBox=Y&VisBox=Y&ObvBox=Y&PtypeBox=N&PopoBox=Y&LightningBox=Y&ConvBox=Y&sta=KTEW

							String obs_site = UtilityMetar.INSTANCE.findClosestObs(glview.getNewY(),glview.getNewX() *-1);
							Intent intent = new Intent(getBaseContext(), ImageShowActivity.class);
							intent.putExtra(ImageShowActivity.Companion.getURL(), new String[] {
									"http://www.nws.noaa.gov/mdl/gfslamp/meteo.php?BackHour=0&TempBox=Y&DewBox=Y&SkyBox=Y&WindSpdBox=Y&WindDirBox=Y&WindGustBox=Y&CigBox=Y&VisBox=Y&ObvBox=Y&PtypeBox=N&PopoBox=Y&LightningBox=Y&ConvBox=Y&sta="
											+ obs_site,
									obs_site  + " Meteogram"});
							startActivity(intent);
						} else if(strName.contains("Show radar status message"))
							new GetRadarStatus().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
						dialog.dismiss();
					}
				});

	}

	private void alertDialogTDWR() {

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
						rid1= MyApplication.Companion.getSpace().split(strName)[0];
						prod="TZL";
						RIDMapSwitch(rid1);
						setTitle( prod);
						new GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
						dialog.dismiss();
					}
				});

	}

	private class GetRadarStatus extends AsyncTask<String, String, String> {

		String radarStatus="";

		@Override
		protected String doInBackground(String... params) {
            radarStatus= UtilityDownload.INSTANCE.getRadarStatusMessage(rid1);
			return "Executed";
		}

		@Override
		protected void onPostExecute(String result) {
			UtilityAlertDialog.INSTANCE.showHelpText(Utility.INSTANCE.fromHtml(radarStatus),a);
		}
	}

	private ViewColorLegend legend;

	private void showLegend() {
		if (!legendShown) {
			if (prod.equals("DSA") || prod.equals("DAA"))
				dspLegendMax = (255f/OGLR.getRadarL3Object().getHalfword3132())*0.01f;

			legendShown =true;
			RelativeLayout.LayoutParams rLParams =
					new RelativeLayout.LayoutParams(
							RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT); // change FILL_PARENT
			rLParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 1);

			legend = new ViewColorLegend(this,prod);
			rl.addView(legend,rLParams);

			MyApplication.Companion.setRadarShowLegend(true);
			MyApplication.Companion.getEditor().putString("RADAR_SHOW_LEGEND", "true");
			MyApplication.Companion.getEditor().apply();
		} else {
			rl.removeView(legend);
			legendShown =false;

			MyApplication.Companion.setRadarShowLegend(false);
			MyApplication.Companion.getEditor().putString("RADAR_SHOW_LEGEND", "false");
			MyApplication.Companion.getEditor().apply();
		}
	}

	private void updateLegend() {
		rl.removeView(legend);
		RelativeLayout.LayoutParams rLParams =
				new RelativeLayout.LayoutParams(
						RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT); // change FILL_PARENT
		rLParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 1);
		legend = new ViewColorLegend(this,prod);
		rl.addView(legend,rLParams);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			// Respond to the action bar's Up/Home button

			case android.R.id.home:
				if (MyApplication.Companion.getPreferences().getString("LAUNCH_TO_RADAR","false").equals("false"))
					NavUtils.navigateUpFromSameTask(this);
				else
					navigateUp();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	// thanks http://stackoverflow.com/questions/19999619/navutils-navigateupto-does-not-start-any-activity user882209
	private void navigateUp() {
		final Intent upIntent = NavUtils.getParentActivityIntent(this);
		if (NavUtils.shouldUpRecreateTask(this, upIntent) || isTaskRoot()) {
			TaskStackBuilder.create(this).addNextIntentWithParentStack(upIntent).startActivities();
		} else {
			NavUtils.navigateUpTo(this, upIntent);
		}
	}

	private class GetMetar extends AsyncTask<String, String, String> {

		String txt;

		@Override
		protected String doInBackground(String... params) {

			txt = UtilityMetar.INSTANCE.findClosestMetar(glview.getNewY(),glview.getNewX() *-1);
			return "Executed";
		}

		@Override
		protected void onPostExecute(String result) {

			UtilityAlertDialog.INSTANCE.showHelpText(txt,a);
		}
	}

	private class GetContentVWP extends AsyncTask<String, String, String> {

		String txt;

		@Override
		protected String doInBackground(String... params) {

			txt = UtilityWXOGL.INSTANCE.getVWP(getBaseContext(),rid1);
			return "Executed";
		}

		@Override
		protected void onPostExecute(String result) {

			Intent intent = new Intent(getBaseContext(), TextScreenActivity.class);
			intent.putExtra(TextScreenActivity.Companion.getURL(),
					new String[]{txt, rid1 + " VAD Wind Profile"});
			startActivity(intent);
		}
	}
}