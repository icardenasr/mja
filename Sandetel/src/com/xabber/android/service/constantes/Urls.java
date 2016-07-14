package com.xabber.android.service.constantes;

public class Urls {

	/**
	 * Mode (development, preproduction, production)
	 */
	public static final String MODE_DEV = "development";
	public static final String MODE_PREPRO = "preproduction";
	public static final String MODE_PRO = "production";
	/**
	 * 
	 */

	public static String Sergio = "22:7070";
	public static String Alvaro = "64:8080";

	public static final String MODE = MODE_PREPRO;

	public static final String URL_BASEDEV = "http://192.168.1." + Alvaro
			+ "/distribuidores-rs/services/";
	public static final String URL_BASEREPRO = "https://apps.s-dos.es/distribuidores-rs/services/";
	public static final String URL_BASEPRO = "http://";

	public static String getUrlBase() {

		String url = URL_BASEDEV;
		if (MODE.equals(MODE_DEV)) {
			url = URL_BASEDEV;
		} else if (MODE.equals(MODE_PREPRO)) {
			url = URL_BASEREPRO;
		} else if (MODE.equals(MODE_PRO)) {
			url = URL_BASEPRO;
		}

		return url;
	}

	// CONSIGNA
	public static final String WS_CONSIGNA = "https://consigna.juntadeandalucia.es/ficheros/nuevows";

	// DESCARGA

	// Consigna local
	public static final String LOCAL_CONSIGNA = "/MensajeriaJunta/";
	public static final String LOCAL_CONSIGNA_PATH = "#@&";

	// public static final String WS_DEFAULT = getUrlBase() + "";
	//
	// public static final String WS_CATEGORIAS = getUrlBase()
	// + "categoria/porTipo";
	// public static final String WS_QUESTIONS = getUrlBase() + "contenido";
	//
	// public static final String WS_CONTENIDO_DESCARGA = getUrlBase()
	// + "contenido/descargaArchivo";
	//
	// public static final String WS_SEARCH = getUrlBase()
	// + "contenido/descargaArchivo";
	//
	// public static final String WS_USER_LOGIN = getUrlBase() +
	// "usuario/login";
	//
	// public static final String WS_USER_UPDATE = getUrlBase()
	// + "usuario/editarUsuario";
	//
	// public static final String WS_USER_UPDATE_PASS = getUrlBase()
	// + "usuario/cambiarPassword";

}
