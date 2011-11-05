package org.tloss.vatgia;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.http.HttpVersion;
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
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.PrettyXmlSerializer;
import org.htmlcleaner.TagNode;
import org.tloss.common.DefaultResponseHandler;
import org.tloss.common.Image;
import org.tloss.common.PasswordUtils;
import org.tloss.common.utils.Utils;

public class Vatgia {
	HttpClient httpclient = new DefaultHttpClient();
	ResponseHandler<String> responseHandler = new DefaultResponseHandler();
	int counter = 0;
	int maxRequest;

	public Vatgia() {
		initScriptEngine();
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
			getBonus(xml);
			List<?> list = document.selectNodes("//div[@class='name']/a/@href");
			for (int i = 0; i < list.size(); i++) {
				Node node = (Node) list.get(i);
				httpGetStepOne = new HttpGet("http://www.vatgia.com"
						+ node.getText());
				setHeader(httpGetStepOne);
				responseBody = httpclient.execute(httpGetStepOne,
						responseHandler);
				tagNode = new HtmlCleaner(props).clean(new StringReader(
						responseBody));
				xml = new PrettyXmlSerializer(props).getAsString(tagNode,
						"utf-8");
				getBonus(xml);
				counter++;
				mustWait();
			}
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

	long currentMoney = -1;
	long tryCount = 0;
	long maxTryCount = 5;

	public void getBonus(String content) throws Exception {
		mustWaitMin();

		int index = content.indexOf("/ajax_v2/bonus/");
		if (index > 0) {
			int index2 = content.indexOf(";", index);
			if (index2 > 0) {
				String sub = content.substring(0, index2 + 1);
				int index3 = sub.lastIndexOf("<![CDATA[");
				if (index3 > 0) {
					String js = sub.substring(index3 + 9);
					js = "function getBonus(){ " + js
							+ " return src_script + '.js'; }";
					invocableEngine.eval(js);
					Object object = ((Invocable) invocableEngine)
							.invokeFunction("getBonus", new Object[] {});
					String link = "http://vatgia.com" + object;
					initHttpClient(httpclient);
					HttpGet httpGetStepOne = new HttpGet(link);
					setHeader(httpGetStepOne);
					String responseBody = httpclient.execute(httpGetStepOne,
							responseHandler);

					if (responseBody != null) {
						int index4 = responseBody
								.indexOf("<b style=\"color: red;\">");
						if (index4 > 0) {
							if (tryCount < maxRequest) {
								tryCount = 0;
							}
							int index5 = responseBody.indexOf("</b>");
							if (index5 > 0) {
								String amount = responseBody.substring(
										index4 + 23, index5);
								System.out.println(amount);
								long lamount = Long.parseLong(amount
										.replaceAll("\\.", ""));
								if (lamount != currentMoney) {
									currentMoney = lamount;
									if (currentMoney > 2000) {
										System.out
												.println("Enought money fo transfer!");
										mustWait();
										convert();
									}
								} else {
									System.exit(0);
								}
							}
						} else {
							if (tryCount < maxTryCount) {
								tryCount++;
							} else {
								System.exit(0);
							}
						}
					}
				}
			}
		}
	}

	Captcha captcha = new Captcha();

	public void convert() throws Exception {
		initHttpClient(httpclient);
		HttpGet httpGetStepOne = new HttpGet(
				"http://slave.vatgia.com/profile/?module=view_bonus");
		setHeader(httpGetStepOne);
		String responseBody = httpclient.execute(httpGetStepOne,
				responseHandler);
		int index = responseBody.indexOf("/home/security_code.php");
		if (index > 0) {
			int index2 = responseBody.indexOf("\"", index);
			if (index2 > 0) {
				String url = responseBody.substring(index, index2);
				mustWaitMin();
				Image image = Utils.download("http://slave.vatgia.com" + url,
						false, httpclient);
				InputStream in = new ByteArrayInputStream(image.getData());
				BufferedImage img = ImageIO.read(in);
				String fileName = captcha.antiNoise(img);
				String result = captcha.recognizeText(fileName);
				System.out.println("captcha result: " + result);
				if (captcha.validate(result.trim())) {
					mustWait();
					HttpPost httpPost = new HttpPost(
							"http://slave.vatgia.com/profile/?module=view_bonus");
					MultipartEntity entity = new MultipartEntity();
					entity.addPart("security_code",
							new StringBody(result.trim()));
					entity.addPart("actions", new StringBody("convert"));
					httpPost.setEntity(entity);
					setHeader(httpPost);
					responseBody = httpclient
							.execute(httpPost, responseHandler);
				} else {
					System.out.println("error captcha: " + fileName);
				}
			}
		}
	}

	ScriptEngine invocableEngine;

	public ScriptEngine initScriptEngine() {
		ScriptEngineManager mgr = new ScriptEngineManager();
		ScriptEngine jsEngine = mgr.getEngineByName("JavaScript");
		invocableEngine = jsEngine;
		return invocableEngine;
	}

	public void getUrls(String startUrl, ArrayList<String> arr)
			throws Exception {
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
		getBonus(xml);
		List<?> list = document
				.selectNodes("//div[@class='content']/ul[@class='list_category']/li[@class='fl']/a/@href");

		if (list.isEmpty()) {
			// http://www.vatgia.com/392/may-tinh-linh-kien.html
			String[] temp = startUrl.split("/");
			arr.add(temp[3]);
		} else {
			for (int i = 0; i < list.size(); i++) {
				Node node = (Node) list.get(i);
				String link = node.getText();
				mustWait();
				getUrls("http://www.vatgia.com" + link, arr);
			}
		}
	}

	public static void main(String[] args) throws Exception {
		try {
			Vatgia vatgia = new Vatgia();
			PasswordUtils.loadKeyStore();
			Properties properties = new Properties();
			properties.load(new FileInputStream("vatgia.properties"));
			String username = properties.getProperty("username", "");
			String password = properties.getProperty("password", "");
			String maxRequest = properties.getProperty("maxRequest", "500");
			int maxReq = Integer.parseInt(maxRequest);
			vatgia.setMaxRequest(maxReq);
			String[] startUrls = properties.getProperty("startUrl", "").split(
					",");
			password = PasswordUtils.decryt(password);
			ArrayList<String> urls = new ArrayList<String>();
			if (vatgia.login(username, password)) {
				vatgia.mustWait();
				for (int i = 0; i < startUrls.length; i++) {
					try {
						urls.clear();
						vatgia.getUrls(startUrls[i], urls);
						for (int j = 0; j < urls.size(); j++) {
							vatgia.sendRequest("http://www.vatgia.com/home/listudv.php?module=product&iCat="
									+ urls.get(j));
						}
					} catch (Exception e) {
						e.printStackTrace(System.out);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}
}
