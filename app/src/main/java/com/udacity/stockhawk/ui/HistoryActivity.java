package com.udacity.stockhawk.ui;

import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spanned;
import android.view.MenuItem;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.utils.CustomMarkerView;
import com.udacity.stockhawk.utils.StockUtils;
import com.udacity.stockhawk.utils.XAxisDateFormatter;
import com.udacity.stockhawk.utils.YAxisPriceFormatter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HistoryActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int STOCK_LOADER = 1;
    private String symbol;

    @BindView(R.id.lineChart)
    LineChart mLineChart;

    @BindView(R.id.text_view_variations)
    TextView mTextViewVariations;

    private String mHistoryData;
    private String mName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        ButterKnife.bind(this);
        symbol = (String) getIntent().getExtras().getCharSequence("symbol");
        getSupportLoaderManager().initLoader(STOCK_LOADER, null, this);

        final Drawable upArrow = ContextCompat.getDrawable(this, R.drawable.ic_arrow_left_white_24dp);
        upArrow.setColorFilter(ContextCompat.getColor(HistoryActivity.this, android.R.color.white), PorterDuff.Mode.SRC_ATOP);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(upArrow);
        }

        if (mHistoryData != null)
            setUpLineChart();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                Contract.Quote.URI,
                new String[]{
                        Contract.Quote.COLUMN_NAME,
                        Contract.Quote.COLUMN_HISTORY
                },
                Contract.Quote.COLUMN_SYMBOL + " = ?",
                new String[]{symbol},
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        data.moveToFirst();
        //Index 0 because already list only history column
        mName = data.getString(0);
        mHistoryData = data.getString(1);
        setUpLineChart();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void setUpLineChart() {

        Spanned html;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            html = Html.fromHtml(getString(R.string.last_five_weeks, mName),Html.FROM_HTML_MODE_LEGACY);
        } else {
            html = Html.fromHtml(getString(R.string.last_five_weeks, mName));
        }
        mTextViewVariations.setText(html);

        Pair<Float, List<Entry>> result = StockUtils.getFormattedStockHistory(mHistoryData);
        List<Entry> dataPairs = result.second;
        Float referenceTime = result.first;
        LineDataSet dataSet = new LineDataSet(dataPairs, "");
        dataSet.setValueTextColor(android.R.color.white);
        dataSet.setHighLightColor(android.R.color.white);

        LineData lineData = new LineData(dataSet);
        mLineChart.setData(lineData);

        XAxis xAxis = mLineChart.getXAxis();
        xAxis.setValueFormatter(new XAxisDateFormatter("dd", referenceTime));
        xAxis.setDrawGridLines(false);

        YAxis yAxisRight = mLineChart.getAxisRight();
        yAxisRight.setEnabled(false);

        YAxis yAxis = mLineChart.getAxisLeft();
        yAxis.setValueFormatter(new YAxisPriceFormatter());
        yAxis.setDrawGridLines(false);

        MarkerView customMarkerView = new CustomMarkerView(this, R.layout.marker_view, getLastButOneData(dataPairs), referenceTime);

        Legend legend = mLineChart.getLegend();
        legend.setEnabled(false);

        mLineChart.setMarker(customMarkerView);

        //disable all interactions with the graph
        mLineChart.setDragEnabled(false);
        mLineChart.setScaleEnabled(false);
        mLineChart.setDragDecelerationEnabled(false);
        mLineChart.setPinchZoom(false);
        mLineChart.setDoubleTapToZoomEnabled(false);
        Description description = new Description();
        description.setText(" ");
        mLineChart.setDescription(description);
        mLineChart.setExtraOffsets(10, 0, 0, 10);
        mLineChart.animateX(1500, Easing.EasingOption.Linear);
        mLineChart.notifyDataSetChanged();
    }

    private Entry getLastButOneData(List<Entry> dataPairs) {
        if (dataPairs.size() > 2) {
            return dataPairs.get(dataPairs.size() - 2);
        } else {
            return dataPairs.get(dataPairs.size() - 1);
        }
    }
}
