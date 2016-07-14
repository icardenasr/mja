package com.xabber.android.service.ws_client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
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
import com.xabber.android.service.ws_client.dto.LocalCacheWS;
import com.xabber.android.service.ws_client.dto.Respuesta;
import com.xabber.android.service.ws_client.dto.RespuestaObject;
import com.xabber.android.service.ws_client.dto.WebServiceCacheController;
import com.xabber.android.service.ws_client.util.UtilWebServices;
import com.xabber.android.utils.Fechas;
import com.xabber.android.utils.gson.GsonUtil;

/**
 * @version 1.0 24 Oct 2013
 * @author José Fernández
 */
public class CallServiceGet {

	@SuppressWarnings("rawtypes")
	public static Respuesta callService(Context ctx, String userName,
			String password, String url, List<String> lista, Boolean useCache,
			final Class clase, int mode, Boolean localCacheWS) {

		Respuesta respuesta = null;

		switch (mode) {
		case ConstantesWS.MODE_NORMAL:
			respuesta = CallServiceGet.callServiceNormal(ctx, url, lista,
					useCache, clase, localCacheWS);
			break;
		case ConstantesWS.MODE_SPRING:
			respuesta = CallServiceGet.callServiceSpringSecurity(ctx, userName,
					password, url, lista, useCache, clase);
			break;
		case ConstantesWS.MODE_OAUTH:
			// respuesta = CallServiceGet.callServiceOauth(ctx, url, ws, lista,
			// useCache, clase);
			break;
		case ConstantesWS.MODE_NO_LOGIN:
			CallServiceGet.callServiceNoLoginRequired(ctx, userName, password,
					url, lista, useCache, clase);
		default:
			break;
		}

		return respuesta;

	}

	/**
	 * LLamada al servicio mediante SpringSecurity
	 * 
	 * @param ctx
	 * @param url
	 * @param ws
	 * @param lista
	 * @param useCache
	 * @param clase
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	private static Respuesta callServiceNormal(Context ctx, String url,
			List<String> lista, Boolean useCache, final Class clase,
			Boolean localCacheWS) {

		String salida = null;

		GsonBuilder gsonBuilder = new GsonBuilder();

		gsonBuilder.registerTypeAdapter(BaseBO.class,
				new JsonDeserializer<BaseBO>() {

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

		StringBuffer answer = new StringBuffer();
		String urlFull = url;

		if (lista != null) {
			for (int i = 0; i < lista.size(); i++) {
				urlFull += "/" + lista.get(i);
			}
		}

		// Caché de WS local
		// Si está la variable bool a true, comprobaremos si está ya en cache

		List<LocalCacheWS> listLocalCWS = null;
		LocalCacheWS localCacheWSChoose = null;
		// // List<LocalCacheWS> listLocalCWSAux = null;
		// if (localCacheWS) {
		// try {
		// // listLocalCWS = UtilSQLITE.select(((Activity) ctx),
		// // ((LocalCacheWS.class).newInstance()).selectGenerica()
		// // + " WHERE CacheWS.url = '" + urlFull + "'",
		// // LocalCacheWS.class);
		//
		// listLocalCWS = UtilSQLITE.select(((Activity) ctx),
		// ((LocalCacheWS.class).newInstance()).selectGenerica(),
		// LocalCacheWS.class);
		// } catch (NoSuchFieldException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (IllegalAccessException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (InstantiationException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		//
		// for (LocalCacheWS localCacheWS2 : listLocalCWS) {
		// if (localCacheWS2.getUrl().equals(urlFull)) {
		// localCacheWSChoose = localCacheWS2;
		// break;
		// }
		// }
		//
		// }

		// Si esta a true el bool, tenemos en DDBB la llamada guardada y la
		// fecha no ha pasado no hará la llamada de nuevo
		if (!localCacheWS
				|| localCacheWSChoose == null
				|| Fechas.passTime(localCacheWSChoose.getFecha(),
						ConstantesWS.WS_LOCAL)) {

			if (!useCache
					|| (salida = WebServiceCacheController.getInstance()
							.getObject(urlFull)) == null) {

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

					http.setRequestMethod("GET");
					http.setRequestProperty("Content-Type", "application/json");

					// Set time out
					http.setConnectTimeout(ConstantesWS.SERVER_TIME);
					http.setReadTimeout(ConstantesWS.SERVER_TIME);

					// Get the response
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(http.getInputStream()));

					String line;
					while ((line = reader.readLine()) != null) {
						answer.append(line);
					}

					reader.close();

					salida = answer.toString();

				} catch (IOException e) {
					// TODO Auto-generated catch block
					if (e != null) {
						e.printStackTrace();
					}
					try {
						if (401 == http.getResponseCode()) {
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
			}
		} else {
			salida = listLocalCWS.get(0).getContenido();
		}

		return tratamientoResp(salida, clase, respuesta, urlFull, gson,
				useCache);
		// if (salida != null && !salida.equalsIgnoreCase("")) {
		//
		// // // Si esta a true el bool, tenemos en DDBB la llamada guardada y
		// // la
		// // // fecha no ha pasado no hará la llamada de nuevo
		// // if (localCacheWS
		// // && (localCacheWSChoose == null || Fechas.passTime(
		// // localCacheWSChoose.getFecha(),
		// // es.sdos.diamobile.util.Constantes.WS_LOCAL))) {
		// //
		// // // Hacemos el insert del WS
		// // try {
		// //
		// // LocalCacheWS lCacheWS = new LocalCacheWS();
		// // lCacheWS.setUrl(urlFull);
		// // lCacheWS.setContenido(salida);
		// // lCacheWS.setFecha(String.valueOf(Calendar.getInstance()
		// // .getTimeInMillis()));
		// //
		// // if (localCacheWSChoose != null) {
		// // UtilSQLITE.deleteObj(((Activity)
		// // ctx),localCacheWSChoose.deleteObjString());
		// // }
		// //
		// // UtilSQLITE.insertObj(((Activity) ctx),
		// // lCacheWS.updateObj());
		// // } catch (IllegalAccessException e) {
		// // // TODO Auto-generated catch block
		// // e.printStackTrace();
		// // } catch (InstantiationException e) {
		// // // TODO Auto-generated catch block
		// // e.printStackTrace();
		// // } catch (NoSuchFieldException e) {
		// // // TODO Auto-generated catch block
		// // e.printStackTrace();
		// // }
		// //
		// // }
		//
		// try {
		// respuesta = gson.fromJson(salida, Respuesta.class);
		// if ("0".equals(respuesta.getCodError())) {
		// // Guardar en Cache
		// if (useCache) {
		// WebServiceCacheController.getInstance().putObject(
		// urlFull, salida);
		// }
		//
		// respuesta.getError().setCodError("0");
		// } else if (!"401".equals(respuesta.getCodError())) {
		// respuesta.getError().setCodError("1");
		// }
		// } catch (Exception e) {
		// respuesta = Respuesta.getErrorDefaultResp();
		// }
		//
		// }
		//
		// return respuesta;
	}

	/**
	 * LLamada al servicio mediante SpringSecurity
	 * 
	 * @param ctx
	 * @param userName
	 * @param password
	 * @param url
	 * @param ws
	 * @param lista
	 * @param useCache
	 * @param clase
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private static Respuesta callServiceSpringSecurity(Context ctx,
			String userName, String password, String url, List<String> lista,
			Boolean useCache, final Class clase) {

		String salida = null;

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

				addSpringSecurity((HttpsURLConnection) http, userName, password);

				http.setRequestMethod("GET");
				http.setRequestProperty("Content-Type", "application/json");

				// Set time out
				http.setConnectTimeout(ConstantesWS.SERVER_TIME);
				http.setReadTimeout(ConstantesWS.SERVER_TIME);

				// Get the response
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(http.getInputStream()));

				String line;
				while ((line = reader.readLine()) != null) {
					answer.append(line);
				}

				reader.close();

				salida = answer.toString();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				if (e != null) {
					e.printStackTrace();
				}
				try {
					if (401 == http.getResponseCode()) {
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
		}

		return tratamientoResp(salida, clase, respuesta, urlFull, gson,
				useCache);

		// if (salida != null && !salida.equalsIgnoreCase("")) {
		// try {
		// respuesta = gson.fromJson(salida, Respuesta.class);
		// if ("0".equals(respuesta.getCodError())) {
		// // Guardar en Cache
		// if (useCache) {
		// WebServiceCacheController.getInstance().putObject(
		// urlFull, salida);
		// }
		//
		// respuesta.getError().setCodError("0");
		// } else if (!"401".equals(respuesta.getCodError())) {
		// respuesta.getError().setCodError("1");
		// }
		// } catch (Exception e) {
		// respuesta = Respuesta.getErrorDefaultResp();
		// }
		//
		// }
		//
		// return respuesta;
	}

	/**
	 * LLamada al servicio mediante OAuth. Requiere a la aplicación móvil estar
	 * logada contra el servidor.
	 * 
	 * @param ctx
	 * @param userName
	 * @param password
	 * @param url
	 * @param ws
	 * @param lista
	 * @param useCache
	 * @param clase
	 * @return
	 */
	/*
	 * private static Respuesta callServiceOauth(Context ctx, String url, String
	 * ws, List<String> lista, Boolean useCache, final Class clase) {
	 * 
	 * String salida = null;
	 * 
	 * GsonBuilder gsonBuilder = new GsonBuilder();
	 * 
	 * gsonBuilder.registerTypeAdapter(BaseBO.class, new
	 * JsonDeserializer<BaseBO>() {
	 * 
	 * @Override public BaseBO deserialize(JsonElement json, Type arg1,
	 * JsonDeserializationContext arg2) throws JsonParseException {
	 * 
	 * GsonBuilder gsonBuilder2 = new GsonBuilder();
	 * 
	 * gsonBuilder2.registerTypeAdapter(Date.class, new JsonDeserializer<Date>()
	 * {
	 * 
	 * @Override public Date deserialize(JsonElement json, Type arg1,
	 * JsonDeserializationContext arg2) throws JsonParseException { // TODO
	 * Auto-generated method stub if (json.getAsLong() >= 0) {
	 * 
	 * return Fechas .unFormatFecha_long(json .getAsLong()); } else { return
	 * null; } }
	 * 
	 * });
	 * 
	 * gsonBuilder2.registerTypeAdapter(Date.class, new JsonSerializer<Date>() {
	 * 
	 * @Override public JsonElement serialize(Date arg0, Type arg1,
	 * JsonSerializationContext arg2) {
	 * 
	 * return new JsonPrimitive(arg0.getTime()); }
	 * 
	 * });
	 * 
	 * Gson gson = gsonBuilder2.create();
	 * 
	 * return gson.fromJson(json, clase);
	 * 
	 * } // });
	 * 
	 * Gson gson = gsonBuilder.create(); Respuesta respuesta = null;
	 * 
	 * StringBuffer answer = new StringBuffer(); String urlFull = url + ws;
	 * 
	 * if (lista != null) { for (int i = 0; i < lista.size(); i++) { urlFull +=
	 * "/" + lista.get(i); } }
	 * 
	 * // Comprobamos si tenemos que realizar la autenticación if (!useCache ||
	 * (salida = WebServiceCacheController.getInstance().getObject( urlFull)) ==
	 * null) {
	 * 
	 * org.apache.http.HttpResponse response = null;
	 * 
	 * try {
	 * 
	 * HttpGet request = new HttpGet(urlFull);
	 * 
	 * OAuthConsumer consumer = Session.getInstance()
	 * .getOAuthConsumer((Activity) ctx);
	 * 
	 * if (consumer != null) { // sign the request consumer.sign(request); }
	 * 
	 * // send the request HttpClient httpClient = new MyHttpClient(ctx);
	 * response = httpClient.execute(request);
	 * 
	 * StringBuilder total = new StringBuilder();
	 * 
	 * int codeStatus = response.getStatusLine().getStatusCode(); if (codeStatus
	 * == 200) {
	 * 
	 * InputStream is = response.getEntity().getContent();
	 * 
	 * String line = "";
	 * 
	 * // Wrap a BufferedReader around the InputStream BufferedReader rd = new
	 * BufferedReader( new InputStreamReader(is));
	 * 
	 * // Read response until the end while ((line = rd.readLine()) != null) {
	 * total.append(line); } salida = total.toString();
	 * 
	 * } else if (401 == codeStatus) { respuesta =
	 * Respuesta.getErrorLoginResp(); }
	 * 
	 * } catch (IOException e) { // TODO Auto-generated catch block if (e !=
	 * null) { e.printStackTrace(); } } catch (OAuthMessageSignerException e) {
	 * // TODO Auto-generated catch block e.printStackTrace(); } catch
	 * (OAuthExpectationFailedException e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); } catch (OAuthCommunicationException e) { // TODO
	 * Auto-generated catch block e.printStackTrace(); } }
	 * 
	 * return tratamientoResp(salida, clase, respuesta, urlFull, gson,
	 * useCache);
	 * 
	 * // if (clase != BaseBO.class) { // // RespuestaObject respO = null; // if
	 * (salida != null && !salida.equalsIgnoreCase("")) { // try { // respO =
	 * gson.fromJson(salida, RespuestaObject.class); // if
	 * ("0".equals(respO.getCodError())) { // // Guardar en Cache // if
	 * (useCache) { // WebServiceCacheController.getInstance().putObject( //
	 * urlFull, salida); // } // // respO.getError().setCodError("0"); // } // }
	 * catch (Exception e) { // respuesta = Respuesta.getErrorDefaultResp(); //
	 * } // // } else if (respO != null && "401".equals(respO.getCodError())) {
	 * // respO = RespuestaObject.getErrorLoginResp(); // } // // else if
	 * (respuesta!=null && // // !"401".equals(respuesta.getCodError())) { //
	 * else { // respO = RespuestaObject.getErrorDefaultResp(); // } // // if
	 * (respO != null) { // respO.getRespuesta(); // // respuesta = new
	 * Respuesta(null, respO.getError(), // respO.getRespuesta()); // // } // //
	 * } else { // // if (salida != null && !salida.equalsIgnoreCase("")) { //
	 * try { // respuesta = gson.fromJson(salida, Respuesta.class); // if
	 * ("0".equals(respuesta.getCodError())) { // // Guardar en Cache // if
	 * (useCache) { // WebServiceCacheController.getInstance().putObject( //
	 * urlFull, salida); // } // // respuesta.getError().setCodError("0"); // }
	 * // } catch (Exception e) { // respuesta =
	 * Respuesta.getErrorDefaultResp(); // } // // } else if (respuesta != null
	 * // && "401".equals(respuesta.getCodError())) { // respuesta =
	 * Respuesta.getErrorLoginResp(); // } // // else if (respuesta!=null && //
	 * // !"401".equals(respuesta.getCodError())) { // else { // respuesta =
	 * Respuesta.getErrorDefaultResp(); // } // } // // return respuesta; }
	 */
	/**
	 * LLamada al servicio mediante SpringSecurity
	 * 
	 * @param ctx
	 * @param userName
	 * @param password
	 * @param url
	 * @param ws
	 * @param lista
	 * @param useCache
	 * @param clase
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private static Respuesta callServiceNoLoginRequired(Context ctx,
			String consKeyHeader, String key, String url, List<String> lista,
			Boolean useCache, final Class clase) {

		String salida = null;

		GsonUtil<Respuesta> gson = new GsonUtil<Respuesta>();
		Respuesta respuesta = null;

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

				http.setRequestMethod("GET");
				http.setRequestProperty("Content-Type", "application/json");

				// Set time out
				http.setConnectTimeout(ConstantesWS.SERVER_TIME);
				http.setReadTimeout(ConstantesWS.SERVER_TIME);

				// Get the response
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(http.getInputStream()));

				String line;
				while ((line = reader.readLine()) != null) {
					answer.append(line);
				}

				reader.close();

				salida = answer.toString();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				if (e != null) {
					e.printStackTrace();
				}
				try {
					if (401 == http.getResponseCode()) {
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
		}

		// return tratamientoResp(salida, clase, respuesta, urlFull, gson,
		// useCache);

		if (salida != null && !salida.equalsIgnoreCase("")) {
			try {
				respuesta = gson.getFromJsonString(salida, Respuesta.class);
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

	private static void addSpringSecurity(HttpURLConnection http, String user,
			String pass) {

		String cabeceraHttp = user + ":" + pass;
		String cabecera = "Basic "
				+ Base64.encodeToString(cabeceraHttp.getBytes(),
						Base64.URL_SAFE | Base64.NO_WRAP);
		http.setRequestProperty("Authorization", cabecera);

	}

	@SuppressWarnings({ "rawtypes", "unused", "unchecked" })
	private static Respuesta tratamientoResp(String salida, Class clase,
			Respuesta respuesta, String urlFull, Gson gson, Boolean useCache) {
		if (compareClases(clase)) {

			RespuestaObject respO = null;
			if (salida != null && !salida.equalsIgnoreCase("")) {
				try {
					respO = gson.fromJson(salida, RespuestaObject.class);
					if ("0".equals(respO.getCodError())) {
						// Guardar en Cache
						if (useCache) {
							WebServiceCacheController.getInstance().putObject(
									urlFull, salida);
						}
						respO.getError().setCodError("0");
					}
				} catch (Exception e) {
					respuesta = Respuesta.getErrorDefaultResp();
				}

			} else if (respO != null && "401".equals(respO.getCodError())) {
				respO = RespuestaObject.getErrorLoginResp();
			}
			// else if (respuesta!=null &&
			// !"401".equals(respuesta.getCodError())) {
			else {
				respO = RespuestaObject.getErrorDefaultResp();
			}

			if (respO != null) {
				respO.getRespuesta();

				respuesta = new Respuesta(null, respO.getError(),
						respO.getRespuesta());

			}

		} else {

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
					}
				} catch (Exception e) {
					respuesta = Respuesta.getErrorDefaultResp();
				}

			} else if (respuesta != null
					&& "401".equals(respuesta.getCodError())) {
				respuesta = Respuesta.getErrorLoginResp();
			}
			// else if (respuesta!=null &&
			// !"401".equals(respuesta.getCodError())) {
			else {
				respuesta = Respuesta.getErrorDefaultResp();
			}
		}

		return respuesta;
	}

	@SuppressWarnings("rawtypes")
	public static Boolean compareClases(Class clase) {
		List<Class> listClass = new ArrayList<Class>();
		listClass.add(String.class);
		listClass.add(Integer.class);
		listClass.add(Double.class);
		listClass.add(Boolean.class);
		listClass.add(Float.class);
		listClass.add(Date.class);
		listClass.add(Long.class);

		for (Class class1 : listClass) {
			if (class1 == clase) {
				return true;
			}
		}
		return false;

	}
}
