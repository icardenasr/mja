package com.xabber.android.service.contacts;

import java.util.Collection;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

import com.xabber.android.data.account.AccountItem;
import com.xabber.android.data.account.AccountManager;
import com.xabber.android.data.extension.vcard.VCardManager;

public class VcardService extends IntentService {

	public VcardService() {

		super("updateVCards");
	}

	public VcardService(String name) {
		super(name);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (intent != null) {
			Bundle extras = intent.getExtras();
			if (extras != null) {
			}

			String accountItem = null;
			Collection<String> accounts = AccountManager.getInstance()
					.getAccounts();
			if (accounts != null && !accounts.isEmpty()) {
				for (String account : accounts) {
					accountItem = account;
				}
			}

			AccountItem account = AccountManager.getInstance().getAccount(
					accountItem);

			VCardManager.getInstance().updateAllContact(account);

		}

	}

}
