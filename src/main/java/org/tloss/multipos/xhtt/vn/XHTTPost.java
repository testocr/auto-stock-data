package org.tloss.multipos.xhtt.vn;

import java.io.StringReader;
import java.util.Iterator;
import java.util.List;

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
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.PrettyXmlSerializer;
import org.htmlcleaner.TagNode;
import org.tloss.common.Article;
import org.tloss.common.DefaultResponseHandler;
import org.tloss.common.Image;
import org.tloss.common.ImageBody;
import org.tloss.common.UrlUtils;
import org.tloss.multiget.ghacks.GHacks;
import org.tloss.multipos.PostArticle;

public class XHTTPost implements PostArticle {
	HttpClient httpclient = new DefaultHttpClient();
	ResponseHandler<String> responseHandler = new DefaultResponseHandler();

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

		HttpGet httpGetStepOne = new HttpGet(getUrl(LOGIN_FORM_URL, null));
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
		List<?> list = document.selectNodes("//input");
		MultipartEntity entity = new MultipartEntity();
		for (Object object : list) {
			Element element = (Element) object;
			if (element.attributeValue("name") != null) {
				if (element.attributeValue("name").endsWith("account")
						|| element.attributeValue("name").endsWith("password")
						|| element.attributeValue("name").equals(
								"__EVENTTARGET")) {
					if (element.attributeValue("name").endsWith("account")) {
						StringBody body = new StringBody(username);
						entity.addPart(element.attributeValue("name"), body);
					} else if (element.attributeValue("name").equals(
							"__EVENTTARGET")) {
						StringBody body = new StringBody("tab$ctl16$lnkLogin");
						entity.addPart("__EVENTTARGET", body);
					} else {
						StringBody body = new StringBody(password);
						entity.addPart(element.attributeValue("name"), body);
					}
				} else {
					if (element.attributeValue("value") != null) {
						StringBody body = new StringBody(
								element.attributeValue("value"));

						entity.addPart(element.attributeValue("name"), body);
					}
				}
			}
		}
		HttpPost httpPost = new HttpPost(getUrl(LOGIN_POST_URL, null));
		httpPost.setEntity(entity);
		setHeader(httpPost);
		responseBody = httpclient.execute(httpPost, responseHandler);
		httpGetStepOne = new HttpGet(getUrl(HOME_PAGE, null));
		setHeader(httpGetStepOne);
		responseBody = httpclient.execute(httpGetStepOne, responseHandler);
		return responseBody.indexOf("Đăng xuất") >= 0;

	}

	public String getUrl(int type, Object[] options) {
		String url = "";
		switch (type) {
		case PostArticle.LOGIN_FORM_URL:
			url = "http://admin.xhtt.vn/Login.chn";
			break;
		case PostArticle.LOGIN_POST_URL:
			url = "http://admin.xhtt.vn/Login.chn";
			break;
		case PostArticle.POST_FORM_URL:
			url = "http://admin.xhtt.vn/office/add.chn";
			break;
		case PostArticle.POST_URL:
			url = "http://admin.xhtt.vn/office/add.chn";
			break;
		case PostArticle.HOME_PAGE:
			url = "http://admin.xhtt.vn/Default.aspx";
			break;

		case PostArticle.IMAGE_POST_FORM_URL_1:
			url = "http://admin.xhtt.vn/GUI/EditoralOffice/MainOffce/FileManager/default.aspx?function=avatar_loadValue&mode=single&share=share&i=";
			break;
		case PostArticle.IMAGE_POST_FORM_URL_2:
			if (options != null && options.length > 0) {
				url = "http://admin.xhtt.vn/Scripts/UploadFile/Default.aspx?currentFolder="
						+ options[0];
			}
			break;
		}
		return url;
	}

	public boolean login(String username, String password,
			boolean encrytedPassword, Object[] options) throws Exception {
		if (!encrytedPassword) {
			return login(username, password);
		} else {
			throw new Exception("Unsupport login with encryted password!");
		}
	}

	public boolean post(Article article, String urlEdit, String urlPost,
			Object[] options) throws Exception {
		for (Image image : article.getImages()) {
			uploadImage(image);
		}
		initHttpClient(httpclient);

		HttpGet httpGetStepOne = new HttpGet(urlEdit);
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
		List<?> list = document.selectNodes("//form[@id='RenderTable']//input");
		MultipartEntity entity = new MultipartEntity();
		for (Object object : list) {
			Element element = (Element) object;
			if (element.attributeValue("name") != null) {
				if (element.attributeValue("name").equals(
						"tab$ctl16$ctl02$txtSubTitle")) {
					StringBody body = new StringBody((String) options[0]);
					entity.addPart("tab$ctl16$ctl02$txtSubTitle", body);

				} else if (element.attributeValue("name").equals(
						"tab$ctl16$ctl02$txtTitle")) {
					StringBody body = new StringBody(article.getTitle());
					entity.addPart("tab$ctl16$ctl02$txtTitle", body);

				} else if (element.attributeValue("name").equals(
						"tab$ctl16$ctl02$txtSelectedFile")) {
					StringBody body = new StringBody((String) options[1]);
					entity.addPart("tab$ctl16$ctl02$txtSelectedFile", body);

				} else if (element.attributeValue("name").equals(
						"tab$ctl16$ctl02$NewsContent")) {
					StringBody body = new StringBody(article.getContent());
					entity.addPart("tab$ctl16$ctl02$NewsContent", body);

				} else {
					if (!element.attributeValue("name").equals(
							"tab$ctl16$ctl02$btnSend")
							&& element.attributeValue("name") != null) {

						if (element.attributeValue("value") != null) {
							StringBody body = new StringBody(
									element.attributeValue("value"));
							entity.addPart(element.attributeValue("name"), body);

						}
					}
				}
			}
		}
		list = document.selectNodes("//form[@id='RenderTable']//select");
		for (Object object : list) {
			Element element = (Element) object;
			if (element.attributeValue("name") != null) {
				if (element.attribute("name").getValue()
						.equals("tab$ctl16$ctl02$lstCat")) {
					StringBody body = new StringBody((String) options[2]);
					entity.addPart("tab$ctl16$ctl02$lstCat", body);

				} else {
					Iterator<?> iterator = element.nodeIterator();
					Element first = null;
					boolean selected = false;
					for (; iterator.hasNext();) {
						Node ele = (Node) iterator.next();
						if (iterator instanceof Element) {
							Element element2 = (Element) ele;
							if (first == null)
								first = element2;
							if (element2.attribute("selected") != null
									&& "selected".equals(element2.attribute(
											"selected").getValue())) {
								selected = true;
								StringBody body = new StringBody(
										element2.attributeValue("value"));
								entity.addPart(element.attribute("name")
										.getValue(), body);

							}
						}

					}
					if (!selected && first != null) {
						StringBody body = new StringBody(
								first.attributeValue("value"));
						entity.addPart(element.attribute("name").getValue(),
								body);

					}
				}
			}
		}

		StringBody body = new StringBody(article.getDesciption());
		entity.addPart("tab$ctl16$ctl02$txtInit", body);

		body = new StringBody("");
		entity.addPart("tab$ctl16$ctl02$hidLuongSuKien", body);

		body = new StringBody("");
		entity.addPart("tab$ctl16$ctl02$txtIcon", body);

		body = new StringBody("");
		entity.addPart("tab$ctl16$ctl02$txtImageTitle", body);

		body = new StringBody("");
		entity.addPart("tab$ctl16$ctl02$txtSource", body);

		body = new StringBody("");
		entity.addPart("tab$ctl16$ctl02$hdRelatNewsTitle", body);

		body = new StringBody("");
		entity.addPart("tab$ctl16$ctl02$hdMediaTitle", body);

		body = new StringBody("");
		entity.addPart("tab$ctl16$ctl02$hdRelatNews", body);

		body = new StringBody("");
		entity.addPart("tab$ctl16$ctl02$hdMedia", body);

		HttpPost httpPost = new HttpPost(urlPost);
		httpPost.setEntity(entity);
		setHeader(httpPost);
		responseBody = httpclient.execute(httpPost, responseHandler);
		// httpGetStepOne = new HttpGet(getUrl(HOME_PAGE, null));
		// setHeader(httpGetStepOne);
		// responseBody = httpclient.execute(httpGetStepOne, responseHandler);
		// System.out.println(responseBody);
		System.out.println(responseBody);
		return responseBody.indexOf("Đăng xuất") >= 0;
	}

	public String uploadImage(Image image) throws Exception {
		HttpGet httpGetStepOne = new HttpGet(
				getUrl(IMAGE_POST_FORM_URL_1, null));
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
		List<?> list = document.selectNodes("//input[@id='postBackArg']");
		String link = null;
		for (Object object : list) {
			Element element = (Element) object;
			link = element.attributeValue("value");
		}
		if (link != null) {
			link = getUrl(IMAGE_POST_FORM_URL_2, new Object[] { link });
		}
		httpGetStepOne = new HttpGet(link);
		setHeader(httpGetStepOne);
		responseBody = httpclient.execute(httpGetStepOne, responseHandler);
		tagNode = new HtmlCleaner(props).clean(new StringReader(responseBody));
		xml = new PrettyXmlSerializer(props).getAsString(tagNode, "utf-8");
		document = reader.read(new StringReader(xml));
		list = document.selectNodes("//form[@id='form1']//input");
		MultipartEntity entity = new MultipartEntity();
		for (Object object : list) {
			Element element = (Element) object;
			if ("file2".equals(element.attributeValue("name"))) {
				ImageBody fileBody = new ImageBody(image,
						UrlUtils.getFileName(image.getUrl()));
				entity.addPart("file2", fileBody);
			} else if (element.attributeValue("name") != null) {
				StringBody body = new StringBody(
						element.attributeValue("value") == null ? ""
								: element.attributeValue("value"));
				entity.addPart(element.attributeValue("name"), body);
			}
		}

		HttpPost httpPost = new HttpPost(link);
		httpPost.setEntity(entity);
		setHeader(httpPost);
		responseBody = httpclient.execute(httpPost, responseHandler);
		return null;
	}

	public void logout() {

	}

	public static void main(String[] args) throws Exception {
		XHTTPost post = new XHTTPost();
		GHacks makeUseOf = new GHacks();
		Article[] articles = makeUseOf
				.getAll("http://www.ghacks.net/tag/windows-7/");
		System.out.println(post.login("trantung", "z712211z74119"));
		post.post(
				articles[0],
				post.getUrl(POST_FORM_URL, null),
				post.getUrl(POST_URL, null),
				new Object[] {
						"tungt84@gmail.com",
						"Images/Uploaded/Share/2011/09/2011090104041055/freeburningsoftware.png",
						"252" });
	}
}
