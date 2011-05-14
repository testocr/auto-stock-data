package org.tloss.vietstock;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.List;
import java.util.Properties;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.PrettyXmlSerializer;
import org.htmlcleaner.TagNode;

public class VietStock {
	public static void main(String[] args) throws ClientProtocolException,
			IOException, DocumentException {
		Properties properties = new Properties();
		properties.load(VietStock.class
				.getResourceAsStream("/vietstock.properties"));
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(
				properties
						.getProperty("vietstock.url"));

		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		String responseBody = httpclient.execute(httpGet, responseHandler);
		System.out.println(responseBody);
		
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
		List<?> list = document.selectNodes(properties
				.getProperty("vietstock.xpath.stockcode"));
		Document outDocument = DocumentHelper.createDocument();
		Element stock = outDocument.addElement("stocks");
		
		System.out.println(MessageFormat.format(properties.getProperty("vietstock.xpath.ce"), "dadada"));
		for (Object object : list) {
			Element element = (Element) object;
			String stockCode = element.getTextTrim().replaceAll("[^\\w]", "");
			Element ce = (Element) document.selectSingleNode(MessageFormat
					.format(properties.getProperty("vietstock.xpath.ce"),
							stockCode));
			Element fl = (Element) document.selectSingleNode(MessageFormat
					.format(properties.getProperty("vietstock.xpath.fl"),stockCode));
			Element pr = (Element) document.selectSingleNode(MessageFormat
					.format(properties.getProperty("vietstock.xpath.pr"),stockCode));
			stock.addElement("stock").addAttribute("code", stockCode)
					.addAttribute("ce", ce.getTextTrim())
					.addAttribute("fl", fl.getTextTrim())
					.addAttribute("pr", pr.getTextTrim());
			System.out.println(stockCode);
		}
		FileWriter fileWriter = new FileWriter("out.xml");
		OutputFormat format = OutputFormat.createPrettyPrint();
		XMLWriter writer = new XMLWriter(fileWriter, format);
		writer.write(outDocument);
		fileWriter.flush();
		fileWriter.close();
	}
}
