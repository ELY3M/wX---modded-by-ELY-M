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
import android.graphics.Paint;

import joshuatee.wx.externalGraphView.series.Series;

import java.util.ArrayList;
import java.util.List;

/**
 * To be used to plot a second scale
 * on the graph.
 * The second scale has always to have
 * manual bounds.
 * Use #setMinY(double) and #setMaxY(double)
 * to set them.
 * The second scale has it's own array of series.
 *
 * @author jjoe64
 */
public class SecondScale {
    /**
     * reference to the graph
     */
    protected final GraphView mGraph;

    /**
     * array of series for the second
     * scale
     */
    protected final List<Series> mSeries;

    /**
     * flag whether the y axis bounds
     * are manual.
     * For the current version this is always
     * true.
     */
    private final boolean mYAxisBoundsManual = true;

    /**
     *
     */
    protected final RectD mCompleteRange = new RectD();

    protected final RectD mCurrentViewport = new RectD();

    /**
     * label formatter for the y labels
     * on the right side
     */
    protected final LabelFormatter mLabelFormatter;

    protected final double mReferenceY = Double.NaN;

    /**
     * the paint to draw axis titles
     */
    private Paint mPaintAxisTitle;

    /**
     * the title of the vertical axis
     */
    private String mVerticalAxisTitle;

    /**
     * font size of the vertical axis title
     */
    public float mVerticalAxisTitleTextSize;

    /**
     * font color of the vertical axis title
     */
    public int mVerticalAxisTitleColor;

    /**
     * creates the second scale.
     * normally you do not call this constructor.
     * Use graphview.GraphView#getSecondScale()
     * in order to get the instance.
     */
    SecondScale(GraphView graph) {
        mGraph = graph;
        mSeries = new ArrayList<>();
        mLabelFormatter = new DefaultLabelFormatter();
        mLabelFormatter.setViewport(mGraph.getViewport());
    }

    /**
     * @return the series of the second scale
     */
    public List<Series> getSeries() {
        return mSeries;
    }

    /**
     * @return min y bound
     */
    public double getMinY(boolean completeRange) {
        return completeRange ? mCompleteRange.bottom : mCurrentViewport.bottom;
    }

    /**
     * @return max y bound
     */
    public double getMaxY(boolean completeRange) {
        return completeRange ? mCompleteRange.top : mCurrentViewport.top;
    }

    /**
     * @return always true for the current implementation
     */
    public boolean isYAxisBoundsManual() {
        return mYAxisBoundsManual;
    }

    /**
     * @return label formatter for the y labels on the right side
     */
    public LabelFormatter getLabelFormatter() {
        return mLabelFormatter;
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
        List<Series> series = getSeries();
        mCompleteRange.set(0d, 0d, 0d, 0d);
        if (!series.isEmpty() && !series.get(0).isEmpty()) {
            double d = series.get(0).getLowestValueX();
            for (Series s : series) {
                if (!s.isEmpty() && d > s.getLowestValueX()) {
                    d = s.getLowestValueX();
                }
            }
            mCompleteRange.left = d;

            d = series.get(0).getHighestValueX();
            for (Series s : series) {
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
    }

    /**
     * @return the title of the vertical axis
     */
    public String getVerticalAxisTitle() {
        return mVerticalAxisTitle;
    }

    /**
     * @return font size of the vertical axis title
     */
    public float getVerticalAxisTitleTextSize() {
        if (getVerticalAxisTitle() == null || getVerticalAxisTitle().isEmpty()) {
            return 0;
        }
        return mVerticalAxisTitleTextSize;
    }

    /**
     * @param verticalAxisTitleTextSize font size of the vertical axis title
     */
    public void setVerticalAxisTitleTextSize(float verticalAxisTitleTextSize) {
        mVerticalAxisTitleTextSize = verticalAxisTitleTextSize;
    }

    /**
     * @return font color of the vertical axis title
     */
    public int getVerticalAxisTitleColor() {
        return mVerticalAxisTitleColor;
    }

    /**
     * draws the vertical axis title if
     * it is set
     *
     * @param canvas canvas
     */
    protected void drawVerticalAxisTitle(Canvas canvas) {
        if (mVerticalAxisTitle != null && !mVerticalAxisTitle.isEmpty()) {
            mPaintAxisTitle.setColor(getVerticalAxisTitleColor());
            mPaintAxisTitle.setTextSize(getVerticalAxisTitleTextSize());
            float x = canvas.getWidth() - getVerticalAxisTitleTextSize() / 2;
            float y = canvas.getHeight() / 2;
            canvas.save();
            canvas.rotate(-90, x, y);
            canvas.drawText(mVerticalAxisTitle, x, y, mPaintAxisTitle);
            canvas.restore();
        }
    }
}
