/**
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/java/org/apache/commons/httpclient/methods/multipart/MultipartRequestEntity.java,v 1.1 2004/10/06 03:39:59 mbecke Exp $
 * $Revision: 502647 $
 * $Date: 2007-02-02 17:22:54 +0100 (Fri, 02 Feb 2007) $
 *
 * ====================================================================
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package com.jobs.lib_v1.net.http.multipart;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

import org.apache.http.Header;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EncodingUtils;

import com.jobs.lib_v1.net.http.DataHttpConnectionListener;

/***
 * Implements a request entity suitable for an HTTP multipart POST method.
 * <p>
 * The HTTP multipart POST method is defined in section 3.3 of <a href="http://www.ietf.org/rfc/rfc1867.txt">RFC1867</a>: <blockquote> The media-type multipart/form-data follows the rules of all multipart MIME data streams as outlined in RFC 1521. The multipart/form-data contains a series of parts.
 * Each part is expected to contain a content-disposition header where the value is "form-data" and a name attribute specifies the field name within the form, e.g., 'content-disposition: form-data; name="xxxxx"', where xxxxx is the field name corresponding to that field. Field names originally in
 * non-ASCII character sets may be encoded using the method outlined in RFC 1522. </blockquote>
 * </p>
 * <p>
 * This entity is designed to be used in conjunction with the {@link org.apache.http.HttpRequest} to provide multipart posts. Example usage:
 * </p>
 * 
 * <pre>
 * File f = new File(&quot;/path/fileToUpload.txt&quot;);
 * HttpRequest request = new HttpRequest(&quot;http://host/some_path&quot;);
 * Part[] parts = { new StringPart(&quot;param_name&quot;, &quot;value&quot;), new FilePart(f.getName(), f) };
 * filePost.setEntity(new MultipartRequestEntity(parts, filePost.getParams()));
 * HttpClient client = new HttpClient();
 * int status = client.executeMethod(filePost);
 * </pre>
 * 
 * @since 3.0
 */
public class MultipartEntity extends AbstractHttpEntity {
	/*** The Content-Type for multipart/form-data. */
	private static final String MULTIPART_FORM_CONTENT_TYPE = "multipart/form-data";

	/***
	 * Sets the value to use as the multipart boundary.
	 * <p>
	 * This parameter expects a value if type {@link String}.
	 * </p>
	 */
	public static final String MULTIPART_BOUNDARY = "http.method.multipart.boundary";

	/***
	 * The pool of ASCII chars to be used for generating a multipart boundary.
	 */
	private static byte[] MULTIPART_CHARS = "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".getBytes();

	private DataHttpConnectionListener mProgressListener = null;

	/***
	 * Generates a random multipart boundary string.
	 */
	private static byte[] generateMultipartBoundary() {
		Random rand = new Random();
		byte[] bytes = new byte[rand.nextInt(11) + 30]; // a random size from 30 to 40
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = MULTIPART_CHARS[rand.nextInt(MULTIPART_CHARS.length)];
		}
		return bytes;
	}

	/*** The MIME parts as set by the constructor */
	protected Part[] parts;

	private byte[] multipartBoundary;

	private HttpParams params;

	private boolean contentConsumed = false;

	/***
	 * Creates a new multipart entity containing the given parts.
	 * 
	 * @param parts The parts to include.
	 * @param params The params of the HttpMethod using this entity.
	 */
	public MultipartEntity(Part[] parts, HttpParams params, DataHttpConnectionListener l) {
		if (parts == null) {
			throw new IllegalArgumentException("parts cannot be null");
		}

		if (params == null) {
			throw new IllegalArgumentException("params cannot be null");
		}

		this.parts = parts;
		this.params = params;
		this.mProgressListener = l;
	}

	public MultipartEntity(Part[] parts, DataHttpConnectionListener l) {
		setContentType(MULTIPART_FORM_CONTENT_TYPE);

		if (parts == null) {
			throw new IllegalArgumentException("parts cannot be null");
		}

		this.parts = parts;
		this.params = null;
		this.mProgressListener = l;
	}

	/***
	 * Returns the MIME boundary string that is used to demarcate boundaries of this part. The first call to this method will implicitly create a new boundary string. To create a boundary string first the HttpMethodParams.MULTIPART_BOUNDARY parameter is considered. Otherwise a random one is
	 * generated.
	 * 
	 * @return The boundary string of this entity in ASCII encoding.
	 */
	protected byte[] getMultipartBoundary() {
		if (multipartBoundary == null) {
			String temp = null;

			if (params != null) {
				temp = (String) params.getParameter(MULTIPART_BOUNDARY);
			}

			if (temp != null) {
				multipartBoundary = temp.getBytes();
			} else {
				multipartBoundary = generateMultipartBoundary();
			}
		}

		return multipartBoundary;
	}

	/***
	 * Returns <code>true</code> if all parts are repeatable, <code>false</code> otherwise.
	 */
	public boolean isRepeatable() {
		for (int i = 0; i < parts.length; i++) {
			if (!parts[i].isRepeatable()) {
				return false;
			}
		}
		return true;
	}

	public void writeTo(OutputStream out) throws IOException {
		if(null != mProgressListener){
			mProgressListener.setSendTotalLength(getContentLength());
		}

		ProgressOutputStream os = new ProgressOutputStream(out);
		Part.sendParts(os, parts, getMultipartBoundary());
	}

	@Override
	public Header getContentType() {
		StringBuffer buffer = new StringBuffer(MULTIPART_FORM_CONTENT_TYPE);
		buffer.append("; boundary=");
		buffer.append(EncodingUtils.getAsciiString(getMultipartBoundary()));
		return new BasicHeader(HTTP.CONTENT_TYPE, buffer.toString());
	}

	public long getContentLength() {
		try {
			return Part.getLengthOfParts(parts, getMultipartBoundary());
		} catch (Throwable e) {
			return 0;
		}
	}

	public InputStream getContent() throws IOException, IllegalStateException {
		if(null != mProgressListener){
			mProgressListener.setSendTotalLength(getContentLength());
		}

		if (!isRepeatable() && this.contentConsumed) {
			throw new IllegalStateException("Content has been consumed");
		}

		this.contentConsumed = true;

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Part.sendParts(baos, this.parts, this.multipartBoundary);
		ProgressByteArrayInputStream bais = new ProgressByteArrayInputStream(baos.toByteArray());
		return bais;
	}

	public boolean isStreaming() {
		return false;
	}

	private class ProgressByteArrayInputStream extends ByteArrayInputStream {
		public ProgressByteArrayInputStream(byte[] buf) {
			super(buf);
		}

		@Override
		public synchronized int read() {
			int readCount = super.read();
			if(null != mProgressListener){
				mProgressListener.updateSendProgress(readCount);
			}
			return readCount;
		}

		@Override
		public int read(byte[] buffer) throws IOException {
			int readCount = super.read(buffer);
			if(null != mProgressListener){
				mProgressListener.updateSendProgress(readCount);
			}
			return readCount;
		}

		@Override
		public synchronized int read(byte[] buffer, int byteOffset, int byteCount) {
			int readCount = super.read(buffer, byteOffset, byteCount);
			if(null != mProgressListener){
				mProgressListener.updateSendProgress(readCount);
			}
			return readCount;
		}
	}

	private class ProgressOutputStream extends OutputStream {
		private OutputStream mOutputStream = null;

		ProgressOutputStream(OutputStream os) {
			mOutputStream = os;
		}

		@Override
		public void write(int oneByte) throws IOException {
			mOutputStream.write(oneByte);
			if(null != mProgressListener){
				mProgressListener.updateSendProgress(1);
			}
		}

		@Override
		public void flush() throws IOException {
			mOutputStream.flush();
		}

		@Override
		public void close() throws IOException {
			mOutputStream.close();
		}
	}
}
