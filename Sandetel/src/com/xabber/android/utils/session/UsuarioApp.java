package com.xabber.android.utils.session;

import com.xabber.android.service.ws_client.dto.BaseBO;

/**
 * Usuario de la aplicación
 * 
 * @version 1.0 24 Oct 2013
 * @author José Fernández
 */
@SuppressWarnings("serial")
public class UsuarioApp extends BaseBO {

	// @SuppressWarnings({ "rawtypes", "unused" })
	// private Class tipoUsuario;

	/**
	 * Usuario de la sessión de la app
	 */
	private Object usuario;

	/**
	 * Oauth Token
	 */
	private String sOAuthToken;

	/**
	 * Oauth secret
	 */
	private String sOAuthSecret;

	/**
	 * usuario Octopus
	 */
	private String octopusUser;

	@SuppressWarnings("rawtypes")
	public UsuarioApp() {
		// this.tipoUsuario = tipoUsuario;
	}

	public Object getUsuario() {
		return usuario;
	}

	public void setUsuario(Object usuario) {
		this.usuario = usuario;
	}

	public String getsOAuthToken() {
		return sOAuthToken;
	}

	public void setsOAuthToken(String sOAuthToken) {
		this.sOAuthToken = sOAuthToken;
	}

	public String getsOAuthSecret() {
		return sOAuthSecret;
	}

	public void setsOAuthSecret(String sOAuthSecret) {
		this.sOAuthSecret = sOAuthSecret;
	}

	public String getOctopusUser() {
		return octopusUser;
	}

	public void setOctopusUser(String octopusUser) {
		this.octopusUser = octopusUser;
	}

}
