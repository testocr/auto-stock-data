package org.tloss.common;

public class Article {
	String title;
	String content;
	String desciption;

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

}
