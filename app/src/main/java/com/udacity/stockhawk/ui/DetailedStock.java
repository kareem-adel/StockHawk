package com.udacity.stockhawk.ui;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class DetailedStock extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int STOCK_LOADER = 0;
    private LineChart mChart;
    private String symbol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //this is just a workaround as the api switched to https which is not supported yet by the library
        System.setProperty("yahoofinance.baseurl.quotes", "http://finance.yahoo.com/d/quotes.csv");
        System.setProperty("yahoofinance.baseurl.histquotes", "https://ichart.yahoo.com/table.csv");

        setContentView(R.layout.activity_detailed_stock);

        symbol = getIntent().getStringExtra(getString(R.string.symbol));

        getSupportLoaderManager().initLoader(STOCK_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                Contract.Quote.URI,
                Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                getString(R.string.symbol_param), new String[]{symbol}, Contract.Quote.COLUMN_SYMBOL);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.getCount() > 0) {
            data.moveToFirst();
            String historicalResult = data.getString(Contract.Quote.POSITION_HISTORY);

            String[] historyItems = historicalResult.split("\n");

            final ArrayList<Entry> values = new ArrayList<>();

            int i = historyItems.length;
            for (String historyItem : historyItems) {
                String[] DatePrice = historyItem.split(", ");
                String x = DatePrice[0];
                String y = DatePrice[1];
                values.add(0, new Entry(i--, Float.valueOf(y), x));
            }

            mChart = (LineChart) findViewById(R.id.stock_history_chart);
            mChart.setViewPortOffsets(0, 0, 0, 0);
            mChart.setBackgroundColor(Color.rgb(104, 241, 175));
            mChart.getDescription().setEnabled(false);
            mChart.setTouchEnabled(true);
            mChart.setDragEnabled(true);
            mChart.setScaleEnabled(true);
            mChart.setPinchZoom(true);
            mChart.setDrawGridBackground(false);

            XAxis x = mChart.getXAxis();
            x.setEnabled(true);
            x.setTextColor(Color.BLUE);
            x.setPosition(XAxis.XAxisPosition.TOP_INSIDE);
            x.setLabelCount(3);
            x.setValueFormatter(new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    Entry entry = values.get((int) value);
                    String date = (String) entry.getData();
                    return new SimpleDateFormat(getString(R.string.date_format)).format(Long.parseLong(date));
                }
            });

            YAxis y = mChart.getAxisLeft();
            y.setTextColor(Color.BLUE);
            y.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
            y.setDrawGridLines(false);
            y.setAxisLineColor(Color.BLACK);
            mChart.getAxisRight().setEnabled(false);

            setData(values);

            mChart.invalidate();
        }
    }

    private void setData(ArrayList<Entry> values) {

        LineDataSet lineDataSet;

        if (mChart.getData() != null
                && mChart.getData().getDataSetCount() > 0) {
            lineDataSet = (LineDataSet) mChart.getData().getDataSetByIndex(0);
            lineDataSet.setValues(values);
            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();
        } else {
            lineDataSet = new LineDataSet(values, getString(R.string.stock_history));
            lineDataSet.enableDashedLine(10f, 5f, 0f);
            lineDataSet.enableDashedHighlightLine(10f, 5f, 0f);
            lineDataSet.setColor(Color.BLACK);
            lineDataSet.setCircleColor(Color.BLACK);
            lineDataSet.setLineWidth(1f);
            lineDataSet.setCircleRadius(3f);
            lineDataSet.setDrawCircleHole(false);
            lineDataSet.setValueTextSize(9f);
            lineDataSet.setDrawFilled(true);
            lineDataSet.setFormLineWidth(1f);
            lineDataSet.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            lineDataSet.setFormSize(15.f);

            lineDataSet.setFillColor(Color.BLACK);

            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(lineDataSet);
            LineData data = new LineData(dataSets);
            mChart.setData(data);
        }

    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
