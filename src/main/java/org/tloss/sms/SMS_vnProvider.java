package org.tloss.sms;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.AbstractHttpMessage;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.PrettyXmlSerializer;
import org.htmlcleaner.TagNode;
import org.tloss.common.DefaultResponseAndFollowHandler;

public class SMS_vnProvider implements SMSProvider {

	DefaultHttpClient httpclient = new DefaultHttpClient();
	DefaultResponseAndFollowHandler responseHandler = new DefaultResponseAndFollowHandler();

	public void setHeader(AbstractHttpMessage http) {
		http.setHeader(
				"User-Agent",
				"Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB; rv:1.9.2.17) Gecko/20110420 Firefox/3.6.17");
		http.setHeader("Accept",
				"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		http.setHeader("Accept-Language", "en-gb,en;q=0.5");
		http.setHeader("Accept-Encoding", "gzip,deflate");
		http.setHeader("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
		http.setHeader("Connection", "keep-alive");
		http.setHeader("Keep-Alive", "115");
		// http.setHeader("Referer", "http://www.ddth.com/forum.php");
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

	public boolean sendSms(String mobile, String text) throws Exception {
		String responseBody;
		HttpGet httpGetStepOne = new HttpGet("http://sms.vn/");
		setHeader(httpGetStepOne);
		responseBody = httpclient.execute(httpGetStepOne, responseHandler);
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
				.selectNodes("//form[@id='frm_send']//input [@id='txt_count_2']");
		String txt = null;
		for (Object object : list) {
			Element element = (Element) object;
			txt = element.attributeValue("value");
		}

		HttpPost httpPost = new HttpPost("http://sms.vn/send_action.jsp");
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("action", "sendtext"));
		nvps.add(new BasicNameValuePair("txt_mobilenumber_text", mobile));
		nvps.add(new BasicNameValuePair("area_content_text", text));
		if (text.length() > Integer.parseInt(txt)) {
			text = text.substring(0,Integer.parseInt(txt));
		}
		nvps.add(new BasicNameValuePair("txt_count_1", String.valueOf(Integer
				.parseInt(txt) - text.length())));
		nvps.add(new BasicNameValuePair("txt_mobilenumber_link",
				"090,091 ... 097,012..."));
		nvps.add(new BasicNameValuePair("txt_subject",
				"Nháº­p tiÃªu Ä?á» (khÃ´ng dáº¥u)"));
		nvps.add(new BasicNameValuePair("area_link", "Nháº­p Link"));
		nvps.add(new BasicNameValuePair("txt_count_2", txt));

		httpPost.setEntity(new UrlEncodedFormEntity(nvps));
		setHeader(httpPost);
		responseBody = httpclient.execute(httpPost, responseHandler);
		return false;
	}

	boolean logined = false;

	public int getLeftFreeSms() throws Exception {
		String responseBody;
		// http://sms.vn/
		HttpGet httpGetStepOne = new HttpGet("http://sms.vn/");
		setHeader(httpGetStepOne);
		responseBody = httpclient.execute(httpGetStepOne, responseHandler);
		return getLeftFreeSms(responseBody);
	}

	protected int getLeftFreeSms(String data) throws Exception {
		try {
			CleanerProperties props = new CleanerProperties();
			// set some properties to non-default values
			props.setTranslateSpecialEntities(true);
			props.setTransResCharsToNCR(true);
			props.setOmitComments(true);
			// do parsing
			TagNode tagNode = new HtmlCleaner(props).clean(new StringReader(
					data));
			// serialize to xml file
			String xml = new PrettyXmlSerializer(props).getAsString(tagNode,
					"utf-8");
			// System.out.println(xml);
			SAXReader reader = new SAXReader();
			Document document = reader.read(new StringReader(xml));
			List<?> list = document
					.selectNodes("//form[@id='frm_send']//strong/span");
			for (Object object : list) {
				Element element = (Element) object;
				String txt = element.getTextTrim();
				txt = txt.replaceAll("[^0-9]+", "");
				return Integer.parseInt(txt);
			}
		} catch (Exception e) {

		}
		return 0;
	}

	public boolean isLogined() throws Exception {

		return logined;
	}

	protected boolean checkLogin(String data) throws Exception {
		CleanerProperties props = new CleanerProperties();
		// set some properties to non-default values
		props.setTranslateSpecialEntities(true);
		props.setTransResCharsToNCR(true);
		props.setOmitComments(true);
		// do parsing
		TagNode tagNode = new HtmlCleaner(props).clean(new StringReader(data));
		// serialize to xml file
		String xml = new PrettyXmlSerializer(props).getAsString(tagNode,
				"utf-8");
		// System.out.println(xml);
		SAXReader reader = new SAXReader();
		Document document = reader.read(new StringReader(xml));
		List<?> list = document.selectNodes("//fieldset[@id='signin_menu']");
		return list != null && list.size() > 0;
	}

	public boolean login(String user, String password) throws Exception {
		// METHOD: POST
		// URL: http://sms.vn/login/
		// action login
		// txt_username tungt
		// txt_pwd *************
		// email

		String responseBody;
		HttpPost httpPost = new HttpPost("http://sms.vn/login/");
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("action", "login"));
		nvps.add(new BasicNameValuePair("txt_username", user));
		nvps.add(new BasicNameValuePair("txt_pwd", password));
		nvps.add(new BasicNameValuePair("email", ""));

		httpPost.setEntity(new UrlEncodedFormEntity(nvps));
		setHeader(httpPost);
		responseBody = httpclient.execute(httpPost, responseHandler);

		if (responseHandler.isMustFollow()) {
			HttpGet httpGetStepOne = new HttpGet(responseBody);
			setHeader(httpGetStepOne);
			responseBody = httpclient.execute(httpGetStepOne, responseHandler);
		}
		logined = checkLogin(responseBody);
		return logined;
	}

	public static void main(String[] args) throws Exception {
		SMSProvider provider = new SMS_vnProvider();
		if (!provider.isLogined()) {
			provider.login("tungt", "");
			System.out.println(provider.getLeftFreeSms());
			provider.sendSms("0902798844", "abc dsdsbd");
			System.out.println(provider.getLeftFreeSms());
		}
	}
}
