package com.xabber.android.service.dto;

import com.xabber.android.service.ws_client.dto.BaseBO;

/**
 * Data Transfer Object que almacena informacion referente a Archivo.
 */
@SuppressWarnings("serial")
public class ArchivoDTO extends BaseBO {

	private Long id;
	private String nombre;
	private String contentType;

	/** Default constructor. */
	public ArchivoDTO() {
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNombre() {
		return this.nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getContentType() {
		return this.contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

}
