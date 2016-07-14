package com.xabber.android.data;

import java.util.ArrayList;
import java.util.List;

public class ContactObject {
	private String id;
	private String name;
	private List<String> phones;
	private List<String> emails;

	public ContactObject() {
	}

	public ContactObject(String id, String name, List<String> phones,
			List<String> emails) {
		this.id = id;
		this.name = name;
		if (phones != null) {
			this.phones = new ArrayList<String>(phones);
		}
		if (emails != null) {
			this.emails = new ArrayList<String>(emails);
		}
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getPhones() {
		return phones;
	}

	public void setPhones(List<String> phones) {
		this.phones = phones;
	}

	public List<String> getEmails() {
		return emails;
	}

	public void setEmails(List<String> emails) {
		this.emails = emails;
	}

}
