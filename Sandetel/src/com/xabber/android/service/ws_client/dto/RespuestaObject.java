/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.xabber.android.service.ws_client.dto;

import java.util.List;

/**
 * @version 1.0 24 Oct 2013
 * @author José Fernández
 */
public class RespuestaObject<T> {

	private Error error;

	private List<Object> respuesta;

	public RespuestaObject() {

	}

	public RespuestaObject(List<Object> respuesta, Error error) {
		this.error = error;
		this.respuesta = respuesta;
	}

	/**
	 * @return the respuesta
	 */
	public List<Object> getRespuesta() {
		return respuesta;
	}

	/**
	 * @param respuesta
	 *            the respuesta to set
	 */
	public void setRespuesta(List<Object> respuesta) {
		this.respuesta = respuesta;
	}

	public Error getError() {
		return error;
	}

	public void setError(Error error) {
		this.error = error;
	}

	public String getCodError() {
		if (this.error != null) {
			return error.getCodError();
		} else {
			return "-1";
		}

	}

	public String getDescripcion() {
		if (this.error != null) {
			return error.getDescripcion();
		} else {
			return "Error de comunicación con el servidor.";
		}

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static RespuestaObject getErrorDefaultResp() {
		Error error = new Error("-1000",
				"Ha ocurrido un error con la llamada al servidor.");
		RespuestaObject respuesta = new RespuestaObject(null, error);

		return respuesta;

	}

	@SuppressWarnings("rawtypes")
	public static boolean isErrorLogin(RespuestaObject resp) {
		if (resp != null && "401".equals(resp.getCodError())) {
			return true;
		} else {
			return false;
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static RespuestaObject getErrorLoginResp() {
		Error error = new Error("401", "Error de login");
		RespuestaObject respuesta = new RespuestaObject(null, error);

		return respuesta;

	}
}
