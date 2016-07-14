package com.xabber.android.utils.clipboard;

import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import es.juntadeandalucia.android.im.R;

public class ClipBoardUtil {

	public static void copyToClipBoard(Context context, View v) {

		TextView tv = null;
		EditText et = null;

		if (v instanceof TextView) {
			tv = (TextView) v;
		} else if (v instanceof EditText) {
			et = (EditText) et;
		}
		String stringYouExtracted = null;
		if (tv != null) {
			stringYouExtracted = tv.getText().toString();
		} else if (et != null) {
			stringYouExtracted = et.getText().toString();
		}

		copyToClipBoard(context, stringYouExtracted);
	}

	public static void copyToClipBoard(Context context,
			String stringYouExtracted) {

		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
			android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context
					.getSystemService(Context.CLIPBOARD_SERVICE);
			clipboard.setText(stringYouExtracted);
		} else {
			android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context
					.getSystemService(Context.CLIPBOARD_SERVICE);
			android.content.ClipData clip = android.content.ClipData
					.newPlainText("Copied Text", stringYouExtracted);
			clipboard.setPrimaryClip(clip);
		}

		Toast.makeText(context, R.string.copied, Toast.LENGTH_SHORT).show();
	}

}
