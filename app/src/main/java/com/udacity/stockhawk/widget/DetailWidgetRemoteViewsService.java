package com.udacity.stockhawk.widget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by Iuri Menin on 08/03/17.
 */

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DetailWidgetRemoteViewsService extends RemoteViewsService {
    public final String LOG_TAG = DetailWidgetRemoteViewsService.class.getSimpleName();
    private static final String[] FORECAST_COLUMNS = {
            Contract.Quote.TABLE_NAME + "." + Contract.Quote._ID,
            Contract.Quote.COLUMN_SYMBOL,
            Contract.Quote.COLUMN_PRICE,
            Contract.Quote.COLUMN_ABSOLUTE_CHANGE,
            Contract.Quote.COLUMN_HISTORY,
            Contract.Quote.COLUMN_PERCENTAGE_CHANGE
    };
    // these indices must match the projection
    static final int INDEX_QUOTE_ID = 0;
    static final int INDEX_QUOTE_SYMBOL = 1;
    static final int INDEX_QUOTE_PRICE = 2;
    static final int INDEX_QUOTE_ABSOLUTE_CHANGE = 3;
    static final int INDEX_QUOTE_HISTORY = 4;
    static final int INDEX_QUOTE_PERCENTAGE_CHANGE = 5;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
                // Nothing to do
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                final long identityToken = Binder.clearCallingIdentity();
                data = getContentResolver().query(Contract.Quote.URI,
                        Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                        null,
                        null,
                        Contract.Quote.COLUMN_SYMBOL);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION || data == null
                        || !data.moveToPosition(position)) {
                    return null;
                }

                RemoteViews remoteViews = new RemoteViews(getPackageName(),
                        R.layout.list_item_quote);

                String stockSymbol = data.getString(Contract.Quote.POSITION_SYMBOL);
                Float stockPrice = data.getFloat(Contract.Quote.POSITION_PRICE);
                Float absoluteChange = data.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
                int backgroundDrawable;

                DecimalFormat dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
                DecimalFormat dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
                dollarFormatWithPlus.setPositivePrefix("+");
                dollarFormatWithPlus.setMaximumFractionDigits(2);
                dollarFormat.setMaximumFractionDigits(2);
                dollarFormat.setMinimumFractionDigits(2);
                dollarFormatWithPlus.setMinimumFractionDigits(2);

                if (absoluteChange > 0) {
                    backgroundDrawable = R.drawable.percent_change_pill_green;
                } else {
                    backgroundDrawable = R.drawable.percent_change_pill_red;
                }

                remoteViews.setTextViewText(R.id.symbol, stockSymbol);
                remoteViews.setTextViewText(R.id.price, dollarFormat.format(stockPrice));
                remoteViews.setTextViewText(R.id.change, dollarFormatWithPlus.format(absoluteChange));
                remoteViews.setInt(R.id.change, "setBackgroundResource", backgroundDrawable);
                remoteViews.setInt(R.id.list_item_quote, "setBackgroundResource", R.color.material_grey_850);

                final Intent fillInIntent = new Intent();
                Uri stockUri = Contract.Quote.makeUriForStock(stockSymbol);
                fillInIntent.setData(stockUri);
                remoteViews.setOnClickFillInIntent(R.id.list_item_quote, fillInIntent);
                return remoteViews;
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
                return data.moveToPosition(position) ? data.getLong(Contract.Quote.POSITION_ID) : position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
