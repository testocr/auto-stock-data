/**
 * Copyright (c) 2011 TLOSS. All rights reserved.
 * Created on Nov 12, 2011
 * File name : Chodientu.java
 * Package org.tloss.chodientu
 */
package org.tloss.chodientu;

import java.io.FileInputStream;
import java.io.StringReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.AbstractHttpMessage;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.PrettyXmlSerializer;
import org.htmlcleaner.TagNode;
import org.tloss.common.DefaultResponseAndFollowHandler;
import org.tloss.common.DefaultResponseHandler;
import org.tloss.common.PasswordUtils;

/**
 * @author tungt
 * 
 */
public class Chodientu {
	HttpClient httpclient = new DefaultHttpClient();
	ResponseHandler<String> responseHandler = new DefaultResponseHandler();
	DefaultResponseAndFollowHandler followHandler = new DefaultResponseAndFollowHandler();

	public Chodientu() {

	}

	public void setHeader(AbstractHttpMessage http) {
		http.setHeader(
				"User-Agent",
				"Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB; rv:1.9.2.17) Gecko/20110420 Firefox/3.6.17");
		http.setHeader("Accept", "text/html, */*");
		http.setHeader("Accept-Language", "en-gb,en;q=0.5");
		http.setHeader("Accept-Encoding", "gzip,deflate");
		http.setHeader("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
		http.setHeader("Keep-Alive", "115");
		http.setHeader("Connection", "keep-alive");
		http.setHeader("Pragma", "no-cache");
		http.setHeader("Cache-Control", "no-cache");
	}

	public void initHttpClient(HttpClient httpclient) {
		httpclient.getParams().setParameter(
				CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1); // Default
																			// to
																			// HTTP
																			// 1.0
		httpclient.getParams().setParameter(
				CoreProtocolPNames.HTTP_CONTENT_CHARSET, "UTF-8");
		httpclient.getParams().setParameter(ClientPNames.COOKIE_POLICY,
				CookiePolicy.BROWSER_COMPATIBILITY);
	}

	public boolean login(String username, String password) throws Exception {
		initHttpClient(httpclient);
		HttpPost httpPost = new HttpPost(
				"http://chodientu.vn/dang-nhap.html?ref=hoat-dong.html");
		MultipartEntity entity = new MultipartEntity();
		entity.addPart("form_module_id", new StringBody("1511"));
		entity.addPart("userName", new StringBody(username));
		entity.addPart("password", new StringBody(password));
		entity.addPart("page_current", new StringBody("home"));
		entity.addPart(
				"url_govn",
				new StringBody(
						"http://graph.go.vn/oauth/authorize?client_id=FUt5F0Rbfhehj3SxLjEJAIULj2MtyNleE6tL2iRJPn8&redirect_uri=http%3A%2F%2Fchodientu.vn%2Fopenid%2Fopenid.php"));
		entity.addPart(
				"url_facebook",
				new StringBody(
						"https://www.facebook.com/login.php?api_key=166997530001091&cancel_url=http%3A%2F%2Fchodientu.vn%2Fdang-ky.html&display=popup&fbconnect=0&next=http%3A%2F%2Fchodientu.vn%2Fdang-ky.html&return_session=1&session_version=3&v=1.0&canvas=1&req_perms=email%2Cpublish_stream%2Cstatus_update%2Cuser_birthday%2Cuser_location%2Cuser_work_history%2Cuser_photos%2Cuser_videos"));
		entity.addPart("openid", new StringBody(""));
		entity.addPart("emailopend", new StringBody(""));
		httpPost.setEntity(entity);
		setHeader(httpPost);
		String responseBody = httpclient.execute(httpPost, followHandler);
		if (followHandler.isMustFollow()) {
			HttpGet httpGetStepOne = new HttpGet("http://chodientu.vn/"
					+ responseBody);
			setHeader(httpGetStepOne);
			responseBody = httpclient.execute(httpGetStepOne, responseHandler);
		}
		return responseBody.indexOf(username) >= 0;

	}

	public synchronized void sendRequest(String startUrl) throws Exception {

		initHttpClient(httpclient);
		HttpGet httpGetStepOne = new HttpGet(startUrl);
		setHeader(httpGetStepOne);
		String responseBody = httpclient.execute(httpGetStepOne,
				responseHandler);
		// lay ra noi dung xml
		CleanerProperties props = new CleanerProperties();
		// set some properties to non-default values
		props.setTranslateSpecialEntities(true);
		props.setTransResCharsToNCR(true);
		props.setOmitComments(true);
		// do parsing
		TagNode tagNode = new HtmlCleaner(props).clean(new StringReader(
				responseBody));
		// serialize to xml file
		String xml = new PrettyXmlSerializer(props).getAsString(tagNode,
				"utf-8");
		SAXReader reader = new SAXReader();
		Document document = reader.read(new StringReader(xml));

		List<?> list = document.selectNodes("//div[@class='name']/a/@href");
		for (int i = 0; i < list.size(); i++) {
			Node node = (Node) list.get(i);
			httpGetStepOne = new HttpGet("http://www.vatgia.com"
					+ node.getText());
			setHeader(httpGetStepOne);
			responseBody = httpclient.execute(httpGetStepOne, responseHandler);
			tagNode = new HtmlCleaner(props).clean(new StringReader(
					responseBody));
			xml = new PrettyXmlSerializer(props).getAsString(tagNode, "utf-8");

			mustWait();
		}

	}

	public synchronized void mustWait() throws InterruptedException {

		long max = 5;
		long size = 6;
		long real = Math.round(max + size * Math.random());
		wait(real * 1000);
	}

	public synchronized void mustWaitMin() throws InterruptedException {
		long max = 2;
		long size = 2;
		long real = Math.round(max + size * Math.random());
		wait(real * 1000);
	}

	public void search(String keyWord) throws Exception {
		System.out.println("keyword: " +keyWord);
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("keyword", keyWord));
		nvps.add(new BasicNameValuePair("form_module_id", "415"));
		nvps.add(new BasicNameValuePair("category_name", ""));
		nvps.add(new BasicNameValuePair("keyword_item", keyWord));
		nvps.add(new BasicNameValuePair("category_id", "0"));
		nvps.add(new BasicNameValuePair("search-button", ""));
		nvps.add(new BasicNameValuePair("type_search", "1"));
		initHttpClient(httpclient);
		HttpPost httpPost = new HttpPost("http://chodientu.vn/hoat-dong.html");
		setHeader(httpPost);
		httpPost.setEntity(new UrlEncodedFormEntity(nvps));
		String responseBody = httpclient.execute(httpPost, followHandler);
		if (followHandler.isMustFollow()) {
			HttpGet httpGetStepOne = new HttpGet(responseBody);
			setHeader(httpGetStepOne);
			responseBody = httpclient.execute(httpGetStepOne, followHandler);
			mustWait();
			try{
			view(responseBody);
			}catch (Exception e) {
				e.printStackTrace(System.out);
			}
		}

	}

	public void view(String responseBody) throws Exception {
		CleanerProperties props = new CleanerProperties();
		// set some properties to non-default values
		props.setTranslateSpecialEntities(true);
		props.setTransResCharsToNCR(true);
		props.setOmitComments(true);
		// do parsing
		TagNode tagNode = new HtmlCleaner(props).clean(new StringReader(
				responseBody));
		// serialize to xml file
		String xml = new PrettyXmlSerializer(props).getAsString(tagNode,
				"utf-8");
		// System.out.println(xml);
		SAXReader reader = new SAXReader();
		Document document = reader.read(new StringReader(xml));
		List<?> list = document
				.selectNodes("//div[@class='info-detail']/div[@class='row1']/h4/a/@href");
		if (list.size() > 0) {
			int d = (int) (Math.round((Math.random() * (list.size() - 1))) % list
					.size());
			Node element = (Node) list.get(d);
			initHttpClient(httpclient);
			HttpGet httpGetStepOne = new HttpGet(element.getText().replaceAll("\\|", URLEncoder.encode("|", "utf-8")));
			setHeader(httpGetStepOne);
			responseBody = httpclient.execute(httpGetStepOne, followHandler);
		}
	}

	public void logout() throws Exception {
		initHttpClient(httpclient);
		HttpGet httpGetStepOne = new HttpGet(
				"http://chodientu.vn/dang-xuat.html?ref=hoat-dong.html&title=Tho%C3%A1t");
		setHeader(httpGetStepOne);
		String responseBody = httpclient.execute(httpGetStepOne, followHandler);
		if (followHandler.isMustFollow()) {
			httpGetStepOne = new HttpGet("http://chodientu.vn/" + responseBody);
			setHeader(httpGetStepOne);
			responseBody = httpclient.execute(httpGetStepOne, responseHandler);
		}

	}

	public static void main(String[] args) throws Exception {
		Chodientu chodientu = new Chodientu();
		PasswordUtils.loadKeyStore();
		Properties properties = new Properties();
		properties.load(new FileInputStream("chodientu.properties"));
		String username = properties.getProperty("username", "");
		String password = properties.getProperty("password", "");
		password = PasswordUtils.decryt(password);

		int searchTime = Integer.parseInt(properties.getProperty("searchTime",
				"20"));
		int loginTime = Integer.parseInt(properties.getProperty("loginTime",
				"12"));
		int viewDetail = Integer.parseInt(properties.getProperty("viewDetail",
				"20"));

		String[] keyword = properties.getProperty("keyword", "").split(",");
		int max = searchTime >= loginTime ? (searchTime >= viewDetail ? searchTime
				: viewDetail)
				: (loginTime >= viewDetail ? loginTime : viewDetail);
		int i = 0;
		while (i < max) {
			if (chodientu.login(username, password)) {
				chodientu.mustWait();
				try{
				chodientu.search(keyword[i % keyword.length]);
				}catch (Exception e) {
					e.printStackTrace(System.out);
				}
				chodientu.logout();
			}
			i++;
		}

	}

}
