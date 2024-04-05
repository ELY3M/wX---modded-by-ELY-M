/**
 * GraphView
 * Copyright 2016 Jonas Gehring
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Downloaded from the following URL on 2023-12-30 and local modifications have been made
// https://github.com/jjoe64/GraphView
// Please see license at doc/COPYING.GraphView (APL2.0)

package joshuatee.wx.externalGraphView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import joshuatee.wx.externalGraphView.series.Series;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jjoe64
 */
public class GraphView extends View {
    /**
     * Class to wrap style options that are general
     * to graphs.
     *
     * @author jjoe64
     */
    private static final class Styles {
        /**
         * The font size of the title that can be displayed
         * above the graph.
         *
         * @see GraphView#setTitle(String)
         */
        float titleTextSize;

        /**
         * The font color of the title that can be displayed
         * above the graph.
         *
         * @see GraphView#setTitle(String)
         */
        int titleColor;
    }

    /**
     * Helper class to detect tap events on the
     * graph.
     *
     * @author jjoe64
     */
    private static class TapDetector {
        /**
         * save the time of the last down event
         */
        private long lastDown;

        /**
         * point of the tap down event
         */
        private PointF lastPoint;

        /**
         * to be called to process the events
         *
         * @param event
         * @return true if there was a tap event. otherwise returns false.
         */
        public boolean onTouchEvent(MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                lastDown = System.currentTimeMillis();
                lastPoint = new PointF(event.getX(), event.getY());
            } else if (lastDown > 0 && event.getAction() == MotionEvent.ACTION_MOVE) {
                if (Math.abs(event.getX() - lastPoint.x) > 60
                        || Math.abs(event.getY() - lastPoint.y) > 60) {
                    lastDown = 0;
                }
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                return System.currentTimeMillis() - lastDown < 400;
            }
            return false;
        }
    }

    /**
     * our series (this does not contain the series
     * that can be displayed on the right side. The
     * right side series is a special feature of
     * the {@link SecondScale} feature.
     */
    private List<Series> mSeries;

    /**
     * the renderer for the grid and labels
     */
    private GridLabelRenderer mGridLabelRenderer;

    /**
     * viewport that holds the current bounds of
     * view.
     */
    private Viewport mViewport;

    /**
     * title of the graph that will be shown above
     */
    private String mTitle;

    /**
     * wraps the general styles
     */
    private Styles mStyles;

    /**
     * feature to have a second scale e.g. on the
     * right side
     */
    protected SecondScale mSecondScale;

    /**
     * tap detector
     */
    private TapDetector mTapDetector;

    /**
     * renderer for the legend
     */
    private LegendRenderer mLegendRenderer;

    /**
     * paint for the graph title
     */
    private Paint mPaintTitle;

    private boolean mIsCursorMode;

    /**
     * paint for the preview (in the SDK)
     */
    private Paint mPreviewPaint;

    private CursorMode mCursorMode;

    /**
     * Initialize the GraphView view
     *
     * @param context
     */
    public GraphView(Context context) {
        super(context);
        init();
    }

    /**
     * Initialize the GraphView view.
     *
     * @param context
     * @param attrs
     */
    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Initialize the GraphView view
     *
     * @param context
     * @param attrs
     * @param defStyle
     */
    public GraphView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /**
     * initialize the internal objects.
     * This method has to be called directly
     * in the constructors.
     */
    protected void init() {
        mPreviewPaint = new Paint();
        mPreviewPaint.setTextAlign(Paint.Align.CENTER);
        mPreviewPaint.setColor(Color.BLACK);
        mPreviewPaint.setTextSize(50);

        mStyles = new Styles();
        mViewport = new Viewport(this);
        mGridLabelRenderer = new GridLabelRenderer(this);
        mLegendRenderer = new LegendRenderer(this);

        mSeries = new ArrayList<>();
        mPaintTitle = new Paint();

        mTapDetector = new TapDetector();

        loadStyles();
    }

    /**
     * loads the font
     */
    protected void loadStyles() {
        mStyles.titleColor = mGridLabelRenderer.getHorizontalLabelsColor();
        mStyles.titleTextSize = mGridLabelRenderer.getTextSize();
    }

    /**
     * @return the renderer for the grid and labels
     */
    public GridLabelRenderer getGridLabelRenderer() {
        return mGridLabelRenderer;
    }

    /**
     * Add a new series to the graph. This will
     * automatically redraw the graph.
     *
     * @param s the series to be added
     */
    public void addSeries(Series s) {
        s.onGraphViewAttached(this);
        mSeries.add(s);
        onDataChanged(false, false);
    }

    /**
     * important: do not do modifications on the list
     * object that will be returned.
     * Use {@link #removeSeries(com.jjoe64.graphview.series.Series)} and {@link #addSeries(com.jjoe64.graphview.series.Series)}
     *
     * @return all series
     */
    public List<Series> getSeries() {
        // TODO immutable array
        return mSeries;
    }

    /**
     * call this to let the graph redraw and
     * recalculate the viewport.
     * This will be called when a new series
     * was added or removed and when data
     * was appended via {@link com.jjoe64.graphview.series.BaseSeries#appendData(com.jjoe64.graphview.series.DataPointInterface, boolean, int)}
     * or {@link com.jjoe64.graphview.series.BaseSeries#resetData(com.jjoe64.graphview.series.DataPointInterface[])}.
     *
     * @param keepLabelsSize true if you don't want
     *                       to recalculate the size of
     *                       the labels. It is recommended
     *                       to use "true" because this will
     *                       improve performance and prevent
     *                       a flickering.
     * @param keepViewport   true if you don't want that
     *                       the viewport will be recalculated.
     *                       It is recommended to use "true" for
     *                       performance.
     */
    public void onDataChanged(boolean keepLabelsSize, boolean keepViewport) {
        // adjustSteps grid system
        mViewport.calcCompleteRange();
        if (mSecondScale != null) {
            mSecondScale.calcCompleteRange();
        }
        mGridLabelRenderer.invalidate(keepLabelsSize, keepViewport);
        postInvalidate();
    }

    /**
     * draw all the stuff on canvas
     *
     * @param canvas
     */
    protected void drawGraphElements(Canvas canvas) {
        // must be in hardware accelerated mode
        if (!canvas.isHardwareAccelerated()) {
            // just warn about it, because it is ok when making a snapshot
            Log.w("GraphView", "GraphView should be used in hardware accelerated mode." +
                    "You can use android:hardwareAccelerated=\"true\" on your activity. Read this for more info:" +
                    "https://developer.android.com/guide/topics/graphics/hardware-accel.html");
        }

        drawTitle(canvas);
        mViewport.drawFirst(canvas);
        mGridLabelRenderer.draw(canvas);
        for (Series s : mSeries) {
            s.draw(this, canvas, false);
        }
        if (mSecondScale != null) {
            for (Series s : mSecondScale.getSeries()) {
                s.draw(this, canvas, true);
            }
        }

        if (mCursorMode != null) {
            mCursorMode.draw(canvas);
        }

        mViewport.draw(canvas);
        mLegendRenderer.draw(canvas);
    }

    /**
     * will be called from Android system.
     *
     * @param canvas Canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        if (isInEditMode()) {
            canvas.drawColor(Color.rgb(200, 200, 200));
            canvas.drawText("GraphView: No Preview available", getWidth() / 2, getHeight() / 2, mPreviewPaint);
        } else {
            drawGraphElements(canvas);
        }
    }

    /**
     * Draws the Graphs title that will be
     * shown above the viewport.
     * Will be called by GraphView.
     *
     * @param canvas Canvas
     */
    protected void drawTitle(Canvas canvas) {
        if (mTitle != null && !mTitle.isEmpty()) {
            mPaintTitle.setColor(mStyles.titleColor);
            mPaintTitle.setTextSize(mStyles.titleTextSize);
            mPaintTitle.setTextAlign(Paint.Align.CENTER);
            float x = canvas.getWidth() / 2;
            float y = mPaintTitle.getTextSize();
            canvas.drawText(mTitle, x, y, mPaintTitle);
        }
    }

    /**
     * Calculates the height of the title.
     *
     * @return the actual size of the title.
     * if there is no title, 0 will be
     * returned.
     */
    protected int getTitleHeight() {
        if (mTitle != null && !mTitle.isEmpty()) {
            return (int) mPaintTitle.getTextSize();
        } else {
            return 0;
        }
    }

    /**
     * @return the viewport of the Graph.
     * @see com.jjoe64.graphview.Viewport
     */
    public Viewport getViewport() {
        return mViewport;
    }

    /**
     * Called by Android system if the size
     * of the view was changed. Will recalculate
     * the viewport and labels.
     *
     * @param w
     * @param h
     * @param oldw
     * @param oldh
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        onDataChanged(false, false);
    }

    /**
     * @return the space on the left side of the
     * view from the left border to the
     * beginning of the graph viewport.
     */
    public int getGraphContentLeft() {
        int border = getGridLabelRenderer().getStyles().padding;
        return border + getGridLabelRenderer().getLabelVerticalWidth() + getGridLabelRenderer().getVerticalAxisTitleWidth();
    }

    /**
     * @return the space on the top of the
     * view from the top border to the
     * beginning of the graph viewport.
     */
    public int getGraphContentTop() {
        return getGridLabelRenderer().getStyles().padding + getTitleHeight();
    }

    /**
     * @return the height of the graph viewport.
     */
    public int getGraphContentHeight() {
        int border = getGridLabelRenderer().getStyles().padding;
        int graphHeight = getHeight() - (2 * border) - getGridLabelRenderer().getLabelHorizontalHeight() - getTitleHeight();
        graphHeight -= getGridLabelRenderer().getHorizontalAxisTitleHeight();
        return graphHeight;
    }

    /**
     * @return the width of the graph viewport.
     */
    public int getGraphContentWidth() {
        int border = getGridLabelRenderer().getStyles().padding;
        int graphWidth = getWidth() - (2 * border) - getGridLabelRenderer().getLabelVerticalWidth();
        if (mSecondScale != null) {
            graphWidth -= getGridLabelRenderer().getLabelVerticalSecondScaleWidth();
            graphWidth -= mSecondScale.getVerticalAxisTitleTextSize();
        }
        return graphWidth;
    }

    /**
     * will be called from Android system.
     *
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean b = mViewport.onTouchEvent(event);
        boolean a = super.onTouchEvent(event);

        // is it a click?
        if (mTapDetector.onTouchEvent(event)) {
            for (Series s : mSeries) {
                s.onTap(event.getX(), event.getY());
            }
            if (mSecondScale != null) {
                for (Series s : mSecondScale.getSeries()) {
                    s.onTap(event.getX(), event.getY());
                }
            }
        }

        return b || a;
    }

    /**
     * @noinspection EmptyMethod
     */
    @Override
    public void computeScroll() {
        super.computeScroll();
//        mViewport.computeScroll();
    }

    /**
     * @return the legend renderer.
     * @see com.jjoe64.graphview.LegendRenderer
     */
    public LegendRenderer getLegendRenderer() {
        return mLegendRenderer;
    }

    /**
     * @return the title that will be shown
     * above the graph.
     */
    public String getTitle() {
        return mTitle;
    }

    /**
     * Set the title of the graph that will
     * be shown above the graph's viewport.
     *
     * @param mTitle the title
     * @see #setTitleColor(int) to set the font color
     * @see #setTitleTextSize(float) to set the font size
     */
    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    /**
     * creates the second scale logic and returns it
     *
     * @return second scale object
     */
    public SecondScale getSecondScale() {
        if (mSecondScale == null) {
            // this creates the second scale
            mSecondScale = new SecondScale(this);
            mSecondScale.setVerticalAxisTitleTextSize(mGridLabelRenderer.mStyles.textSize);
        }
        return mSecondScale;
    }

    /**
     * Removes all series of the graph.
     */
    public void removeAllSeries() {
        mSeries.clear();
        onDataChanged(false, false);
    }

    /**
     * takes a snapshot and return it as bitmap
     *
     * @return snapshot of graph
     */
    public Bitmap takeSnapshot() {
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        draw(canvas);
        return bitmap;
    }

    public CursorMode getCursorMode() {
        return mCursorMode;
    }

    public boolean isCursorMode() {
        return mIsCursorMode;
    }
}
