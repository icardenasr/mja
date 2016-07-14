package com.xabber.android.utils.dialogs;

import java.io.Serializable;

public interface IDialogAction extends Serializable {

	void onPositiveButton();

	void onNegativeButton();

}
