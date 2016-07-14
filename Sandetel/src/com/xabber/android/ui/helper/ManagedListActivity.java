/**
 * Copyright (c) 2013, Redsolution LTD. All rights reserved.
 * 
 * This file is part of Xabber project; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License, Version 3.
 * 
 * Xabber is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License,
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package com.xabber.android.ui.helper;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Adapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.xabber.android.data.ActivityManager;

import es.juntadeandalucia.android.im.R;

/**
 * Base class for all ListActivities.
 * 
 * Adds custom activity logic.
 * 
 * @author alexander.ivanov
 * 
 */
public abstract class ManagedListActivity extends ManagedBaseActivity {

	protected Adapter listAdapter;

	protected ListView listView;

	// protected PullToRefreshListView listViewPull;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		ActivityManager.getInstance().onCreate(this);
		super.onCreate(savedInstanceState);

		listView = (ListView) findViewById(R.id.list);
	}

	@Override
	protected void onResume() {
		ActivityManager.getInstance().onResume(this);
		super.onResume();
	}

	@Override
	protected void onPause() {
		ActivityManager.getInstance().onPause(this);
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		ActivityManager.getInstance().onDestroy(this);
		super.onDestroy();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		ActivityManager.getInstance().onNewIntent(this, intent);
		super.onNewIntent(intent);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		ActivityManager.getInstance().onActivityResult(this, requestCode,
				resultCode, data);
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void startActivity(Intent intent) {
		ActivityManager.getInstance().updateIntent(this, intent);
		super.startActivity(intent);
	}

	@Override
	public void startActivityForResult(Intent intent, int requestCode) {
		ActivityManager.getInstance().updateIntent(this, intent);
		super.startActivityForResult(intent, requestCode);
	}

	public Adapter getListAdapter() {
		return listAdapter;
	}

	public void setListAdapter(ListAdapter listAdapter) {
		this.listAdapter = listAdapter;

		if (this.listAdapter != null) {
			ListView lv = getListView();
			if (lv != null) {
				lv.setAdapter(listAdapter);
			}
		}
	}

	public ListView getListView() {

		if (listView == null) {

			View o = findViewById(R.id.list);

			// if (o instanceof PullToRefreshListView) {
			// listViewPull = (PullToRefreshListView) o;
			// listView = ((PullToRefreshListView) o).getRefreshableView();
			// } else {

			listView = (ListView) findViewById(R.id.list);
			// }
		}

		return listView;
	}

	//
	// public PullToRefreshListView getListViewPull() {
	// return listViewPull;
	// }

	public void setListView(ListView listView) {
		this.listView = listView;
	}
}
