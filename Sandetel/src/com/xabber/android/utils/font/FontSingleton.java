package com.xabber.android.utils.font;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;

public class FontSingleton extends Activity {

	public static final int ROBOTO_REGULAR = 0;
	public static final int ROBOTO_BOLD = 1;

	private Typeface robotoRegular = null;
	private Typeface robotoBold = null;

	private static FontSingleton fontSingleton = null;

	public static FontSingleton getInstance(Context contexto) {

		if (fontSingleton == null) {
			fontSingleton = new FontSingleton(contexto);
		}
		return fontSingleton;
	}

	private FontSingleton(Context contexto) {
		setRobotoRegular(Typeface.createFromAsset(contexto.getAssets(),
				"RobotoCondensed-Regular.ttf"));
		setRobotoBold(Typeface.createFromAsset(contexto.getAssets(),
				"RobotoCondensed-Bold.ttf"));
	}

	public Typeface getFont(int tipo) {

		switch (tipo) {
		case ROBOTO_REGULAR:
			return fontSingleton.getRobotoRegular();
		case ROBOTO_BOLD:
			return fontSingleton.getRobotoBold();
		default:
			return fontSingleton.getRobotoRegular();
		}

	}

	public Typeface getRobotoRegular() {
		return robotoRegular;
	}

	public void setRobotoRegular(Typeface robotoRegular) {
		this.robotoRegular = robotoRegular;
	}

	public Typeface getRobotoBold() {
		return robotoBold;
	}

	public void setRobotoBold(Typeface robotoBold) {
		this.robotoBold = robotoBold;
	}

}
