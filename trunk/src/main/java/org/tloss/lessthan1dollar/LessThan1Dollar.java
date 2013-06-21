package org.tloss.lessthan1dollar;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpHost;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.conn.params.ConnRoutePNames;
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
import org.tloss.common.Image;
import org.tloss.common.utils.Utils;

public class LessThan1Dollar {
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

	ProxyOption option;

	public void setOption(ProxyOption option) {
		this.option = option;
	}

	public void initHttpClient(HttpClient httpclient) {
		if (option != null && option.isUserProxy()) {
			HttpHost proxy = new HttpHost(option.getProxy(), option.getPort());
			httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,
					proxy);
		}
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

	protected boolean isLogined(String responseBody) {
		return responseBody.contains("logout\">Log out</a>");
	}

	protected String clearInvaliedXml(String responseBody) {
		return responseBody.replace("xml:lang", "lang");
	}

	protected LoginForm parse(String host, String responseBody)
			throws Exception {
		LoginForm form = new LoginForm();
		form.setLogined(isLogined(responseBody));
		responseBody = clearInvaliedXml(responseBody);
		Image image = null;
		CleanerProperties props = new CleanerProperties();
		// set some properties to non-default values
		props.setTranslateSpecialEntities(true);
		props.setTransResCharsToNCR(true);
		props.setOmitComments(true);

		TagNode tagNode = new HtmlCleaner(props).clean(new StringReader(
				responseBody));
		// serialize to xml file
		String xml = new PrettyXmlSerializer(props).getAsString(tagNode,
				"utf-8");
		// System.out.println(xml);
		SAXReader reader = new SAXReader();
		reader.setFeature("http://xml.org/sax/features/namespaces", false);
		reader.setFeature("http://xml.org/sax/features/namespace-prefixes",
				false);

		Document document = reader.read(new StringReader(xml));
		List<?> list = document
				.selectNodes("//form[@id='user-login-form']//input |//form[@id='user-login-form']//img");
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();

		for (Object object : list) {
			Element element = (Element) object;
			if (element.attribute("value") != null) {
				if (!("name".equals(element.attributeValue("name"))
						|| "pass".equals(element.attributeValue("name")) || "captcha_response"
						.equals(element.attributeValue("name")))) {
					nvps.add(new BasicNameValuePair(element
							.attributeValue("name"), element
							.attributeValue("value")));
				}
			} else if ("img".equals(element.getName())) {
				image = Utils.download(
						host + "/" + element.attributeValue("src"), false,
						httpclient);
			}
		}
		form.setInput(nvps);
		form.setCaptcha(image);
		return form;
	}

	public LoginForm proLogin(String host) throws Exception {

		initHttpClient(httpclient);
		HttpGet httpGetStepOne = null;

		httpGetStepOne = new HttpGet(host);
		setHeader(httpGetStepOne);
		String responseBody = httpclient.execute(httpGetStepOne,
				responseHandler);

		return parse(host, responseBody);
	}

	public int importData(String host, String trace, String date, String last)
			throws Exception {
		// /?q=last_buyer/settings/import/trace
		HttpGet httpGetStepOne = null;

		httpGetStepOne = new HttpGet(host
				+ "/?q=last_buyer/settings/import/trace");
		setHeader(httpGetStepOne);
		String responseBody = httpclient.execute(httpGetStepOne,
				responseHandler);
		if (!isLogined(responseBody)) {
			return Constants.ERROR_MUST_LOGIN;
		}
		responseBody = clearInvaliedXml(responseBody);
		CleanerProperties props = new CleanerProperties();
		// set some properties to non-default values
		props.setTranslateSpecialEntities(true);
		props.setTransResCharsToNCR(true);
		props.setOmitComments(true);

		TagNode tagNode = new HtmlCleaner(props).clean(new StringReader(
				responseBody));
		// serialize to xml file
		String xml = new PrettyXmlSerializer(props).getAsString(tagNode,
				"utf-8");
		// System.out.println(xml);
		SAXReader reader = new SAXReader();
		reader.setFeature("http://xml.org/sax/features/namespaces", false);
		reader.setFeature("http://xml.org/sax/features/namespace-prefixes",
				false);

		Document document = reader.read(new StringReader(xml));
		List<?> list = document
				.selectNodes("//form[@id='admin-import-trace-form']//input");
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();

		for (Object object : list) {
			Element element = (Element) object;
			if (element.attribute("value") != null) {
				if (!"trace_id".equals(element.attributeValue("name"))
						&& !"date".equals(element.attributeValue("name")))
					nvps.add(new BasicNameValuePair(element
							.attributeValue("name"), element
							.attributeValue("value")));
			}
		}

		nvps.add(new BasicNameValuePair("method", "0"));
		nvps.add(new BasicNameValuePair("last", last));

		HttpPost httpPost = new HttpPost(host
				+ "/?q=last_buyer/settings/import/trace");
		nvps.add(new BasicNameValuePair("trace_id", trace));
		nvps.add(new BasicNameValuePair("date", date));
		httpPost.setEntity(new UrlEncodedFormEntity(nvps));
		setHeader(httpPost);
		responseBody = httpclient.execute(httpPost, responseHandler);
		if ("SUCCESS".equals(responseBody))
			return Constants.SUCCESS;
		else
			return Constants.ERROR_GENERAL;
	}

	public boolean logout() {
		return true;
	}

	public LoginResult login(String username, String password,
			boolean encrytedPassword, String captcha, String host,
			List<NameValuePair> nvps) throws Exception {
		LoginResult loginResult = new LoginResult();
		// name admin
		// pass
		// captcha_sid 1199
		// captcha_token a2ebcdba3fab44f2c36559e4930eff5e
		// captcha_response CFhjb
		// op Log in
		// form_build_id form-09b709b3969ab282c83ebdb4012c8fda
		// form_id user_login_block

		boolean result = false;
		String responseBody = null;
		HttpPost httpPost = new HttpPost(host + "/?q=node&destination=node");
		nvps.add(new BasicNameValuePair("name", username));
		nvps.add(new BasicNameValuePair("pass", password));
		if (captcha != null) {
			nvps.add(new BasicNameValuePair("captcha_response", captcha));
		}
		httpPost.setEntity(new UrlEncodedFormEntity(nvps));
		setHeader(httpPost);
		responseBody = httpclient.execute(httpPost, responseHandler);
		if (responseHandler.isMustFollow()) {
			HttpGet httpGetStepOne = null;
			httpGetStepOne = new HttpGet(responseBody);
			setHeader(httpGetStepOne);
			responseBody = httpclient.execute(httpGetStepOne, responseHandler);

			result = isLogined(responseBody);
			if (!result) {
				loginResult.setForm(parse(host, responseBody));
			}
		}
		loginResult.setResult(result);
		return loginResult;
	}

	public static void main(String[] args) throws Exception {
		final String host = "http://localhost/drupal-6.28/";
		final AdminControlFrame frame = new AdminControlFrame("Amdin control",
				host);
		frame.setVisible(true);
	}
}
