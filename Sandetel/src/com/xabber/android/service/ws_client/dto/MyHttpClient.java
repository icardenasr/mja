package com.xabber.android.service.ws_client.dto;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import org.apache.http.HttpVersion;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import android.content.Context;

import com.xabber.android.service.ws_client.constantes.ConstantesWS;

/**
 * @version 1.0 24 Oct 2013
 * @author José Fernández
 */
public class MyHttpClient extends DefaultHttpClient {

	final Context context;

	public MyHttpClient(Context context) {
		this.context = context;
	}

	@Override
	protected ClientConnectionManager createClientConnectionManager() {

		// SchemeRegistry registry = new SchemeRegistry();
		// registry.register(new Scheme("http",
		// PlainSocketFactory.getSocketFactory(), 80));
		// // Register for port 443 our SSLSocketFactory with our keystore
		// // to the ConnectionManager
		// registry.register(new Scheme("https", newSslSocketFactory(), 443));

		KeyStore trustStore;
		SSLSocketFactory sf = null;
		try {
			trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			trustStore.load(null, null);

			sf = new MySSLSocketFactory(trustStore);
			sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnrecoverableKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

		ConnManagerParams.setTimeout(params, ConstantesWS.SERVER_TIME);
		HttpConnectionParams.setConnectionTimeout(params,
				ConstantesWS.SERVER_TIME);
		HttpConnectionParams.setSoTimeout(params, ConstantesWS.SERVER_TIME);

		SchemeRegistry registry = new SchemeRegistry();
		registry.register(new Scheme("http", PlainSocketFactory
				.getSocketFactory(), 80));
		// registry.register(new Scheme("https", newSslSocketFactory(), 443));
		registry.register(new Scheme("https", sf, 443));

		// ClientConnectionManager ccm = new ThreadSafeClientConnManager(params,
		// registry);

		return new SingleClientConnManager(getParams(), registry);
	}

}