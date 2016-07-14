package com.xabber.android.ui.helper;

import android.app.Activity;

import com.google.analytics.tracking.android.EasyTracker;

public class ManagedBaseActivity extends Activity {

	@Override
	protected void onStart() {
		EasyTracker.getInstance().activityStart(this);

		super.onStart();
	}

	@Override
	protected void onStop() {

		EasyTracker.getInstance().activityStop(this);
		super.onStop();
	}

}
