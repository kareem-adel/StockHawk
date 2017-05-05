package com.udacity.stockhawk.ui;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.widget.RemoteViews;

import com.udacity.stockhawk.R;

/**
 * Created by kareem on 4/11/2017.
 */

public class mWidgetProvider extends AppWidgetProvider {

    ContentObserver contentObserver;


    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        //QuoteSyncJob.syncImmediately(context);
        //this is just a workaround as the api switched to https which is not supported yet by the library
        System.setProperty("yahoofinance.baseurl.quotes", "http://finance.yahoo.com/d/quotes.csv");
        System.setProperty("yahoofinance.baseurl.histquotes", "https://ichart.yahoo.com/table.csv");

        for (int appWidgetId : appWidgetIds) {
            Intent intentMain = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intentMain, 0);
            Intent intent = new Intent(context, mWidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.appwidget);
            rv.setOnClickPendingIntent(R.id.widget_container, pendingIntent);
            rv.setRemoteAdapter(R.id.stack_view, intent);
            rv.setEmptyView(R.id.stack_view, R.id.empty_view);
            appWidgetManager.updateAppWidget(appWidgetId, rv);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
}
