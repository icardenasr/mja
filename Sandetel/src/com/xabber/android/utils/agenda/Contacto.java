package com.xabber.android.utils.agenda;

import java.io.Serializable;
import java.util.List;

public class Contacto implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Integer id;

	private String nombre;

	private List<String> telefonoList;

	private List<String> emailList;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public List<String> getTelefonoList() {
		return telefonoList;
	}

	public void setTelefonoList(List<String> telefonoList) {
		this.telefonoList = telefonoList;
	}

	public List<String> getEmailList() {
		return emailList;
	}

	public void setEmailList(List<String> emailList) {
		this.emailList = emailList;
	}

	// public Contact getContact() {
	// Contact contact = new Contact();
	// contact.setId(getId());
	// contact.setName(getNombre());
	// List<String> phonesList = getTelefonoList();
	// if (phonesList != null) {
	// for (String numeroContactoAgenda : getTelefonoList()) {
	//
	// String numeroAgendaParsed = numeroContactoAgenda.replace(" ",
	// "");
	// if (numeroAgendaParsed != null) {
	// if (numeroAgendaParsed.length() < 9) {
	// contact.setShortPhone(numeroAgendaParsed);
	// } else {
	//
	// contact.setLongPhone(numeroAgendaParsed);
	// }
	// }
	//
	// }
	// }
	//
	// return contact;
	//
	// }

}
