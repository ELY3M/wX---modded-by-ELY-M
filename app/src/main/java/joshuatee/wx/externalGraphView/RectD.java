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

/**
 * Created by jonas on 05.06.16.
 */
public class RectD {
    public double left;
    public double right;
    public double top;
    public double bottom;

    public RectD() {
    }

    public RectD(double lLeft, double lTop, double lRight, double lBottom) {
        set(lLeft, lTop, lRight, lBottom);
    }

    public double width() {
        return right - left;
    }

    public double height() {
        return bottom - top;
    }

    public void set(double lLeft, double lTop, double lRight, double lBottom) {
        left = lLeft;
        right = lRight;
        top = lTop;
        bottom = lBottom;
    }
}
