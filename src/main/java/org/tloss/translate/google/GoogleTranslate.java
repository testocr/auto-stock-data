package org.tloss.translate.google;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.AbstractHttpMessage;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.HTTP;
import org.tloss.common.Article;
import org.tloss.common.ByteArrayResponseHandler;
import org.tloss.common.DefaultResponseHandler;
import org.tloss.multiget.ghacks.GHacks;
import org.tloss.translate.Translate;

/**
 * 
 * @author tungt
 * 
 */
public class GoogleTranslate implements Translate {
	public static Logger logger = Logger.getLogger(GoogleTranslate.class
			.getName());

	public GoogleTranslate() {
		initScriptEngine();
	}

	public Article transalte(Article article, String lang1, String lang2)
			throws Exception {
		try {
			String content = translate(article.getContent(), lang1, lang2);
			String title = translate(article.getTitle(), lang1, lang2);
			String desciption = translate(article.getDesciption(), lang1, lang2);
			Article art = new Article();
			art.setContent(content);
			art.setCreate(article.getCreate());
			art.setDesciption(desciption);
			art.setImages(article.getImages());
			art.setSource(art.getSource());
			art.setTitle(title);
			art.setUrl(article.getUrl());
			art.setSource(article.getSource());
			return art;
		} catch (Exception e) {
			logger.log(Level.INFO,"Error to transale URL: " + article.getUrl());
			throw e;
		}
	}

	public String translate(String data, String lang1, String lang2)
			throws Exception {
		String responseBody = null;
		try {
			initHttpClient(httpclient);
			HttpPost httpPost = new HttpPost(
					"http://translate.google.com.vn/translate_a/t");
			setHeader(httpPost);
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			nvps.add(new BasicNameValuePair("client", "t"));
			nvps.add(new BasicNameValuePair("text", data));
			nvps.add(new BasicNameValuePair("hl", lang1));
			nvps.add(new BasicNameValuePair("sl", lang1));
			nvps.add(new BasicNameValuePair("tl", lang2));
			nvps.add(new BasicNameValuePair("multires", "1"));
			nvps.add(new BasicNameValuePair("otf", "1"));
			nvps.add(new BasicNameValuePair("pc", "1"));
			nvps.add(new BasicNameValuePair("ssel", "0"));
			nvps.add(new BasicNameValuePair("tsel", "0"));
			httpPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
			responseBody = httpclient.execute(httpPost, responseHandler);
			Object result = invocableEngine.invokeFunction(
					"getTranslateString", responseBody);
			return result.toString();
		} catch (Exception e) {
			logger.log(Level.INFO, "Error to transale", e);
			logger.log(Level.INFO, "Error data: " + responseBody);
			throw e;
		}
	}

	HttpClient httpclient = new DefaultHttpClient();
	ResponseHandler<String> responseHandler = new DefaultResponseHandler();
	ResponseHandler<byte[]> byteArrayResponseHandler = new ByteArrayResponseHandler();
	Invocable invocableEngine;

	public Invocable initScriptEngine() {
		ScriptEngineManager mgr = new ScriptEngineManager();
		ScriptEngine jsEngine = mgr.getEngineByName("JavaScript");
		try {
			jsEngine.eval(new InputStreamReader(GoogleTranslate.class
					.getResourceAsStream("google.js")));
			invocableEngine = (Invocable) jsEngine;
		} catch (ScriptException e) {
		}
		return invocableEngine;
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

	public static void main(String[] args) throws Exception {
		GHacks makeUseOf = new GHacks();
		Article[] articles = makeUseOf
				.getAll("http://www.ghacks.net/tag/windows-7/");
		GoogleTranslate googleTranslate = new GoogleTranslate();
		for (int i = 0; i < articles.length; i++) {
			Article article = googleTranslate
					.transalte(articles[i], "en", "vi");
			System.out.println(article.getContent());
		}
	}

}
