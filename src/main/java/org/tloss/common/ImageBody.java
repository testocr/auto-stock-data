/**
 * Copyright (c) 2011 TLOSS. All rights reserved.
 * Created on Sep 4, 2011
 * File name : ImageBody.java
 * Package org.tloss.common
 */
package org.tloss.common;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.entity.mime.MIME;
import org.apache.http.entity.mime.content.AbstractContentBody;

/**
 * @author tungt
 * 
 */
public class ImageBody extends AbstractContentBody {

	private final Image image;
	private final String filename;
	private final String charset;

	public ImageBody(final Image image, final String filename,
			final String mimeType, final String charset) {
		super(mimeType);
		if (image == null) {
			throw new IllegalArgumentException("Image may not be null");
		}
		this.image = image;
		this.filename = filename;
		this.charset = charset;
	}

	public ImageBody(final Image image, final String filename,
			final String mimeType) {
		this(image, filename, mimeType, null);
	}

	public ImageBody(final Image image, final String filename) {
		this(image, filename, "application/octet-stream");
	}

	public InputStream getInputStream() throws IOException {
		return new ByteArrayInputStream(this.image.getData());
	}

	@Deprecated
	public void writeTo(final OutputStream out, int mode) throws IOException {
		writeTo(out);
	}

	public void writeTo(final OutputStream out) throws IOException {
		if (out == null) {
			throw new IllegalArgumentException("Output stream may not be null");
		}
		InputStream in = new ByteArrayInputStream(this.image.getData());
		try {
			byte[] tmp = new byte[4096];
			int l;
			while ((l = in.read(tmp)) != -1) {
				out.write(tmp, 0, l);
			}
			out.flush();
		} finally {
			in.close();
		}
	}

	public String getTransferEncoding() {
		return MIME.ENC_BINARY;
	}

	public String getCharset() {
		return charset;
	}

	public long getContentLength() {
		return image.getData().length;
	}

	public String getFilename() {
		return filename;
	}

	public Image getImage() {
		return this.image;
	}
}
