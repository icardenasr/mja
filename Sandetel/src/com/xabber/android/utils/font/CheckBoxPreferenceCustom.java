package com.xabber.android.utils.font;

import android.content.Context;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class CheckBoxPreferenceCustom extends CheckBoxPreference {

	public CheckBoxPreferenceCustom(Context context) {
		super(context);
	}

	public CheckBoxPreferenceCustom(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CheckBoxPreferenceCustom(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onBindView(View view) {
		// TODO Auto-generated method stub
		super.onBindView(view);

		TextView textViewTitle = (TextView) view
				.findViewById(android.R.id.title);

		if (textViewTitle != null) {
			textViewTitle.setTypeface(FontSingleton.getInstance(
					this.getContext()).getFont(FontSingleton.ROBOTO_REGULAR));
		}

		TextView textViewSummary = (TextView) view
				.findViewById(android.R.id.summary);
		if (textViewSummary != null) {
			textViewSummary.setTypeface(FontSingleton.getInstance(
					this.getContext()).getFont(FontSingleton.ROBOTO_REGULAR));
		}
	}

}
