package org.godsendjoseph.pet_app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;
public class CurrencyUtils {
    private static final String PREF_CURRENCY = "currency";
    private static final String DEFAULT_CURRENCY = "$";

    public static String formatCurrency(Context context, double amount) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String currencySymbol = preferences.getString(PREF_CURRENCY, DEFAULT_CURRENCY);

        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.getDefault());

        try {
            // Try to find a currency by symbol
            switch (currencySymbol) {
                case "£":
                    format.setCurrency(Currency.getInstance("GBP"));
                    break;
                case "€":
                    format.setCurrency(Currency.getInstance("EUR"));
                    break;
                case "¥":
                    format.setCurrency(Currency.getInstance("JPY"));
                    break;
                case "₹":
                    format.setCurrency(Currency.getInstance("INR"));
                    break;
                default:
                    format.setCurrency(Currency.getInstance("USD"));
            }
        } catch (Exception e) {
            // If there's an error, just use the default formatter
        }

        return format.format(amount);
    }
}
