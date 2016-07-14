package com.xabber.android.ui.own;

import java.util.Collection;
import java.util.List;

import Constantes.Constantes;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.xabber.android.data.Application;
import com.xabber.android.data.NetworkException;
import com.xabber.android.data.SettingsManager;
import com.xabber.android.data.account.AccountItem;
import com.xabber.android.data.account.AccountManager;
import com.xabber.android.data.account.AccountType;
import com.xabber.android.data.account.IActionAfterLoad;
import com.xabber.android.data.account.StatusMode;
import com.xabber.android.data.intent.AccountIntentBuilder;
import com.xabber.android.service.dto.UsuarioDTO;
import com.xabber.android.service.ws_client.service_ws.UsuarioWS;
import com.xabber.android.ui.ContactList;
import com.xabber.android.ui.OAuthActivity;
import com.xabber.android.ui.helper.ManagedActivity;
import com.xabber.android.ui.helper.OrbotHelper;
import com.xabber.android.utils.ddbb.DDBBManager;
import com.xabber.android.utils.preferences.UtilPreferences;

import es.juntadeandalucia.android.im.R;

public class LoginAct extends ManagedActivity implements OnClickListener {

	public static final String EXTRA_URI_IMPORT = "com.xabber.android.ui.own.LoginAct.URI_IMPORT";
	private static final int DIALOG_CLOSE_APPLICATION_ID = 0x57;
	private static final String SAVED_ACCOUNT_TYPE = "com.xabber.android.ui.AccountAdd.ACCOUNT_TYPE";
	private static final int OAUTH_WML_REQUEST_CODE = 1;
	private static final int ORBOT_DIALOG_ID = 9050;
	private Boolean useOrbotView = false;
	private Boolean syncableView = true;
	private List<AccountType> accountTypes;
	private AccountType accountType;

	/**
	 * 
	 */

	private EditText etUsu, etPass;
	private Button btnOk;
	private UsuarioDTO usuarioDTO;
	private TextView terminosTV;

	private ProgressDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);

		PreferenceManager.setDefaultValues(this, R.xml.account_editor_xmpp,
				false);

		// Mostrar contactos offLine
		SettingsManager.setContactsShowOffline(true);

		// DIALOG_START_AT_BOOT_ID:
		// Iniciar app con el inicio del móvil
		SettingsManager.setStartAtBootSuggested();
		SettingsManager.setConnectionStartAtBoot(true);

		// putValue(
		// source,
		// R.string.account_tls_mode_key,
		// Integer.valueOf(accountItem.getConnectionSettings()
		// .getTlsMode().ordinal()));
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);

		Editor ed = prefs.edit();

		ed.putString(getString(R.string.account_tls_mode_key),
				getString(R.string.account_tls_legacy_summary));

		ed.commit();

		// UtilPreferences.setPreferences(this,
		// getString(R.string.account_tls_mode_key),
		// getString(R.string.account_tls_legacy_summary));
		actionImportOrLoged();

		setContentView(R.layout.account_add);

		accountTypes = AccountManager.getInstance().getAccountTypes();
		if (accountTypes != null && !accountTypes.isEmpty()) {
			accountType = accountTypes.get(0);
		}

		loadWidgets();

		createDialog();

		// startActivity(AccountEditor.createIntent(this, ""));
	}

	private void loadWidgets() {

		etUsu = (EditText) findViewById(R.id.account_user_name);

		etPass = (EditText) findViewById(R.id.account_password);

		btnOk = (Button) findViewById(R.id.ok);
		InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		inputManager.hideSoftInputFromWindow(btnOk.getWindowToken(),
				InputMethodManager.HIDE_NOT_ALWAYS);
		btnOk.setOnClickListener(this);

		// Texto de terminos de uso

		terminosTV = (TextView) findViewById(R.id.terminos_text);
		SpannableString contentTerminos = new SpannableString(getResources()
				.getString(R.string.terminos_login));
		contentTerminos.setSpan(new UnderlineSpan(), 0,
				contentTerminos.length(), 0);
		terminosTV.setText(contentTerminos);

		terminosTV.setOnClickListener(new OnClickListener() {
			public void onClick(View thisButton) {
				String url = "https://correo.juntadeandalucia.es/webmail/plugins/help/content/terminos.html";
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				startActivity(i);
			}
		});

	}

	private Boolean prepareCall() {

		if (etUsu.getText().toString().equals("")) {

			Toast.makeText(this, getString(R.string.field_usu),
					Toast.LENGTH_SHORT).show();

			return false;
		}
		if (etPass.getText().toString().equals("")) {

			Toast.makeText(this, getString(R.string.field_pass),
					Toast.LENGTH_SHORT).show();
			return false;
		}
		usuarioDTO = new UsuarioDTO();
		usuarioDTO.setNick(etUsu.getText().toString());
		// usuarioDTO.setPassword(UtilLogin.md5(etPass.getText().toString()));
		usuarioDTO.setPassword(etPass.getText().toString());

		return true;

	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.ok:

			showDialog();
			if (prepareCall()) {

				// HashMap<Integer, Object> paramsWS = new HashMap<Integer,
				// Object>();
				// paramsWS.put(CallWSAsyn.PARAMS_TYPE_CALL,
				// CallWSAsyn.TYPE_POST);
				// paramsWS.put(CallWSAsyn.PARAMS_CLASE, UsuarioDTO.class);
				// paramsWS.put(CallWSAsyn.PARAMS_MODE_SECURITY,
				// ConstantesWS.MODE_NORMAL);
				// // paramsWS.put(CallWSAsyn.PARAMS_DIALOG_TITTLE, "Title");
				// // paramsWS.put(CallWSAsyn.PARAMS_DIALOG_BODY, "Subject");
				// // paramsWS.put(CallWSAsyn.PARAMS_URL, Urls.WS_GrupoRH);
				// // paramsWS.put(CallWSAsyn.PARAMS_CAN_CANCEL_DIALOG, true);
				// // paramsWS.put(CallWSAsyn.PARAMS_USER_SPRING, "Jose");
				// // paramsWS.put(CallWSAsyn.PARAMS_PASS_SPRING, "123456");
				// // paramsWS.put(CallWSAsyn.PARAMS_USE_CACHE, true);
				// // paramsWS.put(CallWSAsyn.PARAMS_LOCAL_CACHE, false);
				// // paramsWS.put(CallWSAsyn.PARAMS_SHOW_DIALOG, true);
				// paramsWS.put(CallWSAsyn.PARAMS_DATA_POST, usuarioDTO);
				// paramsWS.put(CallWSAsyn.PARAMS_SHOW_TOAST, false);
				// // paramsWS.put(CallWSAsyn.PARAMS_SESSION, session);
				//
				// new UsuarioWS(UsuarioWS.LOGIN, this, null, paramsWS, etPass
				// .getText().toString());

				if (usuarioDTO != null) {

					// UtilPreferences.setPreferences(ctx,
					// KEY_USUARIO, usuResult);
					loginChat(usuarioDTO);

				}
			}
			break;

		default:
			break;
		}

	}

	public void loginChat(final UsuarioDTO usuario) {
		if (useOrbotView && !OrbotHelper.isOrbotInstalled()) {
			showDialog(ORBOT_DIALOG_ID);
			return;
		}
		// AccountType accountType = (AccountType) accountTypeView
		// .getSelectedItem();

		final String usu = etUsu.getText().toString();
		final String pass = etPass.getText().toString();

		AccountManager.getInstance().setiActionAfterLoad(
				new IActionAfterLoad() {

					@Override
					public void actionAlreadyLoad() {

						Application application = Application.getInstance();
						if (application != null) {
							application.setWaitForLogin(true);
							application.setLoginAct(LoginAct.this);
						}

						actionAddContact(usu, usuario, pass);
					}
				});
		AccountManager.getInstance().onLoad();

	}

	private void actionAddContact(String usu, UsuarioDTO usuario, String pass) {
		Collection<String> allAccounts = AccountManager.getInstance()
				.getAllAccounts();
		String account = null;
		if (allAccounts != null && !allAccounts.isEmpty()) {

			for (String accountDDBB : allAccounts) {
				if (accountDDBB.contains(usu)) {

					// Reseteamos la pass
					AccountItem accountItem = AccountManager.getInstance()
							.getAccount(accountDDBB);
					accountItem.getConnectionSettings().setPassword(pass);
					AccountManager.getInstance().requestToWriteAccount(
							accountItem);

					account = accountDDBB;
					AccountManager.getInstance().setEnable(account, true);
					AccountManager.getInstance().setSyncable(account, true);
					break;
				}
			}

		}

		if (account == null) {

			if (accountType.getProtocol().isOAuth()) {
				startActivityForResult(
						OAuthActivity.createIntent(this,
								accountType.getProtocol()),
						OAUTH_WML_REQUEST_CODE);
			} else {
				// EditText userView = (EditText)
				// findViewById(R.id.account_user_name);
				// EditText passwordView = (EditText)
				// findViewById(R.id.account_password);
				if (usuario != null) {

					try {
						account = AccountManager.getInstance().addAccount(usu,
								pass, accountType, syncableView, true,
								useOrbotView);
					} catch (NetworkException e) {
						Application.getInstance().onError(e);
						return;
					}

					setResult(RESULT_OK,
							createAuthenticatorResult(this, account));

				}
			}
		}

		if (account != null) {

			((Application) getApplication()).onCreate();

			AccountItem accountItem = AccountManager.getInstance().getAccount(
					account);

			AccountManager.getInstance().setEnable(account, true);
			accountItem.forceReconnect();
			accountItem.updateConnection(true);

			// AccountManager.getInstance().onAccountChanged(account);

			// AccountManager.getInstance().onAccountEnabled(accountItem);
			// AccountManager.getInstance().onAccountOnline(accountItem);
			// accountItem.updateConnection(true);
			// accountItem.forceReconnect();
			// AccountManager.getInstance().setEnable(account, true);

			// AccountManager.getInstance().requestToWriteAccount(accountItem);
			// AccountManager.getInstance().addAccount(accountItem);

			// AccountManager.getInstance().onAccountChanged(
			// accountItem.getAccount());

			// AccountManager.getInstance().onAccountChanged(account);

			// AccountManager.getInstance().onAccountEnabled(accountItem);

			AccountManager.getInstance().setStatus(account,
					StatusMode.available, accountItem.getStatusText());

			// ConnectionManager.getInstance().updateConnections(true);

			// try {
			// PresenceManager.getInstance().resendPresence(account);
			// } catch (NetworkException e) {
			// }
			// AccountManager.getInstance().onAccountChanged(account);

			UtilPreferences.setPreferences(this, Constantes.ACCOUNT, account);

			// startActivity(ContactList.createPersistentIntent(this));
			// finish();

			UtilPreferences
					.setPreferences(this, UsuarioWS.KEY_USUARIO, usuario);

			// setResult(RESULT_OK, createAuthenticatorResult(this, account));

			// System.exit(0);

			// Intent i = new Intent(this, ContactList.class);
			// startActivity(i);
			// finish();
			//
			// setResult(RESULT_OK, createAuthenticatorResult(this, account));

		}
	}

	private static Intent createAuthenticatorResult(Context context,
			String account) {
		return new AccountIntentBuilder(null, null).setAccount(account).build();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(SAVED_ACCOUNT_TYPE, accountType.getName());
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == OAUTH_WML_REQUEST_CODE) {
			if (resultCode == RESULT_OK && !OAuthActivity.isInvalidated(data)) {
				String token = OAuthActivity.getToken(data);
				if (token == null) {
					Application.getInstance().onError(
							R.string.AUTHENTICATION_FAILED);
				} else {
					String account;
					try {
						account = AccountManager.getInstance().addAccount(null,
								token, accountType, true, true, useOrbotView);
					} catch (NetworkException e) {
						Application.getInstance().onError(e);
						return;
					}
					setResult(RESULT_OK,
							createAuthenticatorResult(this, account));
					finish();
				}
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {

			Application.getInstance().requestToClose();
			showDialog(DIALOG_CLOSE_APPLICATION_ID);
			unregisterListeners();

			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					// Close activity if application was not killed yet.

					finish();
				}
			}, CLOSE_ACTIVITY_AFTER_DELAY);

			return super.onKeyDown(keyCode, event);
		}

		return false;

	}

	private void unregisterListeners() {
		// Application.getInstance().removeUIListener(
		// OnAccountChangedListener.class, this);
		// Application.getInstance().removeUIListener(
		// OnContactChangedListener.class, this);
		// Application.getInstance().removeUIListener(OnChatChangedListener.class,
		// this);
		// contactListAdapter.removeRefreshRequests();
	}

	/**
	 * Select contact to be invited to the room was requested.
	 */

	private static final long CLOSE_ACTIVITY_AFTER_DELAY = 300;

	@SuppressWarnings("deprecation")
	@Override
	protected Dialog onCreateDialog(int id) {
		super.onCreateDialog(id);
		switch (id) {

		case DIALOG_CLOSE_APPLICATION_ID:
			ProgressDialog progressDialog = new ProgressDialog(this);
			progressDialog
					.setMessage(getString(R.string.application_state_closing));
			progressDialog.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					finish();
				}
			});
			progressDialog.setIndeterminate(true);
			return progressDialog;
		default:
			return null;
		}
	}

	public static Intent createPersistentIntent(Context context) {
		Intent intent = new Intent(context, LoginAct.class);
		intent.setAction("android.intent.action.MAIN");
		intent.addCategory("android.intent.category.LAUNCHER");
		intent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		return intent;
	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	@Override
	protected void onStart() {
		super.onStart();
		((Application) getApplication()).setLoginAct(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		((Application) getApplication()).setLoginAct(null);
	}

	public void actionImportOrLoged() {
		actionImportOrLoged(null);
	}

	public void actionImportOrLoged(Uri uriExtra) {

		// Uri uriFile = null;
		// Bundle extras = getIntent().getExtras();
		// if (extras != null) {
		// String uri = extras.getString(EXTRA_URI_IMPORT);
		// if (uri != null) {
		// uriFile = Uri.parse(uri);
		// }
		// } else {
		// uriFile = uriExtra;
		// }

		Uri uriFile = null;
		if (uriExtra == null) {
			Bundle extras = getIntent().getExtras();
			if (extras != null) {
				String uri = extras.getString(EXTRA_URI_IMPORT);
				if (uri != null) {
					uriFile = Uri.parse(uri);
				}
			}

		} else {
			uriFile = uriExtra;
		}
		if (UtilPreferences.getPreferences(this, UsuarioWS.KEY_USUARIO,
				UsuarioDTO.class) != null) {

			Intent i = new Intent(this, ContactList.class);
			if (uriFile != null) {
				i.putExtra(EXTRA_URI_IMPORT, uriFile.toString());
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
						| Intent.FLAG_ACTIVITY_NEW_TASK);
			}

			startActivity(i);

			finish();
		} else {
			if (uriFile != null) {
				DDBBManager.getInstance(this).startImport(uriFile);
			}
		}

	}

	private void createDialog() {

		dialog = new ProgressDialog(this);
		dialog.setMessage(getString(R.string.wait_please));

	}

	private void showDialog() {

		if (dialog != null && !dialog.isShowing()) {
			dialog.show();
		}
	}

	public void dissmissDialog() {

		if (dialog != null && dialog.isShowing()) {
			dialog.dismiss();
		}

	}

}
