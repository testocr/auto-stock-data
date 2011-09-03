package org.tloss.common;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Article {
	String title;
	String content;
	String desciption;
	Date create;
	String source;
	List<Image> images;

	public Article() {
		images = new ArrayList<Image>();
	}

	public Article(String title, String content) {

		this.title = title;
		this.content = content;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public void setDesciption(String desciption) {
		this.desciption = desciption;
	}

	public String getDesciption() {
		return desciption;
	}

	public Date getCreate() {
		return create;
	}

	public void setCreate(Date create) {
		this.create = create;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public List<Image> getImages() {
		return images;
	}

	public void setImages(List<Image> images) {
		this.images = images;
	}

}
