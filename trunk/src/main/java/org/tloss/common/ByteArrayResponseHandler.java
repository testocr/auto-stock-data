/**
 * Copyright (c) 2011 TLOSS. All rights reserved.
 * Created on Sep 3, 2011
 * File name : ByteArrayResponseHandler.java
 * Package org.tloss.common
 */
package org.tloss.common;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.DeflateDecompressingEntity;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.util.EntityUtils;

/**
 * @author tungt
 * 
 */
public class ByteArrayResponseHandler implements ResponseHandler<byte[]> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.http.client.ResponseHandler#handleResponse(org.apache.http
	 * .HttpResponse)
	 */
	public byte[] handleResponse(HttpResponse response)
			throws ClientProtocolException, IOException {

		HttpEntity entity = response.getEntity();
		if (entity.getContentEncoding() != null
				&& "deflate".equals(entity.getContentEncoding().getValue())) {
			entity = new DeflateDecompressingEntity(entity);
		}
		if (entity.getContentEncoding() != null
				&& "gzip".equals(entity.getContentEncoding().getValue())) {
			entity = new GzipDecompressingEntity(entity);
		}
		return entity == null ? null : EntityUtils.toByteArray(entity);
	}

}
