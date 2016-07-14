package com.xabber.android.service.dto;

import com.xabber.android.service.ws_client.dto.BaseBO;

/**
 * Data Transfer Object que almacena informacion referente a Categoria.
 */
@SuppressWarnings("serial")
public class CategoriaDTO extends BaseBO {

	private Long id;
	private String nombre;

	/** Default constructor. */
	public CategoriaDTO() {
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

}
