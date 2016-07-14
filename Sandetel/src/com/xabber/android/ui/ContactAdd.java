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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.ReportedData.Row;
import org.jivesoftware.smackx.search.UserSearchManager;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.xabber.android.data.Application;
import com.xabber.android.data.ContactManager;
import com.xabber.android.data.NetworkException;
import com.xabber.android.data.account.AccountManager;
import com.xabber.android.data.intent.EntityIntentBuilder;
import com.xabber.android.data.message.AbstractChat;
import com.xabber.android.data.message.MessageManager;
import com.xabber.android.data.roster.PresenceManager;
import com.xabber.android.data.roster.RosterContact;
import com.xabber.android.data.roster.RosterManager;
import com.xabber.android.data.roster.SubscriptionRequest;
import com.xabber.android.ui.adapter.AccountChooseAdapter;
import com.xabber.android.ui.adapter.AccountConfiguration;
import com.xabber.android.ui.dialog.ConfirmDialogBuilder;
import com.xabber.android.ui.dialog.ConfirmDialogListener;
import com.xabber.android.ui.dialog.DialogBuilder;
import com.xabber.android.utils.alpha.AlphaUtil;
import com.xabber.android.utils.async.AsyncTask;
import com.xabber.android.utils.dialogs.DialogGeneric;
import com.xabber.android.utils.dialogs.IDialogAction;
import com.xabber.android.utils.keyboard.KeyBoardUtil;

import es.juntadeandalucia.android.im.R;

public class ContactAdd extends GroupListActivity implements
		View.OnClickListener, ConfirmDialogListener, OnItemSelectedListener {

	/**
	 * Action for subscription request to be show.
	 * 
	 * Clear action on dialog dismiss.
	 */
	private static final String ACTION_SUBSCRIPTION_REQUEST = "com.xabber.android.data.SUBSCRIPTION_REQUEST";

	private static final String SAVED_ACCOUNT = "com.xabber.android.ui.ContactAdd.SAVED_ACCOUNT";
	private static final String SAVED_USER = "com.xabber.android.ui.ContactAdd.SAVED_USER";
	private static final String SAVED_NAME = "com.xabber.android.ui.ContactAdd.SAVED_NAME";

	private static final int DIALOG_SUBSCRIPTION_REQUEST_ID = 0x20;

	private String account;
	private String user;

	private SubscriptionRequest subscriptionRequest;

	/**
	 * Views
	 */
	private Spinner accountView;
	private EditText userView;
	private EditText nameView;
	private ProgressBar pbAdd;

	/**
	 * Accounts.
	 */
	final TreeMap<String, AccountConfiguration> accounts = new TreeMap<String, AccountConfiguration>();

	/**
	 * List of rooms and active chats grouped by users inside accounts.
	 */
	final TreeMap<String, TreeMap<String, AbstractChat>> abstractChats = new TreeMap<String, TreeMap<String, AbstractChat>>();

	@Override
	protected void onInflate(Bundle savedInstanceState) {
		setContentView(R.layout.contact_add);

		findViewById(R.id.iv_options).setVisibility(View.INVISIBLE);

		pbAdd = (ProgressBar) findViewById(R.id.pb_add);

		ListView listView = getListView();
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.contact_add_header, listView,
				false);
		listView.addHeaderView(view, null, false);

		accountView = (Spinner) view.findViewById(R.id.contact_account);
		accountView.setAdapter(new AccountChooseAdapter(this));
		accountView.setOnItemSelectedListener(this);
		userView = (EditText) view.findViewById(R.id.contact_user);
		nameView = (EditText) view.findViewById(R.id.contact_name);
		((Button) view.findViewById(R.id.ok)).setOnClickListener(this);

		TextView tv = (TextView) view.findViewById(android.R.id.message);
		tv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				showAddGroupDialog();
			}
		});

		ImageView ivBack = (ImageView) findViewById(R.id.iv_back);
		ivBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ContactAdd.this.finish();
			}
		});

		String name;
		Intent intent = getIntent();
		if (savedInstanceState != null) {
			account = savedInstanceState.getString(SAVED_ACCOUNT);
			user = savedInstanceState.getString(SAVED_USER);
			name = savedInstanceState.getString(SAVED_NAME);
		} else {
			account = getAccount(intent);
			user = getUser(intent);
			if (account == null || user == null)
				name = null;
			else {
				name = RosterManager.getInstance().getName(account, user);
				if (user.equals(name))
					name = null;
			}
		}
		if (account == null) {
			Collection<String> accounts = AccountManager.getInstance()
					.getAccounts();
			if (accounts.size() == 1)
				account = accounts.iterator().next();
		}
		if (account != null) {
			for (int position = 0; position < accountView.getCount(); position++)
				if (account.equals(accountView.getItemAtPosition(position))) {
					accountView.setSelection(position);
					break;
				}
		}
		if (user != null) {
			// ((TextView) findViewById(R.id.tv_titulo))
			// .setText(getString(R.string.contact_pending));

			((LinearLayout) findViewById(R.id.ll_contact_user))
					.setVisibility(View.VISIBLE);
			((TextView) findViewById(R.id.tv_contact_user)).setText(user);

			((LinearLayout) findViewById(R.id.ll_add_user))
					.setVisibility(View.GONE);

			((TextView) findViewById(R.id.tv_subtitulo))
					.setVisibility(View.GONE);

			((TextView) findViewById(android.R.id.message))
					.setVisibility(View.GONE);

			((TextView) findViewById(R.id.tv_rechazar_contact))
					.setVisibility(View.VISIBLE);
			((TextView) findViewById(R.id.tv_rechazar_contact))
					.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							try {
								PresenceManager.getInstance()
										.discardSubscription(
												subscriptionRequest
														.getAccount(),
												subscriptionRequest.getUser());
							} catch (NetworkException e) {
								Application.getInstance().onError(e);
							}
							finish();
						}
					});
			userView.setText(user);
		} else {

			((LinearLayout) findViewById(R.id.ll_contact_user))
					.setVisibility(View.GONE);
			((TextView) findViewById(R.id.tv_titulo))
					.setText(getString(R.string.contact_add));

			((LinearLayout) findViewById(R.id.ll_add_user))
					.setVisibility(View.VISIBLE);
			((TextView) findViewById(R.id.tv_subtitulo))
					.setVisibility(View.VISIBLE);
			((TextView) findViewById(android.R.id.message))
					.setVisibility(View.VISIBLE);
			((TextView) findViewById(R.id.tv_rechazar_contact))
					.setVisibility(View.GONE);
		}

		if (name != null)
			nameView.setText(name);
		if (ACTION_SUBSCRIPTION_REQUEST.equals(intent.getAction())) {
			subscriptionRequest = PresenceManager.getInstance()
					.getSubscriptionRequest(account, user);
			if (subscriptionRequest == null) {
				Application.getInstance().onError(R.string.ENTRY_IS_NOT_FOUND);
				finish();
				return;
			}
		} else {
			subscriptionRequest = null;
		}

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(SAVED_ACCOUNT,
				(String) accountView.getSelectedItem());
		outState.putString(SAVED_USER, userView.getText().toString());
		outState.putString(SAVED_NAME, nameView.getText().toString());
	}

	@Override
	protected void onResume() {
		super.onResume();
		// if (subscriptionRequest != null)

		// showDialog(DIALOG_SUBSCRIPTION_REQUEST_ID);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.ok:

			new ActionOk(view).execute();

			break;
		default:
			break;
		}
	}

	class ActionOk extends AsyncTask<Void, Void, Boolean> {

		private View button;

		public ActionOk(View button) {
			this.button = button;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			AlphaUtil.setCustomAlpha(button, 20);
			button.setOnClickListener(null);

			pbAdd.setVisibility(View.VISIBLE);

			KeyBoardUtil.showhide(getApplicationContext(), userView);
			KeyBoardUtil.showhide(getApplicationContext(), nameView);

		}

		@Override
		protected Boolean doInBackground(Void... params) {

			Boolean result = false;

			if (checkFields()) {

				result = true;
			}

			return result;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);

			if (result) {

				ContactManager contactManager = new ContactManager(
						getApplicationContext());
				List<String> contact = new ArrayList<String>();
				contact.add(userView.getText().toString()
						+ "@"
						+ AccountManager.getInstance().getAccount(account)
								.getConnectionSettings().getServerName());
				contactManager.syncroContacts(contact);

				pbAdd.setVisibility(View.GONE);
				button.setOnClickListener(ContactAdd.this);
				AlphaUtil.setCustomAlpha(button, 100);

				HashMap<String, Serializable> paramsDialog = new HashMap<String, Serializable>();
				paramsDialog.put(DialogGeneric.PARAMS_DIALOG_TITLE,
						getString(R.string.contact_add));
				paramsDialog.put(DialogGeneric.PARAMS_DIALOG_MESSAGE, String
						.format(getString(R.string.sure_add_contact), userView
								.getText().toString()));
				paramsDialog.put(DialogGeneric.PARAMS_DIALOG_POSITIVE_BUTTON,
						getString(R.string.aceptar));
				paramsDialog.put(DialogGeneric.PARAMS_DIALOG_NEGATIVE_BUTTON,
						getString(R.string.cancelar));

				IDialogAction iDialogAction = new IDialogAction() {

					@Override
					public void onPositiveButton() {
						actionOk();
					}

					@Override
					public void onNegativeButton() {

					}
				};
				paramsDialog.put(DialogGeneric.PARAMS_DIALOG_ACTION_INTERFACE,
						iDialogAction);

				DialogGeneric dialog = new DialogGeneric(ContactAdd.this,
						paramsDialog);

				dialog.createDialog();
				dialog.showDialog();
			} else {
				pbAdd.setVisibility(View.GONE);
				button.setOnClickListener(ContactAdd.this);
				AlphaUtil.setCustomAlpha(button, 100);
			}

		}

	}

	private Boolean checkFields() {

		String auxUser = userView.getText().toString();

		if ("".equals(auxUser)) {
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					Toast.makeText(ContactAdd.this,
							getString(R.string.EMPTY_USER_NAME),
							Toast.LENGTH_LONG).show();
				}
			});

			return false;
		}

		if (auxUser.length() < 8) {
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					Toast.makeText(ContactAdd.this,
							getString(R.string.LENGHT_USER_NAME),
							Toast.LENGTH_LONG).show();
				}
			});

			return false;
		}

		String userAux = "";

		if (this.user == null) {
			userAux = auxUser
					+ "@"
					+ AccountManager.getInstance().getAccount(account)
							.getConnectionSettings().getServerName();
		} else {
			userAux = auxUser;
		}

		String user = userAux;
		if ("".equals(user)) {
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					Toast.makeText(ContactAdd.this,
							getString(R.string.EMPTY_USER_NAME),
							Toast.LENGTH_LONG).show();
				}
			});

			return false;
		}
		String account = (String) accountView.getSelectedItem();
		if (account == null) {
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					Toast.makeText(ContactAdd.this,
							getString(R.string.EMPTY_ACCOUNT),
							Toast.LENGTH_LONG).show();
				}
			});

			return false;
		}
		if (account.split("@")[0].equals(user.split("@")[0])) {
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					Toast.makeText(ContactAdd.this,
							getString(R.string.own_account), Toast.LENGTH_LONG)
							.show();
				}
			});

			return false;
		}

		if (userExist(user.split("@")[0])) {

			if (subscriptionRequest != null)
				try {
					PresenceManager.getInstance().acceptSubscription(
							subscriptionRequest.getAccount(),
							subscriptionRequest.getUser());
				} catch (NetworkException e) {
					Application.getInstance().onError(e);
				}
			getIntent().setAction(null);

			// Obtenemos todos los contactos del usuario
			Collection<RosterContact> contacts = RosterManager.getInstance()
					.getContacts();

			// Comparamos con el nuevo usuario para comprobar si ya está en
			// la
			// lista
			String userAdd = userView.getText().toString();
			for (RosterContact rosterContact : contacts) {

				String userDDBB = rosterContact.getUser();
				if (userDDBB != null) {
					userDDBB = userDDBB.split("@")[0];
				}

				if (userAdd != null && userDDBB != null
						&& userAdd.equalsIgnoreCase(userDDBB)) {
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							Toast.makeText(ContactAdd.this,
									getString(R.string.contact_exists),
									Toast.LENGTH_LONG).show();
						}
					});

					return false;
				}

			}
		} else {
			return false;
		}

		this.user = user;
		return true;

	}

	private void actionOk() {

		try {

			RosterManager.getInstance().createContact(account, user,
					nameView.getText().toString(), getSelected());
			PresenceManager.getInstance().requestSubscription(account, user);
		} catch (NetworkException e) {
			Application.getInstance().onError(e);
			finish();
			return;
		}

		Toast.makeText(getApplicationContext(),
				getString(R.string.contact_add_succesful), Toast.LENGTH_SHORT)
				.show();
		MessageManager.getInstance().openChat(account, user);
		finish();

	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = super.onCreateDialog(id);
		if (dialog != null)
			return dialog;
		switch (id) {
		case DIALOG_SUBSCRIPTION_REQUEST_ID:
			return new ConfirmDialogBuilder(this,
					DIALOG_SUBSCRIPTION_REQUEST_ID, this).setMessage(
					subscriptionRequest.getConfirmation()).create();
		default:
			return null;
		}
	}

	@Override
	public void onAccept(DialogBuilder dialogBuilder) {
		super.onAccept(dialogBuilder);
		switch (dialogBuilder.getDialogId()) {
		case DIALOG_SUBSCRIPTION_REQUEST_ID:
			try {
				PresenceManager.getInstance().acceptSubscription(
						subscriptionRequest.getAccount(),
						subscriptionRequest.getUser());
			} catch (NetworkException e) {
				Application.getInstance().onError(e);
			}
			getIntent().setAction(null);
			break;
		}
	}

	@Override
	public void onDecline(DialogBuilder dialogBuilder) {
		super.onDecline(dialogBuilder);
		switch (dialogBuilder.getDialogId()) {
		case DIALOG_SUBSCRIPTION_REQUEST_ID:
			try {
				PresenceManager.getInstance().discardSubscription(
						subscriptionRequest.getAccount(),
						subscriptionRequest.getUser());
			} catch (NetworkException e) {
				Application.getInstance().onError(e);
			}
			finish();
			break;
		}
	}

	@Override
	public void onCancel(DialogBuilder dialogBuilder) {
		super.onCancel(dialogBuilder);
		switch (dialogBuilder.getDialogId()) {
		case DIALOG_SUBSCRIPTION_REQUEST_ID:
			finish();
			break;
		}
	}

	@Override
	Collection<String> getInitialGroups() {
		String account = (String) accountView.getSelectedItem();
		if (account == null)
			return Collections.emptyList();
		return RosterManager.getInstance().getGroups(account);
	}

	@Override
	Collection<String> getInitialSelected() {
		return Collections.emptyList();
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		String account = (String) accountView.getSelectedItem();
		if (account == null) {
			onNothingSelected(parent);
		} else {
			HashSet<String> groups = new HashSet<String>(RosterManager
					.getInstance().getGroups(account));
			groups.addAll(getSelected());
			setGroups(groups, getSelected());
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		setGroups(getSelected(), getSelected());
	}

	public static Intent createIntent(Context context) {
		return createIntent(context, null);
	}

	private static Intent createIntent(Context context, String account,
			String user) {
		return new EntityIntentBuilder(context, ContactAdd.class)
				.setAccount(account).setUser(user).build();
	}

	public static Intent createIntent(Context context, String account) {
		return createIntent(context, account, null);
	}

	public static Intent createSubscriptionIntent(Context context,
			String account, String user) {
		Intent intent = createIntent(context, account, user);
		intent.setAction(ACTION_SUBSCRIPTION_REQUEST);
		return intent;
	}

	private static String getAccount(Intent intent) {
		return EntityIntentBuilder.getAccount(intent);
	}

	private static String getUser(Intent intent) {
		return EntityIntentBuilder.getUser(intent);
	}

	private boolean userExist(String usuario) {
		XMPPConnection mXMPPConnection = AccountManager.getInstance()
				.getAccount(account).getConnectionThread().getXMPPConnection();

		UserSearchManager search = new UserSearchManager(mXMPPConnection);
		Form searchForm = null;
		try {
			searchForm = search.getSearchForm("vjud."
					+ mXMPPConnection.getServiceName());
		} catch (XMPPException e) {
			e.printStackTrace();
		}
		if (searchForm != null) {

			String searchUser = usuario + "@"
					+ mXMPPConnection.getServiceName();
			Form answerForm = searchForm.createAnswerForm();
			answerForm.setAnswer("uid", usuario);
			org.jivesoftware.smackx.ReportedData data = null;
			try {
				data = search.getSearchResults(answerForm, "vjud."
						+ mXMPPConnection.getServiceName());
			} catch (XMPPException e) {
				e.printStackTrace();
			}
			if (data.getRows() != null) {
				Iterator<Row> it = data.getRows();
				while (it.hasNext()) {
					Row row = it.next();
					Iterator iterator = row.getValues("jid");
					if (iterator.hasNext()) {
						String element = iterator.next().toString();

						if (element != null && searchUser != null
								&& element.equals(searchUser)) {
							return true;
						}

					}

				}

			}

			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					Toast.makeText(getApplicationContext(),
							R.string.no_user_exist, Toast.LENGTH_LONG).show();
				}
			});

			return false;
		}

		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(getApplicationContext(),
						"sin conexion servidor", Toast.LENGTH_LONG).show();
			}
		});

		return false;
	}
}
