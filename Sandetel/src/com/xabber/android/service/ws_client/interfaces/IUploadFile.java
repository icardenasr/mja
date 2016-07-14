package com.xabber.android.service.ws_client.interfaces;

import java.io.Serializable;

import com.xabber.android.data.message.Consigna;

public interface IUploadFile extends Serializable {

	void onResultCorrectWS(Consigna consigna);
	
	void onREsultFailedWS();

}
