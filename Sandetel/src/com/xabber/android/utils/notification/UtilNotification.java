package com.xabber.android.utils.notification;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class UtilNotification {

	public static String SENDER_ID = "745085116934";
	public static final String URL_BASE = "https://juntadeandalucia.es:8443/";
	// public static final String URL_BASE = "http://10.240.234.212:8080/";
	private static final String TAG = "notificacion_push";
	private static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";
	private static final String PROPERTY_EXPIRATION_TIME = "onServerExpirationTimeMs";
	private static final String PROPERTY_USER = "user";
	private static final String namePref = "preferencias";

	public static final String KEY_PREF_NOTIF = "keyPrefNotif";

	public static void register(Context context, String userID) {

		// Chequemos si está instalado Google Play Services
		if (checkPlayServices(context)) {
			// GoogleCloudMessaging gcm = GoogleCloudMessaging
			// .getInstance(context);

			// Obtenemos el Registration ID guardado
			String regid = getRegistrationId(context, userID);

			// Si no disponemos de Registration ID comenzamos el registro
			if (regid.equals("")) {

				TareaRegistroGCM tarea = new TareaRegistroGCM(context);
				tarea.execute(userID);
			}
		}

	}

	public static void unregister(Context context, String userID) {
		TareaDesRegistroGCM tarea = new TareaDesRegistroGCM(context);
		tarea.execute(userID);
	}

	private static boolean checkPlayServices(Context context) {
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(context);
		if (resultCode != ConnectionResult.SUCCESS) {

			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {

				try {
					GooglePlayServicesUtil.getErrorDialog(resultCode,
							(Activity) context, 9000).show();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				// Log.i(TAG, "Dispositivo no soportado.");
				// finish();
			}
			return false;
		}
		return true;
	}

	private static String getRegistrationId(Context context, String usrID) {
		SharedPreferences prefs = context.getSharedPreferences(namePref,
				Context.MODE_PRIVATE);

		String registrationId = prefs.getString(PROPERTY_REG_ID, "");

		if (registrationId.length() == 0) {
			// Log.d(TAG, "Registro GCM no encontrado.");
			return "";
		}

		String registeredUser = prefs.getString(PROPERTY_USER, registrationId);

		int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION,
				Integer.MIN_VALUE);

		long expirationTime = prefs.getLong(PROPERTY_EXPIRATION_TIME, -1);

		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm",
				Locale.getDefault());
		String expirationDate = sdf.format(new Date(expirationTime));

		Log.d(TAG, "Registro GCM encontrado (usuario=" + registeredUser
				+ ", version=" + registeredVersion + ", expira="
				+ expirationDate + ")");

		int currentVersion = getAppVersion(context);

		if (registeredVersion != currentVersion) {
			Log.d(TAG, "Nueva versión de la aplicación.");
			return "";
		} else if (System.currentTimeMillis() > expirationTime) {
			Log.d(TAG, "Registro GCM expirado.");
			return "";
		} else if (!usrID.toString().equals(registeredUser)) {
			Log.d(TAG, "Nuevo nombre de usuario.");
			return "";
		}

		return registrationId;
	}

	private static int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0);

			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			throw new RuntimeException("Error al obtener versión: " + e);
		}
	}

}
