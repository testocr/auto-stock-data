/**
 * Copyright (c) 2011 TLOSS. All rights reserved.
 * Created on Sep 4, 2011
 * File name : UrlUtils.java
 * Package org.tloss.common
 */
package org.tloss.common;

/**
 * @author tungt
 * 
 */
public class UrlUtils {
	public static String getFileName(String url) {
		String result = null;
		if (url != null) {
			int index = url.lastIndexOf("/");
			if (index >= 0 && index + 1 < url.length()) {
				result = url.substring(index + 1);
			} else {
				result = url;
			}
			index =  result.lastIndexOf("?");
			if (index > 0) {
				result = result.substring(0,index);
			}
		}
		return result;
	}
}
