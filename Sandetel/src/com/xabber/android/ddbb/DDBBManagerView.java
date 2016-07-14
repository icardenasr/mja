package com.xabber.android.ddbb;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.xabber.android.data.Application;
import com.xabber.android.ui.own.LoginAct;

public class DDBBManagerView extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Uri uriFile = getIntent().getData();

		LoginAct loginAct = ((Application) getApplication()).getLoginAct();
		if (loginAct != null) {
			loginAct.actionImportOrLoged(uriFile);
		} else {
			Intent i = new Intent(this, LoginAct.class);
			if (uriFile != null) {
				i.putExtra(LoginAct.EXTRA_URI_IMPORT, uriFile.toString());
			}
			i.setAction("android.intent.action.MAIN");
			i.addCategory("android.intent.category.LAUNCHER");
			i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);

		}
		finish();
	}

}
