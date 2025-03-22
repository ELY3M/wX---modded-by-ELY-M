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

package joshuatee.wx.externalGraphView.series;

/**
 * Listener for the tap event which will be
 * triggered when the user touches on a datapoint.
 * Use this in graphview.series.BaseSeries#setOnDataPointTapListener(OnDataPointTapListener)
 *
 * @author jjoe64
 */
public interface OnDataPointTapListener {
    /**
     * gets called when the user touches on a datapoint.
     *
     * @param series    the corresponding series
     * @param dataPoint the data point that was tapped on
     */
    void onTap(Series series, DataPointInterface dataPoint);
}
