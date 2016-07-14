package com.xabber.android.service.ws_client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.content.Context;
import android.util.Base64;

import com.xabber.android.service.ws_client.constantes.ConstantesWS;
import com.xabber.android.service.ws_client.dto.BaseBO;
import com.xabber.android.service.ws_client.dto.Data;
import com.xabber.android.service.ws_client.dto.MyHttpClient;
import com.xabber.android.service.ws_client.dto.Respuesta;

public class CallServiceDataGet {

	@SuppressWarnings("rawtypes")
	public static Respuesta callService(Context ctx, String name,
			String userName, String password, String url, List<String> lista,
			Boolean useCache, final Class clase, int mode, Boolean localCacheWS) {

		Respuesta respuesta = null;

		switch (mode) {
		case ConstantesWS.MODE_NORMAL:
			respuesta = callServiceNormal(ctx, url, name, lista);
			break;
		case ConstantesWS.MODE_SPRING:
			respuesta = callServiceSpringSecurity(ctx, userName, password, url,
					name, lista);
			break;
		case ConstantesWS.MODE_OAUTH:
			// respuesta = callServiceOauth(ctx, url, ws, lista,
			// useCache, clase);
			break;
		case ConstantesWS.MODE_NO_LOGIN:
			// callServiceNoLoginRequired(ctx, userName,
			// password,
			// url, ws, lista, useCache, clase);
		default:
			break;
		}

		return respuesta;

	}

	@SuppressWarnings({ "rawtypes", "unused", "unchecked" })
	public static Respuesta callServiceNormal(Context ctx, String url,
			String name, List<String> lista) {

		String urlFull = url;

		if (lista != null) {
			for (int i = 0; i < lista.size(); i++) {
				urlFull += "/" + lista.get(i);
			}
		}

		Respuesta respuesta = null;

		String mimeType = null;

		InputStream is = null;

		HttpParams httpParameters = new BasicHttpParams();
		int timeoutConnection = ConstantesWS.SERVER_TIME;
		HttpConnectionParams.setConnectionTimeout(httpParameters,
				timeoutConnection);
		// Set the default socket timeout (SO_TIMEOUT)
		// in milliseconds which is the timeout for waiting for data.
		int timeoutSocket = ConstantesWS.SERVER_TIME;
		HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

		// Create a new HttpClient and Post Header
		HttpClient httpclient = new MyHttpClient(ctx);
		HttpGet httpGet = new HttpGet(urlFull);

		// if (urlFull.contains("secure")) {
		// addSpringSecurity((HttpsURLConnection) httpclient);
		// }

		try {

			// httpGet.setHeader("Content-Type:", "application/octet-stream");
			// httpGet.setHeader("Content-Disposition:",
			// "attachment; filename=\"$zipBaseName.ZIP\"");

			// Execute HTTP Post Request
			HttpResponse response = httpclient.execute(httpGet);
			if (response.getHeaders("Content-Type").length > 0) {
				mimeType = response.getHeaders("Content-Type")[0].getValue();
				if ("text/html".equals(mimeType)) {
					mimeType = null;
				} else if ("image/jpg".equals(mimeType)) {
					mimeType = "jpg";
				}
			}
			HttpEntity entity = response.getEntity();
			is = entity.getContent();

			ByteArrayOutputStream output = new ByteArrayOutputStream();
			if (is != null) {

				byte data[] = new byte[1024];

				long total = 0;
				int count;
				try {
					while ((count = is.read(data)) != -1) {
						total += count;
						output.write(data, 0, count);
					}

					output.flush();
					output.close();
					is.close();

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			respuesta = Respuesta.getErrorDefaultResp();
			if (output != null && output.size() > 0) {

				Data data = new Data();
				data.setContenido(output.toByteArray());
				if (mimeType != null) {
					data.setContentType(mimeType);
				}
				if (name != null) {
					data.setNombre(name);
				}
				List<BaseBO> resp = new ArrayList<BaseBO>();
				resp.add(data);

				respuesta.setRespuesta(resp);
				respuesta.getError().setCodError("0");
			}

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			if (e != null) {
				e.printStackTrace();
			}
			try {
				if (401 == ((HttpsURLConnection) httpclient).getResponseCode()) {
					respuesta = Respuesta.getErrorLoginResp();
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				respuesta = Respuesta.getErrorDefaultResp();
				if ("true".equals(respuesta.getCodError())) {
					respuesta.getError().setCodError("0");
				} else if (!"401".equals(respuesta.getCodError())) {
					respuesta.getError().setCodError("1");
				}
			}
		}

		return respuesta;

	}

	@SuppressWarnings({ "rawtypes", "unused", "unchecked" })
	public static Respuesta callServiceSpringSecurity(Context ctx,
			String userName, String password, String url, String name,
			List<String> lista) {

		String urlFull = url;

		if (lista != null) {
			for (int i = 0; i < lista.size(); i++) {
				urlFull += "/" + lista.get(i);
			}
		}

		Respuesta respuesta = null;

		String mimeType = null;

		InputStream is = null;

		HttpParams httpParameters = new BasicHttpParams();
		int timeoutConnection = ConstantesWS.SERVER_TIME;
		HttpConnectionParams.setConnectionTimeout(httpParameters,
				timeoutConnection);
		// Set the default socket timeout (SO_TIMEOUT)
		// in milliseconds which is the timeout for waiting for data.
		int timeoutSocket = ConstantesWS.SERVER_TIME;
		HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

		// Create a new HttpClient and Post Header
		HttpClient httpclient = new MyHttpClient(ctx);
		HttpGet httpGet = new HttpGet(urlFull);

		addSpringSecurity((HttpsURLConnection) httpclient, userName, password);

		// if (urlFull.contains("secure")) {
		// addSpringSecurity((HttpsURLConnection) httpclient);
		// }

		try {

			// httpGet.setHeader("Content-Type:", "application/octet-stream");
			// httpGet.setHeader("Content-Disposition:",
			// "attachment; filename=\"$zipBaseName.ZIP\"");

			// Execute HTTP Post Request
			HttpResponse response = httpclient.execute(httpGet);
			if (response.getHeaders("Content-Type").length > 0) {
				mimeType = response.getHeaders("Content-Type")[0].getValue();
				if ("text/html".equals(mimeType)) {
					mimeType = null;
				} else if ("image/jpg".equals(mimeType)) {
					mimeType = "jpg";
				}
			}
			HttpEntity entity = response.getEntity();
			is = entity.getContent();

			ByteArrayOutputStream output = new ByteArrayOutputStream();
			if (is != null) {

				byte data[] = new byte[1024];

				long total = 0;
				int count;
				try {
					while ((count = is.read(data)) != -1) {
						total += count;
						output.write(data, 0, count);
					}

					output.flush();
					output.close();
					is.close();

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			respuesta = Respuesta.getErrorDefaultResp();
			if (output != null && output.size() > 0) {

				Data data = new Data();
				data.setContenido(output.toByteArray());
				if (mimeType != null) {
					data.setContentType(mimeType);
				}
				if (name != null) {
					data.setNombre(name);
				}
				List<BaseBO> resp = new ArrayList<BaseBO>();
				resp.add(data);

				respuesta.setRespuesta(resp);
				respuesta.getError().setCodError("0");
			}

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			if (e != null) {
				e.printStackTrace();
			}
			try {
				if (401 == ((HttpsURLConnection) httpclient).getResponseCode()) {
					respuesta = Respuesta.getErrorLoginResp();
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				respuesta = Respuesta.getErrorDefaultResp();
				if ("true".equals(respuesta.getCodError())) {
					respuesta.getError().setCodError("0");
				} else if (!"401".equals(respuesta.getCodError())) {
					respuesta.getError().setCodError("1");
				}
			}
		}

		return respuesta;

	}

	private static void addSpringSecurity(HttpURLConnection http, String user,
			String pass) {

		String cabeceraHttp = user + ":" + pass;
		String cabecera = "Basic "
				+ Base64.encodeToString(cabeceraHttp.getBytes(),
						Base64.URL_SAFE | Base64.NO_WRAP);
		http.setRequestProperty("Authorization", cabecera);

	}

}
