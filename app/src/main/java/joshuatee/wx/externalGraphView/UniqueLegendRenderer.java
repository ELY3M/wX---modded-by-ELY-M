// Downloaded from the following URL on 2023-12-30
// https://github.com/jjoe64/GraphView
// Please see license at doc/COPYING.GraphView (APL2.0)

package joshuatee.wx.externalGraphView;

import android.util.Pair;

import joshuatee.wx.externalGraphView.series.Series;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A LegendRenderer that renders items with the same name and color only once in the legend
 * Created by poseidon on 27.02.18.
 */
public class UniqueLegendRenderer extends LegendRenderer {
    /**
     * creates legend renderer
     *
     * @param graphView regarding graphview
     */
    public UniqueLegendRenderer(GraphView graphView) {
        super(graphView);
    }

    @Override
    protected List<Series> getAllSeries() {
        List<Series> originalSeries = super.getAllSeries();
        List<Series> distinctSeries = new ArrayList<>();
        Set<Pair<Integer, String>> uniqueSeriesKeys = new HashSet<>();
        for (Series series : originalSeries)
            if (uniqueSeriesKeys.add(new Pair<>(series.getColor(), series.getTitle())))
                distinctSeries.add(series);
        return distinctSeries;
    }
}
