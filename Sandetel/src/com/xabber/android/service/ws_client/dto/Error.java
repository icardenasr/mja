/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.xabber.android.service.ws_client.dto;

/**
 * @version 1.0 24 Oct 2013
 * @author José Fernández
 */
public class Error {

	private String descripcion;

	private String codError;

	public Error(String codError, String descripcion) {
		this.descripcion = descripcion;
		this.codError = codError;
	}

	/**
	 * @return the descripcion
	 */
	public String getDescripcion() {
		return descripcion;
	}

	/**
	 * @param descripcion
	 *            the descripcion to set
	 */
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	/**
	 * @return the codError
	 */
	public String getCodError() {
		return codError;
	}

	/**
	 * @param codError
	 *            the codError to set
	 */
	public void setCodError(String codError) {
		this.codError = codError;
	}
}
