package com.xabber.android.utils.notification;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.xabber.android.ui.ContactList;
import com.xabber.android.utils.gson.GsonUtil;
import com.xabber.android.utils.preferences.UtilPreferences;

public class TareaDesRegistroGCM extends AsyncTask<String, Integer, Boolean> {

	private static final String PROPERTY_USER = "user";
	private static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";
	private static final String PROPERTY_EXPIRATION_TIME = "onServerExpirationTimeMs";
	private static final Long EXPIRATION_TIME_MS = (long) (1000 * 3600 * 24 * 7);
	private static final String namePref = "preferencias";
	private static final String TAG = "notificacion_push";
	private Context context;

	private static final String URL_DOWN_USER = "msg-push-server/BajaCliente";

	private static final String passphrase = "m3ns4j3r14_54d351";
	private static final int MAX_ATTEMPTS = 5;
	private static final int BACKOFF_MILLI_SECONDS = 2000;
	private static final Random random = new Random();
	public static final int SERVER_TIME = 15000;

	public TareaDesRegistroGCM(Context context) {
		this.context = context;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);

		if (this.context != null && this.context instanceof ContactList) {
			((ContactList) this.context).exit();
		}

	}

	@Override
	protected Boolean doInBackground(String... params) {
		Boolean result = false;

		try {
			// if (gcm == null) {

			// }

			// Log.d(TAG, "Registrado en GCM: registration_id=" + regid);

			// Nos registramos en nuestro servidor
			boolean registrado = unRegister(context, params[0]);

			// Guardamos los datos del registro
			if (registrado) {

				UtilPreferences.setPreferences(context,
						UtilNotification.KEY_PREF_NOTIF, false);

				GoogleCloudMessaging gcm = GoogleCloudMessaging
						.getInstance(context);
				gcm.unregister();
				// setRegistrationId(context, params[0], "");
				setRegistrationId(context);

				result = true;
			}
		} catch (IOException ex) {
			Log.d(TAG, "Error registro en GCM:" + ex.getMessage());
		}

		return result;
	}

	public static boolean unRegister(Context context, String userID) {

		// IdentificationManager.sync(context);

		// Log.i(TAG, "registering device (regId = " + regId + ")");

		boolean bSalida = true;
		Boolean res = null;

		long backoff = BACKOFF_MILLI_SECONDS + random.nextInt(1000);
		// Once GCM returns a registration id, we need to register it in the
		// demo server. As the server might be down, we will retry it a couple
		// times.
		for (int i = 1; i <= MAX_ATTEMPTS; i++) {
			Log.d(TAG, "Attempt #" + i + " to register");
			try {

				List<String> params = new ArrayList<String>();

				// OctopushDispositive dispositivo = new OctopushDispositive(
				// idUsuario, OctopushConstants.TIPO_DISP,
				// IdentificationManager.getOpenUDID(), regId,
				// getAppKey(context), Locale.getDefault().toString());
				String url = URL_DOWN_USER;
				if (userID != null) {
					url += "?uid=" + userID;
				}
				// if (regId != null) {
				//
				// url += "&id_tlfno=" + regId;
				// }
				//
				// url += "&red=" + red;

				url += "&passphrase=" + passphrase;

				Log.d("URL REGISTER", "URL REGISTER " + url);

				res = callServicePost(context, UtilNotification.URL_BASE, url,
						params, null);

				if (res != null && res) {
					bSalida = true;
					break;
				}
				backoff *= 2;
			} catch (Exception e) {

				Log.e(TAG, "Failed to register on attempt " + i, e);
				if (i == MAX_ATTEMPTS) {
					break;
				}
				try {
					Log.d(TAG, "Sleeping for " + backoff + " ms before retry");
					Thread.sleep(backoff);
				} catch (InterruptedException e1) {
					// Activity finished before we complete - exit.
					Log.d(TAG, "Thread interrupted: abort remaining retries!");
					Thread.currentThread().interrupt();
					bSalida = false;
				}
				// increase backoff exponentially
				backoff *= 2;
			}
		}
		return bSalida;
	}

	private void setRegistrationId(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(namePref,
				Context.MODE_PRIVATE);

		// int appVersion = getAppVersion(context);

		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_USER, "");
		editor.putString(PROPERTY_REG_ID, "");
		editor.putInt(PROPERTY_APP_VERSION, 0);
		editor.putLong(PROPERTY_EXPIRATION_TIME, 0);

		editor.commit();
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

	public static Boolean callServicePost(Context ctx, String url, String ws,
			List<String> lista, Object json) {

		String salida = null;
		String gsonString = "";

		if (json != null) {
			gsonString = new GsonUtil().getBuilder().create().toJson(json);
		}

		StringBuffer answer = new StringBuffer();
		String urlFull = url + ws;

		if (lista != null) {
			for (int i = 0; i < lista.size(); i++) {
				urlFull += "/" + lista.get(i);
			}
		}

		if (urlFull != null) {

			HttpURLConnection http = null;

			try {

				URL urlCompleta = new URL(urlFull);

				HttpParams httpParameters = new BasicHttpParams();
				int timeoutConnection = SERVER_TIME;
				HttpConnectionParams.setConnectionTimeout(httpParameters,
						timeoutConnection);
				// Set the default socket timeout (SO_TIMEOUT)
				// in milliseconds which is the timeout for waiting for data.
				int timeoutSocket = SERVER_TIME;
				HttpConnectionParams
						.setSoTimeout(httpParameters, timeoutSocket);

				if (urlCompleta.getProtocol().toLowerCase().equals("https")) {
					trustAllHosts();
					HttpsURLConnection https = (HttpsURLConnection) urlCompleta
							.openConnection();
					https.setHostnameVerifier(DO_NOT_VERIFY);
					http = https;
				} else {
					http = (HttpURLConnection) urlCompleta.openConnection();
				}

				// conn.setSSLSocketFactory(newSslSocketFactory(ctx));

				http.setRequestMethod("POST");
				http.setRequestProperty("Content-Type", "application/json");

				http.setDoOutput(true);
				http.setDoInput(true);

				OutputStreamWriter writer = new OutputStreamWriter(
						http.getOutputStream());

				//

				// write parameters
				writer.write(gsonString);
				writer.flush();

				// Get the response
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(http.getInputStream()));

				String line;
				while ((line = reader.readLine()) != null) {
					answer.append(line);
				}
				writer.close();
				reader.close();

				salida = answer.toString();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				try {
					if (401 == http.getResponseCode()) {
						return false;
					}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					return false;
				}
			}
		}

		if (salida != null && !salida.equalsIgnoreCase("")) {

			try {
				GsonBuilder gsonBuilder = new GsonBuilder();
				Gson gson = gsonBuilder.create();

				String respuesta = gson.fromJson(salida, String.class);
				if ("OK".equals(respuesta)) {
					return true;
				} else {
					Log.d(TAG, "ERROR_PUSH " + respuesta);
					return false;
				}
			} catch (Exception e) {
				return false;
			}

		}
		return false;
	}

	final public static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
		@Override
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	};

	private static void trustAllHosts() {
		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {

			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return new java.security.cert.X509Certificate[] {};
			}

			@Override
			public void checkClientTrusted(
					java.security.cert.X509Certificate[] chain, String authType)
					throws java.security.cert.CertificateException {
				// TODO Auto-generated method stub

			}

			@Override
			public void checkServerTrusted(
					java.security.cert.X509Certificate[] chain, String authType)
					throws java.security.cert.CertificateException {
				// TODO Auto-generated method stub

			}
		} };

		// Install the all-trusting trust manager
		try {
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection
					.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
