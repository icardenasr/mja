package com.xabber.android.data.message.chat;

import android.database.sqlite.SQLiteStatement;

import com.xabber.android.data.DatabaseManager;

/**
 * Storage with silence settings for each contact.
 * 
 * @author jose.fernández
 * 
 */
public class SilenceTable extends AbstractChatPropertyTable<Boolean> {

	static final String NAME = "chat_silence";

	private final static SilenceTable instance;

	static {
		instance = new SilenceTable(DatabaseManager.getInstance());
		DatabaseManager.getInstance().addTable(instance);
	}

	public static SilenceTable getInstance() {
		return instance;
	}

	private SilenceTable(DatabaseManager databaseManager) {
		super(databaseManager);
	}

	@Override
	protected String getTableName() {
		return NAME;
	}

	@Override
	String getValueType() {
		return "INTEGER";
	}

	@Override
	void bindValue(SQLiteStatement writeStatement, Boolean value) {
		// TODO Auto-generated method stub

	}

}
