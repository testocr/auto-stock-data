package org.tloss.lessthan1dollar;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.tloss.common.Image;

public class LoginForm {
	boolean logined;
	Image captcha;
	List<NameValuePair> input = new ArrayList<NameValuePair>();

	public Image getCaptcha() {
		return captcha;
	}

	public void setCaptcha(Image captcha) {
		this.captcha = captcha;
	}

	public List<NameValuePair> getInput() {
		return input;
	}

	public void setInput(List<NameValuePair> input) {
		this.input = input;
	}

	public void setLogined(boolean logined) {
		this.logined = logined;
	}

	public boolean isLogined() {
		return logined;
	}

}
