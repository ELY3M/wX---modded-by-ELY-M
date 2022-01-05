package joshuatee.wx.util;

/*
 * Copyright (C) 2011 Scott Lund
 * Modified in 2013 by Oleksii Chyrkov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



/*eclipse quickstart:

- git clone https://github.com/catchthecows/AndroidImageMap.git
- Run Eclipse and choose a workspace
- File|New->Project...
- Select Android Project from Existing Code
- Browse to AndroidImageMap and hit Finish

An implementation of an HTML map like element in an Android View:

- Supports images as drawable or bitmap in layout
- Allows for a list of area tags in xml
- Enables use of cut and paste HTML area tags to a resource xml (ie, the ability to take an HTML map - and image and use it with minimal editing)
- Supports panning if the image is larger than the device screen
- Supports pinch-zoom
- Supports callbacks when an area is tapped.
- Supports showing annotations as bubble text and provide callback if the bubble is tapped


New in this version:
By default, the initial image is resized to fit the view dimensions with no regard to maintaining aspect ratio. This appears to be the most common use.

To have the aspect ratio kept:
change this (ImageView.java line 54) to from true to false
private boolean mFitImageToScreen=true;

Common issues:
NOTE: You will need to change the xmlns (xml namespace) to match the base package of your application
xmlns:ctc="http://schemas.android.com/apk/res/com.ctc.android.widget"
becomes
xmlns:ctc="http://schemas.android.com/apk/res/YOURPACKAGE"

The package is listed in your AndroidManifest.xml file (ie package="com.ctc.android.widget" )



Use notes:

To associate the map with the img, list the map in in ImageMap attributes

<!--?xml version="1.0" encoding="utf-8"?-->
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:ctc="http://schemas.android.com/apk/res/com.ctc.android.widget"
    android:orientation="vertical"
    android:layout_width="fill_parent"
    android:layout_height="fill_parent"
    >
    <com.ctc.android.widget.ImageMap
        android:id="@+id/map"
        android:layout_width="fill_parent"
        android:layout_height="fill_parent"
        android:src="@drawable/usamap"
        ctc:map="usamap"/>
</LinearLayout>


The area map is specified in your project at res/xml/map.xml
One difference over HTML maps is that each area must have an id. I went back and forth on this requirement, and I may change the code to allow for areas without id. The code will use the name attribute if present, otherwise it will look for title or alt.

<!--?xml version="1.0" encoding="utf-8"?-->
<maps xmlns:android="http://schemas.android.com/apk/res/android">
    <map name="gridmap">
        <area id="@+id/area1001" shape="rect" coords="118,124,219,226" />
        <area id="@+id/area1002" shape="rect" coords="474,374,574,476" />
        <area id="@+id/area1004" shape="rect" coords="710,878,808,980" />
        <area id="@+id/area1005" shape="circle" coords="574,214,74" />
        <area id="@+id/area1006" shape="poly" coords="250,780,250,951,405,951" />
        <area id="@+id/area1007" shape="poly" coords="592,502,592,730,808,730,808,502,709,502,709,603,690,603,690,502" />
    </map>
    <map name="usamap">
        <area id="@+id/area1" shape="poly" coords="230,35,294,38,299,57,299,79,228,79" />
...
     </map>
</maps>

The image itself is placed in res/drawable-nodpi so that the system will not attempt to fit the image to the device based on dpi. This way we are guaranteed that our area coordinates will map properly to the displayed image. If you want to use different density drawables, you will have to make changes in the code based on the DisplayMetrics.density.

Here is a sample activity that finds the view in the layout and adds an on click handler

public class ImageMapTestActivity extends Activity {
    ImageMap mImageMap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // find the image map in the view
        mImageMap = (ImageMap)findViewById(R.id.map);

        // add a click handler to react when areas are tapped
        mImageMap.addOnImageMapClickedHandler(new ImageMap.OnImageMapClickedHandler() {
            @Override
            public void onImageMapClicked(int id) {
                // when the area is tapped, show the name in a
                // text bubble
                mImageMap.showBubble(id);
            }

            @Override
            public void onBubbleClicked(int id) {
                // react to info bubble for area being tapped
            }
        });
    }
}

Don't hesitate to ask if you have any other questions.
 */


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.widget.ImageView;
import android.widget.Scroller;
import org.xmlpull.v1.XmlPullParser;
import java.util.ArrayList;
import java.util.HashMap;

import joshuatee.wx.R;

@SuppressLint("WrongCall")
public class ImageMap extends ImageView
{
	// mFitImageToScreen
	// if true - initial image resized to fit the screen, aspect ratio may be broken
	// if false- initial image resized so that no empty screen is visible, aspect ratio maintained
	// image size will likely be larger than screen

	// by default, this is true
	private boolean mFitImageToScreen=true;

	// For certain images, it is best to always resize using the original
	// image bits. This requires keeping the original image in memory along with the
	// current sized version and thus takes extra memory.
	// If you always want to resize using the original, set mScaleFromOriginal to true
	// If you want to use less memory, and the image scaling up and down repeatedly
	// does not blur or loose quality, set mScaleFromOriginal to false

	// by default, this is false
	private boolean mScaleFromOriginal=false;

	// mMaxSize controls the maximum zoom size as a multiplier of the initial size.
	// Allowing size to go too large may result in memory problems.
	// set this to 1.0f to disable resizing
	// by default, this is 1.5f
	private static final float defaultMaxSize = 1.5f;
	private float mMaxSize = 1.5f;

	/* Touch event handling variables */
	private VelocityTracker mVelocityTracker;

	private int mTouchSlop;
	private int mMinimumVelocity;
	private int mMaximumVelocity;

	private Scroller mScroller;

	private boolean mIsBeingDragged = false;

	private final HashMap<Integer,TouchPoint> mTouchPoints = new HashMap<>();
	private TouchPoint mMainTouch=null;
	private TouchPoint mPinchTouch=null;

	/* Pinch zoom */
	private float mInitialDistance;
	private boolean mZoomEstablished=false;
	private int mLastDistanceChange=0;
	private boolean mZoomPending=false;

	/* Paint objects for drawing info bubbles */
	private Paint textPaint;
	private Paint bubblePaint;
	private Paint bubbleShadowPaint;

	/*
	 * Bitmap handling
	 */
	private Bitmap mImage;
	private Bitmap mOriginal;

	// Info about the bitmap (sizes, scroll bounds)
	// initial size
	private int mImageHeight;
	private int mImageWidth;
	private float mAspect;
	// scaled size
	private int mExpandWidth;
	private int mExpandHeight;
	// the right and bottom edges (for scroll restriction)
	private int mRightBound;
	private int mBottomBound;
	// the current zoom scaling (X and Y kept separate)
	private float mResizeFactorX;
	private float mResizeFactorY;
	// minimum height/width for the image
	private int mMinWidth=-1;
	private int mMinHeight=-1;
	// maximum height/width for the image
	private int mMaxWidth=-1;
	private int mMaxHeight=-1;

	// the position of the top left corner relative to the view
	private int mScrollTop;
	private int mScrollLeft;

	// view height and width
	private int mViewHeight=-1;
	private int mViewWidth=-1;

	/*
	 * containers for the image map areas
	 * using SparseArray<Area> instead of HashMap for the sake of performance
	 */
	private final ArrayList<Area> mAreaList = new ArrayList<>();
	private final SparseArray<Area> mIdToArea = new SparseArray<>();

	// click handler list
	private ArrayList<OnImageMapClickedHandler> mCallbackList;

	// list of open info bubbles
	private final SparseArray<Bubble> mBubbleMap = new SparseArray<>();

	// accounting for screen density
	//private float densityFactor;

	/*
	 * Constructors
	 */
	public ImageMap(Context context) {
		super(context);
		init();
	}

	public ImageMap(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
		loadAttributes(attrs);
	}

	public ImageMap(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		init();
		loadAttributes(attrs);
	}

	/**
	 * get the map name from the attributes and load areas from xml
	 * param attrs
	 */
	private void loadAttributes(AttributeSet attrs)
	{
		TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ImageMap);

		this.mFitImageToScreen = a.getBoolean(R.styleable.ImageMap_fitImageToScreen, true);
		this.mScaleFromOriginal = a.getBoolean(R.styleable.ImageMap_scaleFromOriginal, false);
		this.mMaxSize = a.getFloat(R.styleable.ImageMap_maxSizeFactor, defaultMaxSize);

		// changed this from local variable to class field
		String mapName = a.getString(R.styleable.ImageMap_map);
		if (mapName != null)
		{
			loadMap(mapName);
		}
	}

	/**
	 * parse the maps.xml resource and pull out the areas
	 * @param map - the name of the map to load
	 */
	private void loadMap(String map) {
		boolean loading = false;
		try {
			int mapRes;
			switch (map) {
				case "cwamap":
					mapRes = R.xml.map_wfo;
					break;
				case "soundings":
					mapRes = R.xml.map_soundings;
					break;
				case "ncar_nexrad_sites":
					mapRes = R.xml.map_radarsite;
					break;
				case "cwamap3":
					mapRes = R.xml.map_states;
					break;
				default:
					mapRes = R.xml.map_ca;
					break;
			}
			UtilityLog.INSTANCE.d("IMAGEMAP", map);
			XmlResourceParser xpp = getResources().getXml(mapRes);

			int eventType = xpp.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if(eventType == XmlPullParser.START_DOCUMENT) {
					// Start document
					// This is a useful branch for a debug log if
					// parsing is not working
				} else if(eventType == XmlPullParser.START_TAG) {
					String tag = xpp.getName();

					if (tag.equalsIgnoreCase("map")) {
						String mapname = xpp.getAttributeValue(null, "name");
						if (mapname !=null) {
							if (mapname.equalsIgnoreCase(map)) {
								loading=true;
							}
						}
					}
					if (loading) {
						if (tag.equalsIgnoreCase("area")) {
							Area a;
							String shape = xpp.getAttributeValue(null, "shape");
							String coords = xpp.getAttributeValue(null, "coords");
							String id = xpp.getAttributeValue(null, "id");

							// as a name for this area, try to find any of these
							// attributes
							// name attribute is custom to this impl (not standard in html area tag)
							String name = xpp.getAttributeValue(null, "name");
							if (name == null) {
								name = xpp.getAttributeValue(null, "title");
							}
							if (name == null) {
								name = xpp.getAttributeValue(null, "alt");
							}

							if ((shape != null) && (coords != null)) {
								a = addShape(shape,name,coords,id);
								if (a != null) {
									// add all of the area tag attributes
									// so that they are available to the
									// implementation if needed (see getAreaAttribute)
									for (int i=0;i<xpp.getAttributeCount();i++) {
										String attrName = xpp.getAttributeName(i);
										String attrVal = xpp.getAttributeValue(null,attrName);
										a.addValue(attrName, attrVal);
									}
								}
							}
						}
					}
				} else if(eventType == XmlPullParser.END_TAG) {
					String tag = xpp.getName();
					if (tag.equalsIgnoreCase("map")) {
						loading = false;
					}
				}
				eventType = xpp.next();
			}
		}  // Having trouble loading? Log this exception
		catch (Exception xppe) {
			UtilityLog.INSTANCE.handleException(xppe);
		}
	}

	/**
	 * Create a new area and add to tracking
	 * Changed this from private to protected!
	 * param shape
	 * param name
	 * param coords
	 * param id
	 * return
	 */
	private Area addShape(String shape, String name, String coords, String id)
	{
		Area a = null;
		String rid = id.replace("@+id/", "");
		int _id;

		// FIXME appt2
		_id = Integer.parseInt(rid.replace("@",""));

		if (_id != 0)
		{
			if (shape.equalsIgnoreCase("rect"))
			{
				String[] v = coords.split(",");
				if (v.length == 4)
				{
					a = new RectArea(_id, name, Float.parseFloat(v[0]),
							Float.parseFloat(v[1]),
							Float.parseFloat(v[2]),
							Float.parseFloat(v[3]));
				}
			}
			if (shape.equalsIgnoreCase("circle"))
			{
				String[] v = coords.split(",");
				if (v.length == 3) {
					a = new CircleArea(_id,name, Float.parseFloat(v[0]),
							Float.parseFloat(v[1]),
							Float.parseFloat(v[2])
							);
				}
			}
			if (shape.equalsIgnoreCase("poly"))
			{
				a = new PolyArea(_id,name, coords);
			}
			if (a != null)
			{
				addArea(a);
			}
		}
		return a;
	}

	private void addArea(Area a)
	{
		mAreaList.add(a);
		mIdToArea.put(a.getId(), a);
	}

	/**
	 * initialize the view
	 */
	private void init()
	{
		// set up paint objects
		initDrawingTools();

		// create a scroller for flinging
		mScroller = new Scroller(getContext());

		// get some default values from the system for touch/drag/fling
		final ViewConfiguration configuration = ViewConfiguration
				.get(getContext());
		mTouchSlop = configuration.getScaledTouchSlop();
		mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
		mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();

		//find out the screen density
		//densityFactor = getResources().getDisplayMetrics().density;
	}

	/*
	 * These methods will be called when images or drawables are set
	 * in the XML for the view. We handle either bitmaps or drawables
	 * @see android.widget.ImageView#setImageBitmap(android.graphics.Bitmap)
	 */
	@Override
	public void setImageBitmap(Bitmap bm)
	{
		if (mImage==mOriginal)
		{
			mOriginal=null;
		}
		else
		{
			mOriginal.recycle();
			mOriginal=null;
		}
		if (mImage != null)
		{
			mImage.recycle();
			mImage=null;
		}
		mImage = bm;
		mOriginal = bm;
		mImageHeight = mImage.getHeight();
		mImageWidth = mImage.getWidth();
		mAspect = (float)mImageWidth / mImageHeight;
		setInitialImageBounds();
	}

	@Override
	public void setImageResource(int resId) {
		Bitmap b = BitmapFactory.decodeResource(getResources(), resId);
		setImageBitmap(b);
	}

	@Override
	public void setImageDrawable(Drawable drawable) {
		if (drawable instanceof BitmapDrawable) {
			BitmapDrawable bd = (BitmapDrawable) drawable;
			setImageBitmap(bd.getBitmap());
		}
	}

	/**
	 * setup the paint objects for drawing bubbles
	 */
	private void initDrawingTools() {
		textPaint = new Paint();
		textPaint.setColor(0xFF000000);
		textPaint.setTextSize(30);
		textPaint.setTypeface(Typeface.SERIF);
		textPaint.setTextAlign(Paint.Align.CENTER);
		textPaint.setAntiAlias(true);

		Paint textOutlinePaint = new Paint();
		textOutlinePaint.setColor(0xFF000000);
		textOutlinePaint.setTextSize(18);
		textOutlinePaint.setTypeface(Typeface.SERIF);
		textOutlinePaint.setTextAlign(Paint.Align.CENTER);
		textOutlinePaint.setStyle(Paint.Style.STROKE);
		textOutlinePaint.setStrokeWidth(2);

		bubblePaint=new Paint();
		bubblePaint.setColor(0xFFFFFFFF);
		bubbleShadowPaint=new Paint();
		bubbleShadowPaint.setColor(0xFF000000);

	}

	/*
	 * Called by the scroller when flinging
	 * @see android.view.View#computeScroll()
	 */
	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			int oldX = mScrollLeft;
			int oldY = mScrollTop;

			int x = mScroller.getCurrX();
			int y = mScroller.getCurrY();

			if (oldX != x) {
				moveX(x-oldX);
			}
			if (oldY != y) {
				moveY(y-oldY);
			}
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);

		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		int chosenWidth = chooseDimension(widthMode, widthSize);
		int chosenHeight = chooseDimension(heightMode, heightSize);

		setMeasuredDimension(chosenWidth, chosenHeight);
	}

	private int chooseDimension(int mode, int size) {
		if (mode == MeasureSpec.AT_MOST || mode == MeasureSpec.EXACTLY)
		{
			return size;
		}
		else
		{
			// (mode == MeasureSpec.UNSPECIFIED)
			return getPreferredSize();
		}
	}


	/**
	 * set the initial bounds of the image
	 */
	private void setInitialImageBounds()
	{
		if (mFitImageToScreen)
		{
			setInitialImageBoundsFitImage();
		}
		else
		{
			setInitialImageBoundsFillScreen();
		}
	}

	/**
	 * setInitialImageBoundsFitImage sets the initial image size to match the
	 * screen size. aspect ratio may be broken
	 */
	private void setInitialImageBoundsFitImage()
	{
		if (mImage != null)
		{
			if (mViewWidth > 0)
			{
				mMinHeight = mViewHeight;
				mMinWidth = mViewWidth;
				mMaxWidth = (int)(mMinWidth * mMaxSize);
				mMaxHeight = (int)(mMinHeight * mMaxSize);

				mScrollTop = 0;
				mScrollLeft = 0;
				scaleBitmap(mMinWidth, mMinHeight);
			}
		}
	}

	/**
	 * setInitialImageBoundsFillScreen sets the initial image size to so that there
	 * is no uncovered area of the device
	 */
	private void setInitialImageBoundsFillScreen()
	{
		if (mImage != null)
		{
			if (mViewWidth > 0)
			{
				boolean resize=false;

				int newWidth=mImageWidth;
				int newHeight=mImageHeight;

				// The setting of these max sizes is very arbitrary
				// Need to find a better way to determine max size
				// to avoid attempts too big a bitmap and throw OOM
				if (mMinWidth==-1)
				{
					// set minimums so that the largest
					// direction we always filled (no empty view space)
					// this maintains initial aspect ratio
					if (mViewWidth > mViewHeight)
					{
						mMinWidth = mViewWidth;
						mMinHeight = (int)(mMinWidth/mAspect);
					} else {
						mMinHeight = mViewHeight;
						mMinWidth = (int)(mAspect*mViewHeight);
					}
					mMaxWidth = (int)(mMinWidth * 1.5f);
					mMaxHeight = (int)(mMinHeight * 1.5f);
				}

				if (newWidth < mMinWidth) {
					newWidth = mMinWidth;
					newHeight = (int) (((float) mMinWidth / mImageWidth) * mImageHeight);
					resize = true;
				}
				if (newHeight < mMinHeight) {
					newHeight = mMinHeight;
					newWidth = (int) (((float) mMinHeight / mImageHeight) * mImageWidth);
					resize = true;
				}

				mScrollTop = 0;
				mScrollLeft = 0;

				// scale the bitmap
				if (resize) {
					scaleBitmap(newWidth, newHeight);
				} else {
					mExpandWidth=newWidth;
					mExpandHeight=newHeight;

					mResizeFactorX = ((float) newWidth / mImageWidth);
					mResizeFactorY = ((float) newHeight / mImageHeight);
					mRightBound = -(mExpandWidth - mViewWidth);
					mBottomBound = -(mExpandHeight - mViewHeight);
				}
			}
		}
	}

	/**
	 * Set the image to new width and height
	 * create a new scaled bitmap and dispose of the previous one
	 * recalculate scaling factor and right and bottom bounds
	 * param newWidth
	 * param newHeight
	 */
	private void scaleBitmap(int newWidth, int newHeight) {
		// Technically since we always keep aspect ratio intact
		// we should only need to check one dimension.
		// Need to investigate and fix
		if ((newWidth > mMaxWidth) || (newHeight > mMaxHeight)) {
			newWidth = mMaxWidth;
			newHeight = mMaxHeight;
		}
		if ((newWidth < mMinWidth) || (newHeight < mMinHeight)) {
			newWidth = mMinWidth;
			newHeight = mMinHeight;
		}

		if ((newWidth != mExpandWidth) || (newHeight!=mExpandHeight)) {
			// NOTE: depending on the image being used, it may be
			// better to keep the original image available and
			// use those bits for resize. Repeated grow/shrink
			// can render some images visually non-appealing
			// see comments at top of file for mScaleFromOriginal
			// try to create a new bitmap
			// If you get a recycled bitmap exception here, check to make sure
			// you are not setting the bitmap both from XML and in code
			Bitmap newbits = Bitmap.createScaledBitmap(mScaleFromOriginal ? mOriginal:mImage, newWidth,
					newHeight, true);
			// if successful, fix up all the tracking variables
			if (newbits != null) {
				if (mImage!=mOriginal) {
					mImage.recycle();
				}
				mImage = newbits;
				mExpandWidth=newWidth;
				mExpandHeight=newHeight;
				mResizeFactorX = ((float) newWidth / mImageWidth);
				mResizeFactorY = ((float) newHeight / mImageHeight);

				mRightBound = mExpandWidth>mViewWidth ? -(mExpandWidth - mViewWidth) : 0;
				mBottomBound = mExpandHeight>mViewHeight ? -(mExpandHeight - mViewHeight) : 0;
			}
		}
	}

	private void resizeBitmap(int amount) {
		int adjustHeight = (int)(amount / mAspect);
		scaleBitmap( mExpandWidth + amount, mExpandHeight + adjustHeight);
	}

	/**
	 * watch for screen size changes and reset the background image
	 */
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		// save device height width, we use it a lot of places
		mViewHeight = h;
		mViewWidth = w;
		// fix up the image
		setInitialImageBounds();
	}

	private int getPreferredSize() {
		return 300;
	}

	/**
	 * the onDraw routine when we are using a background image
	 *
	 * param canvas
	 */
	private void drawMap(Canvas canvas)
	{
		canvas.save();
		if (mImage != null)
		{
			if (!mImage.isRecycled())
			{
				canvas.drawBitmap(mImage, mScrollLeft, mScrollTop, null);
			}
		}
		canvas.restore();
	}

	private void drawBubbles(Canvas canvas)
	{
		for (int i = 0; i < mBubbleMap.size(); i++)
		{
			int key = mBubbleMap.keyAt(i);
			Bubble b = mBubbleMap.get(key);
			if (b != null)
			{
				b.onDraw(canvas);
			}
		}
	}

	private void drawLocations(Canvas canvas)
	{
		for (Area a : mAreaList)
		{

			a.onDraw(canvas);
		}
	}

	/**
	 * Paint the view
	 * image first, location decorations next, bubbles on top
	 */
	@Override
	protected void onDraw(Canvas canvas)
	{
		drawMap(canvas);
		drawLocations(canvas);
		drawBubbles(canvas);
	}

	/*
	 * Touch handler
	 * This handler manages an arbitrary number of points
	 * and detects taps, moves, flings, and zooms
	 */
	//public boolean onTouchEvent(@NotNull MotionEvent ev)

	public boolean onTouchEvent( MotionEvent ev)
	{
		int id;

		if (mVelocityTracker == null)
		{
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(ev);

		final int action = ev.getAction();

		int pointerCount = ev.getPointerCount();
		int index = 0;

		if (pointerCount > 1) {
			// If you are using new API (level 8+) use these constants
			// instead as they are much better names
			index = (action & MotionEvent.ACTION_POINTER_INDEX_MASK);
			index = index >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
		}

		switch (action & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			// Clear all touch points
			// In the case where some view up chain is messing with our
			// touch events, it is possible to miss UP and POINTER_UP
			// events. Whenever ACTION_DOWN happens, it is intended
			// to always be the first touch, so we will drop tracking
			// for any points that may have been orphaned
			for ( TouchPoint t: mTouchPoints.values() ) {
				onLostTouch(t.getTrackingPointer());
			}
			// fall through planned
		case MotionEvent.ACTION_POINTER_DOWN:
			id = ev.getPointerId(index);
			onTouchDown(id,ev.getX(index),ev.getY(index));
			break;

		case MotionEvent.ACTION_MOVE:
			for (int p=0;p<pointerCount;p++) {
				id = ev.getPointerId(p);
				TouchPoint t = mTouchPoints.get(id);
				if (t!=null) {
					onTouchMove(t,ev.getX(p),ev.getY(p));
				}
				// after all moves, check to see if we need
				// to process a zoom
				processZoom();
			}
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP:
			id = ev.getPointerId(index);
			onTouchUp(id);
			break;
		case MotionEvent.ACTION_CANCEL:
			// Clear all touch points on ACTION_CANCEL
			// according to the google devs, CANCEL means cancel
			// tracking every touch.
			// cf: http://groups.google.com/group/android-developers/browse_thread/thread/8b14591ead5608a0/ad711bf24520e5c4?pli=1
			for ( TouchPoint t: mTouchPoints.values() ) {
				onLostTouch(t.getTrackingPointer());
			}
			// let go of the velocity tracker per API Docs
			if (mVelocityTracker != null) {
				mVelocityTracker.recycle();
				mVelocityTracker = null;
			}
			break;
		}
		return true;
	}


	private void onTouchDown(int id, float x, float y) {
		// create a new touch point to track this ID
		TouchPoint t;
		synchronized (mTouchPoints) {
			// This test is a bit paranoid and research should
			// be done sot that it can be removed. We should
			// not find a touch point for the id
			t = mTouchPoints.get(id);
			if (t == null) {
				t = new TouchPoint(id);
				mTouchPoints.put(id,t);
			}

			// for pinch zoom, we need to pick two touch points
			// they will be called Main and Pinch
			if (mMainTouch == null) {
				mMainTouch = t;
			} else {
				if (mPinchTouch == null) {
					mPinchTouch=t;
					// second point established, set up to
					// handle pinch zoom
					startZoom();
				}
			}
		}
		t.setPosition(x,y);
	}

	/*
	 * Track pointer moves
	 */
	private void onTouchMove(TouchPoint t, float x, float y) {
		// mMainTouch will drag the view, be part of a
		// pinch zoom, or trigger a tap
		if (t == mMainTouch) {
			if (mPinchTouch == null) {
				// only on point down, this is a move
				final int deltaX = (int) (t.getX() - x);
				final int xDiff = (int) Math.abs(t.getX() - x);

				final int deltaY = (int) (t.getY() - y);
				final int yDiff = (int) Math.abs(t.getY() - y);

				if (!mIsBeingDragged) {
					if ((xDiff > mTouchSlop) || (yDiff > mTouchSlop)) {
						// start dragging about once the user has
						// moved the point far enough
						mIsBeingDragged = true;
					}
				} else {
					// being dragged, move the image
					if (xDiff > 0) {
						moveX(-deltaX);
					}
					if (yDiff > 0) {
						moveY(-deltaY);
					}
					t.setPosition(x, y);
				}
			} else {
				// two fingers down means zoom
				t.setPosition(x, y);
				onZoom();
			}
		} else {
			if (t == mPinchTouch) {
				// two fingers down means zoom
				t.setPosition(x, y);
				onZoom();
			}
		}
	}

	/*
	 * touch point released
	 */
	private void onTouchUp(int id) {
		synchronized (mTouchPoints) {
			TouchPoint t = mTouchPoints.get(id);
			if (t != null) {
				if (t == mMainTouch) {
					if (mPinchTouch==null) {
						// This is either a fling or tap
						if (mIsBeingDragged) {
							// view was being dragged means this is a fling
							final VelocityTracker velocityTracker = mVelocityTracker;
							velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);

							int xVelocity = (int) velocityTracker.getXVelocity();
							int yVelocity = (int) velocityTracker.getYVelocity();

							int xfling = Math.abs(xVelocity) > mMinimumVelocity ? xVelocity
									: 0;
							int yfling = Math.abs(yVelocity) > mMinimumVelocity ? yVelocity
									: 0;

							if ((xfling != 0) || (yfling != 0)) {
								fling(-xfling, -yfling);
							}

							mIsBeingDragged = false;

							// let go of the velocity tracker
							if (mVelocityTracker != null) {
								mVelocityTracker.recycle();
								mVelocityTracker = null;
							}
						} else {
							// no movement - this was a tap
							onScreenTapped((int)mMainTouch.getX(), (int)mMainTouch.getY());
						}
					}
					mMainTouch=null;
					mZoomEstablished=false;
				}
				if (t == mPinchTouch) {
					// lost the 2nd pointer
					mPinchTouch=null;
					mZoomEstablished=false;
				}
				mTouchPoints.remove(id);
				// shuffle remaining pointers so that we are still
				// tracking. This is necessary for proper action
				// on devices that support > 2 touches
				regroupTouches();
			}
			//else {
				// lost this ID somehow
				// This happens sometimes due to the way some
				// devices manage touch
			//}
		}
	}

	/*
	 * Touch handling varies from device to device, we may think we
	 * are tracking an id which goes missing
	 */
	private void onLostTouch(int id) {
		synchronized (mTouchPoints) {
			TouchPoint t = mTouchPoints.get(id);
			if (t != null) {
				if (t == mMainTouch) {
					mMainTouch=null;
				}
				if (t == mPinchTouch) {
					mPinchTouch=null;
				}
				mTouchPoints.remove(id);
				regroupTouches();
			}
		}
	}

	/*
	 * find a touch pointer that is not being used as main or pinch
	 */
	private TouchPoint getUnboundPoint() {
		TouchPoint ret=null;
		for (Integer i : mTouchPoints.keySet()) {
			TouchPoint p = mTouchPoints.get(i);
			if ((p!=mMainTouch)&&(p!=mPinchTouch)) {
				ret = p;
				break;
			}
		}
		return ret;
	}

	/*
	 * go through remaining pointers and try to have
	 * MainTouch and then PinchTouch if possible
	 */
	private void regroupTouches() {
		int s=mTouchPoints.size();
		if (s>0) {
			if (mMainTouch == null) {
				if (mPinchTouch != null) {
					mMainTouch=mPinchTouch;
					mPinchTouch=null;
				} else {
					mMainTouch=getUnboundPoint();
				}
			}
			if (s>1) {
				if (mPinchTouch == null) {
					mPinchTouch=getUnboundPoint();
					startZoom();
				}
			}
		}
	}

	/*
	 * Called when the second pointer is down indicating that we
	 * want to do a pinch-zoom action
	 */
	private void startZoom() {
		// this boolean tells the system that it needs to
		// initialize itself before trying to zoom
		// This is cleaner than duplicating code
		// see processZoom
		mZoomEstablished=false;
	}

	/*
	 * one of the pointers for our pinch-zoom action has moved
	 * Remember this until after all touch move actions are processed.
	 */
	private void onZoom() {
		mZoomPending=true;
	}

	/*
	 * All touch move actions are done, do we need to zoom?
	 */
	private void processZoom() {
		if (mZoomPending) {
			// check pinch distance, set new scale factor
			float dx=mMainTouch.getX()-mPinchTouch.getX();
			float dy=mMainTouch.getY()-mPinchTouch.getY();
			float newDistance=(float)Math.sqrt((dx*dx)+(dy*dy));
			if (mZoomEstablished) {
				// baseline was set, check to see if there is enough
				// movement to resize
				int distanceChange=(int)(newDistance-mInitialDistance);
				int delta=distanceChange-mLastDistanceChange;
				if (Math.abs(delta)>mTouchSlop) {
					mLastDistanceChange=distanceChange;
					resizeBitmap(delta);
					invalidate();
				}
			} else {
				// first run through after touches established
				// just set baseline
				mLastDistanceChange=0;
				mInitialDistance=newDistance;
				mZoomEstablished=true;
			}
			mZoomPending=false;
		}
	}

	/*
	 * Screen tapped x, y is screen coord from upper left and does not account
	 * for scroll
	 */
	private void onScreenTapped(int x, int y)
	{
		boolean missed = true;
		boolean bubble = false;
		// adjust for scroll
		int testx = x-mScrollLeft;
		int testy = y-mScrollTop;
		testx = (int)(((float)testx/mResizeFactorX));
		testy = (int)(((float)testy/mResizeFactorY));

		// check if bubble tapped first
		// in case a bubble covers an area we want it to
		// have precedent
		for (int i = 0 ; i < mBubbleMap.size() ; i++)
		{
			int key = mBubbleMap.keyAt(i);
			Bubble b = mBubbleMap.get(key);
			//it can still be null if there are no bubbles at all
			if (b != null)
			{
				if (b.isInArea((float)x-mScrollLeft,(float)y-mScrollTop))
				{
					b.onTapped();
					bubble=true;
					missed=false;
					// only fire tapped for one bubble
					break;
				}
			}
		}

		if (!bubble)
		{
			// then check for area taps
			for (Area a : mAreaList)
			{
				if (a.isInArea((float)testx,(float)testy))
				{
					if (mCallbackList != null) {
						for (OnImageMapClickedHandler h : mCallbackList)
						{
							h.onImageMapClicked(a.getId(), this);
						}
					}
					missed=false;
					// only fire clicked for one area
					break;
				}
			}
		}

		if (missed)
		{
			// managed to miss everything, clear bubbles
			mBubbleMap.clear();
			invalidate();
		}
	}

	// process a fling by kicking off the scroller
	private void fling(int velocityX, int velocityY)
	{
		int startX = mScrollLeft;
		int startY = mScrollTop;

		mScroller.fling(startX, startY, -velocityX, -velocityY, mRightBound, 0,
				mBottomBound, 0);

		invalidate();
	}

	/*
	 * move the view by this delta in X direction
	 */
	private void moveX(int deltaX) {
		mScrollLeft = mScrollLeft + deltaX;
		if (mScrollLeft > 0) {
			mScrollLeft = 0;
		}
		if (mScrollLeft < mRightBound) {
			mScrollLeft = mRightBound;
		}
		invalidate();
	}

	/*
	 * move the view by this delta in Y direction
	 */
	private void moveY(int deltaY) {
		mScrollTop = mScrollTop + deltaY;
		if (mScrollTop > 0) {
			mScrollTop = 0;
		}
		if (mScrollTop < mBottomBound) {
			mScrollTop = mBottomBound;
		}
		invalidate();
	}

	/*
	 * A class to track touches
	 */
	class TouchPoint {
		final int _id;
		float _x;
		float _y;

		TouchPoint(int id) {
			_id=id;
			_x=0f;
			_y=0f;
		}
		int getTrackingPointer() {
			return _id;
		}
		void setPosition(float x, float y) {
			if ((_x != x) || (_y != y)) {
				_x=x;
				_y=y;
			}
		}
		float getX() {
			return _x;
		}
		float getY() {
			return _y;
		}
	}


	/*
	 * on clicked handler add/remove support
	 */
	public void addOnImageMapClickedHandler( OnImageMapClickedHandler h ) {
		if (h != null) {
			if (mCallbackList == null) {
				mCallbackList = new ArrayList<>();
			}
			mCallbackList.add(h);
		}
	}

	/*
	 * Begin map area support
	 */
	/**
	 * Area is abstract Base for tappable map areas
	 * descendants provide hit test and focal point
	 */
	abstract class Area {
		final int _id;
		String _name;
		HashMap<String,String> _values;
		Bitmap _decoration=null;

		Area(int id, String name) {
			_id = id;
			if (name != null) {
				_name = name;
			}
		}

		int getId() {
			return _id;
		}

		public String getName() {
			return _name;
		}

		// all xml values for the area are passed to the object
		// the default impl just puts them into a hashmap for
		// retrieval later
		void addValue(String key, String value) {
			if (_values == null) {
				_values = new HashMap<>();
			}
			_values.put(key, value);
		}

		// a method for setting a simple decorator for the area
		public void setBitmap(Bitmap b) {
			_decoration = b;
		}

		// an onDraw is set up to provide an extensible way to
		// decorate an area. When drawing remember to take the
		// scaling and translation into account
		void onDraw(Canvas canvas)
		{
			if (_decoration != null)
			{
				float x = (getOriginX() * mResizeFactorX) + mScrollLeft - 17;
				float y = (getOriginY() * mResizeFactorY) + mScrollTop - 17;
				canvas.drawBitmap(_decoration, x, y, null);
			}
		}

		abstract boolean isInArea(float x, float y);
		abstract float getOriginX();
		abstract float getOriginY();
	}

	/**
	 * Rectangle Area
	 */
	class RectArea extends Area {
		final float _left;
		final float _top;
		final float _right;
		final float _bottom;


		RectArea(int id, String name, float left, float top, float right, float bottom) {
			super(id,name);
			_left = left;
			_top = top;
			_right = right;
			_bottom = bottom;
		}

		public boolean isInArea(float x, float y) {
			boolean ret = false;
			if ((x > _left) && (x < _right)) {
				if ((y > _top) && (y < _bottom)) {
					ret = true;
				}
			}
			return ret;
		}

		public float getOriginX() {
			return _left;
		}

		public float getOriginY() {
			return _top;
		}
	}

	/**
	 * Polygon area
	 */
	class PolyArea extends Area {
		final ArrayList<Integer> xpoints = new ArrayList<>();
		final ArrayList<Integer> ypoints = new ArrayList<>();

		// centroid point for this poly
		float _x;
		float _y;

		// number of points (don't rely on array size)
		final int _points;

		// bounding box
		int top=-1;
		int bottom=-1;
		int left=-1;
		int right=-1;

		PolyArea(int id, String name, String coords) {
			super(id,name);

			// split the list of coordinates into points of the
			// polygon and compute a bounding box
			String[] v = coords.split(",");

			int i=0;
			while ((i+1)<v.length) {
				int x = Integer.parseInt(v[i]);
				int y = Integer.parseInt(v[i+1]);
				xpoints.add(x);
				ypoints.add(y);
				top=(top==-1)?y:Math.min(top,y);
				bottom=(bottom==-1)?y:Math.max(bottom,y);
				left=(left==-1)?x:Math.min(left,x);
				right=(right==-1)?x:Math.max(right,x);
				i+=2;
			}
			_points=xpoints.size();

			// add point zero to the end to make
			// computing area and centroid easier
			xpoints.add(xpoints.get(0));
			ypoints.add(ypoints.get(0));

			computeCentroid();
		}

		/**
		 * area() and computeCentroid() are adapted from the implementation
		 * of polygon.java published from a princeton case study
		 * The study is here: http://introcs.cs.princeton.edu/java/35purple/
		 * The polygon.java source is here: http://introcs.cs.princeton.edu/java/35purple/Polygon.java.html
		 */

		// return area of polygon
		double area() {
			double sum = 0.0;
			for (int i = 0; i < _points; i++) {
				sum = sum + (xpoints.get(i) * ypoints.get(i+1)) - (ypoints.get(i) * xpoints.get(i+1));
			}
			sum = 0.5 * sum;
			return Math.abs(sum);
		}

		// compute the centroid of the polygon
		void computeCentroid() {
			double cx = 0.0, cy = 0.0;
			for (int i = 0; i < _points; i++) {
				cx = cx + (xpoints.get(i) + xpoints.get(i+1)) * (ypoints.get(i) * xpoints.get(i+1) - xpoints.get(i) * ypoints.get(i+1));
				cy = cy + (ypoints.get(i) + ypoints.get(i+1)) * (ypoints.get(i) * xpoints.get(i+1) - xpoints.get(i) * ypoints.get(i+1));
			}
			cx /= (6 * area());
			cy /= (6 * area());
			_x=Math.abs((int)cx);
			_y=Math.abs((int)cy);
		}


		@Override
		public float getOriginX() {
			return _x;
		}

		@Override
		public float getOriginY() {
			return _y;
		}

		/**
		 * This is a java port of the
		 * W. Randolph Franklin algorithm explained here
		 * http://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html
		 */
		@Override
		public boolean isInArea(float testx, float testy)
		{
			int i, j;
			boolean c = false;
			for (i = 0, j = _points-1; i < _points; j = i++) {
				if ( ((ypoints.get(i)>testy) != (ypoints.get(j)>testy)) &&
						(testx < (xpoints.get(j)-xpoints.get(i)) * (testy-ypoints.get(i)) / (ypoints.get(j)-ypoints.get(i)) + xpoints.get(i)) )
					c = !c;
			}
			return c;
		}
	}

	/**
	 * Circle Area
	 */
	class CircleArea extends Area {
		final float _x;
		final float _y;
		final float _radius;

		CircleArea(int id, String name, float x, float y, float radius) {
			super(id,name);
			_x = x;
			_y = y;
			_radius = radius;

		}

		public boolean isInArea(float x, float y) {
			boolean ret = false;

			float dx = _x-x;
			float dy = _y-y;

			// if tap is less than radius distance from the center
			float d = (float)Math.sqrt((dx*dx)+(dy*dy));
			if (d<_radius) {
				ret = true;
			}

			return ret;
		}

		public float getOriginX() {
			return _x;
		}

		public float getOriginY() {
			return _y;
		}
	}

	/**
	 * information bubble class
	 */
	class Bubble
	{
		final Area _a;
		String _text;
		float _x;
		float _y;
		int _h;
		int _w;
		int _baseline;
		float _top;
		float _left;

		Bubble(String text, int areaId)
		{
			_a = mIdToArea.get(areaId);
			if (_a != null) {
				float x = _a.getOriginX();
				float y = _a.getOriginY();
				init(text,x,y);
			}
		}

		void init(String text, float x, float y)
		{
			_text = text;
			_x = x*mResizeFactorX;
			_y = y*mResizeFactorY;
			Rect bounds = new Rect();
			textPaint.setTextScaleX(1.0f);
			textPaint.getTextBounds(text, 0, _text.length(), bounds);
			_h = bounds.bottom-bounds.top+20;
			_w = bounds.right-bounds.left+20;

			if (_w>mViewWidth) {
				// too long for the display width...need to scale down
				float newscale=((float)mViewWidth/(float)_w);
				textPaint.setTextScaleX(newscale);
				textPaint.getTextBounds(text, 0, _text.length(), bounds);
				_h = bounds.bottom-bounds.top+20;
				_w = bounds.right-bounds.left+20;
			}

			_baseline = _h-bounds.bottom;
			_left = _x - (_w/2);
			_top = _y - _h - 30;

			// try to keep the bubble on screen
			if (_left < 0) {
				_left = 0;
			}
			if ((_left + _w) > mExpandWidth) {
				_left = mExpandWidth - _w;
			}
			if (_top < 0) {
				_top = _y + 20;
			}
		}

		boolean isInArea(float x, float y) {
			boolean ret = false;

			if ((x>_left) && (x<(_left+_w))) {
				if ((y>_top)&&(y<(_top+_h))) {
					ret = true;
				}
			}

			return ret;
		}

		void onDraw(Canvas canvas)
		{
			if (_a != null) {
				// Draw a shadow of the bubble
				float l = _left + mScrollLeft + 4;
				float t = _top + mScrollTop + 4;
				canvas.drawRoundRect(new RectF(l,t,l+_w,t+_h), 20.0f, 20.0f, bubbleShadowPaint);
				Path path = new Path();
				float ox=_x+ mScrollLeft+ 1;
				float oy=_y+mScrollTop+ 1;
				int yoffset=-35;
				if (_top > _y) {
					yoffset=35;
				}
				// draw shadow of pointer to origin
				path.moveTo(ox,oy);
				path.lineTo(ox-5,oy+yoffset);
				path.lineTo(ox+5+4,oy+yoffset);
				path.lineTo(ox, oy);
				path.close();
				canvas.drawPath(path, bubbleShadowPaint);

				// draw the bubble
				l = _left + mScrollLeft;
				t = _top + mScrollTop;
				canvas.drawRoundRect(new RectF(l,t,l+_w,t+_h), 20.0f, 20.0f, bubblePaint);
				path = new Path();
				ox=_x+ mScrollLeft;
				oy=_y+mScrollTop;
				yoffset=-35;
				if (_top > _y)
				{
					yoffset=35;
				}
				// draw pointer to origin
				path.moveTo(ox,oy);
				path.lineTo(ox-5,oy+yoffset);
				path.lineTo(ox+5,oy+yoffset);
				path.lineTo(ox, oy);
				path.close();
				canvas.drawPath(path, bubblePaint);

				// draw the message
				canvas.drawText(_text,l+(_w/2),t+_baseline-10,textPaint);
			}
		}

		void onTapped() {
			// bubble was tapped, notify listeners
			if (mCallbackList != null) {
				for (OnImageMapClickedHandler h : mCallbackList) {
					h.onBubbleClicked(_a.getId());
				}
			}
		}
	}

	/**
	 * Map tapped callback interface
	 */
	public interface OnImageMapClickedHandler
	{
		/**
		 * Area with 'id' has been tapped
		 * param id
		 */
		void onImageMapClicked(int id, ImageMap imageMap);
		/**
		 * Info bubble associated with area 'id' has been tapped
		 * param id
		 */
		void onBubbleClicked(int id);
	}
}