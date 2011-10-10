package org.tloss.common.utils;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.message.AbstractHttpMessage;
import org.tloss.common.ByteArrayResponseHandler;
import org.tloss.common.Image;

public class Utils {
	public static void setHeader(AbstractHttpMessage http) {
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

	public static Image download(String url, boolean skipData,
			HttpClient httpclient) throws Exception {
		Image image = new Image();
		image.setUrl(url);
		if (!skipData) {
			ResponseHandler<byte[]> byteArrayResponseHandler = new ByteArrayResponseHandler();
			HttpGet httpGetStepOne = new HttpGet(url);
			setHeader(httpGetStepOne);
			byte[] bs = httpclient.execute(httpGetStepOne,
					byteArrayResponseHandler);
			image.setData(bs);
		}
		return image;
	}

}
