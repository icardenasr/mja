package com.xabber.android.utils.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.xabber.android.service.ws_client.CallWSAsyn;
import com.xabber.android.utils.async.AsyncTask;

import es.juntadeandalucia.android.im.R;

/**
 * Contructor de ProgressDialog
 * 
 * @version 1.0 24 Oct 2013
 * @author José Fernández
 */
public class UtilDialog {

	/**
	 * ProgressDialog a mostrar
	 */
	private static ProgressDialog loadingDialog = null;

	/**
	 * Crea e inicializa un ProgressDialog
	 * 
	 * @param ctx
	 * @param callSync
	 * @return
	 */
	public static ProgressDialog getLoadingDialog(Context ctx,
			CallWSAsyn callSync) {
		return getLoadingDialog(ctx, callSync, null, null, false);
	}

	/**
	 * Crea e inicializa un ProgressDialog
	 * 
	 * @param ctx
	 * @param callSync
	 * @param cancel
	 * @return
	 */
	public static ProgressDialog getLoadingDialog(Context ctx,
			CallWSAsyn callSync, Boolean cancel) {
		return getLoadingDialog(ctx, callSync, null, null, cancel);
	}

	/**
	 * Crea e inicializa un ProgressDialog
	 * 
	 * @param ctx
	 * @param callSync
	 * @param titutlo
	 * @param subject
	 * @return
	 */
	public static ProgressDialog getLoadingDialog(Context ctx,
			CallWSAsyn callSync, int titutlo, int subject) {
		return getLoadingDialog(ctx, callSync, ctx.getString(titutlo),
				ctx.getString(subject), false);
	}

	/**
	 * Crea e inicializa un ProgressDialog
	 * 
	 * @param ctx
	 * @param callSync
	 * @param titutlo
	 * @param subject
	 * @param cancel
	 * @return
	 */
	public static ProgressDialog getLoadingDialog(Context ctx,
			CallWSAsyn callSync, int titutlo, int subject, Boolean cancel) {
		return getLoadingDialog(ctx, callSync, ctx.getString(titutlo),
				ctx.getString(subject), cancel);
	}

	/**
	 * Crea e inicializa un ProgressDialog
	 * 
	 * @param ctx
	 * @param callSync
	 * @param titutlo
	 * @param subject
	 * @param cancel
	 * @return
	 */
	public static ProgressDialog getLoadingDialog(Context ctx,
			final CallWSAsyn callSync, String titutlo, String subject,
			Boolean cancel) {

		loadingDialog = new ProgressDialog(ctx);
		if (titutlo != null) {
			loadingDialog.setTitle(titutlo);
		} else {
			loadingDialog.setTitle(ctx.getString(R.string.cargando));
		}
		if (subject != null) {
			loadingDialog.setMessage(subject);
		} else {
			loadingDialog.setMessage(ctx.getString(R.string.espere));
		}
		if (cancel != null) {
			loadingDialog.setCancelable(cancel);
		} else {
			loadingDialog.setCancelable(false);
		}

		loadingDialog
				.setOnCancelListener(new DialogInterface.OnCancelListener() {

					@Override
					public void onCancel(DialogInterface dialog) {
						if (callSync != null) {
							callSync.cancel(true);
						}
					}
				});

		return loadingDialog;
	}

	/**
	 * Devuelve el Dialog guardando
	 * 
	 * @param contexto
	 * @return
	 */
	public static ProgressDialog getSavingDialog(Context contexto) {

		loadingDialog = new ProgressDialog(contexto);
		loadingDialog.setTitle(contexto.getString(R.string.guardando));
		loadingDialog.setMessage(contexto.getString(R.string.espere));
		loadingDialog.setCancelable(false);

		return loadingDialog;
	}

	/**
	 * Devuelve el Dialog borrando
	 * 
	 * @param contexto
	 * @return
	 */
	public static ProgressDialog getDeletingDialog(Context contexto) {

		loadingDialog = new ProgressDialog(contexto);
		loadingDialog.setTitle(contexto.getString(R.string.borrando));
		loadingDialog.setMessage(contexto.getString(R.string.espere));
		loadingDialog.setCancelable(false);

		return loadingDialog;
	}

	/**
	 * Muestra el alert dialog de cargando
	 */
	public static void showLoadingDialog() {
		// showDialog(loadingDialog);

		new ShowAlertCTask(loadingDialog).execute();
	}

	/**
	 * Cierra el alert dialog cargando
	 */
	public static void dismissLoadingDialog() {
		dismiss(loadingDialog);
	}

	/**
	 * Muestra un AlerDialog
	 * 
	 * @param dialog
	 */
	public static void showDialog(final Dialog dialog) {
		/*
		 * if(dialog!=null && !dialog.isShowing()) new Thread(new Runnable(){
		 * public void run(){
		 */
		dialog.show();
		/*
		 * } }).start();
		 */
	}

	/**
	 * Comprueba si el alert se está mostrando
	 * 
	 * @param alertDialog
	 * @return
	 */
	public static boolean isShowing(AlertDialog alertDialog) {
		return alertDialog != null && alertDialog.isShowing();
	}

	/**
	 * Cierra un alert si se está mostrando
	 * 
	 * @param alertDialog
	 */
	public static void dismiss(AlertDialog alertDialog) {

		if (alertDialog != null && alertDialog.isShowing()) {
			alertDialog.dismiss();
		}
	}

	private static class ShowAlertCTask extends AsyncTask<Void, Void, Void> {

		private AlertDialog alertDialog;

		public ShowAlertCTask(AlertDialog alertDialog) {

			this.alertDialog = alertDialog;

		}

		protected void onPostExecute(Void dResult) {

		}

		protected void onPreExecute() {

			// alertDialog.show();

		}

		@Override
		protected Void doInBackground(Void... params) {

			return null;
		}

	}

}
