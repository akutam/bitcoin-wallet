/*
 * Copyright 2011-2014 the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.schildbach.wallet;

import java.math.BigInteger;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.widget.RemoteViews;

import com.google.bitcoin.core.Wallet;
import com.google.bitcoin.core.Wallet.BalanceType;

import de.schildbach.wallet.ui.RequestCoinsActivity;
import de.schildbach.wallet.ui.SendCoinsQrActivity;
import de.schildbach.wallet.ui.WalletActivity;
import de.schildbach.wallet.ui.send.SendCoinsActivity;
import de.schildbach.wallet.util.GenericUtils;
import de.schildbach.wallet.util.WalletUtils;
import de.schildbach.wallet_test.R;

/**
 * @author Andreas Schildbach
 */
public class WalletBalanceWidgetProvider extends AppWidgetProvider
{
	private static final Logger log = LoggerFactory.getLogger(WalletBalanceWidgetProvider.class);

	@Override
	public void onUpdate(final Context context, final AppWidgetManager appWidgetManager, final int[] appWidgetIds)
	{
		final WalletApplication application = (WalletApplication) context.getApplicationContext();
		final Wallet wallet = application.getWallet();
		final BigInteger balance = wallet.getBalance(BalanceType.ESTIMATED);

		updateWidgets(context, appWidgetManager, appWidgetIds, balance);
	}

	public static void updateWidgets(final Context context, final Wallet wallet)
	{
		final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

		final ComponentName providerName = new ComponentName(context, WalletBalanceWidgetProvider.class);

		try
		{
			final int[] appWidgetIds = appWidgetManager.getAppWidgetIds(providerName);

			if (appWidgetIds.length > 0)
			{
				final BigInteger balance = wallet.getBalance(BalanceType.ESTIMATED);

				WalletBalanceWidgetProvider.updateWidgets(context, appWidgetManager, appWidgetIds, balance);
			}
		}
		catch (final RuntimeException x) // system server dead?
		{
			log.warn("cannot update app widgets", x);
		}
	}

	private static void updateWidgets(final Context context, @Nonnull final AppWidgetManager appWidgetManager, @Nonnull final int[] appWidgetIds,
			@Nonnull final BigInteger balance)
	{
		final Configuration config = new Configuration(PreferenceManager.getDefaultSharedPreferences(context));
		final Spannable balanceStr = new SpannableString(GenericUtils.formatValue(balance, config.getBtcPrecision(), config.getBtcShift()));
		WalletUtils.formatSignificant(balanceStr, WalletUtils.SMALLER_SPAN);

		for (final int appWidgetId : appWidgetIds)
		{
			final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.wallet_balance_widget_content);

			final String currencyCode = config.getBtcPrefix();
			if (Constants.CURRENCY_CODE_BTC.equals(currencyCode))
				views.setImageViewResource(R.id.widget_wallet_prefix, R.drawable.currency_symbol_btc);
			else if (Constants.CURRENCY_CODE_MBTC.equals(currencyCode))
				views.setImageViewResource(R.id.widget_wallet_prefix, R.drawable.currency_symbol_mbtc);
			else if (Constants.CURRENCY_CODE_UBTC.equals(currencyCode))
				views.setImageViewResource(R.id.widget_wallet_prefix, R.drawable.currency_symbol_ubtc);

			views.setTextViewText(R.id.widget_wallet_balance, balanceStr);

			views.setOnClickPendingIntent(R.id.widget_button_balance,
					PendingIntent.getActivity(context, 0, new Intent(context, WalletActivity.class), 0));
			views.setOnClickPendingIntent(R.id.widget_button_request,
					PendingIntent.getActivity(context, 0, new Intent(context, RequestCoinsActivity.class), 0));
			views.setOnClickPendingIntent(R.id.widget_button_send,
					PendingIntent.getActivity(context, 0, new Intent(context, SendCoinsActivity.class), 0));
			views.setOnClickPendingIntent(R.id.widget_button_send_qr,
					PendingIntent.getActivity(context, 0, new Intent(context, SendCoinsQrActivity.class), 0));

			appWidgetManager.updateAppWidget(appWidgetId, views);
		}
	}
}
