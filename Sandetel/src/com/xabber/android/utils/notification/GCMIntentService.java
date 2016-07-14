package com.xabber.android.utils.notification;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.xabber.android.ui.own.LoginAct;

import es.juntadeandalucia.android.im.R;

public class GCMIntentService extends IntentService {
	private static final int NOTIF_ALERTA_ID = 1;

	public GCMIntentService() {
		super("GCMIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

		String messageType = gcm.getMessageType(intent);
		Bundle extras = intent.getExtras();
		
		System.out.println("ENTRA NOTIFICACION");

		if (!extras.isEmpty()) {
			if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
				mostrarNotification(extras.getString("txt"));
			}
		}

		GCMBroadcastReceiver.completeWakefulIntent(intent);
	}

	private void mostrarNotification(String msg) {
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this)
				.setSmallIcon(R.drawable.ico_notify)
				.setContentTitle(
						getApplicationContext().getString(R.string.app_name))
				.setContentText(msg);

		Intent notIntent = new Intent(this, LoginAct.class);
		PendingIntent contIntent = PendingIntent.getActivity(this, 0,
				notIntent, 0);

		mBuilder.setContentIntent(contIntent);

		mBuilder.setAutoCancel(true);

		mNotificationManager.notify(NOTIF_ALERTA_ID, mBuilder.build());
	}
}
