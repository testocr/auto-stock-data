/**
 * Copyright (c) 2011 TLOSS. All rights reserved.
 * Created on Oct 20, 2011
 * File name : MuaCungLoanTin.java
 * Package org.tloss.muachung
 */
package org.tloss.muachung.v2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
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
import org.tloss.common.Article;
import org.tloss.common.DefaultResponseHandler;
import org.tloss.common.PasswordUtils;
import org.tloss.multipos.PostArticle;

/**
 * @author tungt
 * 
 */
public class MuaCungLoanTin {
	/**
	 * 
	 */
	public MuaCungLoanTin() {
		initScriptEngine();
		vistedLink = new ArrayList<String>();
	}

	HttpClient httpclient = new DefaultHttpClient();
	ResponseHandler<String> responseHandler = new DefaultResponseHandler();

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

	/**
	 * buoc 1 : <br/>
	 * truy cap vao http://haiphongit.com/forum/index.php<br/>
	 * Lay thong tin securitytoken trong the input<br/>
	 * <input type="hidden" name="securitytoken"
	 * value="1308967949-a4c8fcec93507a24dc933e6878c9da0afb2347af" /><br/>
	 * Buoc 2: ma hoa mat khau voi md5<br/>
	 * "http://haiphongit.com/forum/login.php?do=login"<br/>
	 * vb_login_username =username<br/>
	 * vb_login_password<br/>
	 * s<br/>
	 * securitytoken =noi dung securitytoken<br/>
	 * do =login<br/>
	 * vb_login_md5password =mat khau ma hoa<br/>
	 * vb_login_md5password_utf =HaiPhongITPost<br/>
	 * Buoc3: kiem tra dang nhap thanh cong va chuyen ve trang chu<br/>
	 * http://haiphongit.com/forum/index.php<br/>
	 * 
	 * @throws Exception
	 */
	public boolean login(String username, String password) throws Exception {
		return login(username, password, false, null);
	}

	public String getUrl(int type, Object[] options) {
		String url = "";
		switch (type) {
		case PostArticle.LOGIN_FORM_URL:
			url = "http://muachung.vn//ajax.php?act=customer&code=login";
			break;
		case PostArticle.LOGIN_POST_URL:
			url = "http://muachung.vn//ajax.php?act=customer&code=login";
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

	List<String> vistedLink;

	/**
	 * buoc 0: vao link efit<br/>
	 * http://haiphongit.com/forum/newthread.php?do=newthread&f=90<br/>
	 * buoc 1: gui bai<br/>
	 * http://haiphongit.com/forum/newthread.php?do=postthread&f=90<br/>
	 * subject tieu de <br/>
	 * threaddesc mo ta <br/>
	 * message nodung <br/>
	 * wysiwyg 0<br/>
	 * iconid 0<br/>
	 * s <br/>
	 * securitytoken so ma sercurity<br/>
	 * f chuyen muc. vi du 90<br/>
	 * do postthread<br/>
	 * posthash <br/>
	 * poststarttime<br/>
	 * loggedinuser 195249<br/>
	 * sbutton Gá»Ÿi Ã�á»� TÃ i Má»›i<br/>
	 * parseurl 1<br/>
	 * emailupdate 9999<br/>
	 * polloptions 4<br/>
	 * 
	 * buoc2: rediect den bai viet <br/>
	 * http://haiphongit.com/forum/showthread.php?p=207598#post207598
	 * 
	 * 
	 */
	public boolean post(Article article, String urlEdit, String urlPost,
			Object[] options) throws Exception {

		boolean result = false;
		if (!vistedLink.contains(urlEdit)) {
			List<String> newCollectedLink = (List<String>) options[0];
			vistedLink.add(urlEdit);
			initHttpClient(httpclient);

			HttpGet httpGetStepOne = new HttpGet(urlEdit);
			setHeader(httpGetStepOne);
			String responseBody = httpclient.execute(httpGetStepOne,
					responseHandler);
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
			List<?> list = document
					.selectNodes("//a[contains(@class,'buttonLoantin')]");
			for (Object object : list) {
				Element element = (Element) object;
				if (element.attribute("id") != null
						&& element.attributeValue("onclick") != null) {
					// btnLoantin1300
					String id = element.attribute("id").getValue();
					id = id.substring(10);
					HttpPost httpPost = new HttpPost(
							"http://muachung.vn/ajax.php?act=connect&mccode=check-loantin");

					List<NameValuePair> nvps = new ArrayList<NameValuePair>();
					nvps.add(new BasicNameValuePair("item_id", id));
					nvps.add(new BasicNameValuePair("yahoo",
							(String) options[2]));
					nvps.add(new BasicNameValuePair("facebook",
							(String) options[1]));
					nvps.add(new BasicNameValuePair("content", ""));
					nvps.add(new BasicNameValuePair("rand", String.valueOf(Math
							.random())));
					httpPost.setEntity(new UrlEncodedFormEntity(nvps,
							HTTP.UTF_8));
					setHeader(httpPost);
					responseBody = httpclient
							.execute(httpPost, responseHandler);

					System.out.println("proccess id: " + id + ", result: "
							+ responseBody);
				}
			}
			list = document.selectNodes("//div[@class='title']/a/@href");
			for (Object object : list) {
				Node node = (Node) object;
				String newUrl = node.getText();
				if (!newCollectedLink.contains(newUrl)
						&& !vistedLink.contains(newCollectedLink)) {
					newCollectedLink.add(newUrl);
				}
			}
			result = true;
		}

		return result;
	}

	public boolean isNew(String dburl, String url, String user) {
		try {
			Connection con = getConnection(dburl);
			if (con != null) {
				PreparedStatement stmt = null;
				String query = "select * from muachung where url =? and username=?";
				try {
					stmt = con.prepareStatement(query);
					stmt.setString(1, url);
					stmt.setString(2, user);
					ResultSet rs = stmt.executeQuery();
					while (rs.next()) {
						return false;
					}
				} catch (SQLException e) {

				} finally {
					if (stmt != null) {
						try {
							stmt.close();
						} catch (SQLException e) {
							e.printStackTrace(System.out);
						}
					}
					if (con != null) {
						try {
							con.close();
						} catch (SQLException e) {
							e.printStackTrace(System.out);
						}
					}
				}
			}
		} catch (SQLException e1) {
			e1.printStackTrace(System.out);
		}
		return true;

	}

	public void deleleVistedLink(String dburl, String url, String user) {
		Connection con;
		try {
			con = getConnection(dburl);
			if (con != null) {
				PreparedStatement stmt = null;
				String query = "delete from muachung where url =? and username=?";
				try {
					stmt = con.prepareStatement(query);
					stmt.setString(1, url);
					stmt.setString(2, user);
					stmt.executeUpdate();
					con.commit();

				} catch (SQLException e) {
					e.printStackTrace(System.out);
				} finally {
					if (stmt != null) {
						try {
							stmt.close();
						} catch (SQLException e) {
							e.printStackTrace(System.out);
						}
					}
					if (con != null) {
						try {
							con.close();
						} catch (SQLException e) {
							e.printStackTrace(System.out);
						}
					}
				}
			}

		} catch (SQLException e1) {
			e1.printStackTrace(System.out);
		}
	}

	public void initStartLink(String dburl, String url[], String user) {
		if (url != null) {
			for (int i = 0; i < url.length; i++) {
				deleleVistedLink(dburl, url[i], user);
			}
		}
	}

	public void saveVistedLink(String dburl, String url, String user) {
		Connection con;
		try {
			con = getConnection(dburl);
			if (con != null) {
				PreparedStatement stmt = null;
				String query = "insert into muachung(url,username) values(?,?)";
				try {
					stmt = con.prepareStatement(query);
					stmt.setString(1, url);
					stmt.setString(2, user);
					stmt.executeUpdate();
					con.commit();

				} catch (SQLException e) {
					e.printStackTrace(System.out);
				} finally {
					if (stmt != null) {
						try {
							stmt.close();
						} catch (SQLException e) {
							e.printStackTrace(System.out);
						}
					}
					if (con != null) {
						try {
							con.close();
						} catch (SQLException e) {
							e.printStackTrace(System.out);
						}
					}
				}
			}

		} catch (SQLException e1) {
			e1.printStackTrace(System.out);
		}
	}

	public boolean post2(Article article, String urlEdit, String urlPost,
			Object[] options) throws Exception {

		boolean result = false;
		String user = (String) options[0];
		String dbUrl = (String) options[3];
		if (isNew(dbUrl, urlEdit, user)) {
			saveVistedLink(dbUrl, urlEdit, user);
			initHttpClient(httpclient);

			HttpGet httpGetStepOne = new HttpGet(urlEdit);
			setHeader(httpGetStepOne);
			String responseBody = httpclient.execute(httpGetStepOne,
					responseHandler);
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
			List<?> list = document
					.selectNodes("//a[contains(@class,'buttonLoantin')]");
			for (Object object : list) {
				Element element = (Element) object;
				if (element.attribute("id") != null
						&& element.attributeValue("onclick") != null) {
					// btnLoantin1300
					String id = element.attribute("id").getValue();
					id = id.substring(10);
					HttpPost httpPost = new HttpPost(
							"http://muachung.vn/ajax.php?act=connect&mccode=check-loantin");

					List<NameValuePair> nvps = new ArrayList<NameValuePair>();
					nvps.add(new BasicNameValuePair("item_id", id));
					nvps.add(new BasicNameValuePair("yahoo",
							(String) options[2]));
					nvps.add(new BasicNameValuePair("facebook",
							(String) options[1]));
					nvps.add(new BasicNameValuePair("content", ""));
					nvps.add(new BasicNameValuePair("rand", String.valueOf(Math
							.random())));
					httpPost.setEntity(new UrlEncodedFormEntity(nvps,
							HTTP.UTF_8));
					setHeader(httpPost);
					responseBody = httpclient
							.execute(httpPost, responseHandler);

					System.out.println("proccess id: " + id + ", result: "
							+ responseBody);
				}
			}
			result = true;
		}

		return result;
	}

	public void logout() {
		try {
			initHttpClient(httpclient);
			HttpPost httpPost = new HttpPost(
					"http://muachung.vn/ajax.php?act=customer&code=logout");
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			nvps.add(new BasicNameValuePair("rand", String.valueOf(Math
					.random())));
			httpPost.setEntity(new UrlEncodedFormEntity(nvps));
			setHeader(httpPost);
			String responseBody = httpclient.execute(httpPost, responseHandler);
			System.out.println(responseBody);
		} catch (Exception e) {

		}
	}

	public boolean login(String username, String password,
			boolean encrytedPassword, Object[] options) throws Exception {
		boolean result = false;
		initHttpClient(httpclient);

		HttpPost httpPost = new HttpPost(getUrl(PostArticle.LOGIN_POST_URL,
				null));
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("email", username));
		nvps.add(new BasicNameValuePair("pass", password));
		nvps.add(new BasicNameValuePair("save", "off"));
		nvps.add(new BasicNameValuePair("rand", String.valueOf(Math.random())));
		httpPost.setEntity(new UrlEncodedFormEntity(nvps));
		setHeader(httpPost);
		String responseBody = httpclient.execute(httpPost, responseHandler);
		result = (Boolean) invocableEngine.invokeFunction("checkLoginSucess",
				responseBody);
		System.out.println(responseBody);
		return result;
	}

	Invocable invocableEngine;

	public Invocable initScriptEngine() {
		ScriptEngineManager mgr = new ScriptEngineManager();
		ScriptEngine jsEngine = mgr.getEngineByName("JavaScript");
		try {
			jsEngine.eval(new InputStreamReader(MuaCungLoanTin.class
					.getResourceAsStream("muachung.js")));
			invocableEngine = (Invocable) jsEngine;
		} catch (ScriptException e) {
		}
		return invocableEngine;
	}

	public List<String> collectionLink(String[] startUrls) {
		List<String> collectedLink = new ArrayList<String>();
		List<String> newCollectedLink = new ArrayList<String>();
		List<String> vistedLink = new ArrayList<String>();
		List<String> temp;
		for (int j = 0; j < startUrls.length; j++) {
			String startUrl = startUrls[j];
			collectedLink.add(startUrl);
		}
		do {
			for (Iterator<String> iterator = collectedLink.iterator(); iterator
					.hasNext();) {
				String startUrl = (String) iterator.next();
				collectionLink(startUrl, newCollectedLink, vistedLink);
			}
			collectedLink.clear();
			temp = collectedLink;
			collectedLink = newCollectedLink;
			newCollectedLink = temp;

		} while (!(collectedLink.isEmpty() && newCollectedLink.isEmpty()));
		return vistedLink;
	}

	public boolean haveCollectionLink() {
		return new File("collectionLink.properties").exists();
	}

	public boolean createCollectionLinkFile(List<String> vistedLink) {
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(
					"collectionLink.properties");
			Properties properties = new Properties();
			String links = "";
			for (String link : vistedLink) {
				if (links.equals("")) {
					links = link;
				} else {
					links = links + "," + link;
				}
			}
			properties.setProperty("collectionLink", links);
			properties.store(fileOutputStream, "");
			fileOutputStream.flush();
			fileOutputStream.close();
		} catch (Exception e) {
			e.printStackTrace(System.out);
			return false;
		}
		return true;
	}

	public List<String> loadCollectionLink() {
		ArrayList<String> result = new ArrayList<String>();
		try {
			Properties properties = new Properties();
			properties.load(new FileInputStream("collectionLink.properties"));
			String links = properties.getProperty("collectionLink");
			if (links != null) {
				String[] linkArray = links.split(",");
				for (int i = 0; i < linkArray.length; i++) {
					result.add(linkArray[i]);
				}
			}
		} catch (Exception e) {
			e.printStackTrace(System.out);
			result.clear();
		}
		return result;
	}

	public void collectionLink(String urlEdit, List<String> newCollectedLink,
			List<String> vistedLink) {
		try {
			if (!vistedLink.contains(urlEdit)) {
				vistedLink.add(urlEdit);
				initHttpClient(httpclient);

				HttpGet httpGetStepOne = new HttpGet(urlEdit);
				setHeader(httpGetStepOne);
				String responseBody = httpclient.execute(httpGetStepOne,
						responseHandler);
				// System.out.println(responseBody);
				// lay ra noi dung xml
				CleanerProperties props = new CleanerProperties();
				// set some properties to non-default values
				props.setTranslateSpecialEntities(true);
				props.setTransResCharsToNCR(true);
				props.setOmitComments(true);
				// do parsing
				TagNode tagNode = new HtmlCleaner(props)
						.clean(new StringReader(responseBody));
				// serialize to xml file
				String xml = new PrettyXmlSerializer(props).getAsString(
						tagNode, "utf-8");
				// System.out.println(xml);
				SAXReader reader = new SAXReader();
				Document document = reader.read(new StringReader(xml));
				// <input type="hidden" value="0" id="vB_Editor_001_mode"
				// name="wysiwyg">
				List<?> list = document
						.selectNodes("//div[@class='title' or @class='titleBoxName' or @class='SellingTitle mTop5']/a/@href");
				for (Object object : list) {
					Node node = (Node) object;
					String newUrl = node.getText();
					if (!newCollectedLink.contains(newUrl)
							&& !vistedLink.contains(newCollectedLink)) {
						newCollectedLink.add(newUrl);
					}
				}
				list = document
						.selectNodes("//div[@class='blockSoldItem blockSoldHide1 mTop10 blockSoldItemBorder']/div/a/@href");
				for (Object object : list) {
					Node node = (Node) object;
					String newUrl = node.getText();
					if (!newCollectedLink.contains(newUrl)
							&& !vistedLink.contains(newCollectedLink)) {
						newCollectedLink.add(newUrl);
					}
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}

	protected Connection getConnection(String url) throws SQLException {
		try {
			Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
			Connection conn = null;
			Properties connectionProps = new Properties();
			connectionProps.put("user", "");
			connectionProps.put("password", "");
			conn = DriverManager.getConnection(url, connectionProps);
			return conn;
		} catch (ClassNotFoundException e) {

		}
		return null;

	}

	public static void main(String[] args) throws Exception {
		PasswordUtils.loadKeyStore();
		MuaCungLoanTin cungLoanTin = new MuaCungLoanTin();
		Properties properties = new Properties();
		properties.load(new FileInputStream("muachung.properties"));
		String[] usernames = null;
		if (args != null && args.length >= 1) {
			usernames = new String[] { args[0] };
		} else {
			usernames = properties.getProperty("username", "").split(",");
		}
		String[] passwords = properties.getProperty("password", "").split(",");
		String[] startUrls = properties.getProperty("startUrl", "").split(",");
		String facebook = properties.getProperty("facebook", "0");
		String yahoo = properties.getProperty("yahoo", "1");
		String databaseUrl = properties.getProperty("databaseUrl",
				"jdbc:derby:muachung");
		List<String> collectionLink;
		if (!cungLoanTin.haveCollectionLink()) {
			List<String> vistedLink = cungLoanTin.collectionLink(startUrls);
			cungLoanTin.createCollectionLinkFile(vistedLink);
			collectionLink = vistedLink;
		} else {
			collectionLink = cungLoanTin.loadCollectionLink();
		}
		for (int i = 0; i < usernames.length; i++) {
			String username = usernames[i];
			String password;
			if (i >= passwords.length) {
				password = "";
			} else {
				password = PasswordUtils.decryt(passwords[i]);
			}
			cungLoanTin.initStartLink(databaseUrl, startUrls, username);
			if (cungLoanTin.login(username, password)) {

				for (Iterator<String> iterator = collectionLink.iterator(); iterator
						.hasNext();) {
					String startUrl = (String) iterator.next();
					cungLoanTin.post2(null, startUrl, null, new Object[] {
							username, facebook, yahoo, databaseUrl });
				}

				cungLoanTin.logout();
			}
		}
		System.out.println(">>> DONE");
	}
}
