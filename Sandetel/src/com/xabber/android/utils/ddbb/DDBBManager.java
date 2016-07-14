package com.xabber.android.utils.ddbb;

import java.util.Collection;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.widget.Toast;

import com.xabber.android.data.DatabaseManager;
import com.xabber.android.data.account.AccountManager;
import com.xabber.android.data.account.StatusMode;
import com.xabber.android.service.dto.UsuarioDTO;
import com.xabber.android.service.ws_client.service_ws.UsuarioWS;
import com.xabber.android.ui.ContactList;
import com.xabber.android.utils.preferences.UtilPreferences;

import es.juntadeandalucia.android.im.R;

public class DDBBManager {

	private static DDBBManager instance;

	private Activity activity;

	private DDBBManager(Activity activity) {
		this.activity = activity;
	}

	public static DDBBManager getInstance(Activity activity) {
		if (instance == null) {
			instance = new DDBBManager(activity);
		} else {
			instance.setActivity(activity);
		}

		return instance;
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
	}

	public void startImport(final Uri uriImport) {

		final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setCancelable(false);
		builder.setTitle(R.string.import_atention);
		builder.setMessage(R.string.import_alert);
		builder.setPositiveButton(R.string.si,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						actionImport(uriImport);
					}
				});
		builder.setNegativeButton(R.string.no,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				});

		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (!activity.isFinishing()) {
					builder.create().show();
				}
			}
		});

	}

	private void actionImport(Uri uriImport) {


		AccountManager.getInstance().removeAllAccount();
		DatabaseManager.getInstance().removeAll();

		AccountManager.getInstance().clearAccounts();

		Boolean isImportSuccesful = false;

		if (uriImport != null) {
			isImportSuccesful = DatabaseManager.getInstance().importDatabase(
					activity, uriImport.getPath());

		}

		if (isImportSuccesful) {
			
			AccountManager.getInstance().setAvailableStatusAllAcount();

			Toast.makeText(activity, R.string.import_succesful,
					Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(activity, R.string.import_error, Toast.LENGTH_SHORT)
					.show();
		}
		createIntent();

	}

	private void createIntent() {
		UsuarioDTO user = (UsuarioDTO) UtilPreferences.getPreferences(activity,
				UsuarioWS.KEY_USUARIO, UsuarioDTO.class);
		if (user != null) {
			if (activity instanceof ContactList) {
				((ContactList) activity).actionLogOut();
			}

		}

	}

}
