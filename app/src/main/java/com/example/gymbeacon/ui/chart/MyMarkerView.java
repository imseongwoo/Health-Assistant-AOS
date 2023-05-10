package com.example.gymbeacon.ui.chart;

import android.content.Context;
import android.widget.TextView;

import com.example.gymbeacon.R;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Utils;

public class MyMarkerView extends MarkerView {
    private TextView lineChartContent;
//    private static Context context;

    public MyMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);

//        this.context = context;
        lineChartContent = (TextView) findViewById(R.id.lineChartContentHead);

    }

//    public MyMarkerView() {
//        super(MyMarkerView.context, R.layout.custom_marker_view);
//    }

    // callbacks everytime the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    @Override
    public void refreshContent(Entry e, Highlight highlight) {

        if (e instanceof CandleEntry) {
            CandleEntry ce = (CandleEntry) e;
            lineChartContent.setText("" + Utils.formatNumber((int)ce.getHigh(), 0, true) + "회");
        } else {
            lineChartContent.setText("" + Utils.formatNumber((int)e.getY(), 0, true) + "회");
        }

        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth() / 2), -getHeight());
    }
}
