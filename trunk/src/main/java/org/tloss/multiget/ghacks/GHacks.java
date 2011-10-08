package org.tloss.multiget.ghacks;

import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
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
import org.tloss.common.ByteArrayResponseHandler;
import org.tloss.common.DefaultResponseHandler;
import org.tloss.common.Image;
import org.tloss.multiget.AutoGetArticle;

public class GHacks implements AutoGetArticle {
	HttpClient httpclient = new DefaultHttpClient();
	ResponseHandler<String> responseHandler = new DefaultResponseHandler();
	ResponseHandler<byte[]> byteArrayResponseHandler = new ByteArrayResponseHandler();

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
		return true;
	}

	public boolean login(String username, String password,
			boolean encrytedPassword, Object[] options) throws Exception {
		return true;
	}

	SimpleDateFormat dateFormat = new SimpleDateFormat(" E MMMM dd yyyy ");

	public Image download(String url, boolean skipData) throws Exception {
		Image image = new Image();
		image.setUrl(url);
		if (!skipData) {
			HttpGet httpGetStepOne = new HttpGet(url);
			setHeader(httpGetStepOne);
			byte[] bs = httpclient.execute(httpGetStepOne,
					byteArrayResponseHandler);
			image.setData(bs);
		}
		return image;
	}

	public Article get(String url) throws Exception {
		initHttpClient(httpclient);

		HttpGet httpGetStepOne = new HttpGet(url);
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
		List<?> list = document.selectNodes("//div[@class='article']/h1");
		String data = "";
		Article article = new Article();
		article.setDesciption(url);
		for (Object object : list) {
			Element element = (Element) object;
			data = element.getTextTrim();
			article.setTitle(data);
		}
		list = document.selectNodes("//div[@class='content']/p");
		StringBuffer buffer = new StringBuffer();
		for (Object object : list) {
			Element element = (Element) object;
			if ("tags".equals(element.attributeValue("class"))) {
				data = element.getTextTrim();
				// "Author: , Tuesday August 30, 2011 - Tags: , , , ,"
				String[] temps = data.split(",");
				String temp = temps[1] + temps[2];
				temps = temp.split("-");
				article.setCreate(dateFormat.parse(temps[0]));
			} else {
				Iterator<?> divChilrenNodes = ((Element) element)
						.nodeIterator();
				for (; divChilrenNodes.hasNext();) {
					Node node = (Node) divChilrenNodes.next();
					if ("img".equalsIgnoreCase(node.getName())
							&& node.getNodeType() == Node.ELEMENT_NODE) {
						Element img = (Element) node;

						Image image = download(img.attributeValue("src"), true);
						article.getImages().add(image);
						if (image.hashCode() < 0) {
							buffer.append(" IMGM")
									.append(Math.abs(image.hashCode()))
									.append(" ");
						} else {
							buffer.append(" IMG").append(image.hashCode())
									.append(" ");
						}
					}
				}
				data = element.getTextTrim();
				buffer.append(data);
			}

		}
		article.setContent(buffer.toString());
		article.setSource("ghacks");
		return article;
	}

	public void logout() {
	}

	public boolean isNew(String url, Object[] data) {
		return false;
	}

	public Article[] getAll(String url) throws Exception {
		ArrayList<Article> articles = new ArrayList<Article>();
		initHttpClient(httpclient);

		HttpGet httpGetStepOne = new HttpGet(url);
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
		List<?> list = document.selectNodes("//div[@class='article']/h1/a");
		String link = "";
		Article article;
		for (Object object : list) {
			Element element = (Element) object;
			link = element.attributeValue("href");
			article = get(link);
			articles.add(article);

		}
		Article[] result = new Article[articles.size()];
		articles.toArray(result);
		return result;
	}

	public static void main(String[] args) throws Exception {
		GHacks makeUseOf = new GHacks();
		Article[] articles = makeUseOf
				.getAll("http://www.ghacks.net/tag/windows-7/");

	}
}
