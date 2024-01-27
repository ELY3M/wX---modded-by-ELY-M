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

/**
 * Interface to use as label formatter.
 * Implement this in order to generate
 * your own labels format.
 * It is recommended to override {@link com.jjoe64.graphview.DefaultLabelFormatter}.
 *
 * @author jjoe64
 */
public interface LabelFormatter {
    /**
     * converts a raw number as input to
     * a formatted string for the label.
     *
     * @param value    raw input number
     * @param isValueX true if it is a value for the x axis
     *                 false if it is a value for the y axis
     * @return the formatted number as string
     */
    String formatLabel(double value, boolean isValueX);

    /**
     * will be called in order to have a
     * reference to the current viewport.
     * This is useful if you need the bounds
     * to generate your labels.
     * You store this viewport in as member variable
     * and access it e.g. in the {@link #formatLabel(double, boolean)}
     * method.
     *
     * @param viewport the used viewport
     */
    void setViewport(Viewport viewport);
}
