package com.xabber.android.service.ws_client.oauth;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import android.os.StrictMode;

import com.xabber.android.utils.session.UsuarioApp;

/**
 * @version 1.0 24 Oct 2013
 * @author José Fernández
 */
public class OAuthHelper {

	private OAuthConsumer mConsumer;
	private OAuthProvider mProvider;

	private String mCallbackUrl;

	private String sOAuthToken;

	private String sOAuthSecret;

	private String sConsumerKey;

	private String sConsumerSecret;

	public OAuthHelper(String consumerKey, String consumerSecret, String scope,
			String callbackUrl, String baseURL)
			throws UnsupportedEncodingException {
		mConsumer = new CommonsHttpOAuthConsumer(consumerKey, consumerSecret);
		mProvider = new CommonsHttpOAuthProvider(
				baseURL
						+ ConstantesOAUTH.REQUESTTOKEN
						+ "?scope="
						+ URLEncoder.encode(ConstantesOAUTH.REQUESTTOKEN_SCOPE,
								"utf-8"),
				baseURL + ConstantesOAUTH.ACCESSTOKEN, baseURL
						+ ConstantesOAUTH.USERAUTHORIZATION);
		mProvider.setOAuth10a(true);
		mCallbackUrl = (callbackUrl == null ? OAuth.OUT_OF_BAND : callbackUrl);

		this.sConsumerKey = consumerKey;
		this.sConsumerSecret = consumerSecret;

	}

	public String getRequestToken() throws OAuthMessageSignerException,
			OAuthNotAuthorizedException, OAuthExpectationFailedException,
			OAuthCommunicationException {

		// /Cambios para que funcione oauth en Android 2.2
		if (android.os.Build.VERSION.SDK_INT >= 9) {
			// Cambios para que funcione oauth en Android 4.0
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}

		String authUrl = mProvider
				.retrieveRequestToken(mConsumer, mCallbackUrl);
		return authUrl;
	}

	public String[] getAccessToken(String verifier)
			throws OAuthMessageSignerException, OAuthNotAuthorizedException,
			OAuthExpectationFailedException, OAuthCommunicationException {
		mProvider.retrieveAccessToken(mConsumer, verifier);

		return new String[] { mConsumer.getToken(), mConsumer.getTokenSecret() };
	}

	public OAuthConsumer getmConsumer() {
		return mConsumer;
	}

	public OAuthConsumer getmConsumer(UsuarioApp usuarioApp) {

		this.mConsumer = new CommonsHttpOAuthConsumer(this.sConsumerKey,
				this.sConsumerSecret);

		this.mConsumer.setTokenWithSecret(usuarioApp.getsOAuthToken(),
				usuarioApp.getsOAuthSecret());

		return mConsumer;
	}

	/**
	 * Método que loga un usuario
	 * 
	 * @return
	 */
	public Boolean loginOauth(String user, String pass, String baseURL) {

		Boolean salida = true;

		try {

			// String salidaError;
			// Document doc = null;

			/* Primero recuperamos el token y la dirección de login */

			/*
			 * Cargamos la web del login para obtener el idSession - Le ponemos
			 * como userAgent a un android.
			 */
			String uri = this.getRequestToken();

			Connection.Response res2;
			res2 = Jsoup
					.connect(uri)
					.method(Connection.Method.GET)
					.userAgent(
							"Mozilla/5.0 (Linux; U; Android 4.0.3; ko-kr; LG-L160L Build/IML74K) AppleWebkit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30")
					.execute();

			String idSession = Utils.cookies(ConstantesOAUTH.COOKIE_SESSIONID,
					res2.cookies());

			// doc = res2.parse();

			/*
			 * Realizamos el login con los datos enviados y le establecemos el
			 * idSession anterior
			 */

			Connection con = Jsoup
					.connect(baseURL + ConstantesOAUTH.LOGIN_WEB)
					.data("j_password", pass)
					.data("j_username", user)
					.userAgent(
							"Mozilla/5.0 (Linux; U; Android 4.0.3; ko-kr; LG-L160L Build/IML74K) AppleWebkit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30")
					.method(Connection.Method.POST);

			if (idSession != null) {
				con.cookie(ConstantesOAUTH.COOKIE_SESSIONID, idSession);
			}

			Connection.Response res = con.execute();

			Document docRes1 = res.parse();

			// Usuario y contraseña incorrectos
			if (docRes1.toString().contains("introducidos son incorrectos")) {
				salida = false;

			} else if (docRes1
					.toString()
					.contains(
							"Aun no has validado tu direccion de correo electronico. Debes validarla para poder acceder a la aplicacion.")) {
				salida = null;
			}

			else {

				/*
				 * Recuperamos la la url de salida y parseamos los parámetros
				 */

				// String oauthToken = "";
				String oauth_verifier = "";
				String url = res.url().getQuery();

				String[] params = url.split("&");
				if (params.length == 2) {
					for (int i = 0; i < params.length; i++) {
						String[] split = params[i].split("=");

						if (params[i].contains("oauth_token=")) {
							// oauthToken = split[1];
						} else if (params[i].contains("oauth_verifier=")) {
							oauth_verifier = split[1];
						}
					}
				}

				// Pedimos el token de autenticación
				String[] paramsArray = this.getAccessToken(oauth_verifier);
				if (paramsArray != null && paramsArray.length == 2) {
					sOAuthToken = paramsArray[0];
					sOAuthSecret = paramsArray[1];
				}

			}
		} catch (Exception e) {
			// TODO: handle exception
			salida = false;
		}

		return salida;
	}

	public String getsOAuthToken() {
		return sOAuthToken;
	}

	public void setsOAuthToken(String sOAuthToken) {
		this.sOAuthToken = sOAuthToken;
	}

	public String getsOAuthSecret() {
		return sOAuthSecret;
	}

	public void setsOAuthSecret(String sOAuthSecret) {
		this.sOAuthSecret = sOAuthSecret;
	}

}
