package com.xabber.android.ui.own;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

import com.xabber.android.ui.helper.ManagedActivity;

import es.juntadeandalucia.android.im.R;

public class AcercadeAct extends ManagedActivity implements OnClickListener {

	Button helpButton;
	Button supportButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.acerca_de);

		ImageView ivBack = (ImageView) findViewById(R.id.iv_back);
		ivBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				AcercadeAct.this.finish();
			}
		});
		
		helpButton = (Button)findViewById(R.id.button_help);
		helpButton.setOnClickListener(new OnClickListener() {
            public void onClick(View thisButton) {
            	String url = "https://correo.juntadeandalucia.es/ayuda/index.html#/mensajeria/android";
            	Intent i = new Intent(Intent.ACTION_VIEW);
            	i.setData(Uri.parse(url));
            	startActivity(i);
            }
        });

		supportButton = (Button)findViewById(R.id.button_support);
		supportButton.setOnClickListener(new OnClickListener() {
            public void onClick(View thisButton) {
            	String url = "https://correo.juntadeandalucia.es/ayuda/index.html#/soporte";
            	Intent i = new Intent(Intent.ACTION_VIEW);
            	i.setData(Uri.parse(url));
            	startActivity(i);
            }
        });
		
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

	}

}
