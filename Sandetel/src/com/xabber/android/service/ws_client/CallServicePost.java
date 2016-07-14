package com.xabber.android.service.ws_client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import android.content.Context;
import android.util.Base64;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.xabber.android.service.ws_client.constantes.ConstantesWS;
import com.xabber.android.service.ws_client.dto.BaseBO;
import com.xabber.android.service.ws_client.dto.Respuesta;
import com.xabber.android.service.ws_client.dto.WebServiceCacheController;
import com.xabber.android.service.ws_client.util.UtilWebServices;
import com.xabber.android.utils.Fechas;
import com.xabber.android.utils.gson.GsonUtil;
import com.xabber.xmpp.archive.Session;

/**
 * @version 1.0 24 Oct 2013
 * @author José Fernández
 */
public class CallServicePost {

	@SuppressWarnings("rawtypes")
	public static Respuesta callService(Context ctx, CallWSAsyn callWSAsyn,
			String userName, String password, String url, List<String> lista,
			Object json, Boolean useCache, int mode, Class clase,
			Session session) {

		Respuesta respuesta = null;

		if (callWSAsyn != null && !callWSAsyn.isCancelled()) {

			switch (mode) {
			case ConstantesWS.MODE_NORMAL:
				respuesta = CallServicePost.callServiceNormal(ctx, url, lista,
						json, useCache, clase);
				break;

			default:
				break;
			}

		}

		return respuesta;

	}

	@SuppressWarnings("rawtypes")
	private static Respuesta callServiceNormal(Context ctx, String url,
			List<String> lista, Object json, Boolean useCache, final Class clase) {

		String salida = null;
		String gsonString = "";

		GsonBuilder gsonBuilder = new GsonBuilder();

		gsonBuilder.registerTypeAdapter(BaseBO.class,
				new JsonDeserializer<BaseBO>() {

					@SuppressWarnings("unchecked")
					@Override
					public BaseBO deserialize(JsonElement json, Type arg1,
							JsonDeserializationContext arg2)
							throws JsonParseException {

						GsonBuilder gsonBuilder2 = new GsonBuilder();

						gsonBuilder2.registerTypeAdapter(Date.class,
								new JsonDeserializer<Date>() {

									@Override
									public Date deserialize(JsonElement json,
											Type arg1,
											JsonDeserializationContext arg2)
											throws JsonParseException {
										// TODO Auto-generated method stub
										if (json.getAsLong() >= 0) {

											return Fechas
													.unFormatFecha_long(json
															.getAsLong());
										} else {
											return null;
										}
									}

								});

						gsonBuilder2.registerTypeAdapter(Date.class,
								new JsonSerializer<Date>() {

									@Override
									public JsonElement serialize(Date arg0,
											Type arg1,
											JsonSerializationContext arg2) {

										return new JsonPrimitive(arg0.getTime());
									}

								});

						Gson gson = gsonBuilder2.create();

						return gson.fromJson(json, clase);

					}
					//
				});

		Gson gson = gsonBuilder.create();
		Respuesta respuesta = null;

		if (json != null) {
			gsonString = new GsonUtil().getBuilder().create().toJson(json);
		}

		StringBuffer answer = new StringBuffer();
		String urlFull = url;

		if (lista != null) {
			for (int i = 0; i < lista.size(); i++) {
				urlFull += "/" + lista.get(i);
			}
		}

		if (!useCache
				|| (salida = WebServiceCacheController.getInstance().getObject(
						urlFull)) == null) {

			HttpURLConnection http = null;

			try {

				URL urlCompleta = new URL(urlFull);

				if (urlCompleta.getProtocol().toLowerCase().equals("https")) {
					UtilWebServices.trustAllHosts();
					HttpsURLConnection https = (HttpsURLConnection) urlCompleta
							.openConnection();
					https.setHostnameVerifier(UtilWebServices.DO_NOT_VERIFY);
					http = https;
				} else {
					http = (HttpURLConnection) urlCompleta.openConnection();
				}

				http.setRequestMethod("POST");
				http.setRequestProperty("Content-Type", "application/json");

				http.setDoOutput(true);
				http.setDoInput(true);

				OutputStreamWriter writer = new OutputStreamWriter(
						http.getOutputStream());

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
						respuesta = Respuesta.getErrorLoginResp();
					}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					respuesta = Respuesta.getErrorDefaultResp();
				}
			}
		}

		if (salida != null && !salida.equalsIgnoreCase("")) {
			try {

				respuesta = gson.fromJson(salida, Respuesta.class);
				if ("0".equals(respuesta.getCodError())) {
					// Guardar en Cache
					if (useCache) {
						WebServiceCacheController.getInstance().putObject(
								urlFull, salida);
					}

					respuesta.getError().setCodError("0");
				} else if (!"401".equals(respuesta.getCodError())) {
					respuesta.getError().setCodError("1");
				}
			} catch (Exception e) {
				respuesta = Respuesta.getErrorDefaultResp();
			}

		}

		return respuesta;
	}

	private static void addSpringSecurity(HttpURLConnection http,
			String userName, String pass) {

		String cabeceraHttp = userName + ":" + pass;

		String cabecera = "Basic "
				+ Base64.encodeToString(cabeceraHttp.getBytes(),
						Base64.URL_SAFE | Base64.NO_WRAP);

		http.setRequestProperty("Authorization", cabecera);

	}

}
