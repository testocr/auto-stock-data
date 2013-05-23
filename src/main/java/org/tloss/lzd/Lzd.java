/**
 * Copyright (c) 2011 TLOSS. All rights reserved.
 * Created on Sep 1, 2011
 * File name : ZingDeal.java
 * Package org.tloss.zingdeal
 */
package org.tloss.lzd;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.AbstractHttpMessage;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.HTTP;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.PrettyXmlSerializer;
import org.htmlcleaner.TagNode;
import org.tloss.common.Article;
import org.tloss.common.DefaultResponseAndFollowHandler;
import org.tloss.multipos.PostArticle;

/**
 * @author tungt
 * 
 */
public class Lzd {
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

	public boolean login(String username, String password) throws Exception {
		return login(username, password, false, null);
	}

	public String getUrl(int type, Object[] options) {
		String url = "";
		switch (type) {
		case PostArticle.LOGIN_FORM_URL:
			url = "http://51deal.vn/account/login.php";
			break;
		case PostArticle.LOGIN_POST_URL:
			url = "http://51deal.vn/account/login.php";
			break;
		case PostArticle.POST_FORM_URL:
			if (options != null && options.length > 0) {
				url = "http://www.ddth.com/newthread.php?do=newthread&f="

				+ options[0];
			}
			break;
		case PostArticle.POST_URL:
			if (options != null && options.length > 0) {
				url = "http://www.ddth.com/newthread.php?do=postthread&f="
						+ options[0];
			}
			break;
		}
		return url;
	}

	public boolean post(Article article, String urlEdit, String urlPost,
			Object[] options) throws Exception {
		boolean result = false;
		initHttpClient(httpclient);

		HttpGet httpGetStepOne = new HttpGet(urlEdit);
		setHeader(httpGetStepOne);
		String responseBody = httpclient.execute(httpGetStepOne,
				responseHandler);
		// responseBody =responseBody.replaceAll("&quot;TÃ¡m&quot;", "Tam");
		responseBody = responseBody.replaceAll("\"TÃ¡m\"", "Tam");

		// System.out.println(responseBody);
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
		// System.out.println(xml);
		SAXReader reader = new SAXReader();
		Document document = reader.read(new StringReader(xml));
		// <input type="hidden" value="0" id="vB_Editor_001_mode"
		// name="wysiwyg">
		List<?> list = document.selectNodes("//div[@class='MuaDeal']/a");
		String link = null;
		for (Object object : list) {
			Element element = (Element) object;
			if (element.attribute("href") != null) {
				if (link == null)
					link = element.attribute("href").getValue();
			}
		}
		if (!"#this".equals(link)) {
			httpGetStepOne = new HttpGet("http://deal.zing.vn" + link);
			setHeader(httpGetStepOne);
			responseBody = httpclient.execute(httpGetStepOne, responseHandler);
			tagNode = new HtmlCleaner(props).clean(new StringReader(
					responseBody));
			// serialize to xml file
			xml = new PrettyXmlSerializer(props).getAsString(tagNode, "utf-8");
			// System.out.println(xml);
			reader = new SAXReader();
			document = reader.read(new StringReader(xml));
			list = document.selectNodes("//form[@id='DealBuyForm']");
			for (Object object : list) {
				Element element = (Element) object;
				if (element.attribute("action") != null) {

					link = element.attribute("action").getValue();
				}
				HttpPost httpPost = new HttpPost("http://deal.zing.vn" + link);
				List<NameValuePair> nvps = new ArrayList<NameValuePair>();
				List<?> list2 = element
						.selectNodes("//form[@id='DealBuyForm']//input");
				for (Object object2 : list2) {
					Element element2 = (Element) object2;
					if (element2.attribute("name") != null
							&& element2.attribute("value") != null) {
						if (element2.attribute("id") != null
								&& ("DealPaymentTypeIdTmp6".equals(element2
										.attribute("id").getValue()) || "DealPaymentTypeId6"
										.equals(element2.attribute("id")
												.getValue()))) {

						} else if ("data[Deal][user_id]".equals(element2
								.attribute("name").getValue())) {
							nvps.add(new BasicNameValuePair(element2.attribute(
									"name").getValue(), (String) options[0]));
							System.out.println(element2.attribute("name")
									.getValue() + " : " + (String) options[0]);
						} else {

							nvps.add(new BasicNameValuePair(element2.attribute(
									"name").getValue(), element2.attribute(
									"value").getValue()));
							System.out.println(element2.attribute("name")
									.getValue()
									+ " : "
									+ element2.attribute("value").getValue());
						}
					}

				}
				list2 = element
						.selectNodes("//form[@id='DealBuyForm']//select");
				for (Object object2 : list2) {
					Element element2 = (Element) object2;
					if (element2.attribute("name") != null) {
						Iterator<?> iterator = element2.nodeIterator();
						for (; iterator.hasNext();) {
							Node ele3 = (Node) iterator.next();
							if (ele3 instanceof Element) {
								Element element3 = (Element) ele3;
								if (element3.attribute("selected") != null
										&& "selected".equals(element3
												.attribute("selected")
												.getValue())
										&& element3.attribute("value") != null) {
									nvps.add(new BasicNameValuePair(element2
											.attribute("name").getValue(),
											element3.attribute("value")
													.getValue()));
									System.out.println(element2.attribute(
											"name").getValue()
											+ " : "
											+ element3.attribute("value")
													.getValue());
								}
							}
						}
					}

				}

				httpPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
				setHeader(httpPost);
				responseBody = httpclient.execute(httpPost, responseHandler);
				System.out.println(responseBody);
			}
			result = true;
		}

		return result;
	}

	public void logout() {

	}

	public boolean login(String username, String password,
			boolean encrytedPassword, Object[] options) throws Exception {
		boolean result = false;
		initHttpClient(httpclient);
		CleanerProperties props = new CleanerProperties();
		// set some properties to non-default values
		props.setTranslateSpecialEntities(true);
		props.setTransResCharsToNCR(true);
		props.setOmitComments(true);

		HttpGet httpGetStepOne = null;
		String responseBody = null;
		// access to https://www.lazada.vn/customer/account/login/
		httpGetStepOne = new HttpGet(
				"https://www.lazada.vn/customer/account/login/");
		setHeader(httpGetStepOne);
		responseBody = httpclient.execute(httpGetStepOne, responseHandler);

		TagNode tagNode = new HtmlCleaner(props).clean(new StringReader(
				responseBody));
		// serialize to xml file
		String xml = new PrettyXmlSerializer(props).getAsString(tagNode,
				"utf-8");
		// System.out.println(xml);
		SAXReader reader = new SAXReader();
		Document document = reader.read(new StringReader(xml));
		List<?> list = document
				.selectNodes("//form[@id='form-account-login']//input[@name='YII_CSRF_TOKEN']");
		String YII_CSRF_TOKEN = null;
		for (Object object : list) {
			Element element = (Element) object;
			if (element.attribute("value") != null) {
				YII_CSRF_TOKEN = element.attributeValue("value");
			}
		}

		HttpPost httpPost = new HttpPost(
				"https://www.lazada.vn/customer/account/login/");
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("YII_CSRF_TOKEN", YII_CSRF_TOKEN));
		nvps.add(new BasicNameValuePair("LoginForm[email]", username));
		nvps.add(new BasicNameValuePair("LoginForm[password]", password));
		nvps.add(new BasicNameValuePair("pagetype", "customeraccount"));

		httpPost.setEntity(new UrlEncodedFormEntity(nvps));
		setHeader(httpPost);
		responseBody = httpclient.execute(httpPost, responseHandler);
		if (responseHandler.isMustFollow()) {
			// https://www.lazada.vn/customer/account/
			httpGetStepOne = new HttpGet("https://www.lazada.vn" + responseBody);
			setHeader(httpGetStepOne);
			responseBody = httpclient.execute(httpGetStepOne, responseHandler);
		}
		return result;
	}

	public void addToCart(String url, String vc,String p,String sku)
			throws ClientProtocolException, IOException, DocumentException {
		CleanerProperties props = new CleanerProperties();
		// set some properties to non-default values
		props.setTranslateSpecialEntities(true);
		props.setTransResCharsToNCR(true);
		props.setOmitComments(true);
		HttpGet httpGetStepOne = null;
		String responseBody = null;
		// access to https://www.lazada.vn/customer/account/login/
		httpGetStepOne = new HttpGet(url);
		setHeader(httpGetStepOne);
		responseBody = httpclient.execute(httpGetStepOne, responseHandler);

		TagNode tagNode = new HtmlCleaner(props).clean(new StringReader(
				responseBody));
		// serialize to xml file
		String xml = new PrettyXmlSerializer(props).getAsString(tagNode,
				"utf-8");
		// System.out.println(xml);
		SAXReader reader = new SAXReader();
		Document document = reader.read(new StringReader(xml));
		String YII_CSRF_TOKEN = null;
		List<?> list = document.selectNodes("//input[@name='YII_CSRF_TOKEN']");
		for (Object object : list) {
			Element element = (Element) object;
			if (element.attribute("value") != null) {
				YII_CSRF_TOKEN = element.attributeValue("value");
			}
		}
		String addCartUrl = "http://www.lazada.vn/ajax/cart/add/?return=json&p="+p+"&sku="+sku+"&quantity=1&YII_CSRF_TOKEN="
				+ YII_CSRF_TOKEN;
		httpGetStepOne = new HttpGet(addCartUrl);
		setHeader(httpGetStepOne);
		responseBody = httpclient.execute(httpGetStepOne, responseHandler);
		boolean cantView = false;
		boolean stop = false;
		while (!stop) {
			httpGetStepOne = new HttpGet("http://www.lazada.vn/cart/index/");
			setHeader(httpGetStepOne);
			responseBody = httpclient.execute(httpGetStepOne, responseHandler);

			tagNode = new HtmlCleaner(props).clean(new StringReader(
					responseBody));
			// serialize to xml file
			xml = new PrettyXmlSerializer(props).getAsString(tagNode, "utf-8");
			// System.out.println(xml);

			document = reader.read(new StringReader(xml));
			YII_CSRF_TOKEN = null;
			if (!cantView) {
				cantView = true;
			} else {
				list = document
						.selectNodes("//div[@class='box s-error mbs msgBox']/div");
				
				if (list == null || list.size() == 0) {
					stop =  true;
				}else{
					for (Object object : list) {
						Element element = (Element) object;
					}
				}
			}

			list = document.selectNodes("//input[@name='YII_CSRF_TOKEN']");
			for (Object object : list) {
				Element element = (Element) object;
				if (element.attribute("value") != null) {
					YII_CSRF_TOKEN = element.attributeValue("value");
				}
			}
			// YII_CSRF_TOKEN 01ef9a0df18fe4ff04e642b0df0557ebeb55c56e
			// cartUpdate 1
			// qty_NO793EL28IBJANVN-60216 1
			// couponcode 1ESCA165.1
			HttpPost httpPost = new HttpPost("http://www.lazada.vn/cart/");
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			nvps.add(new BasicNameValuePair("YII_CSRF_TOKEN", YII_CSRF_TOKEN));
			nvps.add(new BasicNameValuePair("cartUpdate", "1"));
			nvps.add(new BasicNameValuePair("qty_"+sku, "1"));
			nvps.add(new BasicNameValuePair("couponcode",vc));

			httpPost.setEntity(new UrlEncodedFormEntity(nvps));
			setHeader(httpPost);
			responseBody = httpclient.execute(httpPost, responseHandler);
			if(responseHandler.isMustFollow()){
				if(responseBody.indexOf("/cart/index/")==-1){
					stop =  true;
				}
			}

		}
		httpGetStepOne = new HttpGet("http://www.lazada.vn/checkout/index/");
		setHeader(httpGetStepOne);
		responseBody = httpclient.execute(httpGetStepOne, responseHandler);
		httpGetStepOne = new HttpGet("http://www.lazada.vn/checkout/finish/");
		setHeader(httpGetStepOne);
		responseBody = httpclient.execute(httpGetStepOne, responseHandler);
		
		tagNode = new HtmlCleaner(props).clean(new StringReader(
				responseBody));
		// serialize to xml file
		xml = new PrettyXmlSerializer(props).getAsString(tagNode, "utf-8");
		// System.out.println(xml);
		document = reader.read(new StringReader(xml));
		
		list = document.selectNodes("//form[@action='/checkout/finish/']//input");
		
		HttpPost httpPost = new HttpPost("https://www.lazada.vn/checkout/finish/");
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		
		
		for (Object object : list) {
			Element element = (Element) object;
			if (element.attribute("name") != null) {
				nvps.add(new BasicNameValuePair(element.attributeValue("name"), element.attributeValue("value")));
			}
		}
		
		httpPost.setEntity(new UrlEncodedFormEntity(nvps));
		setHeader(httpPost);
		responseBody = httpclient.execute(httpPost, responseHandler);
		System.out.println(responseBody);
	}

	public static void main(String[] args) throws Exception {
		Lzd lzd = new Lzd();
		lzd.login("myname74119@gmail.com", "");

		String vc = "1SALES01ExQT72ct";
		String url = "http://www.lazada.vn/Nokia-Lumia-710-LCD-37-5MP-8GB-Trang-28284.html";
		String p = "NO793EL15PVWANVN";
		String sku ="NO793EL15PVWANVN-28290";
		
		lzd.addToCart(url, vc,p,sku);

	}
}