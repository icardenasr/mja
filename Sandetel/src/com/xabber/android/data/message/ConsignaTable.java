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
package com.xabber.android.data.message;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.xabber.android.data.DatabaseManager;
import com.xabber.android.data.entity.AbstractEntityTable;

/**
 * Storage with messages.
 * 
 * @author alexander.ivanov
 */
public class ConsignaTable extends AbstractEntityTable {

	private static final class Fields implements AbstractEntityTable.Fields {

		private Fields() {
		}

		/**
		 * Message archive collection tag.
		 */
		public static final String NAME = "name";
		public static final String SIZE = "size";
		public static final String URL_CONSIGNA = "url_consigna";
		public static final String URL_DOWNLOAD = "url_download";
		public static final String URL_LOCAL = "url_local";

		/**
		 * Time when this message was created locally.
		 */
		public static final String TIMESTAMP = "timestamp";

	}

	private static final String NAME = "consigna";
	private static final String[] PROJECTION = new String[] { Fields._ID,
			Fields.ACCOUNT, Fields.USER, Fields.NAME, Fields.SIZE,
			Fields.URL_CONSIGNA, Fields.URL_DOWNLOAD, Fields.URL_LOCAL,
			Fields.TIMESTAMP };

	private final DatabaseManager databaseManager;
	private SQLiteStatement insertNewMessageStatement;
	private final Object insertNewMessageLock;

	private final static ConsignaTable instance;

	static {
		instance = new ConsignaTable(DatabaseManager.getInstance());
		DatabaseManager.getInstance().addTable(instance);
	}

	public static ConsignaTable getInstance() {
		return instance;
	}

	private ConsignaTable(DatabaseManager databaseManager) {
		this.databaseManager = databaseManager;
		insertNewMessageStatement = null;
		insertNewMessageLock = new Object();
	}

	@Override
	public void create(SQLiteDatabase db) {
		String sql;
		sql = "CREATE TABLE " + NAME + " (" + Fields._ID
				+ " INTEGER PRIMARY KEY," + Fields.ACCOUNT + " TEXT,"
				+ Fields.USER + " TEXT," + Fields.NAME + " TEXT," + Fields.SIZE
				+ " TEXT," + Fields.URL_CONSIGNA + " TEXT,"
				+ Fields.URL_DOWNLOAD + " TEXT," + Fields.URL_LOCAL + " TEXT,"
				+ Fields.TIMESTAMP + " INTEGER );";
		DatabaseManager.execSQL(db, sql);
		sql = "CREATE INDEX " + NAME + "_list ON " + NAME + " ("
				+ Fields.ACCOUNT + ", " + Fields.USER + ", " + Fields.TIMESTAMP
				+ " ASC)";
		DatabaseManager.execSQL(db, sql);
	}

	@Override
	public void migrate(SQLiteDatabase db, int toVersion) {
		super.migrate(db, toVersion);
		switch (toVersion) {

		case 67:

			String sql;
			sql = "CREATE TABLE " + NAME + " (" + Fields._ID
					+ " INTEGER PRIMARY KEY," + Fields.ACCOUNT + " TEXT,"
					+ Fields.USER + " TEXT," + Fields.NAME + " TEXT,"
					+ Fields.SIZE + " TEXT," + Fields.URL_CONSIGNA + " TEXT,"
					+ Fields.URL_DOWNLOAD + " TEXT," + Fields.URL_LOCAL
					+ " TEXT," + Fields.TIMESTAMP + " INTEGER);";
			DatabaseManager.execSQL(db, sql);
			sql = "CREATE INDEX " + NAME + "_list ON " + NAME + " ("
					+ Fields.ACCOUNT + ", " + Fields.USER + ", "
					+ Fields.TIMESTAMP + " ASC)";
			DatabaseManager.execSQL(db, sql);

			break;

		default:
			break;
		}
	}

	/**
	 * Save new message to the database.
	 * 
	 * @return Assigned id.
	 */
	long add(String account, String bareAddress, String name, String size,
			String urlConsigna, String urlDownload, String urlLocal,
			Date timeStamp) {

		synchronized (insertNewMessageLock) {
			if (insertNewMessageStatement == null) {
				SQLiteDatabase db = databaseManager.getWritableDatabase();
				insertNewMessageStatement = db.compileStatement("INSERT INTO "
						+ NAME + " (" + Fields.ACCOUNT + ", " + Fields.USER
						+ ", " + Fields.NAME + ", " + Fields.SIZE + ", "
						+ Fields.URL_CONSIGNA + ", " + Fields.URL_DOWNLOAD
						+ ", " + Fields.URL_LOCAL + ", " + Fields.TIMESTAMP
						+ ") VALUES " + "(?, ?, ?, ?, ?, ?, ?, ?);");
			}
			insertNewMessageStatement.bindString(1, account);
			insertNewMessageStatement.bindString(2, bareAddress);
			insertNewMessageStatement.bindString(3, name);
			insertNewMessageStatement.bindString(4, size);
			if (urlConsigna != null) {
				insertNewMessageStatement.bindString(5, urlConsigna);
			}
			if (urlDownload != null) {
				insertNewMessageStatement.bindString(6, urlDownload);
			}
			if (urlLocal == null) {
				urlLocal = "";
			}
			if (urlLocal != null) {
				insertNewMessageStatement.bindString(7, urlLocal);
			}
			insertNewMessageStatement.bindLong(8, timeStamp.getTime());

			return insertNewMessageStatement.executeInsert();
		}
	}

	/**
	 * @param account
	 * @param bareAddress
	 * @return Result set with consigna for the message.
	 */
	Cursor list(String account, String bareAddress) {
		SQLiteDatabase db = databaseManager.getReadableDatabase();
		return db.query(NAME, PROJECTION, Fields.ACCOUNT + " = ? AND "
				+ Fields.USER + " = ?", new String[] { account, bareAddress },
				null, null, Fields.TIMESTAMP);
	}

	/**
	 * @param account
	 * @param bareAddress
	 * @return Result set with messages for the chat.
	 */
	Cursor listById(String account, String bareAddress, Integer id) {
		SQLiteDatabase db = databaseManager.getReadableDatabase();
		return db.query(NAME, PROJECTION, Fields.ACCOUNT + " = ? AND "
				+ Fields.USER + " = ? AND " + Fields._ID + "= ?", new String[] {
				account, bareAddress, String.valueOf(id) }, null, null,
				Fields.TIMESTAMP);
	}

	void removeConsignas(Collection<Long> ids) {
		if (ids.isEmpty())
			return;
		SQLiteDatabase db = databaseManager.getWritableDatabase();
		db.delete(NAME, DatabaseManager.in(Fields._ID, ids), null);
	}

	void updateConsigna(Long id, HashMap<String, String> params) {
		SQLiteDatabase db = databaseManager.getWritableDatabase();
		ContentValues values = new ContentValues();
		if (params != null && !params.isEmpty()) {
			for (Map.Entry<String, String> entry : params.entrySet()) {
				System.out.println(entry.getKey() + "/" + entry.getValue());
				values.put(entry.getKey(), entry.getValue());
			}
		}

		db.update(NAME, values, Fields._ID + " = ?",
				new String[] { String.valueOf(id) });

	}

	public void updateUrlLocalConsigna(Long id, String urlLocal) {
		SQLiteDatabase db = databaseManager.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(Fields.URL_LOCAL, urlLocal);

		db.update(NAME, values, Fields._ID + " = ?",
				new String[] { String.valueOf(id) });

	}

	@Override
	protected String getTableName() {
		return NAME;
	}

	@Override
	protected String[] getProjection() {
		return PROJECTION;
	}

	static long getId(Cursor cursor) {
		return cursor.getLong(cursor.getColumnIndex(Fields._ID));
	}

	static String getName(Cursor cursor) {
		return cursor.getString(cursor.getColumnIndex(Fields.NAME));
	}

	static String getSize(Cursor cursor) {
		return cursor.getString(cursor.getColumnIndex(Fields.SIZE));
	}

	static String getUrlConsigna(Cursor cursor) {
		return cursor.getString(cursor.getColumnIndex(Fields.URL_CONSIGNA));
	}

	static String getUrlDownload(Cursor cursor) {
		return cursor.getString(cursor.getColumnIndex(Fields.URL_DOWNLOAD));
	}

	static String getUrlLocal(Cursor cursor) {
		return cursor.getString(cursor.getColumnIndex(Fields.URL_LOCAL));
	}

	static Date getTimeStamp(Cursor cursor) {
		return new Date(cursor.getLong(cursor.getColumnIndex(Fields.TIMESTAMP)));
	}

}