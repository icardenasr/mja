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
package com.xabber.android.ui;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.xabber.android.data.Application;
import com.xabber.android.data.ContactManager;
import com.xabber.android.data.NetworkException;
import com.xabber.android.data.account.AccountManager;
import com.xabber.android.data.account.OnAccountChangedListener;
import com.xabber.android.data.entity.BaseEntity;
import com.xabber.android.data.intent.EntityIntentBuilder;
import com.xabber.android.data.roster.AbstractContact;
import com.xabber.android.data.roster.OnContactChangedListener;
import com.xabber.android.data.roster.RosterManager;
import com.xabber.android.utils.alpha.AlphaUtil;
import com.xabber.android.utils.async.AsyncTask;
import com.xabber.android.utils.dialogs.DialogGeneric;
import com.xabber.android.utils.dialogs.IDialogAction;
import com.xabber.xmpp.address.Jid;

import es.juntadeandalucia.android.im.R;

public class ContactEditor extends GroupListActivity implements
		OnContactChangedListener, AdapterView.OnItemClickListener,
		OnAccountChangedListener, OnClickListener {

	// Contacto contacto;
	private List<String> phones;

	private String account;
	private String user;

	private Button exitButton;

	private View vEmail, tvEmail, vPhone, tvPhone;

	private AbstractContact abstractContact;

	private ContactManager contactManager;

	@Override
	protected void onInflate(Bundle savedInstanceState) {
		setContentView(R.layout.contact_editor);

		View header = getLayoutInflater().inflate(
				R.layout.contact_editor_header, null);
		View footer = getLayoutInflater().inflate(
				R.layout.contact_editor_footer, null);

		Intent intent = getIntent();
		account = ContactEditor.getAccount(intent);
		user = ContactEditor.getUser(intent);
		if (AccountManager.getInstance().getAccount(account) == null
				|| user == null) {
			Application.getInstance().onError(R.string.ENTRY_IS_NOT_FOUND);
			finish();
			return;
		}

		abstractContact = RosterManager.getInstance().getBestContact(account,
				user);

		TextView tv = (TextView) findViewById(R.id.tv_titulo);
		tv.setText(getString(R.string.editar_perfil));

		vEmail = header.findViewById(R.id.ll_email);
		vEmail.setOnClickListener(this);
		tvEmail = header.findViewById(R.id.tv_email);
		((TextView) tvEmail).setText(abstractContact.getUser());
		vPhone = header.findViewById(R.id.ll_tel);
		AlphaUtil.setCustomAlpha(vPhone, 20);
		tvPhone = header.findViewById(R.id.tv_tel);
		((TextView) tvPhone).setText(getString(R.string.no_phone));

		
		contactManager = new ContactManager(ContactEditor.this);
		new GetPhoneNumers().execute();

		exitButton = (Button) footer.findViewById(R.id.button_exit);
		exitButton.setOnClickListener(new OnClickListener() {
			public void onClick(View thisButton) {

				HashMap<String, Serializable> params = new HashMap<String, Serializable>();
				params.put(DialogGeneric.PARAMS_DIALOG_TITLE,
						getString(R.string.save_contact));
				params.put(DialogGeneric.PARAMS_DIALOG_MESSAGE,
						getString(R.string.sure_save_contact));
				params.put(DialogGeneric.PARAMS_DIALOG_POSITIVE_BUTTON,
						getString(R.string.aceptar));
				params.put(DialogGeneric.PARAMS_DIALOG_NEGATIVE_BUTTON,
						getString(R.string.cancelar));

				IDialogAction iDialogAction = new IDialogAction() {

					@Override
					public void onPositiveButton() {
						saveUser();
						finish();
					}

					@Override
					public void onNegativeButton() {

					}
				};
				params.put(DialogGeneric.PARAMS_DIALOG_ACTION_INTERFACE,
						iDialogAction);

				DialogGeneric dialog = new DialogGeneric(ContactEditor.this,
						params);

				dialog.createDialog();
				dialog.showDialog();

			}
		});
		
		getListView().addHeaderView(header);
		getListView().addFooterView(footer);

		

	}

	@Override
	Collection<String> getInitialGroups() {
		return RosterManager.getInstance().getGroups(account);
	}

	@Override
	Collection<String> getInitialSelected() {
		return RosterManager.getInstance().getGroups(account, user);
	}

	@Override
	protected void onResume() {
		super.onResume();
		((EditText) findViewById(R.id.contact_name)).setText(RosterManager
				.getInstance().getName(account, user));
		Application.getInstance().addUIListener(OnAccountChangedListener.class,
				this);
		Application.getInstance().addUIListener(OnContactChangedListener.class,
				this);
		update();
	}

	@Override
	protected void onPause() {
		super.onPause();
		Application.getInstance().removeUIListener(
				OnAccountChangedListener.class, this);
		Application.getInstance().removeUIListener(
				OnContactChangedListener.class, this);

	}

	private void update() {

		// ContactTitleInflater.updateTitle(findViewById(R.id.title), this,
		// abstractContact);
		// ((TextView) findViewById(R.id.name)).setText(getString(
		// R.string.contact_editor_title, abstractContact.getName()));
		// ((TextView) findViewById(R.id.status_text)).setText(abstractContact
		// .getName());

		Drawable d = abstractContact.getAvatarForContactList();

		ImageView quiContactAvatar = ((ImageView) findViewById(R.id.avatar));
		if (d != null) {
			quiContactAvatar.setImageDrawable(abstractContact
					.getAvatarForContactList());
		} else {
			quiContactAvatar.setImageResource(R.drawable.ico_user);
		}

		// new GetPhoneNumers().execute();
	}

	@Override
	public void onContactsChanged(Collection<BaseEntity> entities) {
		String thisBareAddress = Jid.getBareAddress(user);
		for (BaseEntity entity : entities)
			if (entity.equals(account, thisBareAddress)) {
				update();
				break;
			}
	}

	@Override
	public void onAccountsChanged(Collection<String> accounts) {
		if (accounts.contains(account))
			update();
	}

	public static Intent createIntent(Context context, String account,
			String user) {
		Intent intent = new EntityIntentBuilder(context, ContactEditor.class)
				.setAccount(account).setUser(user).build();
		intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		return intent;
	}

	private static String getAccount(Intent intent) {
		return EntityIntentBuilder.getAccount(intent);
	}

	private static String getUser(Intent intent) {
		return EntityIntentBuilder.getUser(intent);
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.ll_email:

			contactManager.sendEmail(user);

			break;
		case R.id.ll_tel:
			if (phones != null && phones.size() > 0) {
				if (phones.size() > 1) {
					contactManager.createDialogPhones(phones);
				} else {
					contactManager.callPhone(phones.get(0));
				}
			}

			break;

		default:
			break;
		}
	}

	class GetPhoneNumers extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {

			// contacto = AgendaUtil.getContactByEmail(ContactEditor.this,
			// abstractContact.getUser());
			//
			// // contactManager.addContact(abstractContact);
			//
			// contacto = contactManager.getContactByEmail(abstractContact

			if (contactManager != null && abstractContact != null) {
				// .getUser());
				phones = contactManager.getPhonesToPref(abstractContact
						.getUser());
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			setViewPhone();

		}

	}

	private void setViewPhone() {

		if (phones != null && phones.size() > 0) {
			String chain = null;
			if (phones.size() > 1) {
				chain = getString(R.string.see_phones);
			} else {
				chain = phones.get(0);
			}

			AlphaUtil.setCustomAlpha(vPhone, 100);
			((TextView) tvPhone).setText(chain);
			vPhone.setOnClickListener(this);
		} else {
			AlphaUtil.setCustomAlpha(vPhone, 20);
			vPhone.setOnClickListener(null);
			((TextView) tvPhone).setText(getString(R.string.no_phone));
		}

	}

	private void saveUser() {

		try {
			String name = ((EditText) findViewById(R.id.contact_name))
					.getText().toString();
			RosterManager.getInstance().setNameAndGroup(account, user, name,
					getSelected());
		} catch (NetworkException e) {
			Application.getInstance().onError(e);
		}

	}

}
