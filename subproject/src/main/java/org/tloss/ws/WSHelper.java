package org.tloss.ws;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.http.HttpHost;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.AbstractHttpMessage;
import org.apache.http.params.CoreProtocolPNames;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.SimpleXmlSerializer;
import org.htmlcleaner.TagNode;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.btr.proxy.search.ProxySearch;

public class WSHelper {
	ResponseHandler<String> responseHandler = new DefaultResponseHandler();
	DocumentBuilder builder = null;
	private Logger logger = LoggerFactory.getLogger(WSHelper.class);
	public static int step = 0;
	DefaultHttpClient httpclient = null;

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

	public void init(boolean userProxy, String testURL, String user,
			String password, String domain) throws URISyntaxException {
		httpclient = new DefaultHttpClient();
		httpclient.getParams().setParameter(
				CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_0); // Default
																			// to
																			// HTTP
																			// 1.0
		httpclient.getParams().setParameter(
				CoreProtocolPNames.HTTP_CONTENT_CHARSET, "UTF-8");
		httpclient.getParams().setParameter(ClientPNames.COOKIE_POLICY,
				CookiePolicy.BROWSER_COMPATIBILITY);
		if (userProxy) {
			ProxySearch proxySearch = ProxySearch.getDefaultProxySearch();
			ProxySelector myProxySelector = proxySearch.getProxySelector();
			ProxySelector.setDefault(myProxySelector);
			List<Proxy> list = myProxySelector.select(new URI(testURL));
			if (list != null) {
				Proxy proxy = null;
				for (int i = 0; i < list.size(); i++) {
					proxy = list.get(i);
					InetSocketAddress address = (InetSocketAddress) proxy
							.address();
					String hostName = address.getHostName();
					int port = address.getPort();
					NTCredentials credentials = new NTCredentials(user,
							password, hostName, domain);
					httpclient.getCredentialsProvider().setCredentials(
							new AuthScope(hostName, port), credentials);
					HttpHost httpHost = new HttpHost(hostName, port);
					httpclient.getParams().setParameter(
							ConnRoutePNames.DEFAULT_PROXY, httpHost);

				}
			}
		}
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory
				.newInstance();
		try {
			builder = builderFactory.newDocumentBuilder();
			builder.setEntityResolver(new EntityResolver() {

				public InputSource resolveEntity(String publicId,
						String systemId) throws SAXException, IOException {
					return new InputSource(new StringReader(""));
				}
			});
		} catch (ParserConfigurationException e) {
			logger.error("DocumentBuilderFactory is inited fail.", e);
		}
	}

	public Object executeGet(String requestURL, String format)
			throws IOException {
		logger.info("Get URL: " + requestURL);
		HttpGet httpGetStepOne = new HttpGet(requestURL);
		setHeader(httpGetStepOne);
		String responseBody = httpclient.execute(httpGetStepOne,
				responseHandler);
		if ("JSON".endsWith(format)) {
			try {
				step++;
				logger.info("write content to file : " + "step_" + step
						+ ".html");
				FileOutputStream fileOutputStream = new FileOutputStream(
						"step_" + step + ".html");
				fileOutputStream.write(responseBody.getBytes());
				fileOutputStream.flush();
				fileOutputStream.close();
			} catch (Exception e) {
				logger.error("Error write file.", e);
			}
			Object obj = JSONValue.parse(responseBody);
			return obj;
		}
		CleanerProperties props = new CleanerProperties();
		// set some properties to non-default values
		props.setTranslateSpecialEntities(true);
		props.setTransResCharsToNCR(true);
		props.setOmitComments(true);
		logger.info("clear html to xml");
		// do parsing
		TagNode tagNode = new HtmlCleaner(props).clean(responseBody);
		// serialize to xml file
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		new SimpleXmlSerializer(props).writeToStream(tagNode, out, "UTF-8");
		try {
			step++;
			logger.info("write content to file : " + "step_" + step + ".html");
			FileOutputStream fileOutputStream = new FileOutputStream("step_"
					+ step + ".html");
			fileOutputStream.write(out.toByteArray());
			fileOutputStream.flush();
			fileOutputStream.close();
		} catch (Exception e) {
			logger.error("Error write file.", e);
		}
		logger.info("build the document");
		Document document = null;
		try {
			document = builder
					.parse(new ByteArrayInputStream(out.toByteArray()));

		} catch (SAXException e) {
			logger.error("DocumentBuilderFactory is inited fail.", e);
		} catch (IOException e) {
			logger.error("DocumentBuilderFactory is inited fail.", e);
		}
		return document;
	}

	public Node selectNode(String xpath, Object doc)
			throws XPathExpressionException {
		XPath xPath = XPathFactory.newInstance().newXPath();
		Node node = (Node) xPath.compile(xpath).evaluate(doc,
				XPathConstants.NODE);
		return node;
	}

	public List<Node> selectNodes(String xpath, Object doc)
			throws XPathExpressionException {
		XPath xPath = XPathFactory.newInstance().newXPath();
		NodeList nodeList = (NodeList) xPath.compile(xpath).evaluate(doc,
				XPathConstants.NODESET);
		ArrayList<Node> nodes = new ArrayList<Node>();
		for (int i = 0; i < nodeList.getLength(); i++) {
			nodes.add(nodeList.item(i));
		}
		return nodes;
	}

	public Document select(DataSource dataSource, String sql, List<?> formats,
			List<?> strParams) {
		Document document = builder.newDocument();
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		try {
			connection = dataSource.getConnection();
			String finalSQL = String.format(sql, formats.toArray());
			preparedStatement = connection.prepareStatement(finalSQL);
			initPreparedStatement(preparedStatement, strParams.toArray());
			rs = preparedStatement.executeQuery();
			ResultSetMetaData rsmtadta;
			rsmtadta = rs.getMetaData();
			int colCount = rsmtadta.getColumnCount();
			Element row;
			Element col;
			Text val;
			Element rows = document.createElement("rows");
			document.appendChild(rows);
			while (rs.next()) {
				row = document.createElement("row");
				rows.appendChild(row);
				for (int i = 1; i <= colCount; i++) {
					col = document.createElement(rsmtadta.getColumnName(i));
					val = document.createTextNode(rs.getString(rsmtadta
							.getColumnName(i)));
					row.appendChild(col);
					col.appendChild(val);
				}
			}
			rs.close();
			rs = null;
			preparedStatement.close();
			preparedStatement = null;
			connection.close();
			connection = null;
			return document;

		} catch (SQLException e) {
			logger.error("Fail to execute sql.", e);
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e1) {
					logger.error("Fail to execute sql.", e1);
				}
			}
			if (preparedStatement != null) {
				try {
					preparedStatement.close();
				} catch (SQLException e1) {
					logger.error("Fail to execute sql.", e1);
				}
			}
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e1) {
					logger.error("Fail to execute sql.", e1);
				}
			}
		}

		return document;
	}

	protected void initPreparedStatement(PreparedStatement preparedStatement,
			Object... objects) throws SQLException {
		if (objects != null) {
			for (int i = 0; i < objects.length; i++) {
				if (objects[i] != null) {
					if (objects[i] instanceof Long) {
						preparedStatement.setLong(1 + i, (Long) objects[i]);
					} else if (objects[i] instanceof Integer) {
						preparedStatement.setInt(1 + i, (Integer) objects[i]);
					} else if (objects[i] instanceof Float) {
						preparedStatement.setFloat(1 + i, (Float) objects[i]);
					} else if (objects[i] instanceof Double) {
						preparedStatement.setDouble(1 + i, (Double) objects[i]);
					} else if (objects[i] instanceof String) {
						preparedStatement.setString(1 + i,
								(String) objects[i].toString());
					}
				} else {
					preparedStatement.setNull(1 + i, Types.NULL);
				}
			}
		}
	}

	public int update(DataSource dataSource, String sql, List<?> formats,
			List<?> strParams) {
		logger.info("execute SQL: "+sql);
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		try {
			connection = dataSource.getConnection();
			connection.setAutoCommit(false);
			String finalSQL = String.format(sql, formats.toArray());
			preparedStatement = connection.prepareStatement(finalSQL);
			initPreparedStatement(preparedStatement, strParams.toArray());
			int rs = preparedStatement.executeUpdate();
			connection.commit();
			preparedStatement.close();
			preparedStatement = null;
			connection.close();
			connection = null;
			return rs;

		} catch (SQLException e) {
			try {
				connection.rollback();
			} catch (SQLException e1) {
				logger.error("Fail to execute sql.", e1);
			}
			logger.error("Fail to execute sql.", e);
		} finally {
			if (preparedStatement != null) {
				try {
					preparedStatement.close();
				} catch (SQLException e1) {
					logger.error("Fail to execute sql.", e1);
				}
			}
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e1) {
					logger.error("Fail to execute sql.", e1);
				}
			}
		}

		return 0;
	}

	public Integer getInteger(String val) {
		if (val == null || "".equals(val))
			return null;
		return Integer.parseInt(val);
	}

	public Long getLong(String val) {
		if (val == null || "".equals(val))
			return null;
		return Long.parseLong(val);
	}

	public Float getFloat(String val) {
		if (val == null || "".equals(val))
			return null;
		return Float.parseFloat(val);
	}
}
