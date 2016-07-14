package com.xabber.android.data.message;

import java.io.Serializable;
import java.util.Date;

@SuppressWarnings("serial")
public class Consigna implements Serializable {

	private Long id;
	private String name;
	private String size;

	private String urlConsigna;
	private String urlDownload;
	private String urlLocal;

	private Date timeStamp;

	private Boolean downloading = false;
	private Boolean toUpLoad = false;
	private Boolean uplaoding = false;
	private Boolean availableToSend = true;
	private Boolean error = false;

	public Consigna() {
	}

	public Consigna(String name, String size, String urlConsigna,
			String urlLocal) {

		this(null, name, size, urlConsigna, (urlConsigna != null) ? urlConsigna
				+ "/descarga" : null, urlLocal, null);
	}

	public Consigna(Long id, String name, String size, String urlConsigna,
			String urlDownload, String urlLocal, Date timeStamp) {
		this.id = id;
		this.name = name;
		this.size = size;
		this.urlConsigna = urlConsigna;
		this.urlDownload = urlDownload;
		this.urlLocal = urlLocal;
		this.timeStamp = timeStamp;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrlConsigna() {
		return urlConsigna;
	}

	public void setUrlConsigna(String urlConsigna) {
		this.urlConsigna = urlConsigna;
	}

	public String getUrlDownload() {
		return urlDownload;
	}

	public void setUrlDownload(String urlDownload) {
		this.urlDownload = urlDownload;
	}

	public Date getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}

	public void setUrlLocal(String urlLocal) {
		this.urlLocal = urlLocal;
	}

	public String getUrlLocal() {
		return urlLocal;
	}

	public void setDownloading(Boolean downloading) {
		this.downloading = downloading;
	}

	public Boolean getDownloading() {
		return downloading;
	}

	public void setToUpLoad(Boolean toUpLoad) {
		this.toUpLoad = toUpLoad;
	}

	public Boolean getToUpLoad() {
		return toUpLoad;
	}

	public void setUplaoding(Boolean uplaoding) {
		this.uplaoding = uplaoding;
	}

	public Boolean getUplaoding() {
		return uplaoding;
	}

	public void setAvailableToSend(Boolean availableToSend) {
		this.availableToSend = availableToSend;
	}

	public Boolean getAvailableToSend() {
		return availableToSend;
	}

	public void setError(Boolean error) {
		this.error = error;
	}

	public Boolean getError() {
		return error;
	}

}
