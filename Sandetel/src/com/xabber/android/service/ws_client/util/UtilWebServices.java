package com.xabber.android.service.ws_client.util;

import java.net.HttpURLConnection;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.xabber.android.service.ws_client.constantes.ConstantesWS;

/**
 * @version 1.0 24 Oct 2013
 * @author José Fernández
 */
public class UtilWebServices {

	// always verify the host - dont check for certificate
	final public static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
		@Override
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	};

	/**
	 * Permitir conexiones seguras
	 */
	public static void trustAllHosts() {
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

	/**
	 * 
	 * @param http
	 * @param consumerKey
	 * @param consumerPass
	 */
	public static void addHeaderNoAuthetication(HttpURLConnection http,
			String consumerKey, String consumerPass) {

		String timeCode = System.currentTimeMillis() + "";

		http.setRequestProperty(ConstantesWS.CAB_TIME_CODE, timeCode);
		http.setRequestProperty(ConstantesWS.CAB_KEY_HEAD, consumerKey);

		String operationCode = hash_hmac("HmacSHA1", timeCode, consumerKey);

		http.setRequestProperty(ConstantesWS.CAB_OPE_CODE, operationCode);

	}

	/*
	 * Metodo que genera el codigo de operacion para una peticion utilizando el
	 * consumer secret, el codigo de tiempo y un metodo de firma HMAC-SHA1.
	 */
	private static String hash_hmac(String type, String value, String key) {
		try {
			javax.crypto.Mac mac = javax.crypto.Mac.getInstance(type);
			javax.crypto.spec.SecretKeySpec secret = new javax.crypto.spec.SecretKeySpec(
					key.getBytes(), type);
			mac.init(secret);
			byte[] digest = mac.doFinal(value.getBytes());
			StringBuilder sb = new StringBuilder(digest.length * 2);
			String s;
			for (byte b : digest) {
				s = Integer.toHexString(Integer.valueOf(b));
				if (s.length() == 1)
					sb.append('0');
				sb.append(s);
			}
			return sb.toString();
		} catch (Exception e) {
			android.util.Log.v("TAG", "Exception [" + e.getMessage() + "]", e);
		}
		return "";
	}

}
