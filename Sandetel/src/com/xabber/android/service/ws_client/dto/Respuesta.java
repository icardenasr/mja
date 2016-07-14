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
public class Respuesta<T> {

	private Error error;

	private List<BaseBO> respuesta;
	private List<Object> respuestaEsp;

	public Respuesta() {

	}

	public Respuesta(List<BaseBO> respuesta, Error error,
			List<Object> respuestaEsp) {
		this.error = error;
		this.respuesta = respuesta;
		this.setRespuestaEsp(respuestaEsp);
	}

	/**
	 * @return the respuesta
	 */
	public List<BaseBO> getRespuesta() {
		return respuesta;
	}

	/**
	 * @param respuesta
	 *            the respuesta to set
	 */
	public void setRespuesta(List<BaseBO> respuesta) {
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
	public static Respuesta getErrorDefaultResp() {
		Error error = new Error("-1000",
				"Ha ocurrido un error con la llamada al servidor.");
		Respuesta respuesta = new Respuesta(null, error, null);

		return respuesta;

	}

	@SuppressWarnings("rawtypes")
	public static boolean isErrorLogin(Respuesta resp) {
		if (resp != null && "401".equals(resp.getCodError())) {
			return true;
		} else {
			return false;
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Respuesta getErrorLoginResp() {
		Error error = new Error("401", "Error de login");
		Respuesta respuesta = new Respuesta(null, error, null);

		return respuesta;

	}

	public void setRespuestaEsp(List<Object> respuestaEsp) {
		this.respuestaEsp = respuestaEsp;
	}

	public List<Object> getRespuestaEsp() {
		return respuestaEsp;
	}
}
