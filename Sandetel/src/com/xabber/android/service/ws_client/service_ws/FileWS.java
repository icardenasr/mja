package com.xabber.android.service.ws_client.service_ws;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import android.app.Activity;
import android.content.Context;

import com.xabber.android.service.constantes.Urls;
import com.xabber.android.service.ws_client.CallWSAsyn;
import com.xabber.android.service.ws_client.dto.Data;
import com.xabber.android.service.ws_client.interfaces.IDownloadFile;
import com.xabber.android.ui.ChatViewer;
import com.xabber.android.utils.async.AsyncTask;
import com.xabber.android.utils.file.FileManager;

public class FileWS {

	public static final int DOWNLOAD = 1;

	private HashMap<Integer, Object> paramsWS;
	private IDownloadFile iDownloadFile;

	public FileWS(Integer typeAction, Context ctx, List<String> params,
			HashMap<Integer, Object> paramsWS, String url,
			IDownloadFile iDownloadFile) {
		this.paramsWS = paramsWS;
		this.iDownloadFile = iDownloadFile;

		if (typeAction != null) {
			switch (typeAction) {
			case DOWNLOAD:
				if (paramsWS != null) {
					paramsWS.put(CallWSAsyn.PARAMS_URL, url);
				}

				break;

			default:
				break;
			}
		}

		new MakeCall(typeAction, ctx, params, paramsWS).execute();
	}

	public class MakeCall extends CallWSAsyn {

		private Integer typeAction;

		public MakeCall(Integer typeAction, Context ctx, List<String> params,
				HashMap<Integer, Object> paramsWS) {
			super(ctx, params, paramsWS);

			this.typeAction = typeAction;

		}

		@Override
		protected void onResultCorrect() {
			super.onResultCorrect();
			// resutlObjectList
			if (typeAction != null) {
				switch (typeAction) {
				case DOWNLOAD:
					Data data = null;
					if (resutlObjectList != null && !resutlObjectList.isEmpty()) {
						if (resutlObjectList.get(0) instanceof Data) {
							data = (Data) resutlObjectList.get(0);
						}
					}

					if (data != null) {

						// Save File to directory
						new SaveFileInternalExtorage(ctx, UUID.randomUUID()
								+ Urls.LOCAL_CONSIGNA_PATH + fileName, data,
								iDownloadFile).execute();

						if (((Activity) ctx) instanceof ChatViewer) {

						}

					}
					break;

				default:
					break;
				}
			}

			// Toast.makeText(ctx, "Bieeeen", Toast.LENGTH_SHORT).show();
		}

		@Override
		protected void onResultDefaultError() {
			super.onResultDefaultError();
			if (typeAction != null) {
				switch (typeAction) {
				case DOWNLOAD:

					break;
				default:
					break;
				}
			}

		}
	}

	class SaveFileInternalExtorage extends AsyncTask<Void, Void, String> {
		private Context context;
		private String fileName;
		private Data base64;
		private IDownloadFile iDownloadFile;

		public SaveFileInternalExtorage(Context context, String fileName,
				Data data, IDownloadFile iDownloadFile) {
			this.context = context;
			this.fileName = fileName;
			this.base64 = data;
			this.iDownloadFile = iDownloadFile;

		}

		@Override
		protected String doInBackground(Void... params) {

			String path = FileManager.getInstance().saveFile(context, fileName,
					base64);

			return path;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);

			if (iDownloadFile != null && result != null) {
				iDownloadFile.onResultCorrectWS(result);
			}
		}
	}

}
