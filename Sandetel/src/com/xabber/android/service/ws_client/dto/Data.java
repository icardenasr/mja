package com.xabber.android.service.ws_client.dto;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 * 
 * @author jose.fernandez
 */

public class Data extends BaseBO {
	private static final long serialVersionUID = 1L;

	private Long idArchivo;

	private String nombre;

	private String contentType;

	private byte[] contenido;

	public Data() {
	}

	public Data(Long pKId) {
		this.idArchivo = pKId;
	}

	public Data(Long pKId, String vCNombre, String vCContentType,
			byte[] bLContenido) {
		this.idArchivo = pKId;
		this.nombre = vCNombre;
		this.contentType = vCContentType;
		this.contenido = bLContenido;
	}

	@Override
	public int hashCode() {
		int hash = 0;
		hash += (idArchivo != null ? idArchivo.hashCode() : 0);
		return hash;
	}

	@Override
	public boolean equals(Object object) {
		// TODO: Warning - this method won't work in the case the id fields are
		// not set
		if (!(object instanceof Data)) {
			return false;
		}
		Data other = (Data) object;
		if ((this.idArchivo == null && other.idArchivo != null)
				|| (this.idArchivo != null && !this.idArchivo
						.equals(other.idArchivo))) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "es.dia.ayna.vo.Archivo[ idArchivo=" + idArchivo + " ]";
	}

	public Long getIdArchivo() {
		return idArchivo;
	}

	public void setIdArchivo(Long idArchivo) {
		this.idArchivo = idArchivo;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public byte[] getContenido() {
		return contenido;
	}

	public void setContenido(byte[] contenido) {
		this.contenido = contenido;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
