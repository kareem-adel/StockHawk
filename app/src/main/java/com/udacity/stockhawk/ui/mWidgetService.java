package com.udacity.stockhawk.ui;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.support.v4.content.CursorLoader;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by kareem on 4/19/2017.
 */

public class mWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StackRemoteViewsFactory(this.getApplicationContext(), intent);
    }

    private class StackRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

        private final Context mContext;
        private Cursor cursor;
        private DecimalFormat dollarFormat;
        private DecimalFormat dollarFormatWithPlus;
        private DecimalFormat percentageFormat;

        public StackRemoteViewsFactory(Context context, Intent intent) {
            mContext = context;
        }

        @Override
        public void onCreate() {

            //this is just a workaround as the api switched to https which is not supported yet by the library
            System.setProperty("yahoofinance.baseurl.quotes", "http://finance.yahoo.com/d/quotes.csv");
            System.setProperty("yahoofinance.baseurl.histquotes", "https://ichart.yahoo.com/table.csv");

            dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
            dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
            dollarFormatWithPlus.setPositivePrefix("+$");
            percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
            percentageFormat.setMaximumFractionDigits(2);
            percentageFormat.setMinimumFractionDigits(2);
            percentageFormat.setPositivePrefix("+");


            final AppWidgetManager mgr = AppWidgetManager.getInstance(mContext);
            final ComponentName cn = new ComponentName(mContext, mWidgetProvider.class);
            ContentObserver contentObserver = new ContentObserver(new Handler()) {
                @Override
                public void onChange(boolean selfChange) {
                    super.onChange(selfChange);
                    cursor = new CursorLoader(mWidgetService.this, Contract.Quote.URI, Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}), null, null, Contract.Quote.COLUMN_SYMBOL).loadInBackground();
                    mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(cn), R.id.stack_view);
                }
            };
            mContext.getContentResolver().registerContentObserver(Contract.Quote.URI, true, contentObserver);

            cursor = new CursorLoader(mWidgetService.this, Contract.Quote.URI, Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}), null, null, Contract.Quote.COLUMN_SYMBOL).loadInBackground();
        }

        @Override
        public void onDataSetChanged() {

        }


        @Override
        public void onDestroy() {
            if (cursor != null) cursor.close();
        }

        @Override
        public int getCount() {
            int count = 0;
            if (cursor != null) {
                count = cursor.getCount();
            }
            return count;
        }

        @Override
        public RemoteViews getViewAt(int position) {
            cursor.moveToPosition(position);

            RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.list_item_quote);

            rv.setTextViewText(R.id.symbol, cursor.getString(Contract.Quote.POSITION_SYMBOL));
            rv.setTextViewText(R.id.price, cursor.getString(Contract.Quote.POSITION_PRICE));


            float rawAbsoluteChange = cursor.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
            float percentageChange = cursor.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);

            if (rawAbsoluteChange > 0) {
                rv.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_green);
            } else {
                rv.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_red);
            }

            String change = dollarFormatWithPlus.format(rawAbsoluteChange);
            String percentage = percentageFormat.format(percentageChange / 100);

            if (PrefUtils.getDisplayMode(mContext).equals(mContext.getString(R.string.pref_display_mode_absolute_key))) {
                rv.setTextViewText(R.id.change, change);
            } else {
                rv.setTextViewText(R.id.change, percentage);
            }

            return rv;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }

}
