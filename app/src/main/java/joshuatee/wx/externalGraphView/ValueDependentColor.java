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

import joshuatee.wx.externalGraphView.series.DataPointInterface;

/**
 * you can change the color depending on the value.
 * takes only effect for BarGraphSeries.
 * <p>
 * see graphview.series.BarGraphSeries#setValueDependentColor(ValueDependentColor)
 */
public interface ValueDependentColor<T extends DataPointInterface> {
    /**
     * this is called when a bar is about to draw
     * and the color is be loaded.
     *
     * @param data the current input value
     * @return the color that the bar should be drawn with
     * Generate the int via the android.graphics.Color class.
     */
    int get(T data);
}
