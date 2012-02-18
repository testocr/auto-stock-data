/**
 * Copyright (c) 2011 TLOSS. All rights reserved.
 * Created on Nov 12, 2011
 * File name : DefaultResponseAndFollowHandler.java
 * Package org.tloss.common
 */
package org.tloss.common;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.DeflateDecompressingEntity;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.util.EntityUtils;

/**
 * @author tungt
 * 
 */
public class DefaultResponseAndFollowHandler implements ResponseHandler<String> {
	
	boolean mustFollow;

	/**
	 * @return the mustFollow
	 */
	public boolean isMustFollow() {
		return mustFollow;
	}

	public String handleResponse(final HttpResponse response)
			throws HttpResponseException, IOException {
		mustFollow = false;
		HttpEntity entity = response.getEntity();
		Header[] headers = response.getHeaders("Location");
		if (headers != null && headers.length > 0) {
			mustFollow = true;
			return headers[0].getValue();
		}
		if (entity.getContentEncoding() != null
				&& "deflate".equals(entity.getContentEncoding().getValue())) {
			entity = new DeflateDecompressingEntity(entity);
		}
		if (entity.getContentEncoding() != null
				&& "gzip".equals(entity.getContentEncoding().getValue())) {
			entity = new GzipDecompressingEntity(entity);
		}
		return entity == null ? null : EntityUtils.toString(entity);
	}
}
