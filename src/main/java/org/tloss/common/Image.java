/**
 * Copyright (c) 2011 TLOSS. All rights reserved.
 * Created on Sep 3, 2011
 * File name : Image.java
 * Package org.tloss.common
 */
package org.tloss.common;

import java.util.Arrays;

/**
 * @author tungt
 * 
 */
public class Image {
	public String url;
	public byte[] data;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(data);
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Image other = (Image) obj;
		if (!Arrays.equals(data, other.data))
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("<img src=\"").append(url).append("\" />");
		return buffer.toString();
	}
}
