package org.tloss.muachung.register;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringReader;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

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
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.PrettyXmlSerializer;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XmlSerializer;
import org.tloss.common.DefaultResponseAndFollowHandler;
import org.tloss.common.PasswordUtils;

public class Register {
	/**
	 * 
	 */
	public Register() {
		initScriptEngine();
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
		int length = 3 + random.nextInt(10);
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < length; i++) {
			buffer.append(userData[random.nextInt(userData.length)]);
		}
		return buffer.toString();
	}

	Invocable invocableEngine;

	public Invocable initScriptEngine() {
		ScriptEngineManager mgr = new ScriptEngineManager();
		ScriptEngine jsEngine = mgr.getEngineByName("JavaScript");
		try {
			jsEngine.eval(new InputStreamReader(Register.class
					.getResourceAsStream("muachung.js")));
			invocableEngine = (Invocable) jsEngine;
		} catch (ScriptException e) {
		}
		return invocableEngine;
	}

	public boolean register(String userctrl, String passctrl,
			String[] userData, String password, String phone) throws Exception {
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
		System.out.println("username: " +username);
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
			Boolean result = (Boolean) invocableEngine.invokeFunction(
					"checkcreateEmailSuccess", responseBody);
			if (result) {
				// dang ky voi mua chung
				httpPost = new HttpPost(
						"http://muachung.vn/ajax.php?act=customer&code=register");
				nvps = new ArrayList<NameValuePair>();
				nvps.add(new BasicNameValuePair("phone", phone));
				nvps.add(new BasicNameValuePair("email", username
						+ "@box-idea.com"));
				nvps.add(new BasicNameValuePair("pass", password));
				nvps.add(new BasicNameValuePair("uname", ""));
				nvps.add(new BasicNameValuePair("address", ""));
				nvps.add(new BasicNameValuePair("district", ""));
				nvps.add(new BasicNameValuePair("city", "0"));
				nvps.add(new BasicNameValuePair("rand", String.valueOf(Math
						.random())));
				httpPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
				setHeader(httpPost);
				responseBody = httpclient.execute(httpPost, responseHandler);
				result = (Boolean) invocableEngine.invokeFunction(
						"checkcreateAccountSuccess", responseBody);
				if (result) {
					// Login email
					httpPost = new HttpPost(
							"https://server403.webhostingpad.com:2096/login/");
					nvps = new ArrayList<NameValuePair>();
					nvps.add(new BasicNameValuePair("login_theme", "cpanel"));
					nvps.add(new BasicNameValuePair("user", username
							+ "@box-idea.com"));
					nvps.add(new BasicNameValuePair("pass", password));
					nvps.add(new BasicNameValuePair("goto_uri",
							"/?login_theme=cpanel"));
					httpPost.setEntity(new UrlEncodedFormEntity(nvps,
							HTTP.UTF_8));
					setHeader(httpPost);
					responseBody = httpclient
							.execute(httpPost, responseHandler);
					if (responseHandler.isMustFollow()) {
						httpGetStepOne = new HttpGet(
								"https://server403.webhostingpad.com:2096"
										+ responseBody);
						setHeader(httpGetStepOne);
						responseBody = httpclient.execute(httpGetStepOne,
								responseHandler);
						httpGetStepOne = new HttpGet(
								"https://server403.webhostingpad.com:2096/3rdparty/roundcube/index.php");
						setHeader(httpGetStepOne);
						responseBody = httpclient.execute(httpGetStepOne,
								responseHandler);
						boolean mustchekmail = true;
						while (mustchekmail) {
							httpGetStepOne = new HttpGet(
									"https://server403.webhostingpad.com:2096/3rdparty/roundcube/?_task=mail&_action=show&_mbox=INBOX&_uid=1");
							setHeader(httpGetStepOne);
							responseBody = httpclient.execute(httpGetStepOne,
									responseHandler);

							TagNode tagNode = cleaner.clean(new StringReader(
									responseBody));
							// serialize to xml file
							String xml = serializer.getAsString(tagNode,
									"utf-8");
							// System.out.println(xml);

							Document document = reader.read(new StringReader(
									xml));
							// <input type="hidden" value="0"
							// id="vB_Editor_001_mode"
							// name="wysiwyg">
							List<?> list = document
									.selectNodes("//div[@id='messagebody']//a/@href");
							for (Object object : list) {
								mustchekmail = false;
								Node element = (Node) object;
								String url = element.getText();
								httpGetStepOne = new HttpGet(url);
								setHeader(httpGetStepOne);
								responseBody = httpclient.execute(
										httpGetStepOne, responseHandler);
								TagNode tagNode1 = cleaner
										.clean(new StringReader(responseBody));
								// serialize to xml file
								String xml1 = serializer.getAsString(tagNode1,
										"utf-8");
								// System.out.println(xml);

								Document document1 = reader
										.read(new StringReader(xml1));
								// <input type="hidden" value="0"
								// id="vB_Editor_001_mode"
								// name="wysiwyg">
								List<?> list1 = document1
										.selectNodes("//form[@id='shopActiveCustomerForm']//input");
								nvps = new ArrayList<NameValuePair>();
								for (Object object1 : list1) {
									Element element1 = (Element) object1;

									if (element1.attributeValue("name") != null) {
										if ("pass".equals(element1
												.attributeValue("name"))) {
											nvps.add(new BasicNameValuePair(
													"pass", password));
										} else if ("pass1".equals(element1
												.attributeValue("name"))) {
											nvps.add(new BasicNameValuePair(
													"pass1", password));
										} else {
											nvps.add(new BasicNameValuePair(
													element1.attributeValue("name"),
													element1.attributeValue("value") != null ? element1
															.attributeValue("value")
															: ""));
										}
									}
								}
								httpPost = new HttpPost(url);
								httpPost.setEntity(new UrlEncodedFormEntity(
										nvps, HTTP.UTF_8));
								setHeader(httpPost);
								responseBody = httpclient.execute(httpPost,
										responseHandler);
								if (responseHandler.isMustFollow()) {
									httpGetStepOne = new HttpGet(responseBody);
									setHeader(httpGetStepOne);
									responseBody = httpclient.execute(
											httpGetStepOne, responseHandler);
									FileOutputStream fileOutputStream = new FileOutputStream(
											"gen-Account.txt", true);
									PrintStream printStream = new PrintStream(
											fileOutputStream);
									printStream.println(username
											+ "@box-idea.com");
									printStream.flush();
									printStream.close();
									fileOutputStream.close();
								}
							}
							if (mustchekmail) {
								mustWait();
							}
						}
					}
				}

			}

		}
		return false;
	}

	long maxMustWait;
	long sizeMustWait;

	public void setMaxMustWait(long maxMustWait) {
		this.maxMustWait = maxMustWait;
	}

	public void setSizeMustWait(long sizeMustWait) {
		this.sizeMustWait = sizeMustWait;
	}

	public synchronized void mustWait() throws InterruptedException {

		long max = maxMustWait;
		long size = sizeMustWait;
		long real = Math.round(max + size * Math.random());
		wait(real * 1000);
	}

	public boolean loginYahoo(String username, String password)
			throws Exception {
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

		// Loging Yahoo
		httpGetStepOne = new HttpGet(
				"https://login.yahoo.com/config/login_verify2?&.src=ym");
		setHeader(httpGetStepOne);
		responseBody = httpclient.execute(httpGetStepOne, responseHandler);
		TagNode tagNode = cleaner.clean(new StringReader(responseBody));
		// serialize to xml file
		String xml = serializer.getAsString(tagNode, "utf-8");
		// System.out.println(xml);

		Document document = reader.read(new StringReader(xml));
		List<?> list = document.selectNodes("//form[@id='login_form']//input");
		httpPost = new HttpPost("https://login.yahoo.com/config/login");
		nvps = new ArrayList<NameValuePair>();
		for (Object object : list) {
			Element element = (Element) object;
			if (element.attributeValue("name") != null) {
				if ("login".equals(element.attributeValue("name"))) {
					nvps.add(new BasicNameValuePair("login", username));
				} else if ("passwd".equals(element.attributeValue("name"))) {
					nvps.add(new BasicNameValuePair("passwd", password));
				} else {
					nvps.add(new BasicNameValuePair(element
							.attributeValue("name"), element
							.attributeValue("value") != null ? element
							.attributeValue("value") : ""));
				}
			}
		}
		setHeader(httpPost);
		httpPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
		responseBody = httpclient.execute(httpPost, responseHandler);
		// <meta http-equiv="Refresh"
		// content="0; url=http://vn.mg4.mail.yahoo.com/neo/launch?.rand=d9mrkva1i6blr">
		int index1 = responseBody
				.indexOf("<meta http-equiv=\"Refresh\" content=\"0; url=");
		if (index1 > 0) {
			int index2 = responseBody.indexOf("\">", index1);
			if (index2 > 0) {
				String url = responseBody
						.substring(
								index1
										+ ("<meta http-equiv=\"Refresh\" content=\"0; url="
												.length()), index2);
				httpGetStepOne = new HttpGet(url);
				setHeader(httpGetStepOne);
				responseBody = httpclient.execute(httpGetStepOne,
						responseHandler);
				return responseBody.indexOf(username) >= 0;
			}
		}

		return false;
	}

	public static void main(String[] args) throws Exception {
		PasswordUtils.loadKeyStore();
		Register checkMail = new Register();
		Properties properties = new Properties();
		properties.load(new FileInputStream("muachung.properties"));
		String[] userdata = properties.getProperty("userdata", "").split(",");
		String password = PasswordUtils.decryt(properties.getProperty(
				"password", ""));
		String ctrUsername = properties.getProperty("ctr-username", "");
		String ctrPassword = PasswordUtils.decryt(properties.getProperty(
				"ctr-password", ""));
		String yahooUsername = properties.getProperty("yahoo-username", "");
		String yahooPassword = PasswordUtils.decryt(properties.getProperty(
				"yahoo-password", ""));
		String phone = properties.getProperty("phone", "");
		String maxMustWait = properties.getProperty("maxMustWait", "1");
		String sizeMustWait = properties.getProperty("sizeMustWait", "2");
		checkMail.setMaxMustWait(Long.valueOf(maxMustWait));
		checkMail.setSizeMustWait(Long.valueOf(sizeMustWait));
		//if (checkMail.loginYahoo(yahooUsername, yahooPassword)) {
			checkMail.register(ctrUsername, ctrPassword, userdata, password,
					phone);
		//}
	}
}
