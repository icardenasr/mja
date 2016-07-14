package com.xabber.android.utils.font;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

public class btnRobotoRegular extends Button {

	public btnRobotoRegular(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		this.setTypeface(FontSingleton.getInstance(context).getFont(
				FontSingleton.ROBOTO_REGULAR));
	}

	public btnRobotoRegular(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		this.setTypeface(FontSingleton.getInstance(context).getFont(
				FontSingleton.ROBOTO_REGULAR));
	}

	public btnRobotoRegular(Context context) {
		super(context);
		// TODO Auto-generated constructor stub

		this.setTypeface(FontSingleton.getInstance(context).getFont(
				FontSingleton.ROBOTO_REGULAR));
	}

}
