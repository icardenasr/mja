package com.xabber.android.utils.dialogs;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.xabber.android.utils.async.AsyncTask;

public class DialogGeneric {

	/**
	 * Params to configure alert
	 */
	public final static String PARAMS_DIALOG_TITLE = "title";
	public final static String PARAMS_DIALOG_MESSAGE = "body";
	public final static String PARAMS_DIALOG_POSITIVE_BUTTON = "positive_button";
	public final static String PARAMS_DIALOG_NEGATIVE_BUTTON = "negative_button";
	public final static String PARAMS_DIALOG_ACTION_INTERFACE = "action_interface";
	public final static String PARAMS_DIALOG_SHOW_AUTOMATIC = "show_automatic";

	/**
	 * Context of the application
	 */
	private Context context;

	private AlertDialog dialog;

	/**
	 * Container of configurations
	 */
	private HashMap<String, Serializable> params = new HashMap<String, Serializable>();

	/**
	 * Actions of the buttons
	 */
	private IDialogAction iDialogAction;

	/**
	 * Show dialog without user action
	 */
	private Boolean showDialogAutomatic = false;

	public DialogGeneric(Context context, HashMap<String, Serializable> params) {
		this.context = context;
		this.params = params;

	}

	public void showDialog() {

		new ShowDialog().execute();

	}

	private void dismissDialog() {

		if (dialog != null) {
			dialog.dismiss();
		}

	}

	private void getSpecialParams() {
		if (params != null) {

			// Action of the buttons
			Serializable s = params.get(PARAMS_DIALOG_ACTION_INTERFACE);

			if (s != null && s instanceof IDialogAction) {
				iDialogAction = (IDialogAction) s;
			}

			params.remove(PARAMS_DIALOG_ACTION_INTERFACE);

			// Show dialog without action user
			s = params.get(PARAMS_DIALOG_SHOW_AUTOMATIC);
			if (s != null && s instanceof Boolean) {
				showDialogAutomatic = (Boolean) s;
			}
			params.remove(PARAMS_DIALOG_SHOW_AUTOMATIC);
		}
	}

	public void createDialog() {

		getSpecialParams();

		final AlertDialog.Builder builder = new AlertDialog.Builder(context);

		if (params != null) {

			for (Map.Entry<String, Serializable> entry : params.entrySet()) {
				// System.out.println(entry.getKey() + "/" + entry.getValue());
				String key = entry.getKey();
				if (key.equals(PARAMS_DIALOG_TITLE)) {
					if (entry.getValue() instanceof String) {
						builder.setTitle((String) entry.getValue());
					}

				} else if (key.equals(PARAMS_DIALOG_MESSAGE)) {

					if (entry.getValue() instanceof String) {
						builder.setMessage((String) entry.getValue());
					}

				} else if (key.equals(PARAMS_DIALOG_POSITIVE_BUTTON)) {

					if (entry.getValue() instanceof String) {
						// Add positive button
						builder.setPositiveButton((String) entry.getValue(),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										if (iDialogAction != null) {
											iDialogAction.onPositiveButton();
										}
										dismissDialog();
									}

								});
					}

				} else if (key.equals(PARAMS_DIALOG_NEGATIVE_BUTTON)) {
					// Add positive button
					builder.setNegativeButton((String) entry.getValue(),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									if (iDialogAction != null) {
										iDialogAction.onNegativeButton();
									}

									dismissDialog();
								}
							});
				}

			}
		}

		((Activity) context).runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// Create the AlertDialog
				dialog = builder.create();

				if (showDialogAutomatic) {
					showDialog();
				}
			}
		});

	}

	class ShowDialog extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			if (dialog != null) {
				dialog.show();
			}
		}

		@Override
		protected Void doInBackground(Void... params) {
			return null;
		}

	}

}
