package org.tloss.vatgia;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.AbstractHttpMessage;
import org.apache.http.params.CoreProtocolPNames;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.PrettyXmlSerializer;
import org.htmlcleaner.TagNode;
import org.tloss.common.DefaultResponseHandler;
import org.tloss.common.PasswordUtils;

public class Vatgia {
	HttpClient httpclient = new DefaultHttpClient();
	ResponseHandler<String> responseHandler = new DefaultResponseHandler();
	int counter = 0;
	int maxRequest;

	public Vatgia() {

	}

	public void setMaxRequest(int maxRequest) {
		this.maxRequest = maxRequest;
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
				"http://www.vatgia.com/home/act_login.php");
		MultipartEntity entity = new MultipartEntity();
		entity.addPart("loginname", new StringBody(username));
		entity.addPart("password", new StringBody(password));
		entity.addPart("user_login", new StringBody("login"));
		httpPost.setEntity(entity);
		setHeader(httpPost);
		String responseBody = httpclient.execute(httpPost, responseHandler);
		return responseBody.indexOf("window.location.href=\"/\"") >= 0;

	}

	public synchronized void sendRequest(String startUrl) throws Exception {
		if (counter < maxRequest) {
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
				httpclient.execute(httpGetStepOne, responseHandler);
				counter++;
				wait(10000);
			}
		}
	}

	public ArrayList<String> getUrls(String startUrl) throws Exception {
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
		List<?> list = document
				.selectNodes("//div[@class='content']/ul[@class='list_category']/li[@class='fl']/a/@href");
		ArrayList<String> arr = new ArrayList<String>();
		for (int i = 0; i < list.size(); i++) {
			Node node = (Node) list.get(i);
			String link = node.getText();
			String[] tmp = link.split("/");
			arr.add(tmp[1]);
			// 317/may-tinh-laptop.html
		}
		return arr;
	}

	public static void main(String[] args) throws Exception {
		Vatgia vatgia = new Vatgia();
		PasswordUtils.loadKeyStore();
		Properties properties = new Properties();
		properties.load(new FileInputStream("vatgia.properties"));
		String username = properties.getProperty("username", "");
		String password = properties.getProperty("passowrd", "");
		String maxRequest = properties.getProperty("maxRequest", "500");
		int maxReq = Integer.parseInt(maxRequest);
		vatgia.setMaxRequest(maxReq);
		String[] startUrls = properties.getProperty("startUrl", "").split(",");
		password = PasswordUtils.decryt(password);
		if (vatgia.login(username, password)) {
			for (int i = 0; i < startUrls.length; i++) {
				try {
					ArrayList<String> urls = vatgia.getUrls(startUrls[i]);
					for (int j = 0; j < urls.size(); j++) {
						vatgia.sendRequest("http://www.vatgia.com/home/listudv.php?module=product&iCat="+urls.get(j));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
