package com.udacity.stockhawk.sync;

import android.app.IntentService;
import android.content.Intent;

import timber.log.Timber;


public class QuoteIntentService extends IntentService {

    public QuoteIntentService() {
        super(QuoteIntentService.class.getSimpleName());
        System.setProperty("yahoofinance.baseurl.quotes", "http://finance.yahoo.com/d/quotes.csv");
        System.setProperty("yahoofinance.baseurl.histquotes", "https://ichart.yahoo.com/table.csv");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Timber.d("Intent handled");
        QuoteSyncJob.getQuotes(getApplicationContext());
    }
}
