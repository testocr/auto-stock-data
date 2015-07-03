package org.tloss.stock.bvsc;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
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
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.json.JSONArray;
import org.json.JSONObject;
import org.tloss.agpush.AGPush;
import org.tloss.stock.IStock;
import org.tloss.stock.Order;
import org.tloss.stock.RSAUtils;
import org.tloss.stock.StockHold;
import org.tloss.stock.StockHoldItem;
import org.tloss.stock.StockInfo;
import org.tloss.stock.StockOrderPage;
import org.tloss.stock.TrustEverythingSSLTrustManager;
import org.tloss.stock.utils.HtmlUtils;
import org.tloss.stock.utils.HtmlUtils.Form;
import org.tloss.stock.utils.HtmlUtils.Input;

public class BVSC implements IStock {
	HttpClient httpclient = new DefaultHttpClient();

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

		/* proxy */
		HttpHost proxy = new HttpHost("localhost", 5865);
		httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,
				proxy);

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

	public boolean login(String userName, String password) {
		initHttpClient(httpclient);

		try {
			HttpGet httpget = new HttpGet(
					"https://www.baovietsecurities.com.vn/WebOnlineTrading/Account/Login.aspx?ReturnUrl=%2fWebOnlineTrading%2fDefault.aspx");
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
				// downloadResource(response);
				Form form = HtmlUtils
						.parseForm(
								response,
								0,
								"<form method=\"post\" action=\"Login.aspx?ReturnUrl=%2fWebOnlineTrading%2fDefault.aspx\"",
								"</form>");
				if (form.getFormContent() != null) {

					// create a script engine manager
					ScriptEngineManager factory = new ScriptEngineManager();
					// create JavaScript engine
					ScriptEngine engine = factory.getEngineByName("JavaScript");
					// evaluate JavaScript code from given file - specified by
					// first argument
					engine.eval(new InputStreamReader(BVSC.class
							.getResourceAsStream("/md5.js")));
					Invocable inv = (Invocable) engine;
					HtmlUtils.parseFormField(form.getFormContent(), 0,
							"<input", "/>", form);
					List<NameValuePair> formparams = new ArrayList<NameValuePair>();

					for (Input i : form.getInputs()) {
						if ("ctl00$MainContent$txtUserName".equals(i.getName()))
							i.setValue(userName);
						if ("ctl00$MainContent$txtStaticPassWord".equals(i
								.getName()))
							i.setValue(inv.invokeFunction("hex_md5", password)
									.toString());
						if (!"image".equals(i.getType())
								&& !"ctl00$MainContent$chkRememberPass"
										.equals(i.getName())
								&& !"ctl00$btnDisableEnter".equals(i.getName())) {
							formparams.add(new BasicNameValuePair(i.getName(),
									i.getValue() != null ? i.getValue().trim()
											: ""));
						}
					}
					formparams
							.add(new BasicNameValuePair("__ASYNCPOST", "true"));
					formparams.add(new BasicNameValuePair("__LASTFOCUS", ""));
					formparams
							.add(new BasicNameValuePair(
									"ctl00$spManagerLoginMaster",
									"ctl00$MainContent$UpdatePanelLogin|ctl00$MainContent$btnLogin"));

					UrlEncodedFormEntity urlEntity = new UrlEncodedFormEntity(
							formparams, "UTF-8");
					HttpPost httppost = new HttpPost(
							"https://www.baovietsecurities.com.vn/WebOnlineTrading/Account/Login.aspx?ReturnUrl=%2fWebOnlineTrading%2fDefault.aspx");
					httppost.setEntity(urlEntity);
					httppost.setHeader(new BasicHeader("User-Agent",
							"Mozilla/5.0 (Windows NT 6.3; WOW64; rv:37.0) Gecko/20100101 Firefox/37.0"));
					httppost.setHeader(new BasicHeader("Accept",
							"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"));
					httppost.setHeader(new BasicHeader("Accept-Language",
							"en-US,en;q=0.5"));
					httpget.setHeader(new BasicHeader("Accept-Encoding",
							"gzip, deflate"));
					httppost.setHeader(new BasicHeader("X-Requested-With",
							"XMLHttpRequest"));
					httppost.setHeader(new BasicHeader("X-MicrosoftAjax",
							"Delta=true"));
					httppost.setHeader(new BasicHeader(
							"Referer",
							"https://www.baovietsecurities.com.vn/WebOnlineTrading/Account/Login.aspx?ReturnUrl=%2fWebOnlineTrading%2fDefault.aspx"));
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
					String[] tmp = response.split("\\|");
					if ("pageRedirect".equals(tmp[5])) {
						httpget = new HttpGet(
								"https://www.baovietsecurities.com.vn/WebOnlineTrading/Default.aspx");
						httpget.setHeader(new BasicHeader("User-Agent",
								"Mozilla/5.0 (Windows NT 6.3; WOW64; rv:37.0) Gecko/20100101 Firefox/37.0"));
						httpget.setHeader(new BasicHeader("Accept",
								"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"));
						httpget.setHeader(new BasicHeader("Accept-Language",
								"en-US,en;q=0.5"));
						httpget.setHeader(new BasicHeader("Accept-Encoding",
								"gzip, deflate"));

						httpget.setHeader(new BasicHeader("Connection",
								"keep-alive"));
						response1 = httpclient.execute(httpget);

						entity = response1.getEntity();
						writer = new StringWriter();
						inputStream = decompress(entity);
						IOUtils.copy(inputStream, writer);
						inputStream.close();
						response = writer.toString();

						// System.out.println(response);
						return true;
					}

				}

			} finally {
			}

		} catch (Exception e) {
			AGPush.getInstance().sendErrorNotification("ERROR at login", e);
		}

		return false;
	}

	/*
	 * protected void sendErrorNotification(String info, Exception e) {
	 * UnifiedMessage unifiedMessage = new UnifiedMessage.Builder()
	 * .pushApplicationId("d7ebb4ad-1cda-40a8-8bbb-8fe958d636f3")
	 * .masterSecret("68dc7315-410e-447f-b242-6e7c90b04944") .alert(info + " : "
	 * + e.getMessage()).build(); defaultJavaSender.send(unifiedMessage, new
	 * MessageResponseCallback() {
	 * 
	 * public void onComplete(int statusCode) { // do cool stuff }
	 * 
	 * public void onError(Throwable throwable) { // bring out the bad news
	 * throwable.fillInStackTrace(); } }); }
	 * 
	 * protected void sendNotification(String info) {
	 * 
	 * UnifiedMessage unifiedMessage = new UnifiedMessage.Builder()
	 * .pushApplicationId("d7ebb4ad-1cda-40a8-8bbb-8fe958d636f3")
	 * .masterSecret("68dc7315-410e-447f-b242-6e7c90b04944")
	 * .alert(info).build(); defaultJavaSender.send(unifiedMessage, new
	 * MessageResponseCallback() {
	 * 
	 * public void onComplete(int statusCode) { // do cool stuff }
	 * 
	 * public void onError(Throwable throwable) { // bring out the bad news
	 * throwable.printStackTrace(); } }); }
	 */

	public int getAmount() {
		try {
			HttpGet httpget = new HttpGet(
					"https://www.baovietsecurities.com.vn/WebOnlineTrading/OnlineService.svc/accountsummary?reqJSONNocache=4&pv_acctno=0101502292&pv_symbol=&pv_price=0");
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
					"https://www.baovietsecurities.com.vn/WebOnlineTrading/Default.aspx"));
			HttpResponse response1 = httpclient.execute(httpget);

			HttpEntity entity = response1.getEntity();
			StringWriter writer = new StringWriter();
			InputStream inputStream = decompress(entity);
			IOUtils.copy(inputStream, writer);
			inputStream.close();
			String response = writer.toString();
			JSONObject jsonObject = new JSONObject(response);

			return jsonObject.getInt("cashReal");
			// System.out.println(response);
		} catch (Exception e) {
			AGPush.getInstance().sendErrorNotification("Error at getAmount", e);
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

			StringEntity urlEntity = new StringEntity("{\"pv_symbol\":\"" + id
					+ "\",\"pv_ordertype\":1,\"pv_acctno\":\"0101502292\"}",
					"UTF-8");
			HttpPost httppost = new HttpPost(
					"https://www.baovietsecurities.com.vn/WebOnlineTrading/OnlineService.svc/getStockInfo");
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
			// System.out.println(response);
			JSONObject jsonObject = new JSONObject(response);
			rs = new StockInfo();
			rs.setId(jsonObject.getString("symbol"));
			rs.setHightPrice(Integer.parseInt(jsonObject
					.getString("ceilingPrice")));
			rs.setLowPrice(Integer.parseInt(jsonObject.getString("floorPrice")));
			rs.setPrice(Integer.parseInt(jsonObject.getString("referencePrice")));

		} catch (Exception e) {
			AGPush.getInstance().sendErrorNotification(
					"Errror at getStockInfo", e);
		}
		return rs;
	}

	public boolean cancelOder(String id, String orderId, String pin,
			String Qtty, String Price) {
		boolean rs = false;
		try {

			StringEntity urlEntity = new StringEntity(
					"{\"pv_pin\":\""
							+ pin
							+ "\",\"pv_tab\":\"AL\",\"pv_orderID\":\""
							+ orderId
							+ "\",\"pv_afacctno\":\"0101502292\",\"pv_quoteqty\":\"0\",\"pv_limitprice\":\"0\",\"pv_isSavedPass\":false}",
					"UTF-8");
			HttpPost httppost = new HttpPost(
					"https://www.baovietsecurities.com.vn/WebOnlineTrading/OnlineService.svc/cancelorder");
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
			AGPush.getInstance().sendNotification(
					"cancled order id:" + orderId + ",stock:" + id);

		} catch (Exception e) {
			AGPush.getInstance()
					.sendErrorNotification("Error at cancelOder", e);
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
			JSONObject jsonObject = new JSONObject(response);
			urlEntity = new StringEntity(
					"{\"pv_sidecode\":\"B\",\"pv_symbol\":\""
							+ id
							+ "\",\"pv_qty\":\""
							+ volume
							+ "\",\"pv_quoteqty\":\"0\",\"pv_ordertype\":\"LO\",\"pv_action\":\"confirmed\",\"pv_price\":\""
							+ amount
							+ "\",\"pv_limitprice\":\"0\",\"pv_pin\":\""
							+ pin
							+ "\",\"pv_acctno\":\"0101502292\",\"pv_ordertab\":\"ORDINPUT\",\"pv_isSavedPass\":false,\"pv_RequestId\":"
							+ jsonObject.getInt("NextRequestId") + "}", "UTF-8");
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
			AGPush.getInstance().sendNotification(
					"Success order: BUY , stock:" + id + ",vol:" + volume
							+ ",price:" + amount);
		} catch (Exception e) {
			AGPush.getInstance().sendErrorNotification("ERROR At buyStock", e);
		}
		return rs;
	}

	public boolean sellStock(String id, int amount, int volume, String pin) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean logout() {
		// TODO Auto-generated method stub
		return false;
	}

	public StockOrderPage getNormalOrderList(int page, int numberItems) {
		StockOrderPage rs = new StockOrderPage();
		try {
			HttpGet httpget = new HttpGet(
					"https://www.baovietsecurities.com.vn/WebOnlineTrading/OnlineService.svc/getNormalOrderList?_search=false&nd="
							+ System.currentTimeMillis()
							+ "&rows="
							+ numberItems
							+ "&page="
							+ page
							+ "&sidx=orderIDDesc&sord=desc");
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
					"https://www.baovietsecurities.com.vn/WebOnlineTrading/Default.aspx"));
			HttpResponse response1 = httpclient.execute(httpget);

			HttpEntity entity = response1.getEntity();
			StringWriter writer = new StringWriter();
			InputStream inputStream = decompress(entity);
			IOUtils.copy(inputStream, writer);
			inputStream.close();
			String response = writer.toString();
			System.out.println(response);
			JSONObject jsonObject = new JSONObject(response);
			// rs.add(jsonObject);
			if (!jsonObject.isNull("rows")) {
				JSONArray rows = jsonObject.getJSONArray("rows");
				Order order;
				for (int i = 0; i < rows.length(); i++) {
					JSONObject tmp = rows.getJSONObject(i);
					order = new Order();
					order.setAcctno(tmp.getString("acctno"));
					order.setCancelable(tmp.getBoolean("cancelable"));
					order.setCustodycd(tmp.getString("custodycd"));
					order.setExecType(tmp.getString("execType"));
					order.setExecTypeDesc(tmp.getString("execTypeDesc"));
					order.setFeedbackMsg(tmp.getString("feedbackMsg"));
					order.setIsDisposal(tmp.getBoolean("isDisposal"));
					order.setLimitPrice(tmp.getInt("limitPrice"));
					order.setModifiable(tmp.getBoolean("modifiable"));
					order.setOrderer(tmp.getString("orderer"));
					order.setOrderFrom(tmp.getString("orderFrom"));
					order.setOrderID(tmp.getString("orderID"));
					order.setOrderIDDesc(tmp.getString("orderIDDesc"));
					order.setOrderType(tmp.getString("orderType"));
					order.setPriceMatched(tmp.getInt("priceMatched"));
					order.setPriceOrder(tmp.getInt("priceOrder"));
					order.setQtyCancel(tmp.getInt("qtyCancel"));
					order.setQtyMatched(tmp.getInt("qtyMatched"));
					order.setQtyModified(tmp.getInt("qtyModified"));
					order.setQtyOrder(tmp.getInt("qtyOrder"));
					order.setQtyRemain(tmp.getInt("qtyRemain"));
					order.setQuoteQtty(tmp.getInt("quoteQtty"));
					order.setRootOrderID(tmp.getString("rootOrderID"));
					order.setSessionCd(tmp.getString("sessionCd"));
					order.setStatus(tmp.getString("status"));
					order.setStatusValue(tmp.getString("statusValue"));
					order.setSymbol(tmp.getString("symbol"));
					order.setTime(tmp.getString("time"));
					order.setTimeType(tmp.getString("timeType"));
					order.setTimeTypeValue(tmp.getString("timeTypeValue"));
					rs.getOrders().add(order);
				}
			}
			rs.setTotalRecords(jsonObject.getInt("records"));
			rs.setPageNumber(jsonObject.getInt("total"));
		} catch (Exception e) {
			AGPush.getInstance().sendErrorNotification(
					"Error at getNormalOrderList", e);
		}
		return rs;
	}

	public StockHold GetHoldStock(int page, int numberItems) {
		StockHold rs = new StockHold();
		try {
			HttpGet httpget = new HttpGet(
					"https://www.baovietsecurities.com.vn/WebOnlineTrading/OnlineService.svc/GetSEInfo?_search=false&nd="
							+ System.currentTimeMillis()
							+ "&rows="
							+ numberItems
							+ "&page="
							+ page
							+ "&sidx=symbol&sord=asc");
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
					"https://www.baovietsecurities.com.vn/WebOnlineTrading/Default.aspx"));
			HttpResponse response1 = httpclient.execute(httpget);

			HttpEntity entity = response1.getEntity();
			StringWriter writer = new StringWriter();
			InputStream inputStream = decompress(entity);
			IOUtils.copy(inputStream, writer);
			inputStream.close();
			String response = writer.toString();
			//System.out.println(response);
			JSONObject jsonObject = new JSONObject(response);
			// rs.add(jsonObject);
			if (!jsonObject.isNull("rows")) {
				JSONArray rows = jsonObject.getJSONArray("rows");
				StockHoldItem order;
				for (int i = 0; i < rows.length(); i++) {
					JSONObject tmp = rows.getJSONObject(i);
					order = new StockHoldItem();
					order.setAfacctno(tmp.getString("afacctno"));
					order.setAvlqtty(tmp.getInt("avlqtty"));
					order.setSecurities_receiving_t0(tmp
							.getInt("securities_receiving_t0"));
					order.setSecurities_receiving_t1(tmp
							.getInt("securities_receiving_t1"));
					order.setSecurities_receiving_t2(tmp
							.getInt("securities_receiving_t2"));
					order.setSecurities_receiving_t3(tmp
							.getInt("securities_receiving_t3"));
					order.setSecurities_sending_t0(tmp
							.getInt("securities_sending_t0"));
					order.setSecurities_sending_t1(tmp
							.getInt("securities_sending_t1"));
					order.setSecurities_sending_t2(tmp
							.getInt("securities_sending_t2"));
					order.setSecurities_sending_t3(tmp
							.getInt("securities_sending_t3"));
					order.setSymbol(tmp.getString("symbol"));

					rs.getHoldItems().add(order);
				}
			}
			rs.setTotalRecords(jsonObject.getInt("records"));
			rs.setPageNumber(jsonObject.getInt("total"));

		} catch (Exception e) {
			AGPush.getInstance().sendErrorNotification(
					"Error at getNormalOrderList", e);
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
		BVSC bvsc = new BVSC();
		bvsc.login("001C502292", RSAUtils.getPassword("/password.properties"));
		System.out.println(bvsc.getAmount());
		StockOrderPage orderPage = bvsc.getNormalOrderList(1, 5);
		for (int i = 0; i < orderPage.getPageNumber(); i++) {
			List<Order> orders = orderPage.getOrders();
			for (Order order : orders) {
				System.out.println(order);
			}
			orderPage = bvsc.getNormalOrderList(i + 2, 5);
		}
		StockHold stockHold = bvsc.GetHoldStock(1, 10);
		for (int i = 0; i < stockHold.getPageNumber(); i++) {
			List<StockHoldItem> orders = stockHold.getHoldItems();
			for (StockHoldItem order : orders) {
				System.out.println(order);
			}
			stockHold = bvsc.GetHoldStock(i + 2, 10);
		}
		// System.out.println(bvsc.getStockInfo("HQC"));
		// bvsc.buyStock("HQC", 5900, 150, RSAUtils.getPin());
		// bvsc.cancelOder("HQC", "8000160615000047", RSAUtils.getPin());

	}

}
