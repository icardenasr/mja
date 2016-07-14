package com.xabber.android.utils.font;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckedTextView;

public class CheckedTextViewRobotoRegular extends CheckedTextView {

	public CheckedTextViewRobotoRegular(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		this.setTypeface(FontSingleton.getInstance(context).getFont(
				FontSingleton.ROBOTO_REGULAR));
	}

	public CheckedTextViewRobotoRegular(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		this.setTypeface(FontSingleton.getInstance(context).getFont(
				FontSingleton.ROBOTO_REGULAR));
	}

	public CheckedTextViewRobotoRegular(Context context) {
		super(context);
		// TODO Auto-generated constructor stub

		this.setTypeface(FontSingleton.getInstance(context).getFont(
				FontSingleton.ROBOTO_REGULAR));
	}

}
