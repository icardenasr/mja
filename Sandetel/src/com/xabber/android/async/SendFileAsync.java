package com.xabber.android.async;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import com.xabber.android.utils.MimeTypeUtil;
import com.xabber.android.utils.async.AsyncTask;
import com.xabber.android.utils.picture.PictureController;
import com.xabber.android.utils.preferences.UtilPreferences;

import es.juntadeandalucia.android.im.R;

public class SendFileAsync extends AsyncTask<Void, Void, Consigna> {

	public SendFileAsync(Context context, String from, String to, String path,
			IUploadFile iUploadFile) {
		this.context = context;
		this.from = from;
		this.to = to;
		this.path = path;
		this.iUploadFile = iUploadFile;
	}

	// public SendFileService() {
	// super("sendFile");
	// }
	//
	// public SendFileService(String name) {
	// super(name);
	// }

	// NOTIFICATIONS
	// private NotificationManager mNotifyManager;
	// private NotificationCompat.Builder mBuilder;
	// private Long ID;
	private File f;
	// END NOTIFICATIONS

	private Context context;

	private IUploadFile iUploadFile;

	private UsuarioDTO usuario;
	// private ChatViewerAdapter chatViewerAdapter;
	private String path, from, to;

	private FilePacketDTO respuesta;

	// private int progress = 0;

	// public SendFileService() {
	// super("sendFile");
	// }

	// Nuevo metodo introducido para soportar el nuevo certificado de Consigna
	public static HttpClient createHttpClient() {
		try {
	        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
	        trustStore.load(null, null);

	        ConsignaSSLSocketFactory sf = new ConsignaSSLSocketFactory(trustStore);
	        sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

	        HttpParams params = new BasicHttpParams();
	        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
	        HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

	        SchemeRegistry registry = new SchemeRegistry();
	        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
	        registry.register(new Scheme("https", sf, 443));

	        ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

	        return new DefaultHttpClient(ccm, params);
	    } catch (Exception e) {
	        return new DefaultHttpClient();
	    }
	}
	
	@SuppressWarnings("deprecation")
	public Consigna uploadFile(String pass, String path) throws Exception {

		// System.out.println("UPLOAD: Begining");

		Consigna result = null;
		
		//HttpClient httpclient = new DefaultHttpClient();
		HttpClient httpclient = createHttpClient();
		
		String salida = "";
		try {

			// UUID uuid = UUID.randomUUID();
			// ID = uuid.getLeastSignificantBits();

			f = new File(path);

			// createNotification(f.length(), f.getName());

			StringBuffer answer = new StringBuffer();
			HttpPost httppost = new HttpPost(Urls.WS_CONSIGNA);

			String fileName = f.getName();
			if (fileName != null && fileName.contains(Urls.LOCAL_CONSIGNA_PATH)) {
				fileName = fileName.split(Urls.LOCAL_CONSIGNA_PATH)[1];
			}

			Boolean isImage = MimeTypeUtil.isImageType(fileName);

			if (isImage) {
				Bitmap scaleBitmap = PictureController.decodeUri(context,
						Uri.fromFile(new File(path, "")));
				// Convert bitmap to byte array
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				scaleBitmap.compress(CompressFormat.PNG,
						0 /* ignored for PNG */, bos);
				byte[] bitmapdata = bos.toByteArray();

				// write the bytes in file
				FileOutputStream fos = new FileOutputStream(f);
				fos.write(bitmapdata);
			}

			FileBody bin = new FileBody(f, fileName,
					"application/octet-stream", "UTF-8");

			StringBody usuario = new StringBody(from.split("@")[0]);
			StringBody passwd = new StringBody(pass);
			StringBody expiracion = new StringBody("1sem");
			StringBody nothing = new StringBody("");

			CustomMultiPartEntity reqEntity = new CustomMultiPartEntity(
					new ProgressListener() {
						@Override
						public void transferred(long num) {
							// publishProgress((int) ((num / (float) totalSize)
							// * 100));
							// if (progress < num) {
							//
							// mBuilder.setProgress((int) f.length(),
							// (int) num, false);
							//
							// progress = (int) num;
							//
							// // Displays the progress bar for the first time.
							// mBuilder.setProgress((int) f.length(),
							// progress, false);
							//
							// }

							// System.out.println("UPLOAD: Middle " + num);

						}
					});

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

		// System.out.println("UPLOAD: Finish upload");

		GsonBuilder gsonBuilder = new GsonBuilder();
		Gson gson = gsonBuilder.create();
		if (salida != null && !salida.equalsIgnoreCase("")) {
			try {

				respuesta = gson.fromJson(salida, FilePacketDTO.class);

				if (respuesta != null && respuesta.getCode() != null
						&& respuesta.getCode().equals("200")) {

					// // When the loop is finished, updates the notification
					// mBuilder.setContentText(
					// context.getResources().getString(
					// R.string.noti_completed))
					// Removes the progress bar
					// .setProgress(0, 0, false);

					result = sendMessage(respuesta, path);

				}

				// mNotifyManager.notify(ID.intValue(), mBuilder.build());

			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		return result;

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

	private Consigna sendMessage(FilePacketDTO respuesta, String path) {
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
			return null;

		Consigna consigna = new Consigna(respuesta.getNombre(),
				respuesta.getTamano(), respuesta.getUrl(), path);

		if (SettingsManager.chatsHideKeyboard() == ChatsHideKeyboard.always
				|| (context.getResources().getBoolean(R.bool.landscape) && SettingsManager
						.chatsHideKeyboard() == ChatsHideKeyboard.landscape)) {
		}

		return consigna;

	}

	// private void createNotification(final Long lenghtFile, String filename) {
	//
	// mNotifyManager = (NotificationManager) context
	// .getSystemService(Context.NOTIFICATION_SERVICE);
	//
	// mBuilder = new NotificationCompat.Builder(context);
	//
	// Intent resultIntent = new Intent();
	// // Because clicking the notification opens a new ("special") activity,
	// // there's
	// // no need to create an artificial back stack.
	// PendingIntent resultPendingIntent = PendingIntent.getActivity(context,
	// 0, resultIntent, 0);
	//
	// mBuilder.setContentIntent(resultPendingIntent);
	//
	// mBuilder.setContentTitle(filename)
	// .setContentText(
	// context.getResources()
	// .getString(R.string.noti_progress))
	// .setSmallIcon(R.drawable.ic_launcher);
	// // Start a lengthy operation in a background thread
	// new Thread(new Runnable() {
	// @Override
	// public void run() {
	// mBuilder.setProgress(lenghtFile.intValue(), 0, false);
	//
	// }
	// }
	// // Starts the thread by calling the run() method in its Runnable
	// ).start();
	//
	// }

	@Override
	protected Consigna doInBackground(Void... params) {

		usuario = (UsuarioDTO) UtilPreferences.getPreferences(context,
				UsuarioWS.KEY_USUARIO, UsuarioDTO.class);

		try {
			return uploadFile(usuario.getPassword(), path);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	@Override
	protected void onPostExecute(Consigna result) {
		super.onPostExecute(result);

		if (respuesta != null && respuesta.getCode() != null
				&& !respuesta.getCode().equals("200")) {
			// Error subiendo archivo
			if (respuesta.getErr() != null
					&& !respuesta.getErr().equalsIgnoreCase("")) {
				// mBuilder.setContentText(respuesta.getErr())
				// // Removes the progress bar
				// .setProgress(0, 0, false);
				Toast.makeText(context, respuesta.getErr(), Toast.LENGTH_SHORT)
						.show();
			} else {
				// When the loop is finished, updates the notification
				// mBuilder.setContentText("Error")
				// // Removes the progress bar
				// .setProgress(0, 0, false);

				Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
			}
		}

		if (result != null && iUploadFile != null) {
			iUploadFile.onResultCorrectWS(result);
		} else {
			iUploadFile.onREsultFailedWS();
		}
	}
	

}
