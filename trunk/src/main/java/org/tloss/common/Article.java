package org.tloss.common;

import java.util.Date;

public class Article {
	String title;
	String content;
	String desciption;
	Date create;

	public Article() {

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

	
}
