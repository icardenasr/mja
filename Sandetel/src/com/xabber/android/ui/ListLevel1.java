package com.xabber.android.ui;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ListView;

public class ListLevel1 extends ListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {

			ListView lv = getListView();

		}

	}

}
