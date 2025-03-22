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

package joshuatee.wx.externalGraphView.helper;

import joshuatee.wx.externalGraphView.DefaultLabelFormatter;
import joshuatee.wx.externalGraphView.GraphView;
import joshuatee.wx.externalGraphView.LabelFormatter;
import joshuatee.wx.externalGraphView.Viewport;

/**
 * Use this label formatter to show static labels.
 * Static labels are not bound to the data. It is typical used
 * for show text like "low", "middle", "high".
 * You can set the static labels for vertical or horizontal
 * individually and you can define a label formatter that
 * is to be used if you don't define static labels.
 * For example if you only use static labels for horizontal labels,
 * graphview will use the dynamicLabelFormatter for the vertical labels.
 */
public class StaticLabelsFormatter implements LabelFormatter {
    /**
     * reference to the viewport
     */
    protected Viewport mViewport;

    /**
     * the vertical labels, ordered from bottom to the top
     * if it is null, the labels will be generated via the #dynamicLabelFormatter
     */
    protected String[] mVerticalLabels;

    /**
     * the horizontal labels, ordered form the left to the right
     * if it is null, the labels will be generated via the #dynamicLabelFormatter
     */
    protected String[] mHorizontalLabels;

    /**
     * the label formatter that will format the labels
     * for that there are no static labels defined.
     */
    protected LabelFormatter mDynamicLabelFormatter;

    /**
     * reference to the graphview
     */
    protected final GraphView mGraphView;

    /**
     * creates the formatter without any static labels
     * define your static labels via #setHorizontalLabels(String[]) and #setVerticalLabels(String[])
     *
     * @param graphView reference to the graphview
     */
    public StaticLabelsFormatter(GraphView graphView) {
        mGraphView = graphView;
        init(null, null, null);
    }

    /**
     * @param horizontalLabels      the horizontal labels, ordered form the left to the right
     *                              if it is null, the labels will be generated via the #dynamicLabelFormatter
     * @param verticalLabels        the vertical labels, ordered from bottom to the top
     *                              if it is null, the labels will be generated via the #dynamicLabelFormatter
     * @param dynamicLabelFormatter the label formatter that will format the labels
     *                              for that there are no static labels defined.
     */
    protected void init(String[] horizontalLabels, String[] verticalLabels, LabelFormatter dynamicLabelFormatter) {
        mDynamicLabelFormatter = dynamicLabelFormatter;
        if (mDynamicLabelFormatter == null) {
            mDynamicLabelFormatter = new DefaultLabelFormatter();
        }

        mHorizontalLabels = horizontalLabels;
        mVerticalLabels = verticalLabels;
    }

    /**
     * @param value    raw input number
     * @param isValueX true if it is a value for the x axis
     *                 false if it is a value for the y axis
     * @return
     */
    @Override
    public String formatLabel(double value, boolean isValueX) {
        if (isValueX && mHorizontalLabels != null) {
            double minX = mViewport.getMinX(false);
            double maxX = mViewport.getMaxX(false);
            double range = maxX - minX;
            value = value - minX;
            int idx = (int) ((value / range) * (mHorizontalLabels.length - 1));
            return mHorizontalLabels[idx];
        } else if (!isValueX && mVerticalLabels != null) {
            double minY = mViewport.getMinY(false);
            double maxY = mViewport.getMaxY(false);
            double range = maxY - minY;
            value = value - minY;
            int idx = (int) ((value / range) * (mVerticalLabels.length - 1));
            return mVerticalLabels[idx];
        } else {
            return mDynamicLabelFormatter.formatLabel(value, isValueX);
        }
    }

    /**
     * @param viewport the used viewport
     */
    @Override
    public void setViewport(Viewport viewport) {
        mViewport = viewport;
        adjust();
    }

    /**
     * adjusts the number of vertical/horizontal labels
     */
    protected void adjust() {
        mDynamicLabelFormatter.setViewport(mViewport);
        if (mVerticalLabels != null) {
            if (mVerticalLabels.length < 2) {
                throw new IllegalStateException("You need at least 2 vertical labels if you use static label formatter.");
            }
            mGraphView.getGridLabelRenderer().setNumVerticalLabels(mVerticalLabels.length);
        }
        if (mHorizontalLabels != null) {
            if (mHorizontalLabels.length < 2) {
                throw new IllegalStateException("You need at least 2 horizontal labels if you use static label formatter.");
            }
            mGraphView.getGridLabelRenderer().setNumHorizontalLabels(mHorizontalLabels.length);
        }
    }
}
