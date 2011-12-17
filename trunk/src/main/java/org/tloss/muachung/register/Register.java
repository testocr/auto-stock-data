package org.tloss.muachung.register;

import java.io.FileInputStream;
import java.io.StringReader;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Random;

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
import org.apache.http.protocol.HTTP;
import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.PrettyXmlSerializer;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XmlSerializer;
import org.tloss.common.DefaultResponseAndFollowHandler;

public class Register {
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

	HttpClient httpclient = new DefaultHttpClient();
	DefaultResponseAndFollowHandler responseHandler = new DefaultResponseAndFollowHandler();
	SecureRandom random = new SecureRandom();

	public String buildUserName(String[] userData) {
		int length = 1 + random.nextInt(10);
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < length; i++) {
			buffer.append(userData[random.nextInt(userData.length)]);
		}
		return buffer.toString();
	}

	public boolean register(String userctrl, String passctrl,
			String[] userData, String password) throws Exception {
		initHttpClient(httpclient);
		HttpPost httpPost;
		HttpGet httpGetStepOne;
		List<NameValuePair> nvps;
		String responseBody;
		CleanerProperties props = new CleanerProperties();
		// set some properties to non-default values
		props.setTranslateSpecialEntities(true);
		props.setTransResCharsToNCR(true);
		props.setOmitComments(true);
		HtmlCleaner cleaner = new HtmlCleaner(props);
		XmlSerializer serializer = new PrettyXmlSerializer(props);
		SAXReader reader = new SAXReader();
		// Loging control panel
		httpPost = new HttpPost(
				"https://server403.webhostingpad.com:2083/login/");
		nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("user", userctrl));
		nvps.add(new BasicNameValuePair("pass", passctrl));
		nvps.add(new BasicNameValuePair("login_theme", "cpanel"));
		nvps.add(new BasicNameValuePair("goto_uri", "/"));
		setHeader(httpPost);
		httpPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
		responseBody = httpclient.execute(httpPost, responseHandler);
		String username = buildUserName(userData);
		if (responseHandler.isMustFollow()) {
			httpGetStepOne = new HttpGet(
					"https://server403.webhostingpad.com:2083" + responseBody);
			setHeader(httpGetStepOne);
			responseBody = httpclient.execute(httpGetStepOne, responseHandler);
			httpGetStepOne = new HttpGet(
					"https://server403.webhostingpad.com:2083/frontend/x3/mail/pops.html");
			setHeader(httpGetStepOne);
			responseBody = httpclient.execute(httpGetStepOne, responseHandler);
			httpGetStepOne = new HttpGet(
					"https://server403.webhostingpad.com:2083/json-api/cpanel?cpanel_jsonapi_version=2&cpanel_jsonapi_module=Email&cpanel_jsonapi_func=addpop&email="
							+ username
							+ "&password="
							+ password
							+ "&quota=1&domain=box-idea.com&cache_fix="
							+ (new Date().getTime()));
			setHeader(httpGetStepOne);
			responseBody = httpclient.execute(httpGetStepOne, responseHandler);
			System.out.println(responseBody);

		}

		// Login email
		httpPost = new HttpPost(
				"https://server403.webhostingpad.com:2096/login/");
		nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("login_theme", "cpanel"));
		nvps.add(new BasicNameValuePair("user", username+"@box-idea.com"));
		nvps.add(new BasicNameValuePair("pass", password));
		nvps.add(new BasicNameValuePair("goto_uri", "/?login_theme=cpanel"));
		httpPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
		setHeader(httpPost);
		responseBody = httpclient.execute(httpPost, responseHandler);
		if (responseHandler.isMustFollow()) {
			httpGetStepOne = new HttpGet(
					"https://server403.webhostingpad.com:2096" + responseBody);
			setHeader(httpGetStepOne);
			responseBody = httpclient.execute(httpGetStepOne, responseHandler);
			httpGetStepOne = new HttpGet(
					"https://server403.webhostingpad.com:2096/3rdparty/roundcube/index.php");
			setHeader(httpGetStepOne);
			responseBody = httpclient.execute(httpGetStepOne, responseHandler);

			httpGetStepOne = new HttpGet(
					"https://server403.webhostingpad.com:2096/3rdparty/roundcube/?_task=mail&_action=show&_mbox=INBOX&_uid=1");
			setHeader(httpGetStepOne);
			responseBody = httpclient.execute(httpGetStepOne, responseHandler);

			TagNode tagNode = cleaner.clean(new StringReader(responseBody));
			// serialize to xml file
			String xml = serializer.getAsString(tagNode, "utf-8");
			// System.out.println(xml);

			Document document = reader.read(new StringReader(xml));
			// <input type="hidden" value="0" id="vB_Editor_001_mode"
			// name="wysiwyg">
			List<?> list = document
					.selectNodes("//div[@id='messagebody']//a/@href");
			for (Object object : list) {
				Node element = (Node) object;
				System.out.println(element.getText());
			}
		}

		return false;
	}

	public static void main(String[] args) throws Exception {
		Register checkMail = new Register();
		Properties properties = new Properties();
		properties.load(new FileInputStream("muachung.properties"));
		String[] userdata = properties.getProperty("userdata", "").split(",");
		checkMail.register("", "", userdata, "");
	}
}
