package org.tloss.stock.strade;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.DeflateDecompressingEntity;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.jboss.aerogear.unifiedpush.JavaSender;
import org.jboss.aerogear.unifiedpush.SenderClient;
import org.jboss.aerogear.unifiedpush.message.MessageResponseCallback;
import org.jboss.aerogear.unifiedpush.message.UnifiedMessage;
import org.json.JSONObject;
import org.tloss.common.Image;
import org.tloss.common.utils.Utils;
import org.tloss.stock.IStock;
import org.tloss.stock.Order;
import org.tloss.stock.RSAUtils;
import org.tloss.stock.StockInfo;
import org.tloss.stock.StockOrderPage;
import org.tloss.stock.TrustEverythingSSLTrustManager;
import org.tloss.stock.utils.HtmlUtils;
import org.tloss.stock.utils.HtmlUtils.Form;
import org.tloss.stock.utils.HtmlUtils.Input;
import org.tloss.stock.utils.StringUtils;
import org.tloss.vatgia.Captcha;

public class Strade implements IStock {
	HttpClient httpclient = new DefaultHttpClient();
	JavaSender defaultJavaSender = new SenderClient.Builder(
			"https://www.runningchild.com:28443/ag-push/").build();

	private static InputStream decompress(final HttpEntity entity)
			throws IOException {
		final Header encodingHeader = entity.getContentEncoding();

		if (encodingHeader != null) {
			final String encoding = encodingHeader.getValue().toLowerCase();

			if (encoding.equals("gzip")) {
				return new GzipDecompressingEntity(entity).getContent();
			} else if (encoding.equals("deflate")) {
				return new DeflateDecompressingEntity(entity).getContent();
			}
		}

		return entity.getContent();
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

	Captcha captcha = new Captcha("strade.captcha.properties");

	protected String getCaptch() {
		boolean stop = false;
		while (!stop) {
			try {
				HttpPost httppost = new HttpPost(
						"https://www.strade.com.vn/OnlineTrading/Account/GenNew");
				httppost.setHeader(new BasicHeader("User-Agent",
						"Mozilla/5.0 (Windows NT 6.3; WOW64; rv:37.0) Gecko/20100101 Firefox/37.0"));
				httppost.setHeader(new BasicHeader("Accept",
						"application/json, text/javascript, */*; q=0.01"));
				httppost.setHeader(new BasicHeader("Accept-Language",
						"en-US,en;q=0.5"));
				httppost.setHeader(new BasicHeader("Accept-Encoding",
						"gzip, deflate"));
				httppost.setHeader(new BasicHeader("X-Requested-With",
						"XMLHttpRequest"));
				httppost.setHeader(new BasicHeader(
						"Referer",
						"https://www.strade.com.vn/OnlineTrading/Account/Login?ReturnUrl=%2fOnlineTrading"));
				httppost.setHeader(new BasicHeader("Cache-Control", "no-cache"));

				httppost.setHeader(new BasicHeader("Connection", "keep-alive"));
				httppost.setHeader(new BasicHeader("Pragma", "no-cache"));

				HttpResponse response1 = httpclient.execute(httppost);
				HttpEntity entity = response1.getEntity();
				StringWriter writer = new StringWriter();
				InputStream inputStream = decompress(entity);
				IOUtils.copy(inputStream, writer);
				inputStream.close();
				String response = writer.toString();
				Image image = Utils.download("https://www.strade.com.vn/"
						+ response.replace("\"", ""), false, httpclient);
				InputStream in = new ByteArrayInputStream(image.getData());
				BufferedImage img = ImageIO.read(in);
				String fileName = captcha.antiNoise(img, "captcha");
				String result = captcha.recognizeText(fileName, "captcha");
				File file = new File("captcha", fileName);
				file.delete();
				if (result != null && result.trim().length() >= 5) {
					stop = true;
					return result.trim();
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return "";
	}

	public boolean login(String userName, String password) {
		initHttpClient(httpclient);

		try {
			HttpGet httpget = new HttpGet(
					"https://www.strade.com.vn/OnlineTrading/Account/Login?ReturnUrl=%2fOnlineTrading");
			httpget.setHeader(new BasicHeader("User-Agent",
					"Mozilla/5.0 (Windows NT 6.3; WOW64; rv:37.0) Gecko/20100101 Firefox/37.0"));
			httpget.setHeader(new BasicHeader("Accept",
					"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"));
			httpget.setHeader(new BasicHeader("Accept-Language",
					"en-US,en;q=0.5"));
			httpget.setHeader(new BasicHeader("Accept-Encoding",
					"gzip, deflate"));
			httpget.setHeader(new BasicHeader("Connection", "keep-alive"));
			HttpResponse response1 = httpclient.execute(httpget);
			try {
				HttpEntity entity = response1.getEntity();
				StringWriter writer = new StringWriter();
				InputStream inputStream = decompress(entity);
				IOUtils.copy(inputStream, writer);
				inputStream.close();
				String response = writer.toString();
				// System.out.println(response);
				Form form = HtmlUtils
						.parseForm(
								response,
								0,
								"<form action=\"/OnlineTrading/Account/Login?ReturnUrl=%2FOnlineTrading\"",
								"</form>");

				if (form.getFormContent() != null) {

					HtmlUtils.parseFormField(form.getFormContent(), 0,
							"<input", "/>", form);
					List<NameValuePair> formparams = new ArrayList<NameValuePair>();
					String Captcha = getCaptch();

					if (Captcha != null)
						Captcha = Captcha.trim();
					for (Input i : form.getInputs()) {
						if ("UserName".equals(i.getName()))
							i.setValue(userName);
						if ("Password".equals(i.getName()))
							i.setValue(password.toString());
						if ("Captcha".equals(i.getName()))
							i.setValue(Captcha);
						if (i.getName() != null) {
							formparams.add(new BasicNameValuePair(i.getName(),
									i.getValue() != null ? i.getValue().trim()
											: ""));
						}
					}

					UrlEncodedFormEntity urlEntity = new UrlEncodedFormEntity(
							formparams, "UTF-8");
					HttpPost httppost = new HttpPost(
							"https://www.strade.com.vn/OnlineTrading/Account/Login?ReturnUrl=%2FOnlineTrading");
					httppost.setEntity(urlEntity);
					httppost.setHeader(new BasicHeader("User-Agent",
							"Mozilla/5.0 (Windows NT 6.3; WOW64; rv:37.0) Gecko/20100101 Firefox/37.0"));
					httppost.setHeader(new BasicHeader("Accept",
							"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"));
					httppost.setHeader(new BasicHeader("Accept-Language",
							"en-US,en;q=0.5"));
					httpget.setHeader(new BasicHeader("Accept-Encoding",
							"gzip, deflate"));
					httppost.setHeader(new BasicHeader(
							"Referer",
							"https://www.strade.com.vn/OnlineTrading/Account/Login?ReturnUrl=%2fOnlineTrading"));
					httppost.setHeader(new BasicHeader("Cache-Control",
							"no-cache"));
					httppost.setHeader(new BasicHeader("Connection",
							"keep-alive"));
					httppost.setHeader(new BasicHeader("Pragma", "no-cache"));

					response1 = httpclient.execute(httppost);
					entity = response1.getEntity();
					writer = new StringWriter();
					inputStream = decompress(entity);
					IOUtils.copy(inputStream, writer);
					inputStream.close();
					response = writer.toString();
					// https://www.baovietsecurities.com.vn/WebOnlineTrading/Default.aspx

					if (response1.getStatusLine().getStatusCode() == 302) {
						httpget = new HttpGet(
								"https://www.strade.com.vn/OnlineTrading/");
						httpget.setHeader(new BasicHeader("User-Agent",
								"Mozilla/5.0 (Windows NT 6.3; WOW64; rv:37.0) Gecko/20100101 Firefox/37.0"));
						httpget.setHeader(new BasicHeader("Accept",
								"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"));
						httpget.setHeader(new BasicHeader("Accept-Language",
								"en-US,en;q=0.5"));
						httpget.setHeader(new BasicHeader("Accept-Encoding",
								"gzip, deflate"));
						httpget.setHeader(new BasicHeader(
								"Referer",
								"https://www.strade.com.vn/OnlineTrading/Account/Login?ReturnUrl=%2fOnlineTrading"));
						httpget.setHeader(new BasicHeader("Connection",
								"keep-alive"));
						response1 = httpclient.execute(httpget);

						entity = response1.getEntity();
						writer = new StringWriter();
						inputStream = decompress(entity);
						IOUtils.copy(inputStream, writer);
						inputStream.close();
						response = writer.toString();
						return true;
					}

				}

			} finally {
			}

		} catch (Exception e) {
			sendErrorNotification("ERROR at login", e);
		}

		return false;
	}

	protected void sendErrorNotification(String info, Exception e) {
		UnifiedMessage unifiedMessage = new UnifiedMessage.Builder()
				.pushApplicationId("d7ebb4ad-1cda-40a8-8bbb-8fe958d636f3")
				.masterSecret("68dc7315-410e-447f-b242-6e7c90b04944")
				.alert(info + " : " + e.getMessage()).build();
		defaultJavaSender.send(unifiedMessage, new MessageResponseCallback() {

			public void onComplete(int statusCode) {
				// do cool stuff
			}

			public void onError(Throwable throwable) {
				// bring out the bad news
				throwable.fillInStackTrace();
			}
		});
	}

	protected void sendNotification(String info) {

		UnifiedMessage unifiedMessage = new UnifiedMessage.Builder()
				.pushApplicationId("d7ebb4ad-1cda-40a8-8bbb-8fe958d636f3")
				.masterSecret("68dc7315-410e-447f-b242-6e7c90b04944")
				.alert(info).build();
		defaultJavaSender.send(unifiedMessage, new MessageResponseCallback() {

			public void onComplete(int statusCode) {
				// do cool stuff
			}

			public void onError(Throwable throwable) {
				// bring out the bad news
				throwable.printStackTrace();
			}
		});
	}

	public int getAmount() {
		try {
			List<NameValuePair> formparams = new ArrayList<NameValuePair>();
			formparams.add(new BasicNameValuePair("pv_afacctno", "0001102285"));
			UrlEncodedFormEntity urlEntity = new UrlEncodedFormEntity(
					formparams, "UTF-8");
			HttpPost httpget = new HttpPost(
					"https://www.strade.com.vn/OnlineTrading/Balance/GetGeneralAccountInfor");
			httpget.setEntity(urlEntity);
			httpget.setHeader(new BasicHeader("User-Agent",
					"Mozilla/5.0 (Windows NT 6.3; WOW64; rv:37.0) Gecko/20100101 Firefox/37.0"));
			httpget.setHeader(new BasicHeader("Accept",
					"application/json, text/javascript, */*; q=0.01"));
			httpget.setHeader(new BasicHeader("Accept-Language",
					"en-US,en;q=0.5"));
			httpget.setHeader(new BasicHeader("Accept-Encoding",
					"gzip, deflate"));
			httpget.setHeader(new BasicHeader("X-Requested-With",
					"XMLHttpRequest"));

			httpget.setHeader(new BasicHeader("Connection", "keep-alive"));
			httpget.setHeader(new BasicHeader("Referer",
					"https://www.strade.com.vn/OnlineTrading/"));
			HttpResponse response1 = httpclient.execute(httpget);

			HttpEntity entity = response1.getEntity();
			StringWriter writer = new StringWriter();
			InputStream inputStream = decompress(entity);
			IOUtils.copy(inputStream, writer);
			inputStream.close();
			String response = writer.toString();
			JSONObject jsonObject = new JSONObject(response);

			return Integer.parseInt(jsonObject.getString("bl_balance"));
			// System.out.println(response);
		} catch (Exception e) {
			sendErrorNotification("Error at getAmount", e);
		}
		return 0;
	}

	public int getFeePercent() {
		// TODO Auto-generated method stub
		return 0;
	}

	public StockInfo getStockInfo(String id) {
		StockInfo rs = null;
		try {

			List<NameValuePair> formparams = new ArrayList<NameValuePair>();
			formparams.add(new BasicNameValuePair("pv_symbol", id));
			formparams.add(new BasicNameValuePair("pv_ordertype", "1"));
			formparams.add(new BasicNameValuePair("pv_acctno", "0001102285"));
			UrlEncodedFormEntity urlEntity = new UrlEncodedFormEntity(
					formparams, "UTF-8");
			HttpPost httppost = new HttpPost(
					"https://www.strade.com.vn/OnlineTrading/Acc/getStockInfo");
			httppost.setEntity(urlEntity);
			httppost.setHeader(new BasicHeader("User-Agent",
					"Mozilla/5.0 (Windows NT 6.3; WOW64; rv:37.0) Gecko/20100101 Firefox/37.0"));

			httppost.setHeader(new BasicHeader("Accept-Language",
					"en-US,en;q=0.5"));
			httppost.setHeader(new BasicHeader("Accept-Encoding",
					"gzip, deflate"));
			httppost.setHeader(new BasicHeader("X-Requested-With",
					"XMLHttpRequest"));
			httppost.setHeader(new BasicHeader(
					"DXScript",
					"1_145,1_81,1_137,1_136,1_130,1_135,1_121,14_25,14_15,1_138,1_80,14_2,1_88,14_7,1_78,1_128,1_90,1_89,14_8,1_143,1_114,1_144,1_109,14_9,14_24,1_131,1_85,1_110,1_99,1_106,1_140,1_118,1_120,1_129,1_123,14_16,14_18,1_127,1_134,14_21,14_23,1_91,1_139,1_115,14_11,1_126"));

			httppost.setHeader(new BasicHeader("Referer",
					"https://www.strade.com.vn/OnlineTrading/"));
			httppost.setHeader(new BasicHeader("Cache-Control", "no-cache"));
			httppost.setHeader(new BasicHeader("Content-Type",
					"application/json; charset=UTF-8"));
			httppost.setHeader(new BasicHeader("Connection", "keep-alive"));
			httppost.setHeader(new BasicHeader("Pragma", "no-cache"));

			HttpResponse response1 = httpclient.execute(httppost);
			HttpEntity entity = response1.getEntity();
			StringWriter writer = new StringWriter();
			InputStream inputStream = decompress(entity);
			IOUtils.copy(inputStream, writer);
			inputStream.close();
			String response = writer.toString();
			System.out.println(response);
			JSONObject jsonObject = new JSONObject(response);
			rs = new StockInfo();
			rs.setId(jsonObject.getString("symbol"));
			rs.setHightPrice(Integer.parseInt(jsonObject
					.getString("ceilingPrice")));
			rs.setLowPrice(Integer.parseInt(jsonObject.getString("floorPrice")));
			rs.setPrice(Integer.parseInt(jsonObject.getString("referencePrice")));

		} catch (Exception e) {
			sendErrorNotification("Errror at getStockInfo", e);
		}
		return rs;
	}

	public boolean cancelOder(String id, String orderId, String pin,
			String Qtty, String Price) {
		boolean rs = false;
		try {

			StringEntity urlEntity = new StringEntity("{\"Symbol\":\"" + id
					+ "\",\"AfAcctno\":\"0001102285\",\"Qtty\":\"" + Qtty
					+ "\",\"Price\":\"" + Price
					+ "\",\"Side\":\"NS\",\"OrderId\":\"" + orderId + "\"}",
					"UTF-8");
			HttpPost httppost = new HttpPost(
					"https://www.strade.com.vn/OnlineTrading/Order/cancelorder");
			httppost.setEntity(urlEntity);
			httppost.setHeader(new BasicHeader("User-Agent",
					"Mozilla/5.0 (Windows NT 6.3; WOW64; rv:37.0) Gecko/20100101 Firefox/37.0"));
			httppost.setHeader(new BasicHeader("Accept",
					"application/json, text/javascript, */*; q=0.01"));
			httppost.setHeader(new BasicHeader("Accept-Language",
					"en-US,en;q=0.5"));
			httppost.setHeader(new BasicHeader("Accept-Encoding",
					"gzip, deflate"));
			httppost.setHeader(new BasicHeader("X-Requested-With",
					"XMLHttpRequest"));
			httppost.setHeader(new BasicHeader("Referer",
					"https://www.strade.com.vn/OnlineTrading/"));
			httppost.setHeader(new BasicHeader("Cache-Control", "no-cache"));
			httppost.setHeader(new BasicHeader("Content-Type",
					"application/json; charset=UTF-8"));
			httppost.setHeader(new BasicHeader("Connection", "keep-alive"));
			httppost.setHeader(new BasicHeader("Pragma", "no-cache"));

			HttpResponse response1 = httpclient.execute(httppost);
			HttpEntity entity = response1.getEntity();
			StringWriter writer = new StringWriter();
			InputStream inputStream = decompress(entity);
			IOUtils.copy(inputStream, writer);
			inputStream.close();
			String response = writer.toString();
			System.out.println(response);
			sendNotification("cancled order id:" + orderId + ",stock:" + id);

		} catch (Exception e) {
			sendErrorNotification("Error at cancelOder", e);
		}
		return rs;
	}

	public boolean buyStock(String id, int amount, int volume, String pin) {
		boolean rs = false;
		try {

			StringEntity urlEntity = new StringEntity(
					"{\"pv_sidecode\":\"B\",\"pv_symbol\":\""
							+ id
							+ "\",\"pv_qty\":\""
							+ volume
							+ "\",\"pv_quoteqty\":\"0\",\"pv_ordertype\":\"LO\",\"pv_action\":\"preview\",\"pv_price\":\""
							+ amount
							+ "\",\"pv_limitprice\":\"0\",\"pv_pin\":null,\"pv_acctno\":\"0101502292\",\"pv_ordertab\":\"ORDINPUT\",\"pv_RequestId\":\"1\"}",
					"UTF-8");
			HttpPost httppost = new HttpPost(
					"https://www.baovietsecurities.com.vn/WebOnlineTrading/OnlineService.svc/orderplacing_advance");
			httppost.setEntity(urlEntity);
			httppost.setHeader(new BasicHeader("User-Agent",
					"Mozilla/5.0 (Windows NT 6.3; WOW64; rv:37.0) Gecko/20100101 Firefox/37.0"));
			httppost.setHeader(new BasicHeader("Accept",
					"application/json, text/javascript, */*; q=0.01"));
			httppost.setHeader(new BasicHeader("Accept-Language",
					"en-US,en;q=0.5"));
			httppost.setHeader(new BasicHeader("Accept-Encoding",
					"gzip, deflate"));
			httppost.setHeader(new BasicHeader("X-Requested-With",
					"XMLHttpRequest"));
			httppost.setHeader(new BasicHeader("Referer",
					"https://www.baovietsecurities.com.vn/WebOnlineTrading/Default.aspx"));
			httppost.setHeader(new BasicHeader("Cache-Control", "no-cache"));
			httppost.setHeader(new BasicHeader("Content-Type",
					"application/json; charset=UTF-8"));
			httppost.setHeader(new BasicHeader("Connection", "keep-alive"));
			httppost.setHeader(new BasicHeader("Pragma", "no-cache"));

			HttpResponse response1 = httpclient.execute(httppost);
			HttpEntity entity = response1.getEntity();
			StringWriter writer = new StringWriter();
			InputStream inputStream = decompress(entity);
			IOUtils.copy(inputStream, writer);
			inputStream.close();
			String response = writer.toString();
			System.out.println(response);

			urlEntity = new StringEntity(
					"{\"pv_sidecode\":\"B\",\"pv_symbol\":\""
							+ id
							+ "\",\"pv_qty\":\""
							+ volume
							+ "\",\"pv_quoteqty\":\"0\",\"pv_ordertype\":\"LO\",\"pv_action\":\"confirmed\",\"pv_price\":\""
							+ amount
							+ "\",\"pv_limitprice\":\"0\",\"pv_pin\":\""
							+ pin
							+ "\",\"pv_acctno\":\"0101502292\",\"pv_ordertab\":\"ORDINPUT\",\"pv_isSavedPass\":false,\"pv_RequestId\":2}",
					"UTF-8");
			httppost = new HttpPost(
					"https://www.baovietsecurities.com.vn/WebOnlineTrading/OnlineService.svc/orderplacing_advance");
			httppost.setEntity(urlEntity);
			httppost.setHeader(new BasicHeader("User-Agent",
					"Mozilla/5.0 (Windows NT 6.3; WOW64; rv:37.0) Gecko/20100101 Firefox/37.0"));
			httppost.setHeader(new BasicHeader("Accept",
					"application/json, text/javascript, */*; q=0.01"));
			httppost.setHeader(new BasicHeader("Accept-Language",
					"en-US,en;q=0.5"));
			httppost.setHeader(new BasicHeader("Accept-Encoding",
					"gzip, deflate"));
			httppost.setHeader(new BasicHeader("X-Requested-With",
					"XMLHttpRequest"));
			httppost.setHeader(new BasicHeader("Referer",
					"https://www.baovietsecurities.com.vn/WebOnlineTrading/Default.aspx"));
			httppost.setHeader(new BasicHeader("Cache-Control", "no-cache"));
			httppost.setHeader(new BasicHeader("Content-Type",
					"application/json; charset=UTF-8"));
			httppost.setHeader(new BasicHeader("Connection", "keep-alive"));
			httppost.setHeader(new BasicHeader("Pragma", "no-cache"));

			response1 = httpclient.execute(httppost);
			entity = response1.getEntity();
			writer = new StringWriter();
			inputStream = decompress(entity);
			IOUtils.copy(inputStream, writer);
			inputStream.close();
			response = writer.toString();
			System.out.println(response);
			// JSONObject jsonObject = new JSONObject(response);
			sendNotification("Success order: BUY , stock:" + id + ",vol:"
					+ volume + ",price:" + amount);
		} catch (Exception e) {
			sendErrorNotification("ERROR At buyStock", e);
		}
		return rs;
	}

	public boolean sellStock(String id, int amount, int volume, String pin) {
		boolean rs = false;
		try {

			StringEntity urlEntity = new StringEntity(
					"{\"Side\":\"NS\",\"Symbol\":\""
							+ id
							+ "\",\"Qtty\":\""
							+ volume
							+ "\",\"PriceType\":\"LO\",\"Action\":\"preview\",\"Price\":\""
							+ new DecimalFormat("0.0").format((double)amount/1000)
							+ "\",\"DivQtty\":\""
							+ volume
							+ "\",\"AfAcctno\":\"0001102285\",\"pv_ordertab\":\"ORDINPUT\"}",
					"UTF-8");
			HttpPost httppost = new HttpPost(
					"https://www.strade.com.vn/OnlineTrading/Order/orderplacing_advance");
			httppost.setEntity(urlEntity);
			httppost.setHeader(new BasicHeader("User-Agent",
					"Mozilla/5.0 (Windows NT 6.3; WOW64; rv:37.0) Gecko/20100101 Firefox/37.0"));
			httppost.setHeader(new BasicHeader("Accept",
					"application/json, text/javascript, */*; q=0.01"));
			httppost.setHeader(new BasicHeader("Accept-Language",
					"en-US,en;q=0.5"));
			httppost.setHeader(new BasicHeader("Accept-Encoding",
					"gzip, deflate"));
			httppost.setHeader(new BasicHeader("X-Requested-With",
					"XMLHttpRequest"));
			httppost.setHeader(new BasicHeader("Referer",
					"https://www.strade.com.vn/OnlineTrading/"));
			httppost.setHeader(new BasicHeader("Cache-Control", "no-cache"));
			httppost.setHeader(new BasicHeader("Content-Type",
					"application/json; charset=UTF-8"));
			httppost.setHeader(new BasicHeader("Connection", "keep-alive"));
			httppost.setHeader(new BasicHeader("Pragma", "no-cache"));

			HttpResponse response1 = httpclient.execute(httppost);
			HttpEntity entity = response1.getEntity();
			StringWriter writer = new StringWriter();
			InputStream inputStream = decompress(entity);
			IOUtils.copy(inputStream, writer);
			inputStream.close();
			String response = writer.toString();
			System.out.println(response);

			urlEntity = new StringEntity(
					"{\"Side\":\"NS\",\"Symbol\":\""
							+ id
							+ "\",\"Qtty\":\""
							+ volume
							+ "\",\"PriceType\":\"LO\",\"Action\":\"confirmed\",\"Price\":\""
							+ new DecimalFormat("0.0").format((double)amount/1000)
							+ "\",\"DivQtty\":\""
							+ volume
							+ "\",\"AfAcctno\":\"0001102285\",\"pv_ordertab\":\"ORDINPUT\"}",
					"UTF-8");
			httppost = new HttpPost(
					"https://www.strade.com.vn/OnlineTrading/Order/orderplacing_advance");
			httppost.setEntity(urlEntity);
			httppost.setHeader(new BasicHeader("User-Agent",
					"Mozilla/5.0 (Windows NT 6.3; WOW64; rv:37.0) Gecko/20100101 Firefox/37.0"));
			httppost.setHeader(new BasicHeader("Accept",
					"application/json, text/javascript, */*; q=0.01"));
			httppost.setHeader(new BasicHeader("Accept-Language",
					"en-US,en;q=0.5"));
			httppost.setHeader(new BasicHeader("Accept-Encoding",
					"gzip, deflate"));
			httppost.setHeader(new BasicHeader("X-Requested-With",
					"XMLHttpRequest"));
			httppost.setHeader(new BasicHeader("Referer",
					"https://www.strade.com.vn/OnlineTrading/"));
			httppost.setHeader(new BasicHeader("Cache-Control", "no-cache"));
			httppost.setHeader(new BasicHeader("Content-Type",
					"application/json; charset=UTF-8"));
			httppost.setHeader(new BasicHeader("Connection", "keep-alive"));
			httppost.setHeader(new BasicHeader("Pragma", "no-cache"));

			response1 = httpclient.execute(httppost);
			entity = response1.getEntity();
			writer = new StringWriter();
			inputStream = decompress(entity);
			IOUtils.copy(inputStream, writer);
			inputStream.close();
			response = writer.toString();
			System.out.println(response);
			// JSONObject jsonObject = new JSONObject(response);
			sendNotification("Success order: BUY , stock:" + id + ",vol:"
					+ volume + ",price:" + amount);
		} catch (Exception e) {
			sendErrorNotification("ERROR At buyStock", e);
		}
		return rs;
	}

	public boolean logout() {
		// TODO Auto-generated method stub
		return false;
	}

	public StockOrderPage getNormalOrderList(int page, int numberItems) {
		StockOrderPage rs = new StockOrderPage();
		try {
			HttpGet httpget = new HttpGet(
					"https://www.strade.com.vn/OnlineTrading/");
			httpget.setHeader(new BasicHeader("User-Agent",
					"Mozilla/5.0 (Windows NT 6.3; WOW64; rv:37.0) Gecko/20100101 Firefox/37.0"));
			httpget.setHeader(new BasicHeader("Accept",
					"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"));
			httpget.setHeader(new BasicHeader("Accept-Language",
					"en-US,en;q=0.5"));
			httpget.setHeader(new BasicHeader("Accept-Encoding",
					"gzip, deflate"));
			httpget.setHeader(new BasicHeader("Referer",
					"https://www.strade.com.vn/OnlineTrading/"));
			httpget.setHeader(new BasicHeader("Connection", "keep-alive"));
			HttpResponse response1 = httpclient.execute(httpget);

			HttpEntity entity = response1.getEntity();
			StringWriter writer = new StringWriter();
			InputStream inputStream = decompress(entity);
			IOUtils.copy(inputStream, writer);
			inputStream.close();
			String response = writer.toString();
			StringUtils.Result OrderList_CallbackState = StringUtils.search(
					response, 0,
					"<input type=\"hidden\" name=\"OrderList$CallbackState\"",
					"/>");
			if (OrderList_CallbackState.getResult() != null) {
				String OrderList_CallbackStateValue = HtmlUtils
						.getAttributeValue("value",
								OrderList_CallbackState.getResult());
				List<NameValuePair> formparams = new ArrayList<NameValuePair>();

				formparams.add(new BasicNameValuePair("DXCallbackName",
						"OrderList"));
				formparams.add(new BasicNameValuePair("DXCallbackArgument",
						"c0:KV|2;[];CR|2;{};GB|9;7|REFRESH;"));
				formparams.add(new BasicNameValuePair("OrderList$DXSelInput",
						""));
				formparams.add(new BasicNameValuePair("OrderList$DXKVInput",
						"[]"));
				formparams
						.add(new BasicNameValuePair("OrderList$CallbackState",
								OrderList_CallbackStateValue));
				formparams.add(new BasicNameValuePair(
						"OrderList$DXColResizedInput", "{}"));
				formparams.add(new BasicNameValuePair("OrderList$DXSyncInput",
						""));
				formparams.add(new BasicNameValuePair("DXMVCEditorsValues",
						"{}"));

				UrlEncodedFormEntity urlEntity = new UrlEncodedFormEntity(
						formparams, "UTF-8");
				HttpPost httpPost = new HttpPost(
						"https://www.strade.com.vn/OnlineTrading/Trade/getOrderList");
				httpPost.setEntity(urlEntity);
				httpPost.setHeader(new BasicHeader("User-Agent",
						"Mozilla/5.0 (Windows NT 6.3; WOW64; rv:37.0) Gecko/20100101 Firefox/37.0"));
				httpPost.setHeader(new BasicHeader("Accept",
						"application/json, text/javascript, */*; q=0.01"));
				httpPost.setHeader(new BasicHeader("Accept-Language",
						"en-US,en;q=0.5"));
				httpPost.setHeader(new BasicHeader("Accept-Encoding",
						"gzip, deflate"));
				httpPost.setHeader(new BasicHeader("X-Requested-With",
						"XMLHttpRequest"));

				httpPost.setHeader(new BasicHeader("Connection", "keep-alive"));
				httpPost.setHeader(new BasicHeader("Referer",
						"https://www.strade.com.vn/OnlineTrading/"));
				response1 = httpclient.execute(httpPost);

				entity = response1.getEntity();
				writer = new StringWriter();
				inputStream = decompress(entity);
				IOUtils.copy(inputStream, writer);
				inputStream.close();
				response = writer.toString();
				StringUtils.Result OrderList_DXMainTable = StringUtils.search(
						response, 0, "<table id=\"OrderList_DXMainTable\"",
						"</table>");
				Order order = null;
				if (OrderList_DXMainTable.getResult() != null) {
					List<StringUtils.Result> results = StringUtils.searchs(
							OrderList_DXMainTable.getResult(), 0, "<tr",
							"</tr>");
					for (StringUtils.Result r : results) {
						List<StringUtils.Result> tds = StringUtils.searchs(
								r.getResult(), 0, "<td", "</td>");

						boolean skip = false;
						for (int i = 0; i < tds.size() && !skip; i++) {
							StringUtils.Result tdCont = StringUtils.search(tds
									.get(i).getResult(), 0, "\">", "</td>");
							if (i == 0 && "\"></td>".equals(tdCont.getResult())) {
								skip = true;
							} else {
								if (i == 0) {
									order = new Order();
									rs.getOrders().add(order);
									StringUtils.Result oderId = StringUtils
											.search(tdCont.getResult(), 0,
													"onfocusrow('", "');");
									if (oderId.getResult() != null) {
										order.setOrderID(tdCont
												.getResult()
												.substring(
														oderId.getStartIndex()
																+ "onfocusrow('"
																		.length(),
														oderId.getEndIndex()));
									}
								} else if (i == 2) {
									order.setAcctno(tds
											.get(i)
											.getResult()
											.substring(
													tdCont.getStartIndex() + 2,
													tdCont.getEndIndex()));
								} else if (i == 3) {
									order.setSymbol(tds
											.get(i)
											.getResult()
											.substring(
													tdCont.getStartIndex() + 2,
													tdCont.getEndIndex()));
								} else if (i == 4) {
									order.setTime(tds
											.get(i)
											.getResult()
											.substring(
													tdCont.getStartIndex() + 2,
													tdCont.getEndIndex()));
								} else if (i == 5) {
									order.setExecType(tds
											.get(i)
											.getResult()
											.substring(
													tdCont.getStartIndex() + 2,
													tdCont.getEndIndex()));
								} else if (i == 6) {
									order.setStatus(tds
											.get(i)
											.getResult()
											.substring(
													tdCont.getStartIndex() + 2,
													tdCont.getEndIndex()));
								} else if (i == 7) {
									order.setOrderType(tds
											.get(i)
											.getResult()
											.substring(
													tdCont.getStartIndex() + 2,
													tdCont.getEndIndex()));
								} else if (i == 8) {
									order.setOrderFrom(tds
											.get(i)
											.getResult()
											.substring(
													tdCont.getStartIndex() + 2,
													tdCont.getEndIndex()));
								} else if (i == 9) {
									order.setQtyOrder(Integer.parseInt(tds
											.get(i)
											.getResult()
											.substring(
													tdCont.getStartIndex() + 2,
													tdCont.getEndIndex())));
								} else if (i == 10) {
									order.setPriceOrder(Integer.parseInt(tds
											.get(i)
											.getResult()
											.substring(
													tdCont.getStartIndex() + 2,
													tdCont.getEndIndex())
											.replace(",", "")));
								} else if (i == 11) {
									order.setQuoteQtty(Integer.parseInt(tds
											.get(i)
											.getResult()
											.substring(
													tdCont.getStartIndex() + 2,
													tdCont.getEndIndex())
											.replace(",", "")));
								} else if (i == 12) {
									order.setQtyMatched(Integer.parseInt(tds
											.get(i)
											.getResult()
											.substring(
													tdCont.getStartIndex() + 2,
													tdCont.getEndIndex())
											.replace(",", "")));
								} else if (i == 13) {
									order.setPriceMatched(Integer.parseInt(tds
											.get(i)
											.getResult()
											.substring(
													tdCont.getStartIndex() + 2,
													tdCont.getEndIndex())
											.replace(",", "")));
								}
							}
						}
					}
				}

			}
		} catch (Exception e) {
			sendErrorNotification("Error at getNormalOrderList", e);
		}
		return rs;
	}

	public static void main(String[] args) {
		TrustEverythingSSLTrustManager.trustAllSSLCertificatesUniversally();
		javax.net.ssl.HttpsURLConnection
				.setDefaultHostnameVerifier(new javax.net.ssl.HostnameVerifier() {

					public boolean verify(String hostname,
							javax.net.ssl.SSLSession sslSession) {
						return true;
					}
				});
		Strade bvsc = new Strade();
		bvsc.login("017C102285", RSAUtils.getPassword("/strade.properties"));
		System.out.println(bvsc.getAmount());
		StockOrderPage orderPage = bvsc.getNormalOrderList(1, 5);
		for (Order order : orderPage.getOrders()) {
			System.out.println(order);
		}
		// System.out.println(bvsc.getStockInfo("HQC"));
		// bvsc.buyStock("HQC", 5900, 150, RSAUtils.getPin());
		//bvsc.cancelOder("HQC", "8000230615001447", "","150","6.40");
		//System.out.println(bvsc.getStockInfo("HQC"));
		//bvsc.sellStock("HQC", 6400, 150, "");

	}
}
