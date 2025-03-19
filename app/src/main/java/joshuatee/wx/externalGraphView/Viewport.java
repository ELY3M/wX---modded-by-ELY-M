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

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.widget.EdgeEffectCompat;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.EdgeEffect;
import android.widget.OverScroller;

import joshuatee.wx.externalGraphView.series.DataPointInterface;
import joshuatee.wx.externalGraphView.series.Series;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This is the default implementation for the viewport.
 * This implementation so for a normal viewport
 * where there is a horizontal x-axis and a
 * vertical y-axis.
 * This viewport is compatible with
 * - graphview.series.BarGraphSeries
 * - graphview.series.LineGraphSeries
 * - graphview.series.PointsGraphSeries
 *
 * @author jjoe64
 */
public class Viewport {
    /**
     * this reference value is used to generate the
     * vertical labels. It is used when the y axis bounds
     * is set manual and humanRoundingY=false. it will be the minValueY value.
     */
    protected double referenceY = Double.NaN;

    /**
     * this reference value is used to generate the
     * horizontal labels. It is used when the x axis bounds
     * is set manual and humanRoundingX=false. it will be the minValueX value.
     */
    protected double referenceX = Double.NaN;

    /**
     * flag whether the vertical scaling is activated
     */
    protected boolean scalableY;

    /**
     * minimal viewport used for scaling and scrolling.
     * this is used if the data that is available is
     * less then the viewport that we want to be able to display.
     * <p>
     * Double.NaN to disable this value
     */
    private final RectD mMinimalViewport = new RectD(Double.NaN, Double.NaN, Double.NaN, Double.NaN);

    /**
     * the reference number to generate the labels
     *
     * @return by default 0, only when manual bounds and no human rounding
     * is active, the min x value is returned
     */
    protected double getReferenceX() {
        // if the bounds is manual then we take the
        // original manual min y value as reference
        if (isXAxisBoundsManual() && !mGraphView.getGridLabelRenderer().isHumanRoundingX()) {
            if (Double.isNaN(referenceX)) {
                referenceX = getMinX(false);
            }
            return referenceX;
        } else {
            // starting from 0 so that the steps have nice numbers
            return 0;
        }
    }

    /**
     * listener to notify when x bounds changed after
     * scaling or scrolling.
     * This can be used to load more detailed data.
     */
    public interface OnXAxisBoundsChangedListener {
        /**
         * Called after scaling or scrolling with
         * the new bounds
         *
         * @param minX min x value
         * @param maxX max x value
         */
        void onXAxisBoundsChanged(double minX, double maxX, OnXAxisBoundsChangedListener.Reason reason);

        enum Reason {
            SCROLL, SCALE
        }
    }

    /**
     * listener for the scale gesture
     */
    private final ScaleGestureDetector.OnScaleGestureListener mScaleGestureListener
            = new ScaleGestureDetector.OnScaleGestureListener() {
        /**
         * called by android
         * @param detector detector
         * @return always true
         */
        @Override
        public boolean onScale(@NonNull ScaleGestureDetector detector) {
            // --- horizontal scaling ---
            double viewportWidth = mCurrentViewport.width();

            if (mMaxXAxisSize != 0) {
                if (viewportWidth > mMaxXAxisSize) {
                    viewportWidth = mMaxXAxisSize;
                }
            }

            double center = mCurrentViewport.left + viewportWidth / 2;

            float scaleSpanX;
            if (scalableY) {
                scaleSpanX = detector.getCurrentSpanX() / detector.getPreviousSpanX();
            } else {
                scaleSpanX = detector.getScaleFactor();
            }

            viewportWidth /= scaleSpanX;
            mCurrentViewport.left = center - viewportWidth / 2;
            mCurrentViewport.right = mCurrentViewport.left + viewportWidth;

            // viewportStart must not be < minX
            double minX = getMinX(true);
            if (!Double.isNaN(mMinimalViewport.left)) {
                minX = Math.min(minX, mMinimalViewport.left);
            }
            if (mCurrentViewport.left < minX) {
                mCurrentViewport.left = minX;
                mCurrentViewport.right = mCurrentViewport.left + viewportWidth;
            }

            // viewportStart + viewportSize must not be > maxX
            double maxX = getMaxX(true);
            if (!Double.isNaN(mMinimalViewport.right)) {
                maxX = Math.max(maxX, mMinimalViewport.right);
            }
            if (viewportWidth == 0) {
                mCurrentViewport.right = maxX;
            }
            double overlap = mCurrentViewport.left + viewportWidth - maxX;
            if (overlap > 0) {
                // scroll left
                if (mCurrentViewport.left - overlap > minX) {
                    mCurrentViewport.left -= overlap;
                    mCurrentViewport.right = mCurrentViewport.left + viewportWidth;
                } else {
                    // maximal scale
                    mCurrentViewport.left = minX;
                    mCurrentViewport.right = maxX;
                }
            }


            // --- vertical scaling ---
            if (scalableY && detector.getCurrentSpanY() != 0f && detector.getPreviousSpanY() != 0f) {
                boolean hasSecondScale = mGraphView.mSecondScale != null;

                double viewportHeight = mCurrentViewport.height() * -1;

                if (mMaxYAxisSize != 0) {
                    if (viewportHeight > mMaxYAxisSize) {
                        viewportHeight = mMaxYAxisSize;
                    }
                }

                center = mCurrentViewport.bottom + viewportHeight / 2;

                viewportHeight /= detector.getCurrentSpanY() / detector.getPreviousSpanY();
                mCurrentViewport.bottom = center - viewportHeight / 2;
                mCurrentViewport.top = mCurrentViewport.bottom + viewportHeight;

                // ignore bounds when second scale
                if (!hasSecondScale) {
                    // viewportStart must not be < minY
                    double minY = getMinY(true);
                    if (!Double.isNaN(mMinimalViewport.bottom)) {
                        minY = Math.min(minY, mMinimalViewport.bottom);
                    }
                    if (mCurrentViewport.bottom < minY) {
                        mCurrentViewport.bottom = minY;
                        mCurrentViewport.top = mCurrentViewport.bottom + viewportHeight;
                    }

                    // viewportStart + viewportSize must not be > maxY
                    double maxY = getMaxY(true);
                    if (!Double.isNaN(mMinimalViewport.top)) {
                        maxY = Math.max(maxY, mMinimalViewport.top);
                    }
                    if (viewportHeight == 0) {
                        mCurrentViewport.top = maxY;
                    }
                    overlap = mCurrentViewport.bottom + viewportHeight - maxY;
                    if (overlap > 0) {
                        // scroll left
                        if (mCurrentViewport.bottom - overlap > minY) {
                            mCurrentViewport.bottom -= overlap;
                            mCurrentViewport.top = mCurrentViewport.bottom + viewportHeight;
                        } else {
                            // maximal scale
                            mCurrentViewport.bottom = minY;
                            mCurrentViewport.top = maxY;
                        }
                    }
                } else {
                    // ---- second scale ---
                    viewportHeight = mGraphView.mSecondScale.mCurrentViewport.height() * -1;
                    center = mGraphView.mSecondScale.mCurrentViewport.bottom + viewportHeight / 2;
                    viewportHeight /= detector.getCurrentSpanY() / detector.getPreviousSpanY();
                    mGraphView.mSecondScale.mCurrentViewport.bottom = center - viewportHeight / 2;
                    mGraphView.mSecondScale.mCurrentViewport.top = mGraphView.mSecondScale.mCurrentViewport.bottom + viewportHeight;
                }
            }

            // adjustSteps viewport, labels, etc.
            mGraphView.onDataChanged(true, false);

            ViewCompat.postInvalidateOnAnimation(mGraphView);

            return true;
        }

        /**
         * called when scaling begins
         *
         * @param detector detector
         * @return true if it is scalable
         */
        @Override
        public boolean onScaleBegin(@NonNull ScaleGestureDetector detector) {
            // cursor mode
            if (mGraphView.isCursorMode()) {
                return false;
            }

            if (mIsScalable) {
                mScalingActive = true;
                return true;
            } else {
                return false;
            }
        }

        /**
         * called when scaling ends
         * This will re-adjustSteps the viewport.
         *
         * @param detector detector
         */
        @Override
        public void onScaleEnd(@NonNull ScaleGestureDetector detector) {
            mScalingActive = false;

            // notify
            if (mOnXAxisBoundsChangedListener != null) {
                mOnXAxisBoundsChangedListener.onXAxisBoundsChanged(getMinX(false), getMaxX(false), OnXAxisBoundsChangedListener.Reason.SCALE);
            }

            ViewCompat.postInvalidateOnAnimation(mGraphView);
        }
    };

    /**
     * simple gesture listener to track scroll events
     */
    private final GestureDetector.SimpleOnGestureListener mGestureListener
            = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDown(@NonNull MotionEvent e) {
            // cursor mode
            if (mGraphView.isCursorMode()) {
                return true;
            }

            if (!mIsScrollable || mScalingActive) return false;

            // Initiates the decay phase of any active edge effects.
            releaseEdgeEffects();
            // Aborts any active scroll animations and invalidates.
            mScroller.forceFinished(true);
            ViewCompat.postInvalidateOnAnimation(mGraphView);
            return true;
        }

        @Override
        public boolean onScroll(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
            // cursor mode
            if (mGraphView.isCursorMode()) {
                return true;
            }
            if (!mIsScrollable || mScalingActive) return false;

            // Scrolling uses math based on the viewport (as opposed to math using pixels).
            /*
             * Pixel offset is the offset in screen pixels, while viewport offset is the
             * offset within the current viewport. For additional information on surface sizes
             * and pixel offsets, see the docs for {@link computeScrollSurfaceSize()}. For
             * additional information about the viewport, see the comments for
             * {@link mCurrentViewport}.
             */
            double viewportOffsetX = distanceX * mCurrentViewport.width() / mGraphView.getGraphContentWidth();
            double viewportOffsetY = distanceY * mCurrentViewport.height() / mGraphView.getGraphContentHeight();

            // respect minimal viewport
            double completeRangeLeft = mCompleteRange.left;
            if (!Double.isNaN(mMinimalViewport.left)) {
                completeRangeLeft = Math.min(completeRangeLeft, mMinimalViewport.left);
            }
            double completeRangeRight = mCompleteRange.right;
            if (!Double.isNaN(mMinimalViewport.right)) {
                completeRangeRight = Math.max(completeRangeRight, mMinimalViewport.right);
            }
            double completeRangeWidth = completeRangeRight - completeRangeLeft;

            double completeRangeBottom = mCompleteRange.bottom;
            if (!Double.isNaN(mMinimalViewport.bottom)) {
                completeRangeBottom = Math.min(completeRangeBottom, mMinimalViewport.bottom);
            }
            double completeRangeTop = mCompleteRange.top;
            if (!Double.isNaN(mMinimalViewport.top)) {
                completeRangeTop = Math.max(completeRangeTop, mMinimalViewport.top);
            }
            double completeRangeHeight = completeRangeTop - completeRangeBottom;

            int completeWidth = (int) ((completeRangeWidth / mCurrentViewport.width()) * (double) mGraphView.getGraphContentWidth());
            int completeHeight = (int) ((completeRangeHeight / mCurrentViewport.height()) * (double) mGraphView.getGraphContentHeight());

            int scrolledX = (int) (completeWidth
                    * (mCurrentViewport.left + viewportOffsetX - completeRangeLeft)
                    / completeRangeWidth);

            int scrolledY = (int) (completeHeight
                    * (mCurrentViewport.bottom + viewportOffsetY - completeRangeBottom)
                    / completeRangeHeight * -1);
            boolean canScrollX = mCurrentViewport.left > completeRangeLeft
                    || mCurrentViewport.right < completeRangeRight;
            boolean canScrollY = mCurrentViewport.bottom > completeRangeBottom
                    || mCurrentViewport.top < completeRangeTop;

            boolean hasSecondScale = mGraphView.mSecondScale != null;

            // second scale
            double viewportOffsetY2 = 0d;
            if (hasSecondScale) {
                viewportOffsetY2 = distanceY * mGraphView.mSecondScale.mCurrentViewport.height() / mGraphView.getGraphContentHeight();
                canScrollY |= mGraphView.mSecondScale.mCurrentViewport.bottom > mGraphView.mSecondScale.mCompleteRange.bottom
                        || mGraphView.mSecondScale.mCurrentViewport.top < mGraphView.mSecondScale.mCompleteRange.top;
            }

            canScrollY &= scrollableY;

            if (canScrollX) {
                if (viewportOffsetX < 0) {
                    double tooMuch = mCurrentViewport.left + viewportOffsetX - completeRangeLeft;
                    if (tooMuch < 0) {
                        viewportOffsetX -= tooMuch;
                    }
                } else {
                    double tooMuch = mCurrentViewport.right + viewportOffsetX - completeRangeRight;
                    if (tooMuch > 0) {
                        viewportOffsetX -= tooMuch;
                    }
                }

                mCurrentViewport.left += viewportOffsetX;
                mCurrentViewport.right += viewportOffsetX;

                // notify
                if (mOnXAxisBoundsChangedListener != null) {
                    mOnXAxisBoundsChangedListener.onXAxisBoundsChanged(getMinX(false), getMaxX(false), OnXAxisBoundsChangedListener.Reason.SCROLL);
                }
            }
            if (canScrollY) {
                // if we have the second axis we ignore the max/min range
                if (!hasSecondScale) {
                    if (viewportOffsetY < 0) {
                        double tooMuch = mCurrentViewport.bottom + viewportOffsetY - completeRangeBottom;
                        if (tooMuch < 0) {
                            viewportOffsetY -= tooMuch;
                        }
                    } else {
                        double tooMuch = mCurrentViewport.top + viewportOffsetY - completeRangeTop;
                        if (tooMuch > 0) {
                            viewportOffsetY -= tooMuch;
                        }
                    }
                }

                mCurrentViewport.top += viewportOffsetY;
                mCurrentViewport.bottom += viewportOffsetY;

                // second scale
                if (hasSecondScale) {
                    mGraphView.mSecondScale.mCurrentViewport.top += viewportOffsetY2;
                    mGraphView.mSecondScale.mCurrentViewport.bottom += viewportOffsetY2;
                }
            }

            if (canScrollX && scrolledX < 0) {
                mEdgeEffectLeft.onPull(scrolledX / (float) mGraphView.getGraphContentWidth());
            }
            if (!hasSecondScale && canScrollY && scrolledY < 0) {
                mEdgeEffectBottom.onPull(scrolledY / (float) mGraphView.getGraphContentHeight());
            }
            if (canScrollX && scrolledX > completeWidth - mGraphView.getGraphContentWidth()) {
                mEdgeEffectRight.onPull((scrolledX - completeWidth + mGraphView.getGraphContentWidth())
                        / (float) mGraphView.getGraphContentWidth());
            }
            if (!hasSecondScale && canScrollY && scrolledY > completeHeight - mGraphView.getGraphContentHeight()) {
                mEdgeEffectTop.onPull((scrolledY - completeHeight + mGraphView.getGraphContentHeight())
                        / (float) mGraphView.getGraphContentHeight());
            }

            // adjustSteps viewport, labels, etc.
            mGraphView.onDataChanged(true, false);

            ViewCompat.postInvalidateOnAnimation(mGraphView);
            return true;
        }

        @Override
        public boolean onFling(@Nullable MotionEvent e1, @NonNull MotionEvent e2,
                               float velocityX, float velocityY) {
            //fling((int) -velocityX, (int) -velocityY);
            return true;
        }
    };

    /**
     * the state of the axis bounds
     */
    public enum AxisBoundsStatus {
        /**
         * initial means that the bounds gets
         * auto adjusted if they are not manual.
         * After adjusting the status comes to
         * #AUTO_ADJUSTED.
         */
        INITIAL,

        /**
         * after the bounds got auto-adjusted,
         * this status will set.
         */
        AUTO_ADJUSTED,

        /**
         * means that the bounds are fix (manually) and
         * are not to be auto-adjusted.
         */
        FIX
    }

    /**
     * paint to draw background
     */
    private final Paint mPaint;

    /**
     * reference to the graphview
     */
    private final GraphView mGraphView;

    /**
     * this holds the current visible viewport
     * left = minX, right = maxX
     * bottom = minY, top = maxY
     */
    protected final RectD mCurrentViewport = new RectD();

    /**
     * maximum allowed viewport size (horizontal)
     * 0 means use the bounds of the actual data that is
     * available
     */
    protected final double mMaxXAxisSize = 0;

    /**
     * maximum allowed viewport size (vertical)
     * 0 means use the bounds of the actual data that is
     * available
     */
    protected final double mMaxYAxisSize = 0;

    /**
     * this holds the whole range of the data
     * left = minX, right = maxX
     * bottom = minY, top = maxY
     */
    protected final RectD mCompleteRange = new RectD();

    /**
     * flag whether scaling is currently active
     */
    protected boolean mScalingActive;

    /**
     * flag whether the viewport is scrollable
     */
    private boolean mIsScrollable;

    /**
     * flag whether the viewport is scalable
     */
    private boolean mIsScalable;

    /**
     * flag whether the viewport is scalable
     * on the Y axis
     */
    private boolean scrollableY;

    /**
     * gesture detector to detect scrolling
     */
    protected final GestureDetector mGestureDetector;

    /**
     * detect scaling
     */
    protected final ScaleGestureDetector mScaleGestureDetector;

    /**
     * not used - for fling
     */
    protected final OverScroller mScroller;

    /**
     * not used
     */
    private final EdgeEffect mEdgeEffectTop;

    /**
     * not used
     */
    private final EdgeEffect mEdgeEffectBottom;

    /**
     * glow effect when scrolling left
     */
    private final EdgeEffect mEdgeEffectLeft;

    /**
     * glow effect when scrolling right
     */
    private final EdgeEffect mEdgeEffectRight;

    /**
     * state of the x axis
     */
    protected AxisBoundsStatus mXAxisBoundsStatus;

    /**
     * state of the y axis
     */
    protected AxisBoundsStatus mYAxisBoundsStatus;

    /**
     * flag whether the x axis bounds are manual
     */
    private boolean mXAxisBoundsManual;

    /**
     * flag whether the y axis bounds are manual
     */
    private boolean mYAxisBoundsManual;

    /**
     * background color of the viewport area
     * it is recommended to use a semi-transparent color
     */
    private int mBackgroundColor;

    /**
     * listener to notify when x bounds changed after
     * scaling or scrolling.
     * This can be used to load more detailed data.
     */
    protected OnXAxisBoundsChangedListener mOnXAxisBoundsChangedListener;

    /**
     * optional draw a border between the labels
     * and the viewport
     */
    private boolean mDrawBorder;

    /**
     * color of the border
     * <p>
     * see #setDrawBorder(boolean)
     */
    private Integer mBorderColor;

    /**
     * custom paint to use for the border
     * <p>
     * see #setDrawBorder(boolean)
     */
    private Paint mBorderPaint;

    /**
     * creates the viewport
     *
     * @param graphView graphview
     */
    Viewport(GraphView graphView) {
        mScroller = new OverScroller(graphView.getContext());
        mEdgeEffectTop = new EdgeEffect(graphView.getContext());
        mEdgeEffectBottom = new EdgeEffect(graphView.getContext());
        mEdgeEffectLeft = new EdgeEffect(graphView.getContext());
        mEdgeEffectRight = new EdgeEffect(graphView.getContext());
        mGestureDetector = new GestureDetector(graphView.getContext(), mGestureListener);
        mScaleGestureDetector = new ScaleGestureDetector(graphView.getContext(), mScaleGestureListener);

        mGraphView = graphView;
        mXAxisBoundsStatus = AxisBoundsStatus.INITIAL;
        mYAxisBoundsStatus = AxisBoundsStatus.INITIAL;
        mBackgroundColor = Color.TRANSPARENT;
        mPaint = new Paint();
    }

    /**
     * will be called on a touch event.
     * needed to use scaling and scrolling
     *
     * @param event
     * @return true if it was consumed
     */
    public boolean onTouchEvent(MotionEvent event) {
        boolean b = mScaleGestureDetector.onTouchEvent(event);
        b |= mGestureDetector.onTouchEvent(event);
        if (mGraphView.isCursorMode()) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mGraphView.getCursorMode().onDown(event);
                b |= true;
            }
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                mGraphView.getCursorMode().onMove(event);
                b |= true;
            }
            if (event.getAction() == MotionEvent.ACTION_UP) {
                b |= mGraphView.getCursorMode().onUp(event);
            }
        }
        return b;
    }

    /**
     * caches the complete range (minX, maxX, minY, maxY)
     * by iterating all series and all data-points and
     * stores it into #mCompleteRange
     * <p>
     * for the x-range it will respect the series on the
     * second scale - not for y-values
     */
    public void calcCompleteRange() {
        List<Series> series = mGraphView.getSeries();
        List<Series> seriesInclusiveSecondScale = new ArrayList<>(mGraphView.getSeries());
        if (mGraphView.mSecondScale != null) {
            seriesInclusiveSecondScale.addAll(mGraphView.mSecondScale.getSeries());
        }
        mCompleteRange.set(0d, 0d, 0d, 0d);
        if (!seriesInclusiveSecondScale.isEmpty() && !seriesInclusiveSecondScale.get(0).isEmpty()) {
            double d = seriesInclusiveSecondScale.get(0).getLowestValueX();
            for (Series s : seriesInclusiveSecondScale) {
                if (!s.isEmpty() && d > s.getLowestValueX()) {
                    d = s.getLowestValueX();
                }
            }
            mCompleteRange.left = d;

            d = seriesInclusiveSecondScale.get(0).getHighestValueX();
            for (Series s : seriesInclusiveSecondScale) {
                if (!s.isEmpty() && d < s.getHighestValueX()) {
                    d = s.getHighestValueX();
                }
            }
            mCompleteRange.right = d;

            if (!series.isEmpty() && !series.get(0).isEmpty()) {
                d = series.get(0).getLowestValueY();
                for (Series s : series) {
                    if (!s.isEmpty() && d > s.getLowestValueY()) {
                        d = s.getLowestValueY();
                    }
                }
                mCompleteRange.bottom = d;

                d = series.get(0).getHighestValueY();
                for (Series s : series) {
                    if (!s.isEmpty() && d < s.getHighestValueY()) {
                        d = s.getHighestValueY();
                    }
                }
                mCompleteRange.top = d;
            }
        }

        // calc current viewport bounds
        if (mYAxisBoundsStatus == AxisBoundsStatus.AUTO_ADJUSTED) {
            mYAxisBoundsStatus = AxisBoundsStatus.INITIAL;
        }
        if (mYAxisBoundsStatus == AxisBoundsStatus.INITIAL) {
            mCurrentViewport.top = mCompleteRange.top;
            mCurrentViewport.bottom = mCompleteRange.bottom;
        }

        if (mXAxisBoundsStatus == AxisBoundsStatus.AUTO_ADJUSTED) {
            mXAxisBoundsStatus = AxisBoundsStatus.INITIAL;
        }
        if (mXAxisBoundsStatus == AxisBoundsStatus.INITIAL) {
            mCurrentViewport.left = mCompleteRange.left;
            mCurrentViewport.right = mCompleteRange.right;
        } else if (mXAxisBoundsManual && !mYAxisBoundsManual && mCompleteRange.width() != 0) {
            // get highest/lowest of current viewport
            // lowest
            double d = Double.MAX_VALUE;
            for (Series s : series) {
                Iterator<DataPointInterface> values = s.getValues(mCurrentViewport.left, mCurrentViewport.right);
                while (values.hasNext()) {
                    double v = values.next().getY();
                    if (d > v) {
                        d = v;
                    }
                }
            }

            if (d != Double.MAX_VALUE) {
                mCurrentViewport.bottom = d;
            }

            // highest
            d = Double.MIN_VALUE;
            for (Series s : series) {
                Iterator<DataPointInterface> values = s.getValues(mCurrentViewport.left, mCurrentViewport.right);
                while (values.hasNext()) {
                    double v = values.next().getY();
                    if (d < v) {
                        d = v;
                    }
                }
            }

            if (d != Double.MIN_VALUE) {
                mCurrentViewport.top = d;
            }
        }

        // fixes blank screen when range is zero
        if (mCurrentViewport.left == mCurrentViewport.right) mCurrentViewport.right++;
        if (mCurrentViewport.top == mCurrentViewport.bottom) mCurrentViewport.top++;
    }

    /**
     * @param completeRange if true => minX of the complete range of all series
     *                      if false => minX of the current visible viewport
     * @return the min x value
     */
    public double getMinX(boolean completeRange) {
        if (completeRange) {
            return mCompleteRange.left;
        } else {
            return mCurrentViewport.left;
        }
    }

    /**
     * @param completeRange if true => maxX of the complete range of all series
     *                      if false => maxX of the current visible viewport
     * @return the max x value
     */
    public double getMaxX(boolean completeRange) {
        if (completeRange) {
            return mCompleteRange.right;
        } else {
            return mCurrentViewport.right;
        }
    }

    /**
     * @param completeRange if true => minY of the complete range of all series
     *                      if false => minY of the current visible viewport
     * @return the min y value
     */
    public double getMinY(boolean completeRange) {
        if (completeRange) {
            return mCompleteRange.bottom;
        } else {
            return mCurrentViewport.bottom;
        }
    }

    /**
     * @param completeRange if true => maxY of the complete range of all series
     *                      if false => maxY of the current visible viewport
     * @return the max y value
     */
    public double getMaxY(boolean completeRange) {
        if (completeRange) {
            return mCompleteRange.top;
        } else {
            return mCurrentViewport.top;
        }
    }

    /**
     * set the maximal y value for the current viewport.
     * Make sure to set the y bounds to manual via
     * #setYAxisBoundsManual(boolean)
     *
     * @param y max / highest value
     */
    public void setMaxY(double y) {
        mCurrentViewport.top = y;
    }

    /**
     * set the minimal y value for the current viewport.
     * Make sure to set the y bounds to manual via
     * #setYAxisBoundsManual(boolean)
     *
     * @param y min / lowest value
     */
    public void setMinY(double y) {
        mCurrentViewport.bottom = y;
    }

    /**
     * set the maximal x value for the current viewport.
     * Make sure to set the x bounds to manual via
     * {@link #setXAxisBoundsManual(boolean)}
     *
     * @param x max / highest value
     */
    public void setMaxX(double x) {
        mCurrentViewport.right = x;
    }

    /**
     * set the minimal x value for the current viewport.
     * Make sure to set the x bounds to manual via
     * {@link #setXAxisBoundsManual(boolean)}
     *
     * @param x min / lowest value
     */
    public void setMinX(double x) {
        mCurrentViewport.left = x;
    }

    /**
     * release the glowing effects
     */
    private void releaseEdgeEffects() {
        mEdgeEffectLeft.onRelease();
        mEdgeEffectRight.onRelease();
        mEdgeEffectTop.onRelease();
        mEdgeEffectBottom.onRelease();
    }

    /**
     * Draws the overscroll "glow" at the four edges of the chart region, if necessary.
     *
     * @see EdgeEffectCompat
     */
    private void drawEdgeEffectsUnClipped(Canvas canvas) {
        // The methods below rotate and translate the canvas as needed before drawing the glow,
        // since EdgeEffectCompat always draws a top-glow at 0,0.

        boolean needsInvalidate = false;

        if (!mEdgeEffectTop.isFinished()) {
            final int restoreCount = canvas.save();
            canvas.translate(mGraphView.getGraphContentLeft(), mGraphView.getGraphContentTop());
            mEdgeEffectTop.setSize(mGraphView.getGraphContentWidth(), mGraphView.getGraphContentHeight());
            if (mEdgeEffectTop.draw(canvas)) {
                needsInvalidate = true;
            }
            canvas.restoreToCount(restoreCount);
        }

        if (!mEdgeEffectBottom.isFinished()) {
            final int restoreCount = canvas.save();
            canvas.translate(mGraphView.getGraphContentLeft(), mGraphView.getGraphContentTop() + mGraphView.getGraphContentHeight());
            canvas.rotate(180, (float) mGraphView.getGraphContentWidth() / 2, 0);
            mEdgeEffectBottom.setSize(mGraphView.getGraphContentWidth(), mGraphView.getGraphContentHeight());
            if (mEdgeEffectBottom.draw(canvas)) {
                needsInvalidate = true;
            }
            canvas.restoreToCount(restoreCount);
        }

        if (!mEdgeEffectLeft.isFinished()) {
            final int restoreCount = canvas.save();
            canvas.translate(mGraphView.getGraphContentLeft(), mGraphView.getGraphContentTop() + mGraphView.getGraphContentHeight());
            canvas.rotate(-90, 0, 0);
            mEdgeEffectLeft.setSize(mGraphView.getGraphContentHeight(), mGraphView.getGraphContentWidth());
            if (mEdgeEffectLeft.draw(canvas)) {
                needsInvalidate = true;
            }
            canvas.restoreToCount(restoreCount);
        }

        if (!mEdgeEffectRight.isFinished()) {
            final int restoreCount = canvas.save();
            canvas.translate(mGraphView.getGraphContentLeft() + mGraphView.getGraphContentWidth(), mGraphView.getGraphContentTop());
            canvas.rotate(90, 0, 0);
            mEdgeEffectRight.setSize(mGraphView.getGraphContentHeight(), mGraphView.getGraphContentWidth());
            if (mEdgeEffectRight.draw(canvas)) {
                needsInvalidate = true;
            }
            canvas.restoreToCount(restoreCount);
        }

        if (needsInvalidate) {
            ViewCompat.postInvalidateOnAnimation(mGraphView);
        }
    }

    /**
     * will be first called in order to draw
     * the canvas
     * Used to draw the background
     *
     * @param c canvas.
     */
    public void drawFirst(Canvas c) {
        // draw background
        if (mBackgroundColor != Color.TRANSPARENT) {
            mPaint.setColor(mBackgroundColor);
            c.drawRect(
                    mGraphView.getGraphContentLeft(),
                    mGraphView.getGraphContentTop(),
                    mGraphView.getGraphContentLeft() + mGraphView.getGraphContentWidth(),
                    mGraphView.getGraphContentTop() + mGraphView.getGraphContentHeight(),
                    mPaint
            );
        }
        if (mDrawBorder) {
            Paint p;
            if (mBorderPaint != null) {
                p = mBorderPaint;
            } else {
                p = mPaint;
                p.setColor(getBorderColor());
            }
            c.drawLine(
                    mGraphView.getGraphContentLeft(),
                    mGraphView.getGraphContentTop(),
                    mGraphView.getGraphContentLeft(),
                    mGraphView.getGraphContentTop() + mGraphView.getGraphContentHeight(),
                    p
            );
            c.drawLine(
                    mGraphView.getGraphContentLeft(),
                    mGraphView.getGraphContentTop() + mGraphView.getGraphContentHeight(),
                    mGraphView.getGraphContentLeft() + mGraphView.getGraphContentWidth(),
                    mGraphView.getGraphContentTop() + mGraphView.getGraphContentHeight(),
                    p
            );
            // on the right side if we have second scale
            if (mGraphView.mSecondScale != null) {
                c.drawLine(
                        mGraphView.getGraphContentLeft() + mGraphView.getGraphContentWidth(),
                        mGraphView.getGraphContentTop(),
                        mGraphView.getGraphContentLeft() + mGraphView.getGraphContentWidth(),
                        mGraphView.getGraphContentTop() + mGraphView.getGraphContentHeight(),
                        p
                );
            }
        }
    }

    /**
     * draws the glowing edge effect
     *
     * @param c canvas
     */
    public void draw(Canvas c) {
        drawEdgeEffectsUnClipped(c);
    }

    /**
     * @return background of the viewport area
     * @noinspection unused
     */
    public int getBackgroundColor() {
        return mBackgroundColor;
    }

    /**
     * @param mBackgroundColor background of the viewport area
     *                         use transparent to have no background
     */
    public void setBackgroundColor(int mBackgroundColor) {
        this.mBackgroundColor = mBackgroundColor;
    }

    /**
     * @return whether the x axis bounds are manual.
     * @see #setMinX(double)
     * @see #setMaxX(double)
     */
    public boolean isXAxisBoundsManual() {
        return mXAxisBoundsManual;
    }

    /**
     * @param mXAxisBoundsManual whether the x axis bounds are manual.
     * @see #setMinX(double)
     * @see #setMaxX(double)
     */
    public void setXAxisBoundsManual(boolean mXAxisBoundsManual) {
        this.mXAxisBoundsManual = mXAxisBoundsManual;
        if (mXAxisBoundsManual) {
            mXAxisBoundsStatus = AxisBoundsStatus.FIX;
        }
    }

    /**
     * @return whether the y axis bound are manual
     */
    public boolean isYAxisBoundsManual() {
        return mYAxisBoundsManual;
    }

    /**
     * forces the viewport to scroll to the end
     * of the range by keeping the current viewport size.
     * <p>
     * Important: Only takes effect if x axis bounds are manual.
     *
     * @see #setXAxisBoundsManual(boolean)
     */
    public void scrollToEnd() {
        if (mXAxisBoundsManual) {
            double size = mCurrentViewport.width();
            mCurrentViewport.right = mCompleteRange.right;
            mCurrentViewport.left = mCompleteRange.right - size;
            mGraphView.onDataChanged(true, false);
        } else {
            Log.w("GraphView", "scrollToEnd works only with manual x axis bounds");
        }
    }

    /**
     * the border color used. will be ignored when
     * a custom paint is set.
     *
     * @return border color. by default the grid color is used
     * see #setDrawBorder(boolean)
     */
    public int getBorderColor() {
        if (mBorderColor != null) {
            return mBorderColor;
        }
        return mGraphView.getGridLabelRenderer().getGridColor();
    }

    /**
     * the reference number to generate the labels
     *
     * @return by default 0, only when manual bounds and no human rounding
     * is active, the min y value is returned
     */
    protected double getReferenceY() {
        // if the bounds is manual then we take the
        // original manual min y value as reference
        if (isYAxisBoundsManual() && !mGraphView.getGridLabelRenderer().isHumanRoundingY()) {
            if (Double.isNaN(referenceY)) {
                referenceY = getMinY(false);
            }
            return referenceY;
        } else {
            // starting from 0 so that the steps have nice numbers
            return 0;
        }
    }
}
