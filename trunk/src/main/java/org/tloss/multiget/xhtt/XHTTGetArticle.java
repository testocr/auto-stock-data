package org.tloss.multiget.xhtt;

import java.io.StringReader;
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
import org.tloss.common.DefaultResponseHandler;
import org.tloss.multiget.GetArticle;

public class XHTTGetArticle implements GetArticle {

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
		return true;
	}

	/**
	 * Tieu de= //div[@id='content-main']/div[class='cont-article']/h1<br/>
	 * Noi dung= //div[@id='divContentXHTT']<br/>
	 * 
	 */
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
		List<?> list = document
				.selectNodes("//div[@id='content-main']/div[@class='cont-article']/h1");
		String title = "";
		for (Object object : list) {
			Element element = (Element) object;
			title = element.getTextTrim();
		}
		String content = "";
		list = document.selectNodes("//div[@id='divContentXHTT']");
		for (Object object : list) {
			Element element = (Element) object;
			content = getContent(element);
		}
		return new Article(title, content);
	}

	public void logout() {
		// TODO Auto-generated method stub

	}

	public boolean login(String username, String password,
			boolean encrytedPassword, Object[] options) throws Exception {
		return true;
	}

	public String getContent(Element element) {
		StringBuffer buffer = new StringBuffer();
		Iterator<Node> divNodes = element.nodeIterator();
		for (; divNodes.hasNext();) {
			Node div = (Node) divNodes.next();
			if (div.getNodeType() == Node.ELEMENT_NODE) {
				Iterator<Node> divChilrenNodes = ((Element) div).nodeIterator();
				for (; divChilrenNodes.hasNext();) {
					Node node = (Node) divChilrenNodes.next();
					if ("img".equalsIgnoreCase(node.getName())
							&& node.getNodeType() == Node.ELEMENT_NODE) {
						Element img = (Element) node;
						if (img.attributeValue("src") != null) {
							buffer.append("[IMG]")
									.append(img.attributeValue("src"))
									.append("[/IMG]");
						}
					} else if ("a".equalsIgnoreCase(node.getName())
							&& node.getNodeType() == Node.ELEMENT_NODE) {
						Element a = (Element) node;
						if (a.attributeValue("href") != null) {
							buffer.append("[URL=\"")
									.append(a.attributeValue("href"))
									.append("\"]").append(a.getTextTrim())
									.append("[/URL]");
						}
					} else {
						buffer.append(node.getText());
					}
				}
			} else {
				buffer.append(div.getText());
			}
		}
		return buffer.toString();
	}

	public static void main(String[] args) throws Exception {
		XHTTGetArticle article = new XHTTGetArticle();
		article.login("", "");
		Article a = article
				.get("http://xahoithongtin.com.vn/20110511015619360p0c252/tiny-burner-chuong-trinh-ghi-dia-nho-gon-va-nhieu-tinh-nang.htm");
		System.out.println(a.getContent());
	}

	public Article[] getAll(String url) throws Exception {
		return new Article[] { get(url) };
	}

	public Article[] get(String[] url) throws Exception {
		if (url != null) {
			ArrayList<Article> articles = new ArrayList<Article>();
			Article article;
			for (int i = 0; i < url.length; i++) {
				article = get(url[i]);
				articles.add(article);
			}
			Article[] result = new Article[articles.size()];
			articles.toArray(result);
			return result;
		}
		return null;
	}

	public String[] getAllURL(String url) throws Exception {

		return new String[] { url };
	}

}
