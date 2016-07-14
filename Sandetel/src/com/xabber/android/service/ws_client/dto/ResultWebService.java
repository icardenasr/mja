package com.xabber.android.service.ws_client.dto;

/**
 * @version 1.0 24 Oct 2013
 * @author José Fernández
 */
public class ResultWebService {

	public static final int CORRECTO = 0;

	public static final int ERROR_LOGIN = 401;

	private int codigo;

	private String descripcion;

	public ResultWebService() {
		super();
		codigo = 0;
		descripcion = "Todo correcto";
	}

	public ResultWebService(int codigo) {
		super();
		this.codigo = codigo;
	}

	public ResultWebService(String descripcion) {
		super();
		this.codigo = -1;
		this.descripcion = descripcion;
	}

	public ResultWebService(int codigo, String descripcion) {
		super();
		this.codigo = codigo;
		this.descripcion = descripcion;
	}

	public int getCodigo() {
		return codigo;
	}

	public void setCodigo(int codigo) {
		this.codigo = codigo;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public boolean isCorrect() {
		return codigo == ResultWebService.CORRECTO;
	}

	public boolean isErrorLogin() {
		return codigo == ResultWebService.ERROR_LOGIN;
	}

	public static ResultWebService getErrorLogin() {
		return new ResultWebService(ResultWebService.ERROR_LOGIN);
	}

	public static ResultWebService getErrorLogin(String descripcion) {
		return new ResultWebService(ResultWebService.ERROR_LOGIN, descripcion);
	}

	public static ResultWebService getCorrecto() {
		return new ResultWebService(ResultWebService.CORRECTO);
	}

}
