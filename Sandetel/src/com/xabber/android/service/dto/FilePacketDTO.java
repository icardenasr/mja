package com.xabber.android.service.dto;

import com.xabber.android.service.ws_client.dto.BaseBO;

@SuppressWarnings("serial")
public class FilePacketDTO extends BaseBO {

	private String nombre;
	private String url;
	private String caducidad;
	private String remitente;
	private String tamano;
	private String SHA256;
	private String descripcion;
	private String err;
	private String code;

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getCaducidad() {
		return caducidad;
	}

	public void setCaducidad(String caducidad) {
		this.caducidad = caducidad;
	}

	public String getRemitente() {
		return remitente;
	}

	public void setRemitente(String remitente) {
		this.remitente = remitente;
	}

	public String getTamano() {
		return tamano;
	}

	public void setTamano(String tamano) {
		this.tamano = tamano;
	}

	public String getSHA256() {
		return SHA256;
	}

	public void setSHA256(String sHA256) {
		SHA256 = sHA256;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getErr() {
		return err;
	}

	public void setErr(String err) {
		this.err = err;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

}
