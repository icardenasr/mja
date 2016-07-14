package com.xabber.android.data;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import com.xabber.android.utils.preferences.UtilPreferences;

import es.juntadeandalucia.android.im.R;

public class ContactManager {

	private Context context;

	public ContactManager(Context context) {
		this.context = context;
	}

	private List<ContactObject> displayAllContacts(Context context) {

		List<ContactObject> contacts = new ArrayList<ContactObject>();

		ContactObject contactObject = null;
		ContentResolver cr = context.getContentResolver();
		Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null,
				null, null, null);
		if (cur.getCount() > 0) {
			while (cur.moveToNext()) {

				String id, name;
				List<String> phones = new ArrayList<String>(), emails = new ArrayList<String>();

				id = cur.getString(cur
						.getColumnIndex(ContactsContract.Contacts._ID));
				name = cur
						.getString(cur
								.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
				if (Integer
						.parseInt(cur.getString(cur
								.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
					Cursor pCur = cr.query(
							ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
							null,
							ContactsContract.CommonDataKinds.Phone.CONTACT_ID
									+ " = ?", new String[] { id }, null);
					while (pCur.moveToNext()) {
						String phoneNo = pCur
								.getString(pCur
										.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
						if (phoneNo != null) {
							phones.add(phoneNo);
						}
						// System.out.println("Name: " + name + ", Phone No: "
						// + phoneNo);
					}
					pCur.close();
				}

				Cursor eCur = cr.query(
						ContactsContract.CommonDataKinds.Email.CONTENT_URI,
						null, ContactsContract.CommonDataKinds.Email.CONTACT_ID
								+ " = ?", new String[] { id }, null);
				while (eCur.moveToNext()) {
					String email = eCur
							.getString(eCur
									.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
					if (email != null) {
						emails.add(email);
					}
					// System.out
					// .println("Name: " + name + ", Email No: " + email);
				}
				eCur.close();

				contactObject = new ContactObject(id, name, phones, emails);
				if (contactObject != null) {
					contacts.add(contactObject);
				}
			}

		}
		return contacts;
	}

	/**
	 * Convierte un List de teléfonos a un Array
	 * 
	 * @param phones
	 * @return
	 */
	private String[] convertPhonesToArray(List<String> phones) {

		ArrayList<String> stringArrayList = new ArrayList<String>();

		if (phones != null) {
			for (int i = 0; i < phones.size(); i++) {
				stringArrayList.add(phones.get(i));

			}
		}

		String[] stringArray = stringArrayList
				.toArray(new String[stringArrayList.size()]);

		return stringArray;
	}

	/**
	 * Prepara el listado de teléfonos en una cadena para guardarla en las
	 * preferencias
	 * 
	 * @param phones
	 * @return
	 */
	private String prepareToSavePhones(List<String> phones) {
		String reducedPhone = "";

		if (phones != null) {
			for (int i = 0; i < phones.size(); i++) {
				reducedPhone = reducedPhone + phones.get(i) + ",";
			}
		}

		if (reducedPhone.length() > 0) {
			reducedPhone = reducedPhone.subSequence(0,
					reducedPhone.length() - 1).toString();
		}

		return reducedPhone;
	}

	/**
	 * Guarda los teléfonos de un contacto en preferencias
	 * 
	 * @param user
	 * @param phones
	 */
	private void savePhonesToPref(String user, String phones) {

		UtilPreferences.setPreferences(context, user + "_phones", phones);

	}

	/**
	 * Obtiene los teléfonos de un usuario de preferencias
	 * 
	 * @param user
	 * @return
	 */
	private List<String> getToPref(String user) {
		String[] phones;
		String phoneToPref = (String) UtilPreferences.getPreferences(context,
				user + "_phones", String.class);
		phones = phoneToPref.split(",");
		List<String> phoneToUser = new ArrayList<String>();

		if (phones != null && phones.length > 0) {
			for (int i = 0; i < phones.length; i++) {
				if (phones[i] != null && !phones[i].equalsIgnoreCase("")) {
					phoneToUser.add(phones[i]);
				}
			}
		}

		return phoneToUser;

	}

	/**
	 * Obtiene los teléfonos de un usuario de preferencias
	 * 
	 * @param user
	 * @return
	 */
	public List<String> getPhonesToPref(String user) {

		List<String> phones = getToPref(user);

		return phones;

	}

	/**
	 * Proceso de sincronización de los contactos
	 * 
	 * @param contacts
	 */
	public void syncroContacts(List<String> contacts) {
		// Si hay usuarios en el chat
		if (contacts != null && !contacts.isEmpty()) {
			List<ContactObject> contactsDDBB = displayAllContacts(context);
			// Si hay usuarios en la agenda
			if (contactsDDBB != null && !contactsDDBB.isEmpty()) {
				// Recorremos los contactos del chat
				for (String contacto : contacts) {
					// Recorremos los contactos de la agenda
					for (ContactObject agendaContact : contactsDDBB) {

						// Comprobamos si tienen número de teléfono
						if (agendaContact.getPhones() != null
								&& !agendaContact.getPhones().isEmpty()) {
							// Comprobamos si el usuario de la agenda tiene
							// email
							if (agendaContact.getEmails() != null
									&& !agendaContact.getEmails().isEmpty()) {
								// Recorremos los emails del usuario de la
								// agenda
								for (String email : agendaContact.getEmails()) {
									// Si el email conincide con el buscado
									if (contacto.equalsIgnoreCase(email)) {

										// guardamos en preferencias los
										// teléfono
										String phonesToPref = prepareToSavePhones(agendaContact
												.getPhones());
										if (phonesToPref != null
												&& !phonesToPref
														.equalsIgnoreCase("")) {
											savePhonesToPref(contacto,
													phonesToPref);
										}
										break;

									}
								}
							}
						}

					}
				}

			}
		}
	}

	// /**
	// * Proceso de sincronización de los contactos
	// *
	// * @param contacts
	// */
	// public void syncroContacts2(List<String> contacts) {
	//
	// // Si hay usuarios en el chat
	// if (contacts != null && !contacts.isEmpty()) {
	// List<Contacto> AgendaContacts = AgendaUtil.readContacts(context);
	// // Si hay usuarios en la agenda
	// if (AgendaContacts != null && !AgendaContacts.isEmpty()) {
	// // Recorremos los contactos del chat
	// for (String contacto : contacts) {
	// // Recorremos los contactos de la agenda
	// for (Contacto agendaContact : AgendaContacts) {
	// // Comprobamos si tienen número de teléfono
	// if (agendaContact.getTelefonoList() != null
	// && !agendaContact.getTelefonoList().isEmpty()) {
	// // Comprobamos si el usuario de la agenda tiene
	// // email
	// if (agendaContact.getEmailList() != null
	// && !agendaContact.getEmailList().isEmpty()) {
	// // Recorremos los emails del usuario de la
	// // agenda
	// for (String email : agendaContact
	// .getEmailList()) {
	// // Si el email conincide con el buscado
	// if (contacto.equalsIgnoreCase(email)) {
	// // guardamos en preferencias los
	// // teléfono
	// String phonesToPref = prepareToSavePhones(agendaContact
	// .getTelefonoList());
	// if (phonesToPref != null
	// && !phonesToPref
	// .equalsIgnoreCase("")) {
	// savePhonesToPref(contacto,
	// phonesToPref);
	// }
	// break;
	// }
	// }
	// }
	// }
	// }
	// }
	// }
	// }
	// }

	/**
	 * Action call phone
	 * 
	 * @param phone
	 */
	public void callPhone(String phone) {

		Intent callIntent = new Intent(Intent.ACTION_VIEW);
		callIntent.setData(Uri.parse("tel:" + phone));
		context.startActivity(callIntent);

	}

	/**
	 * Action send email
	 * 
	 * @param email
	 */
	public void sendEmail(String email) {

		Intent intent = new Intent(android.content.Intent.ACTION_SENDTO);
		intent.setType("text/plain");
		intent.setData(Uri.parse("mailto:"));
		intent.putExtra(Intent.EXTRA_EMAIL, new String[] { email });
		context.startActivity(Intent.createChooser(intent,
				context.getString(R.string.send_email)));

	}

	/**
	 * Crea un diálogo de selección de télefonos
	 * 
	 * @param phones
	 */
	public void createDialogPhones(final List<String> phones) {

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(R.string.call_to).setItems(
				convertPhonesToArray(phones),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						callPhone(phones.get(which));
					}
				});
		builder.create().show();

	}
}
