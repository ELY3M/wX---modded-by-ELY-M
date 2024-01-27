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

package joshuatee.wx.externalGraphView.helper;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;

import joshuatee.wx.R;
import joshuatee.wx.externalGraphView.GraphView;
import joshuatee.wx.externalGraphView.series.BarGraphSeries;
import joshuatee.wx.externalGraphView.series.BaseSeries;
import joshuatee.wx.externalGraphView.series.DataPoint;
import joshuatee.wx.externalGraphView.series.LineGraphSeries;
import joshuatee.wx.externalGraphView.series.PointsGraphSeries;

/**
 * helper class to use GraphView directly
 * in a XML layout file.
 * <p>
 * You can set the data via attribute <b>app:seriesData</b>
 * in the format: "X=Y;X=Y;..." e.g. "0=5.0;1=5;2=4;3=9"
 * <p>
 * Other styling options:
 * <li>app:seriesType="line|bar|points"</li>
 * <li>app:seriesColor="#ff0000"</li>
 * <li>app:seriesTitle="foobar" - if this is set, the legend will be drawn</li>
 * <li>android:title="foobar"</li>
 * <p>
 * Example:
 * <pre>
 * {@code
 *  <com.jjoe64.graphview.helper.GraphViewXML
 *      android:layout_width="match_parent"
 *      android:layout_height="100dip"
 *      app:seriesData="0=5;2=5;3=0;4=2"
 *      app:seriesType="line"
 *      app:seriesColor="#ee0000" />
 * }
 * </pre>
 *
 * @author jjoe64
 */
public class GraphViewXML extends GraphView {
    /**
     * creates the graphview object with data and
     * other options from xml attributes.
     *
     * @param context
     * @param attrs
     */
    public GraphViewXML(Context context, AttributeSet attrs) {
        super(context, attrs);

        // get attributes
        TypedArray a = context.obtainStyledAttributes(
                attrs,
                R.styleable.GraphViewXML);

        String dataStr = a.getString(R.styleable.GraphViewXML_seriesData);
        int color = a.getColor(R.styleable.GraphViewXML_seriesColor, Color.TRANSPARENT);
        String type = a.getString(R.styleable.GraphViewXML_seriesType);
        String seriesTitle = a.getString(R.styleable.GraphViewXML_seriesTitle);
        String title = a.getString(R.styleable.GraphViewXML_android_title);

        a.recycle();

        // decode data
        DataPoint[] data;
        if (dataStr == null || dataStr.isEmpty()) {
            throw new IllegalArgumentException("Attribute seriesData is required in the format: 0=5.0;1=5;2=4;3=9");
        } else {
            String[] d = dataStr.split(";");
            try {
                data = new DataPoint[d.length];
                int i = 0;
                for (String dd : d) {
                    String[] xy = dd.split("=");
                    data[i] = new DataPoint(Double.parseDouble(xy[0]), Double.parseDouble(xy[1]));
                    i++;
                }
            } catch (Exception e) {
                Log.e("GraphViewXML", e.toString());
                throw new IllegalArgumentException("Attribute seriesData is broken. Use this format: 0=5.0;1=5;2=4;3=9");
            }
        }

        // create series
        BaseSeries<DataPoint> series;
        if (type == null || type.isEmpty()) {
            type = "line";
        }
        switch (type) {
            case "line":
                series = new LineGraphSeries<>(data);
                break;
            case "bar":
                series = new BarGraphSeries<>(data);
                break;
            case "points":
                series = new PointsGraphSeries<>(data);
                break;
            default:
                throw new IllegalArgumentException("unknown graph type: " + type + ". Possible is line|bar|points");
        }
        if (color != Color.TRANSPARENT) {
            series.setColor(color);
        }
        addSeries(series);

        if (seriesTitle != null && !seriesTitle.isEmpty()) {
            series.setTitle(seriesTitle);
            getLegendRenderer().setVisible(true);
        }

        if (title != null && !title.isEmpty()) {
            setTitle(title);
        }
    }
}
