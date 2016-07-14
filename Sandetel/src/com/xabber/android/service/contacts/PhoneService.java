package com.xabber.android.service.contacts;

import java.util.List;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

import com.xabber.android.data.ContactManager;

public class PhoneService extends IntentService {

	private List<String> contacts;

	public PhoneService() {

		super("getPhones");
	}

	public PhoneService(String name) {
		super(name);
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		if (intent != null) {
			Bundle extras = intent.getExtras();
			if (extras != null) {
				contacts = extras.getStringArrayList("contacts");
			}

			ContactManager contactManager = new ContactManager(
					getApplicationContext());

			if (contacts != null && !contacts.isEmpty()) {
				contactManager.syncroContacts(contacts);

			}

		}

	}

}
