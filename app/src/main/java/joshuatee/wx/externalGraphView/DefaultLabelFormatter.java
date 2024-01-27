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

// Downloaded from the following URL on 2023-12-30
// https://github.com/jjoe64/GraphView
// Please see license at doc/COPYING.GraphView (APL2.0)

package joshuatee.wx.externalGraphView;

import java.text.NumberFormat;

/**
 * The label formatter that will be used
 * by default.
 * It will use the NumberFormat from Android
 * and sets the maximal fraction digits
 * depending on the range between min and max
 * value of the current viewport.
 * It is recommended to use this label formatter
 * as base class to implement a custom formatter.
 *
 * @author jjoe64
 */
public class DefaultLabelFormatter implements LabelFormatter {
    /**
     * number formatter for x and y values
     */
    protected final NumberFormat[] mNumberFormatter = new NumberFormat[2];

    /**
     * reference to the viewport of the
     * graph.
     * Will be used to calculate the current
     * range of values.
     */
    protected Viewport mViewport;

    /**
     * uses the default number format for the labels
     */
    public DefaultLabelFormatter() {
    }

    /**
     * use custom number format
     *
     * @param xFormat the number format for the x labels
     * @param yFormat the number format for the y labels
     */
    public DefaultLabelFormatter(NumberFormat xFormat, NumberFormat yFormat) {
        mNumberFormatter[0] = yFormat;
        mNumberFormatter[1] = xFormat;
    }

    /**
     * @param viewport the viewport of the graph
     */
    @Override
    public void setViewport(Viewport viewport) {
        mViewport = viewport;
    }

    /**
     * Formats the raw value to a nice
     * looking label, depending on the
     * current range of the viewport.
     *
     * @param value    raw value
     * @param isValueX true if it's a x value, otherwise false
     * @return the formatted value as string
     */
    public String formatLabel(double value, boolean isValueX) {
        int i = isValueX ? 1 : 0;
        if (mNumberFormatter[i] == null) {
            mNumberFormatter[i] = NumberFormat.getNumberInstance();
            double highestValue = isValueX ? mViewport.getMaxX(false) : mViewport.getMaxY(false);
            double lowestValue = isValueX ? mViewport.getMinX(false) : mViewport.getMinY(false);
            if (highestValue - lowestValue < 0.1) {
                mNumberFormatter[i].setMaximumFractionDigits(6);
            } else if (highestValue - lowestValue < 1) {
                mNumberFormatter[i].setMaximumFractionDigits(4);
            } else if (highestValue - lowestValue < 20) {
                mNumberFormatter[i].setMaximumFractionDigits(3);
            } else if (highestValue - lowestValue < 100) {
                mNumberFormatter[i].setMaximumFractionDigits(1);
            } else {
                mNumberFormatter[i].setMaximumFractionDigits(0);
            }
        }
        return mNumberFormatter[i].format(value);
    }
}
