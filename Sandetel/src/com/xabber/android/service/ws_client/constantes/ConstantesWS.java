package com.xabber.android.service.ws_client.constantes;

/**
 * @version 1.0 24 Oct 2013
 * @author José Fernández
 */
public class ConstantesWS {

	// public static final String URL_BASE =
	// "http://apps.s-dos.es/donasangreand-rs/services/";
	//
	// public static final String WS_GrupoRH = URL_BASE + "gruporh";

	public static final int SERVER_TIME = 15000;
	public static final long WS_LOCAL = 86400000;

	public final static int MODE_NORMAL = 0;
	public final static int MODE_SPRING = 1;
	public final static int MODE_OAUTH = 2;
	public final static int MODE_NO_LOGIN = 3;

	public final static String CAB_KEY_HEAD = "CONSUMER_KEY_HEADER";
	public final static String CAB_OPE_CODE = "CONSUMER_OPE_CODE";
	public final static String CAB_TIME_CODE = "TIME_CODE_HEADER";

	// SQLITE
	public static final String DATABASE_NAME = "DIA.sqlite";
	public static final String SQLITE_TABLE = "SQLITE_TABLE";
	public static final String SQLITE_PRIMARY_KEY = "PRIMARY_KEY";

	public final static String CONFIG_BASE_URL = "baseURL";

}
