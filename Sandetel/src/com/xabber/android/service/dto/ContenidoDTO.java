package com.xabber.android.service.dto;

import java.util.List;

import com.xabber.android.service.ws_client.dto.BaseBO;

/**
 * Data Transfer Object que almacena informacion referente a Contenido.
 */
@SuppressWarnings("serial")
public class ContenidoDTO extends BaseBO {

	private Long id;
	private CategoriaDTO categoria;
	private String titulo;
	private String descripcion;
	private Long tipoContenido;
	private List<ArchivoDTO> archivos;

	/**
	 * Default constructor.
	 */
	public ContenidoDTO() {
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public CategoriaDTO getCategoria() {
		return this.categoria;
	}

	public void setCategoria(CategoriaDTO categoria) {
		this.categoria = categoria;
	}

	public String getTitulo() {
		return this.titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public String getDescripcion() {
		return this.descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public List<ArchivoDTO> getArchivos() {
		return archivos;
	}

	public void setArchivos(List<ArchivoDTO> archivos) {
		this.archivos = archivos;
	}

	public Long getTipoContenido() {
		return tipoContenido;
	}

	public void setTipoContenido(Long tipoContenido) {
		this.tipoContenido = tipoContenido;
	}

}
