package com.xabber.android.utils.agenda;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Contacts.Data;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.widget.Toast;

import com.xabber.android.utils.version.VersionUtil;

public class AgendaUtil implements LoaderManager.LoaderCallbacks<Cursor> {

	private Context context; // Context of application
	private String mSearchTerm; // Stores the current search query term
	private List<Contacto> contacts;

	public AgendaUtil(Context ctx) {

		this.context = ctx;

	}

	public AgendaUtil(Context context, String mSearchTerm) {

		this.context = context;
		this.mSearchTerm = mSearchTerm;

		contacts = new ArrayList<Contacto>();

	}

	public static List<Contacto> getContactByNumber(Context context,
			String number) {
		Uri uri = Uri.withAppendedPath(
				ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
				Uri.encode(number));

		List<Contacto> listContactos = new ArrayList<Contacto>();
		List<String> listTelefonos = new ArrayList<String>();
		List<String> listEmail = new ArrayList<String>();
		Contacto contactoAgenda;

		ContentResolver contentResolver = context.getContentResolver();
		Cursor contactLookup = contentResolver.query(uri, new String[] {
				BaseColumns._ID, ContactsContract.PhoneLookup.DISPLAY_NAME },
				null, null, null);

		try {
			if (contactLookup != null && contactLookup.getCount() > 0) {
				contactLookup.moveToNext();

				contactoAgenda = new Contacto();
				String nombre = contactLookup.getString(contactLookup
						.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
				if (nombre != null)
					contactoAgenda.setNombre(nombre);

				String contactId = contactLookup.getString(contactLookup
						.getColumnIndex(BaseColumns._ID));
				if (contactId != null)
					contactoAgenda.setId(Integer.valueOf(contactId));

				// Recorremos la lista de telefonos del contacto y los
				// añadimos a su lista
				Cursor pCur = contentResolver.query(
						ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
						null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID
								+ " = ?", new String[] { contactId }, null);
				if (pCur != null && pCur.getCount() > 0) {
					String phone = "";
					while (pCur.moveToNext()) {
						phone = pCur
								.getString(pCur
										.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
						listTelefonos.add(phone);
					}
					pCur.close();
				}

				// Recorremos los emails del contacto y los añadimos al
				// objeto
				Cursor emailCur = contentResolver.query(
						ContactsContract.CommonDataKinds.Email.CONTENT_URI,
						null, ContactsContract.CommonDataKinds.Email.CONTACT_ID
								+ " = ?", new String[] { contactId }, null);
				String email = "";
				if (emailCur != null && emailCur.getCount() > 0) {
					while (emailCur.moveToNext()) {

						email = emailCur
								.getString(emailCur
										.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
						listEmail.add(email);

					}
					emailCur.close();
				}

				// Añadimos los telefonos
				contactoAgenda.setEmailList(listEmail);

				// Añadimos los telefonos
				contactoAgenda.setTelefonoList(listTelefonos);
				// Añadimos el contacto
				listContactos.add(contactoAgenda);

			}
		} finally {
			if (contactLookup != null) {
				contactLookup.close();
			}
		}

		return listContactos;
	}

	public static Contacto getContactByEmail(Context context, String email) {
		List<Contacto> aux = new ArrayList<Contacto>(), listContactos = new ArrayList<Contacto>();
		aux = readContacts(context);

		if (aux != null && aux.size() > 0) {
			for (Contacto contacto : aux) {

				if (contacto.getEmailList() != null) {
					if (contacto.getEmailList().contains(email)) {
						listContactos.add(contacto);
						break;
					}
				}
			}
		}
		Contacto contacto = null;
		if (listContactos != null && listContactos.size() > 0) {
			contacto = listContactos.get(0);
		}

		return contacto;
	}

	public static List<Contacto> getContactsByEmails(Context context,
			List<String> emails) {
		List<Contacto> aux = new ArrayList<Contacto>(), listContactos = new ArrayList<Contacto>();
		aux = readContacts(context);

		if (aux != null && aux.size() > 0) {

			for (String email : emails) {

				for (Contacto contacto : aux) {

					if (contacto.getEmailList() != null) {
						if (contacto.getEmailList().contains(email)) {
							listContactos.add(contacto);

							break;
						}
					}
				}
			}
		}

		return listContactos;
	}

	public static List<Contacto> readContacts(Context context) {

		List<Contacto> listContactos = new ArrayList<Contacto>();
		List<String> listTelefonos = new ArrayList<String>();
		List<String> listEmail = new ArrayList<String>();
		Contacto contactoAgenda;

		// Recorremos todos los contactos de la agenda
		ContentResolver cr = context.getContentResolver();
		Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null,
				null, null, null);

		if (cur.getCount() > 0) {
			while (cur.moveToNext()) {

				if (Integer
						.parseInt(cur.getString(cur
								.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {

					listTelefonos = new ArrayList<String>();
					listEmail = new ArrayList<String>();

					// Obtenemos el id y el nombre del contacto
					String id = cur.getString(cur
							.getColumnIndex(ContactsContract.Contacts._ID));
					String name = cur
							.getString(cur
									.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

					contactoAgenda = new Contacto();
					contactoAgenda.setId(Integer.valueOf(id));
					contactoAgenda.setNombre(name);

					// Recorremos la lista de telefonos del contacto y los
					// añadimos a su lista
					Cursor pCur = cr.query(
							ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
							null,
							ContactsContract.CommonDataKinds.Phone.CONTACT_ID
									+ " = ?", new String[] { id }, null);
					if (pCur != null && pCur.getCount() > 0) {
						String phone = "";
						while (pCur.moveToNext()) {
							phone = pCur
									.getString(pCur
											.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
							listTelefonos.add(phone);
						}

					}
					if (pCur != null)
					{
						pCur.close();
					}

					// Recorremos los emails del contacto y los añadimos al
					// objeto
					Cursor emailCur = cr.query(
							ContactsContract.CommonDataKinds.Email.CONTENT_URI,
							null,
							ContactsContract.CommonDataKinds.Email.CONTACT_ID
									+ " = ?", new String[] { id }, null);
					String email = "";
					if (emailCur != null && emailCur.getCount() > 0) {
						while (emailCur.moveToNext()) {

							email = emailCur
									.getString(emailCur
											.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
							listEmail.add(email);

						}

					}
					if (emailCur != null) {
						emailCur.close();
					}

					// Añadimos los telefonos
					contactoAgenda.setEmailList(listEmail);

					// Añadimos los telefonos
					contactoAgenda.setTelefonoList(listTelefonos);
					// Añadimos el contacto
					listContactos.add(contactoAgenda);
				}
			}
		}

		cur.close();
		return listContactos;
	}

	public Boolean editContact(Contacto contactoDTO) {
		Boolean result = false;
		if (context != null) {
			if (contactoDTO != null) {

				String name, shortNumber = null, longNumber = null;
				Integer id;

				id = contactoDTO.getId();
				name = contactoDTO.getNombre() + "";

				List<String> phones = contactoDTO.getTelefonoList();
				if (phones != null) {
					for (String phone : phones) {

						if (phone != null && phone.length() < 9) {
							shortNumber = phone;
						} else {
							longNumber = phone;
						}

					}
				}

				ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
				ContentValues values = new ContentValues();
				values.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
				values.put(Phone.TYPE, Phone.TYPE_CUSTOM);

				if (id != null) {
					ops.add(ContentProviderOperation
							.newInsert(ContactsContract.Data.CONTENT_URI)
							.withValueBackReference(
									ContactsContract.Data.RAW_CONTACT_ID, 0)
							.withValue(
									ContactsContract.Data.MIMETYPE,
									ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
							.withValue(
									ContactsContract.CommonDataKinds.StructuredName.RAW_CONTACT_ID,
									id).build());

					values.put(Data.RAW_CONTACT_ID, id);
				}

				// ------------------------------------------------------ Name
				if (name != null) {
					ops.add(ContentProviderOperation
							.newInsert(ContactsContract.Data.CONTENT_URI)
							.withValueBackReference(
									ContactsContract.Data.RAW_CONTACT_ID, 0)
							.withValue(
									ContactsContract.Data.MIMETYPE,
									ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
							.withValue(
									ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
									name).build());

					values.put(Phone.DISPLAY_NAME, name);
				}

				// ------------------------------------------------------
				// ShortNumber
				// Number
				if (shortNumber != null) {
					ops.add(ContentProviderOperation
							.newInsert(ContactsContract.Data.CONTENT_URI)
							.withValueBackReference(
									ContactsContract.Data.RAW_CONTACT_ID, 0)
							.withValue(
									ContactsContract.Data.MIMETYPE,
									ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
							.withValue(
									ContactsContract.CommonDataKinds.Phone.NUMBER,
									shortNumber)
							.withValue(
									ContactsContract.CommonDataKinds.Phone.TYPE,
									ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
							.build());

					values.put(Phone.NUMBER, shortNumber);
				}
				// ------------------------------------------------------
				// LongNumber
				// Numbers
				if (longNumber != null) {
					ops.add(ContentProviderOperation
							.newInsert(ContactsContract.Data.CONTENT_URI)
							.withValueBackReference(
									ContactsContract.Data.RAW_CONTACT_ID, 0)
							.withValue(
									ContactsContract.Data.MIMETYPE,
									ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
							.withValue(
									ContactsContract.CommonDataKinds.Phone.NUMBER,
									longNumber)
							.withValue(
									ContactsContract.CommonDataKinds.Phone.TYPE,
									ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE)
							.build());
				}

				// Asking the Contact provider to create a new contact
				try {
					context.getContentResolver().applyBatch(
							ContactsContract.AUTHORITY, ops);

					result = true;
				} catch (Exception e) {
					e.printStackTrace();

				}

			}
		}

		return result;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		// If this is the loader for finding contacts in the Contacts Provider
		// (the only one supported)
		if (id == ContactsQuery.QUERY_ID) {
			Uri contentUri;

			// There are two types of searches, one which displays all contacts
			// and
			// one which filters contacts by a search query. If mSearchTerm is
			// set
			// then a search query has been entered and the latter should be
			// used.

			if (mSearchTerm == null) {
				// Since there's no search string, use the content URI that
				// searches the entire
				// Contacts table
				contentUri = ContactsQuery.CONTENT_URI;
			} else {
				// Since there's a search string, use the special content Uri
				// that searches the
				// Contacts table. The URI consists of a base Uri and the search
				// string.
				contentUri = Uri.withAppendedPath(ContactsQuery.FILTER_URI,
						Uri.encode(mSearchTerm));
			}

			// Returns a new CursorLoader for querying the Contacts table. No
			// arguments are used
			// for the selection clause. The search string is either encoded
			// onto the content URI,
			// or no contacts search string is used. The other search criteria
			// are constants. See
			// the ContactsQuery interface.
			return new CursorLoader(context, contentUri,
					ContactsQuery.PROJECTION, ContactsQuery.SELECTION, null,
					ContactsQuery.SORT_ORDER);
		}

		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		// This swaps the new cursor into the adapter.
		if (loader.getId() == ContactsQuery.QUERY_ID) {
			// mAdapter.swapCursor(data);

			// If this is a two-pane layout and there is a search query then
			// there is some additional work to do around default selected
			// search item.
			if (!TextUtils.isEmpty(mSearchTerm)) {
				// Selects the first item in results, unless this fragment has
				// been restored from a saved state (like orientation change)
				// in which case it selects the previously selected search item.
				if (data != null) {
					// Creates the content Uri for the previously selected
					// contact by appending the
					// contact's ID to the Contacts table content Uri
					// final Uri uri =
					// Uri.withAppendedPath(Contacts.CONTENT_URI,
					// String.valueOf(data.getLong(ContactsQuery.ID)));
					// mOnContactSelectedListener.onContactSelected(uri);
					// getListView().setItemChecked(mPreviouslySelectedSearchItem,
					// true);
				} else {
					// No results, clear selection.
					// onSelectionCleared();
				}
				// Only restore from saved state one time. Next time fall back
				// to selecting first item. If the fragment state is saved again
				// then the currently selected item will once again be saved.
				// mPreviouslySelectedSearchItem = 0;
				// mSearchQueryChanged = false;
			}
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		if (loader.getId() == ContactsQuery.QUERY_ID) {
			// When the loader is being reset, clear the cursor from the
			// adapter. This allows the
			// cursor resources to be freed.
			// mAdapter.swapCursor(null);
		}
	}

	@SuppressWarnings("unused")
	private CursorLoader initLoader(int id) {

		// If this is the loader for finding contacts in the Contacts Provider
		// (the only one supported)
		if (id == ContactsQuery.QUERY_ID) {
			Uri contentUri;

			// There are two types of searches, one which displays all contacts
			// and
			// one which filters contacts by a search query. If mSearchTerm is
			// set
			// then a search query has been entered and the latter should be
			// used.

			if (mSearchTerm == null) {
				// Since there's no search string, use the content URI that
				// searches the entire
				// Contacts table
				contentUri = ContactsQuery.CONTENT_URI;
			} else {
				// Since there's a search string, use the special content Uri
				// that searches the
				// Contacts table. The URI consists of a base Uri and the search
				// string.
				contentUri = Uri.withAppendedPath(ContactsQuery.FILTER_URI,
						Uri.encode(mSearchTerm));
			}

			// Returns a new CursorLoader for querying the Contacts table. No
			// arguments are used
			// for the selection clause. The search string is either encoded
			// onto the content URI,
			// or no contacts search string is used. The other search criteria
			// are constants. See
			// the ContactsQuery interface.
			return new CursorLoader(context, contentUri,
					ContactsQuery.PROJECTION, ContactsQuery.SELECTION, null,
					ContactsQuery.SORT_ORDER);
		}

		return null;

	}

	/**
	 * This interface defines constants for the Cursor and CursorLoader, based
	 * on constants defined in the
	 * {@link android.provider.ContactsContract.Contacts} class.
	 */
	public interface ContactsQuery {

		// An identifier for the loader
		final static int QUERY_ID = 1;

		// A content URI for the Contacts table
		final static Uri CONTENT_URI = Contacts.CONTENT_URI;

		// The search/filter query Uri
		final static Uri FILTER_URI = Contacts.CONTENT_FILTER_URI;

		// The selection clause for the CursorLoader query. The search criteria
		// defined here
		// restrict results to contacts that have a display name and are linked
		// to visible groups.
		// Notice that the search on the string provided by the user is
		// implemented by appending
		// the search string to CONTENT_FILTER_URI.
		@SuppressLint("InlinedApi")
		final static String SELECTION = (VersionUtil.hasHoneycomb() ? Contacts.DISPLAY_NAME_PRIMARY
				: Contacts.DISPLAY_NAME)
				+ "<>''" + " AND " + Contacts.IN_VISIBLE_GROUP + "=1";

		// The desired sort order for the returned Cursor. In Android 3.0 and
		// later, the primary
		// sort key allows for localization. In earlier versions. use the
		// display name as the sort
		// key.
		@SuppressLint("InlinedApi")
		final static String SORT_ORDER = VersionUtil.hasHoneycomb() ? Contacts.SORT_KEY_PRIMARY
				: Contacts.DISPLAY_NAME;

		// The projection for the CursorLoader query. This is a list of columns
		// that the Contacts
		// Provider should return in the Cursor.
		@SuppressLint("InlinedApi")
		final static String[] PROJECTION = {

				// The contact's row id
				Contacts._ID,

				// A pointer to the contact that is guaranteed to be more
				// permanent than _ID. Given
				// a contact's current _ID value and LOOKUP_KEY, the Contacts
				// Provider can generate
				// a "permanent" contact URI.
				Contacts.LOOKUP_KEY,

				// In platform version 3.0 and later, the Contacts table
				// contains
				// DISPLAY_NAME_PRIMARY, which either contains the contact's
				// displayable name or
				// some other useful identifier such as an email address. This
				// column isn't
				// available in earlier versions of Android, so you must use
				// Contacts.DISPLAY_NAME
				// instead.
				VersionUtil.hasHoneycomb() ? Contacts.DISPLAY_NAME_PRIMARY
						: Contacts.DISPLAY_NAME,

				// In Android 3.0 and later, the thumbnail image is pointed to
				// by
				// PHOTO_THUMBNAIL_URI. In earlier versions, there is no direct
				// pointer; instead,
				// you generate the pointer from the contact's ID value and
				// constants defined in
				// android.provider.ContactsContract.Contacts.
				VersionUtil.hasHoneycomb() ? Contacts.PHOTO_THUMBNAIL_URI
						: Contacts._ID,

				// The sort order column for the returned Cursor, used by the
				// AlphabetIndexer
				SORT_ORDER, };

		// The query column numbers which map to each value in the projection
		final static int ID = 0;
		final static int LOOKUP_KEY = 1;
		final static int DISPLAY_NAME = 2;
		final static int PHOTO_THUMBNAIL_DATA = 3;
		final static int SORT_KEY = 4;
	}

	/**
	 * Inserta o actualiza un contacto en la agenda
	 * 
	 * @param contactoDTO
	 * @param type
	 *            0 para añadir, 1 para actualizar
	 * @param numberType
	 *            0 para añadir numero corto, 1 para numeros largos
	 * 
	 * @return
	 */
	public Boolean newInsert(Contacto contactoDTO, Integer type,
			Integer numberType) {
		Boolean result = false;
		String shortNumber = null;
		String longNumber = null;
		String DisplayName;
		if (contactoDTO != null) {

			DisplayName = contactoDTO.getNombre();
			List<String> phones = contactoDTO.getTelefonoList();
			if (phones != null) {
				for (String phone : phones) {

					if (phone != null && phone.length() < 9) {
						shortNumber = phone;
					} else {
						longNumber = phone;
					}

				}
			}

			ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

			// Añadir nuevo contacto
			if (type.equals(0)) {
				ops.add(ContentProviderOperation
						.newInsert(ContactsContract.RawContacts.CONTENT_URI)
						.withValue(ContactsContract.RawContacts.ACCOUNT_TYPE,
								null)
						.withValue(ContactsContract.RawContacts.ACCOUNT_NAME,
								null).build());

				if (DisplayName != null) {
					ops.add(ContentProviderOperation
							.newInsert(ContactsContract.Data.CONTENT_URI)
							.withValueBackReference(
									ContactsContract.Data.RAW_CONTACT_ID, 0)
							.withValue(
									ContactsContract.Data.MIMETYPE,
									ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
							.withValue(
									ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
									DisplayName).build());
				}

				if (shortNumber != null) {
					ops.add(ContentProviderOperation
							.newInsert(ContactsContract.Data.CONTENT_URI)
							.withValueBackReference(
									ContactsContract.Data.RAW_CONTACT_ID, 0)
							.withValue(
									ContactsContract.Data.MIMETYPE,
									ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
							.withValue(
									ContactsContract.CommonDataKinds.Phone.NUMBER,
									shortNumber)
							.withValue(
									ContactsContract.CommonDataKinds.Phone.TYPE,
									ContactsContract.CommonDataKinds.Phone.TYPE_WORK)
							.build());
				}

				if (longNumber != null) {

					ops.add(ContentProviderOperation
							.newInsert(ContactsContract.Data.CONTENT_URI)
							.withValueBackReference(
									ContactsContract.Data.RAW_CONTACT_ID, 0)
							.withValue(
									ContactsContract.Data.MIMETYPE,
									ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
							.withValue(
									ContactsContract.CommonDataKinds.Phone.NUMBER,
									longNumber)
							.withValue(
									ContactsContract.CommonDataKinds.Phone.TYPE,
									ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
							.build());
				}
			}
			// Caso de actualizar un contacto
			else {

				if (numberType.equals(1)) {

					// number largo
					Cursor c = context.getContentResolver().query(
							ContactsContract.RawContacts.CONTENT_URI,
							null,
							ContactsContract.CommonDataKinds.Phone.CONTACT_ID
									+ " = ?",
							new String[] { contactoDTO.getId().toString() },
							null);
					int rawContactId = -1;
					if (c.moveToFirst()) {
						rawContactId = c
								.getInt(c
										.getColumnIndex(ContactsContract.RawContacts._ID));
					}

					Builder builder2 = ContentProviderOperation
							.newInsert(ContactsContract.Data.CONTENT_URI)
							.withValue(ContactsContract.Data.RAW_CONTACT_ID,
									rawContactId)
							.withValue(
									ContactsContract.Data.MIMETYPE,
									ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
							.withValue(
									ContactsContract.CommonDataKinds.Phone.NUMBER,
									longNumber)
							.withValue(
									ContactsContract.CommonDataKinds.Phone.TYPE,
									ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
					ops.add(builder2.build());
				} else {

					// number corto
					Cursor c = context.getContentResolver().query(
							ContactsContract.RawContacts.CONTENT_URI,
							null,
							ContactsContract.CommonDataKinds.Phone.CONTACT_ID
									+ " = ?",
							new String[] { contactoDTO.getId().toString() },
							null);
					int rawContactId = -1;
					if (c.moveToFirst()) {
						rawContactId = c
								.getInt(c
										.getColumnIndex(ContactsContract.RawContacts._ID));
					}

					Builder builder2 = ContentProviderOperation
							.newInsert(ContactsContract.Data.CONTENT_URI)
							.withValue(ContactsContract.Data.RAW_CONTACT_ID,
									rawContactId)
							.withValue(
									ContactsContract.Data.MIMETYPE,
									ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
							.withValue(
									ContactsContract.CommonDataKinds.Phone.NUMBER,
									shortNumber)
							.withValue(
									ContactsContract.CommonDataKinds.Phone.TYPE,
									ContactsContract.CommonDataKinds.Phone.TYPE_WORK);
					ops.add(builder2.build());

				}

			}

			// Asking the Contact provider to create a new contact
			try {
				context.getContentResolver().applyBatch(
						ContactsContract.AUTHORITY, ops);

				result = true;
			} catch (Exception e) {
				e.printStackTrace();
				Toast.makeText(context, "Exception: " + e.getMessage(),
						Toast.LENGTH_SHORT).show();
			}

		}
		return result;
	}

	public String cleanNumber(String number) {
		String cleanNumber = "";
		Pattern pp = Pattern.compile("\\d+");
		Matcher m = pp.matcher(number);
		while (m.find()) {
			cleanNumber += m.group();
		}

		return cleanNumber;
	}

	public void setSaveContacts(List<Contacto> saveContacts) {
		this.contacts = saveContacts;
	}

	public List<Contacto> getSaveContacts() {
		return contacts;

	}

}
