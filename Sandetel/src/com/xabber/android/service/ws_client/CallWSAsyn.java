package com.xabber.android.service.ws_client;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.ProgressDialog;
import android.content.Context;
import android.widget.Toast;

import com.xabber.android.service.ws_client.dto.Respuesta;
import com.xabber.android.service.ws_client.dto.ResultWebService;
import com.xabber.android.utils.async.AsyncTask;
import com.xabber.android.utils.dialogs.UtilDialog;
import com.xabber.xmpp.archive.Session;

import es.juntadeandalucia.android.im.R;

/**
 * @version 1.0 24 Oct 2013
 * @author José Fernández
 */
public class CallWSAsyn extends AsyncTask<String, Void, ResultWebService> {

	private CallWSAsyn callWSAsyn;

	/**
	 * Tipo de llamada, GET CALL
	 */
	public static final int PARAMS_TYPE_CALL = 1;
	/**
	 * Url Base de la aplicación
	 */
	public static final int PARAMS_URL = 2;
	/**
	 * Usuario para la acreditación de Spring Security
	 */
	public static final int PARAMS_USER_SPRING = 3;
	/**
	 * Password para la acreditación de Spring Security
	 */
	public static final int PARAMS_PASS_SPRING = 4;
	/**
	 * Variable para poder usar la cache de web Service
	 */
	public static final int PARAMS_USE_CACHE = 5;
	/**
	 * Clase de la lista a obtener en el servicio
	 */
	public static final int PARAMS_CLASE = 6;
	/**
	 * Tipo de seguridad implementada en la llamada
	 */
	public static final int PARAMS_MODE_SECURITY = 7;
	/**
	 * Usar la cache local o no
	 */
	public static final int PARAMS_LOCAL_CACHE = 8;
	/**
	 * Mostrar los dialogos al hacer la llamada
	 */
	public static final int PARAMS_SHOW_DIALOG = 9;
	/**
	 * Objeto a pasar el una petición POST
	 */
	public static final int PARAMS_DATA_POST = 10;
	/**
	 * Mostrar los Toast de control
	 */
	public static final int PARAMS_SHOW_TOAST = 11;
	/**
	 * Posibilidad de cancelar la llamada
	 */
	public static final int PARAMS_CAN_CANCEL_DIALOG = 12;
	/**
	 * Texto que aparecerá en el titulo del dialog. Por defecto aparecerá
	 * "Cargado"
	 */
	public static final int PARAMS_DIALOG_TITTLE = 13;
	/**
	 * Texto que aparecerá en el cuerpo del dialog. Por defecto aparecerá
	 * "Espere , por favor..."
	 */
	public static final int PARAMS_DIALOG_BODY = 14;
	/**
	 * Objeto de Session
	 */
	public static final int PARAMS_SESSION = 15;

	/**
	 * Nombre del archivo para la opción DATA
	 */
	public static final int PARAMS_FILE_NAME = 16;

	/**
	 * Llamada tipo get
	 */
	public final static int TYPE_GET = 1;
	/**
	 * Llamada tipo post
	 */
	public final static int TYPE_POST = 2;
	/**
	 * Llamada tipo Data por Get
	 */
	public final static int TYPE_DATA_GET = 3;

	/**
	 * Contexto de la app
	 */
	protected Context ctx;

	/**
	 * Tipo de llamada, GET CALL
	 */
	protected int typeCall;
	/**
	 * Alert dialog de cargando
	 */
	protected ProgressDialog progressDialog;
	/**
	 * Mostrar o no el progress Dialog
	 */
	protected Boolean showDialog = true;
	/**
	 * Titulo a mostrar en el dialog
	 */
	protected String dialogTittle;
	/**
	 * Body a mostrar en el dialog
	 */
	protected String dialogBody;

	/**
	 * Mostrar o no los toast de verificacion de estado
	 */
	protected Boolean showToast = true;
	/**
	 * Listado de parámetros
	 */
	protected List<String> params;
	/**
	 * Url del servicio a llamar
	 */
	protected String url;
	/**
	 * Usuario para llamadas con spring
	 */
	protected String user;
	/**
	 * Contraseña para llamadas con spring
	 */
	protected String pass;
	/**
	 * Uso de la caché de wb
	 */
	protected Boolean useCache = false;
	/**
	 * Usar la cache local o no
	 */
	protected Boolean localCacheWS = false;
	/**
	 * Clase de la lista a obtener en el servicio
	 */
	@SuppressWarnings("rawtypes")
	protected Class clase;
	/**
	 * Tipo de seguridad implementada en la llamada
	 */
	protected int modeSecurity;
	/**
	 * El resultado del web service
	 */
	protected ResultWebService result;
	/**
	 * La respuesta en forma de listado del result
	 */
	@SuppressWarnings("rawtypes")
	protected List resutlObjectList;
	/**
	 * Los datos de la petición post
	 */
	protected Object dataPost;
	/**
	 * Decidir si puedes o no cancelar la tarea
	 */
	protected Boolean canCancel = false;
	/**
	 * Session del usuario para el modo Oauth
	 */
	protected Session session = null;
	/**
	 * Nombre del archivo para el modo data
	 */
	protected String fileName = null;

	/**
	 * 
	 * @param ctx
	 *            Contexto de la aplicación
	 * @param typeCall
	 *            Tipo de llamada (GET,POST)
	 * @param params
	 *            Parámetros para adjuntar a la url
	 * @param url
	 *            a la que ataca el servicio
	 * @param clase
	 *            que va a devolcer el servicio
	 * @param modeSecurity
	 *            Modo de seguridad del servicio (Normal, Sprin, Oauth)
	 * @param dataPost
	 *            Objeto que se envía del metodo post
	 */
	@SuppressWarnings("rawtypes")
	public CallWSAsyn(Context ctx, int typeCall, List<String> params,
			String url, Class clase, int modeSecurity, Object dataPost) {
		this(ctx, typeCall, params, url, null, null, clase, modeSecurity, null,
				null, true, dataPost);
	}

	/**
	 * @param ctx
	 *            Contexto de la aplicación
	 * @param typeCall
	 *            Tipo de llamada (GET,POST)
	 * @param params
	 *            Parámetros para adjuntar a la url
	 * @param url
	 *            a la que ataca el servicio
	 * @param clase
	 *            que va a devolcer el servicio
	 * @param modeSecurity
	 *            Modo de seguridad del servicio (Normal, Sprin, Oauth)
	 * @param showDialog
	 *            Mostrar o no los dialogs
	 * @param dataPost
	 *            Objeto que se envía del metodo post
	 */
	@SuppressWarnings("rawtypes")
	public CallWSAsyn(Context ctx, int typeCall, List<String> params,
			String url, Class clase, int modeSecurity, Boolean showDialog,
			Object dataPost) {
		this(ctx, typeCall, params, url, null, null, clase, modeSecurity, null,
				null, showDialog, dataPost);
	}

	/**
	 * @param ctx
	 *            Contexto de la aplicación
	 * @param typeCall
	 *            Tipo de llamada (GET,POST)
	 * @param params
	 *            Parámetros para adjuntar a la url
	 * @param url
	 *            a la que ataca el servicio
	 * @param user
	 *            Usuario para el modo de servicio Spring Security
	 * @param pass
	 *            Contraseña para el modo de servicio Spring Security
	 * @param clase
	 *            que va a devolcer el servicio
	 * @param modeSecurity
	 *            Modo de seguridad del servicio (Normal, Sprin, Oauth)
	 * @param showDialog
	 *            Mostrar o no los dialogs
	 * @param dataPost
	 *            Objeto que se envía del metodo post
	 */
	@SuppressWarnings("rawtypes")
	public CallWSAsyn(Context ctx, int typeCall, List<String> params,
			String url, String user, String pass, Class clase,
			int modeSecurity, Object dataPost) {
		this(ctx, typeCall, params, url, user, pass, clase, modeSecurity, null,
				null, true, dataPost);
	}

	/**
	 * @param ctx
	 *            Contexto de la aplicación
	 * @param typeCall
	 *            Tipo de llamada (GET,POST)
	 * @param params
	 *            Parámetros para adjuntar a la url
	 * @param url
	 *            a la que ataca el servicio
	 * @param user
	 *            Usuario para el modo de servicio Spring Security
	 * @param pass
	 *            Contraseña para el modo de servicio Spring Security
	 * @param clase
	 *            que va a devolcer el servicio
	 * @param modeSecurity
	 *            Modo de seguridad del servicio (Normal, Sprin, Oauth)
	 * @param showDialog
	 *            Mostrar o no los dialogs
	 * @param dataPost
	 *            Objeto que se envía del metodo post
	 */
	@SuppressWarnings("rawtypes")
	public CallWSAsyn(Context ctx, int typeCall, List<String> params,
			String url, String user, String pass, Class clase,
			int modeSecurity, Boolean showDialog, Object dataPost) {
		this(ctx, typeCall, params, url, user, pass, clase, modeSecurity, null,
				null, showDialog, dataPost);
	}

	/**
	 * @param ctx
	 *            Contexto de la aplicación
	 * @param typeCall
	 *            Tipo de llamada (GET,POST)
	 * @param params
	 *            Parámetros para adjuntar a la url
	 * @param url
	 *            a la que ataca el servicio
	 * @param user
	 *            Usuario para el modo de servicio Spring Security
	 * @param pass
	 *            Contraseña para el modo de servicio Spring Security
	 * @param clase
	 *            que va a devolcer el servicio
	 * @param modeSecurity
	 *            Modo de seguridad del servicio (Normal, Sprin, Oauth)
	 * @param showDialog
	 *            Mostrar o no los dialogs
	 * @param useCache
	 *            Usar cache de Web Services
	 * @param localCacheWS
	 *            Usar caché local de web Services
	 * @param dataPost
	 *            Objeto que se envía del metodo post
	 */
	@SuppressWarnings("rawtypes")
	public CallWSAsyn(Context ctx, int typeCall, List<String> params,
			String url, Class clase, int modeSecurity, Boolean useCache,
			Boolean localCacheWS, Object dataPost) {
		this(ctx, typeCall, params, url, null, null, clase, modeSecurity,
				useCache, localCacheWS, true, dataPost);
	}

	/**
	 * @param ctx
	 *            Contexto de la aplicación
	 * @param typeCall
	 *            Tipo de llamada (GET,POST)
	 * @param params
	 *            Parámetros para adjuntar a la url
	 * @param url
	 *            a la que ataca el servicio
	 * @param clase
	 *            que va a devolcer el servicio
	 * @param modeSecurity
	 *            Modo de seguridad del servicio (Normal, Sprin, Oauth)
	 * @param showDialog
	 *            Mostrar o no los dialogs
	 * @param useCache
	 *            Usar cache de Web Services
	 * @param localCacheWS
	 *            Usar caché local de web Services
	 * @param dataPost
	 *            Objeto que se envía del metodo post
	 */
	@SuppressWarnings("rawtypes")
	public CallWSAsyn(Context ctx, int typeCall, List<String> params,
			String url, Class clase, int modeSecurity, Boolean useCache,
			Boolean localCacheWS, Boolean showDialog, Object dataPost) {
		this(ctx, typeCall, params, url, null, null, clase, modeSecurity,
				useCache, localCacheWS, showDialog, dataPost);
	}

	/**
	 * @param ctx
	 *            Contexto de la aplicación
	 * @param typeCall
	 *            Tipo de llamada (GET,POST)
	 * @param params
	 *            Parámetros para adjuntar a la url
	 * @param url
	 *            a la que ataca el servicio
	 * @param user
	 *            Usuario para el modo de servicio Spring Security
	 * @param pass
	 *            Contraseña para el modo de servicio Spring Security
	 * @param clase
	 *            que va a devolcer el servicio
	 * @param modeSecurity
	 *            Modo de seguridad del servicio (Normal, Sprin, Oauth)
	 * @param showDialog
	 *            Mostrar o no los dialogs
	 * @param useCache
	 *            Usar cache de Web Services
	 * @param localCacheWS
	 *            Usar caché local de web Services
	 * @param dataPost
	 *            Objeto que se envía del metodo post
	 */
	@SuppressWarnings("rawtypes")
	public CallWSAsyn(Context ctx, int typeCall, List<String> params,
			String url, String user, String pass, Class clase,
			int modeSecurity, Boolean useCache, Boolean localCacheWS,
			Boolean showDialog, Object dataPost) {
		this.callWSAsyn = this;
		this.ctx = ctx;
		this.typeCall = typeCall;
		this.params = params;
		this.url = url;
		this.user = user;
		this.pass = pass;
		this.useCache = useCache;
		this.clase = clase;
		this.modeSecurity = modeSecurity;
		this.localCacheWS = localCacheWS;
		this.showDialog = showDialog;
		this.dataPost = dataPost;
	}

	/**
	 * 
	 * @param ctx
	 *            Contexto de la aplicación
	 * @param params
	 *            Parámetros para adjuntar a la url
	 * @param paramsWS
	 *            HashMap dónde vienen todas las variables que quieras
	 *            configurar(clave, Valor)
	 */

	@SuppressWarnings("rawtypes")
	public CallWSAsyn(Context ctx, List<String> params,
			HashMap<Integer, Object> paramsWS) {

		this.callWSAsyn = this;
		this.ctx = ctx;
		this.params = params;

		if (paramsWS != null) {
			Iterator it = paramsWS.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry e = (Map.Entry) it.next();

				Integer aux = (Integer) e.getKey();

				switch (aux.intValue()) {
				case PARAMS_TYPE_CALL:
					this.typeCall = (Integer) e.getValue();
					break;
				case PARAMS_URL:
					this.url = (String) e.getValue();
					break;
				case PARAMS_USER_SPRING:
					this.user = (String) e.getValue();
					break;
				case PARAMS_PASS_SPRING:
					this.pass = (String) e.getValue();
					break;
				case PARAMS_USE_CACHE:
					this.useCache = (Boolean) e.getValue();
					break;
				case PARAMS_CLASE:
					this.clase = (Class) e.getValue();
					break;
				case PARAMS_MODE_SECURITY:
					this.modeSecurity = (Integer) e.getValue();
					break;
				case PARAMS_LOCAL_CACHE:
					this.localCacheWS = (Boolean) e.getValue();
					break;
				case PARAMS_SHOW_DIALOG:
					this.showDialog = (Boolean) e.getValue();
					break;
				case PARAMS_DATA_POST:
					this.dataPost = e.getValue();
					break;
				case PARAMS_SHOW_TOAST:
					this.showToast = (Boolean) e.getValue();
					break;

				case PARAMS_CAN_CANCEL_DIALOG:
					this.canCancel = (Boolean) e.getValue();
					break;
				case PARAMS_DIALOG_TITTLE:
					this.dialogTittle = (String) e.getValue();
					break;
				case PARAMS_DIALOG_BODY:
					this.dialogBody = (String) e.getValue();
					break;

				case PARAMS_SESSION:
					this.session = (Session) e.getValue();
					break;
				case PARAMS_FILE_NAME:
					this.fileName = (String) e.getValue();
					break;

				default:
					break;
				}

				// System.out.println(e.getKey() + " " + e.getValue());
			}
		}

	}

	@Override
	protected void onPreExecute() {

		if (showDialog != null && showDialog) {
			progressDialog = UtilDialog.getLoadingDialog(ctx, callWSAsyn,
					dialogTittle, dialogBody, canCancel);
			if (progressDialog != null && !progressDialog.isShowing()) {
				progressDialog.show();
			}
		}
	}

	@Override
	protected ResultWebService doInBackground(String... arg0) {
		return realizaLlamada();
	}

	@SuppressWarnings("rawtypes")
	private ResultWebService realizaLlamada() {

		ResultWebService salida = null;
		if (callWSAsyn != null && !callWSAsyn.isCancelled()) {

			try {

				Respuesta res = null;

				if (this.typeCall == CallWSAsyn.TYPE_POST) {

					res = CallServicePost.callService(ctx, callWSAsyn, user,
							pass, url, params, dataPost, useCache,
							modeSecurity, clase, session);
				} else if (this.typeCall == CallWSAsyn.TYPE_GET) {
					res = CallServiceGet
							.callService(ctx, user, pass, url, params,
									useCache, clase, modeSecurity, localCacheWS);

				} else if (this.typeCall == CallWSAsyn.TYPE_DATA_GET) {
					res = CallServiceDataGet.callService(ctx, fileName, user,
							pass, url, params, useCache, clase, modeSecurity,
							localCacheWS);
				}

				if (res != null && res.getCodError().equalsIgnoreCase("0")) {

					salida = ResultWebService.getCorrecto();

					if (res.getRespuesta() != null) {
						resutlObjectList = res.getRespuesta();
					} else if (res.getRespuestaEsp() != null) {
						resutlObjectList = res.getRespuestaEsp();
					}

					// Hacemos las cosas concretas de cada llamada asincrona
					doInBackgroundCorrect();

				} else if (res != null
						&& res.getCodError().equalsIgnoreCase("401")) {
					salida = ResultWebService
							.getErrorLogin(ctx
									.getString(R.string.error_credenciales_incorrectas));

				} else if (res != null && res.getDescripcion() != null
						&& !"".equals(res.getDescripcion())) {
					salida = new ResultWebService(res.getDescripcion());

				} else {
					salida = new ResultWebService(Respuesta
							.getErrorDefaultResp().getDescripcion());
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return salida;
	}

	@Override
	protected void onPostExecute(ResultWebService result) {

		super.onPostExecute(result);

		this.result = result;

		if (result != null && result.isCorrect()) {

			onResultCorrect();

		} else if (result != null && result.isErrorLogin()) {

			// Error con las credenciales de acceso
			onResutlErrorCredentials();

			// customListView.removeAllViews();

		} else {

			onResultDefaultError();

		}

		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
		}

	}

	/**
	 * Método que se ejecutará dentro del doInBackground cuando e relsultado ha
	 * sido bueno
	 */
	protected void doInBackgroundCorrect() {

	}

	/**
	 * Error que se mostrará cuando los datos de acceso no sean correctos
	 */
	protected void onResutlErrorCredentials() {
		if (showToast != null && showToast) {
			Toast.makeText(ctx,
					ctx.getString(R.string.error_credenciales_incorrectas),
					Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * Error por defecto
	 */
	protected void onResultDefaultError() {
		if (showToast != null && showToast) {

			if (result != null) {
				Toast.makeText(ctx, result.getDescripcion(), Toast.LENGTH_LONG)
						.show();
			} else {
				Toast.makeText(ctx, ctx.getString(R.string.result_null),
						Toast.LENGTH_LONG).show();
			}
		}
	}

	/**
	 * Método que se ejecuta en el onPostExecute cuando la salida sea correcta
	 */
	protected void onResultCorrect() {
	}

	/**
	 * The String class represents character strings. All string literals in
	 * Java programs, such as <code>'abc'</code>, are implemented as instances
	 * of this class.
	 * <p>
	 * Strings are constant; their values cannot be changed after they are
	 * created. String buffers support mutable strings. Because String objects
	 * are immutable they can be shared. For example:
	 * 
	 * <pre>
	 * 
	 * 	           String str = 'abc';
	 * </pre>
	 * <p>
	 * is equivalent to:
	 * 
	 * <pre>
	 * 
	 * char data[] = { 'a', 'b', 'c' };
	 * String str = new String(data);
	 * </pre>
	 * 
	 * <p>
	 * The class <code>String</code> includes methods for examining individual
	 * characters of the sequence, for comparing strings, for searching strings,
	 * for extracting substrings, and for creating a copy of a string with all
	 * characters translated to uppercase or to lowercase. Case mapping is based
	 * on the Unicode Standard version specified by the {@link Character} class.
	 * 
	 * <p>
	 * Unless otherwise noted, passing a null argument to a constructor or
	 * method in this class will cause a {@link NullPointerException} to be
	 * thrown.
	 * 
	 * @see Object.toString()
	 * @see StringBuffer
	 * @see StringBuilder
	 * @see Charset
	 * @see Serialized Form
	 */

}
