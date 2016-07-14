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
package com.xabber.android.ui.adapter;

import java.util.HashMap;

import android.app.Activity;
import android.widget.ListView;

import com.xabber.android.data.entity.BaseEntity;
import com.xabber.android.data.roster.RosterContact;
import com.xabber.android.ui.ContactList;

/**
 * Enable smooth scrollbar depend on number of entities.
 * 
 * @author alexander.ivanov
 * 
 * @param <Inflater>
 */
public abstract class SmoothContactAdapter<Inflater extends BaseContactInflater>
		extends BaseContactAdapter<Inflater> {

	/**
	 * Minimum number of item when smooth scroll bar will be disabled.
	 */
	private static final int SMOOTH_SCROLLBAR_LIMIT = 20;

	/**
	 * Managed list view.
	 */
	ListView listView;
	Activity activity;

	public SmoothContactAdapter(Activity activity, ListView listView,
			Inflater inflater) {
		super(activity, inflater);
		this.listView = listView;
		this.activity = activity;
	}

	@Override
	public void onChange() {
		super.onChange();
		listView.setSmoothScrollbarEnabled(baseEntities.size() < SMOOTH_SCROLLBAR_LIMIT);

		// LinkedList<String> cuentas=new LinkedList<String>();

		HashMap<String, Boolean> cuentas = new HashMap<String, Boolean>();

		int numContact = 0;
		BaseEntity baseEntity = null;
		for (BaseEntity baseEntityAux : baseEntities) {
			if (baseEntityAux instanceof RosterContact) {
				baseEntity = baseEntityAux;

				String cuenta = baseEntity.getAccount();
				if (!cuentas.containsKey(cuenta)) {
					cuentas.put(cuenta, true);
					numContact++;
				}
			}
		}
		//
		if (numContact == 1) {
			if (activity instanceof ContactList) {

				((ContactList) activity).goToChatOnContact(baseEntity);
			}

		}
	}

}
