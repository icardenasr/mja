package com.xabber.android.service.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.xabber.android.data.Application;
import com.xabber.android.data.SettingsManager;
import com.xabber.android.data.SettingsManager.ChatsHideKeyboard;
import com.xabber.android.data.message.Consigna;
import com.xabber.android.service.constantes.Urls;
import com.xabber.android.service.dto.FilePacketDTO;
import com.xabber.android.service.dto.UsuarioDTO;
import com.xabber.android.service.ws_client.interfaces.IUploadFile;
import com.xabber.android.service.ws_client.service_ws.UsuarioWS;
import com.xabber.android.ui.own.CustomMultiPartEntity;
import com.xabber.android.ui.own.CustomMultiPartEntity.ProgressListener;
import com.xabber.android.utils.preferences.UtilPreferences;

import es.juntadeandalucia.android.im.R;

public class SendFileService extends IntentService {

	public SendFileService() {
		super("sendFile");
	}

	public SendFileService(String name) {
		super(name);
	}

	// NOTIFICATIONS
	private NotificationManager mNotifyManager;
	private NotificationCompat.Builder mBuilder;
	private Long ID;
	private File f;
	// END NOTIFICATIONS

	private IUploadFile iUploadFile;

	private UsuarioDTO usuario;
	// private ChatViewerAdapter chatViewerAdapter;
	private String path, from, to;

	private int progress = 0;

	// public SendFileService() {
	// super("sendFile");
	// }

	@SuppressWarnings("deprecation")
	public void uploadFile(String pass, String path) throws Exception {
		HttpClient httpclient = new DefaultHttpClient();
		String salida = "";
		try {

			UUID uuid = UUID.randomUUID();
			ID = uuid.getLeastSignificantBits();

			f = new File(path);

			createNotification(f.length(), f.getName());

			StringBuffer answer = new StringBuffer();
			HttpPost httppost = new HttpPost(Urls.WS_CONSIGNA);

			FileBody bin = new FileBody(f);
			StringBody usuario = new StringBody(from.split("@")[0]);
			StringBody passwd = new StringBody(pass);
			StringBody expiracion = new StringBody("1sem");
			StringBody nothing = new StringBody("");

			// MultipartEntity reqEntity = new MultipartEntity();

			CustomMultiPartEntity reqEntity = new CustomMultiPartEntity(
					new ProgressListener() {
						@Override
						public void transferred(long num) {
							// publishProgress((int) ((num / (float) totalSize)
							// * 100));
							if (progress < num) {

								mBuilder.setProgress((int) f.length(),
										(int) num, false);

								progress = (int) num;

								// Displays the progress bar for the first time.
								mBuilder.setProgress((int) f.length(),
										progress, false);

							}
						}
					});

			// MultipartEntity reqEntity = new MultipartEntity() {
			//
			// @Override
			// public void writeTo(final OutputStream outstream)
			// throws IOException {
			// super.writeTo(new CoutingOutputStream(outstream,
			// mNotifyManager, mBuilder, f.length(), ID.intValue()));
			// }
			//
			// };

			reqEntity.addPart("usuario", usuario);
			reqEntity.addPart("passwd", passwd);
			reqEntity.addPart("fichero", bin);
			reqEntity.addPart("expiracion", expiracion);
			reqEntity.addPart("fichero_passwd", nothing);
			reqEntity.addPart("descripcion", nothing);

			// InputStream inputStream = reqEntity.getContent();
			//
			//
			// slurp(inputStream, 1024);

			httppost.setEntity(reqEntity);

			System.out
					.println("executing request " + httppost.getRequestLine());
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity resEntity = response.getEntity();

			System.out.println("----------------------------------------");
			System.out.println(response.getStatusLine());
			if (resEntity != null) {
				System.out.println("Response content length: "
						+ resEntity.getContentLength());
				InputStream is = resEntity.getContent();
				if (is != null) {
					// Writer writer = new StringWriter();

					// char[] buffer = new char[1024];
					BufferedReader reader = null;
					try {
						reader = new BufferedReader(new InputStreamReader(is,
								"UTF-8"));
						String line;
						while ((line = reader.readLine()) != null) {
							answer.append(line);
						}
					} finally {
						if (reader != null) {
							reader.close();
						}
						is.close();
					}

					System.out
							.println("Response content: " + answer.toString());
				} else {
					System.out.println("Response nothing: ");
				}

			}
			resEntity.consumeContent();
			// EntityUtils.consume(resEntity);

			salida = answer.toString();

		} catch (Exception e) {
			e.printStackTrace();
		}

		finally {
			try {
				httpclient.getConnectionManager().shutdown();
			} catch (Exception ignore) {
			}
		}

		GsonBuilder gsonBuilder = new GsonBuilder();
		Gson gson = gsonBuilder.create();
		if (salida != null && !salida.equalsIgnoreCase("")) {
			try {

				FilePacketDTO respuesta = gson.fromJson(salida,
						FilePacketDTO.class);

				if (respuesta != null && respuesta.getCode() != null
						&& respuesta.getCode().equals("200")) {

					// When the loop is finished, updates the notification
					mBuilder.setContentText(
							this.getResources().getString(
									R.string.noti_completed))
					// Removes the progress bar
							.setProgress(0, 0, false);

					sendMessage(respuesta, path);

				} else {
					// Error subiendo archivo
					if (respuesta != null && respuesta.getErr() != null
							&& !respuesta.getErr().equalsIgnoreCase("")) {
						mBuilder.setContentText(respuesta.getErr())
						// Removes the progress bar
								.setProgress(0, 0, false);
					} else {
						// When the loop is finished, updates the notification
						mBuilder.setContentText("Error")
						// Removes the progress bar
								.setProgress(0, 0, false);
					}
				}

				mNotifyManager.notify(ID.intValue(), mBuilder.build());

			} catch (Exception e) {
			}

		}

	}

	public static String slurp(final InputStream is, final int bufferSize) {
		final char[] buffer = new char[bufferSize];
		final StringBuilder out = new StringBuilder();
		try {
			final Reader in = new InputStreamReader(is, "UTF-8");
			try {
				for (;;) {
					int rsz = in.read(buffer, 0, buffer.length);
					if (rsz < 0)
						break;
					out.append(buffer, 0, rsz);
				}
			} finally {
				in.close();
			}
		} catch (UnsupportedEncodingException ex) {
			/* ... */
		} catch (IOException ex) {
			/* ... */
		}
		return out.toString();
	}

	private void sendMessage(FilePacketDTO respuesta, String path) {
		// if (actionWithView == null)
		// return;
		// EditText editView = (EditText) actionWithView
		// .findViewById(R.id.chat_input);
		String text = respuesta.getUrl();
		int start = 0;
		int end = text.length();
		while (start < end
				&& (text.charAt(start) == ' ' || text.charAt(start) == '\n'))
			start += 1;
		while (start < end
				&& (text.charAt(end - 1) == ' ' || text.charAt(end - 1) == '\n'))
			end -= 1;
		text = text.substring(start, end);
		if ("".equals(text))
			return;
		// chatViewerAdapter.setOnTextChangedListener(null);
		// editView.setText("");
		// chatViewerAdapter.setOnTextChangedListener(this);

		Consigna consigna = new Consigna(respuesta.getNombre(),
				respuesta.getTamano(), respuesta.getUrl(), path);

		// iUploadFile = ((Application) getApplication()).getiUploadFile();
		if (iUploadFile != null) {
			iUploadFile.onResultCorrectWS(consigna);
			// ((Application) getApplication()).setiUploadFile(null);
		}

		// sendMessage(respuesta.getUrl());
		// if (exitOnSend)
		// close();
		if (SettingsManager.chatsHideKeyboard() == ChatsHideKeyboard.always
				|| (getResources().getBoolean(R.bool.landscape) && SettingsManager
						.chatsHideKeyboard() == ChatsHideKeyboard.landscape)) {
			// InputMethodManager imm = (InputMethodManager)
			// getSystemService(Context.INPUT_METHOD_SERVICE);
			// imm.hideSoftInputFromWindow(editView.getWindowToken(), 0);
		}

	}

	// private void sendMessage(Consigna consigna) {
	//
	// MessageManager.getInstance().sendMessage(from, to,
	// consigna.getName() + " | " + consigna.getSize(), consigna);
	// // chatViewerAdapter.onChatChange(actionWithView, false);
	// }
	//
	// private void sendMessage(String msg) {
	//
	// MessageManager.getInstance().sendMessage(from, to, msg);
	// // chatViewerAdapter.onChatChange(actionWithView, false);
	// }

	private void createNotification(final Long lenghtFile, String filename) {

		mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		mBuilder = new NotificationCompat.Builder(this);

		// mBuilder.setDefaults(Notification.DEFAULT_LIGHTS
		// | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND);

		Intent resultIntent = new Intent();
		// Because clicking the notification opens a new ("special") activity,
		// there's
		// no need to create an artificial back stack.
		PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0,
				resultIntent, 0);

		mBuilder.setContentIntent(resultPendingIntent);

		// PendingIntent activity = PendingIntent.getActivity(application, 0,
		// new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);
		// mBuilder.setContentIntent(activity);
		mBuilder.setContentTitle(filename)
				.setContentText(
						this.getResources().getString(R.string.noti_progress))
				.setSmallIcon(R.drawable.ic_launcher);
		// Start a lengthy operation in a background thread
		new Thread(new Runnable() {
			@Override
			public void run() {
				// int incr;
				// Do the "lengthy" operation 20 times
				// for (incr = 0; incr <= 100; incr += 5) {
				// // Sets the progress indicator to a max value, the
				// // current completion percentage, and "determinate"
				// // state
				mBuilder.setProgress(lenghtFile.intValue(), 0, false);
				// // Displays the progress bar for the first time.
				// mNotifyManager.notify(0, mBuilder.build());
				// // Sleeps the thread, simulating an operation
				// // that takes time
				// try {
				// // Sleep for 5 seconds
				// Thread.sleep(5 * 1000);
				// } catch (InterruptedException e) {
				// // Log.d(TAG, "sleep failure");
				// }
				// }

			}
		}
		// Starts the thread by calling the run() method in its Runnable
		).start();

	}

	// private class UploadFileAsynck extends AsyncTask<Void, Void, Void> {
	//
	// @Override
	// protected Void doInBackground(Void... params) {
	//
	// try {
	// uploadFile(usuario.getPassword(), path);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	//
	// return null;
	// }
	//
	// }

	@Override
	protected void onHandleIntent(Intent intent) {

		if (intent != null) {
			Bundle extras = intent.getExtras();
			if (extras != null) {
				from = extras.getString("from");
				to = extras.getString("to");
				path = extras.getString("path");
				iUploadFile = (IUploadFile) extras
						.getSerializable("iActionBefore");
			}

			usuario = (UsuarioDTO) UtilPreferences.getPreferences(
					getApplicationContext(), UsuarioWS.KEY_USUARIO,
					UsuarioDTO.class);

			// chatViewerAdapter = ((Application) getApplication())
			// .getChatViewerAdapter();

			try {
				uploadFile(usuario.getPassword(), path);
			} catch (Exception e) {
				e.printStackTrace();
			}

			// new UploadFileAsynck().execute();
		}

	}

	@Override
	public void onDestroy() {

		// if (mNotifyManager != null) {
		// mNotifyManager.cancel(ID.intValue());
		// }

		super.onDestroy();
	}

}
