/**
 * Waffle (https://github.com/Waffle/waffle)
 *
 * Copyright (c) 2010-2018 Application Security, Inc.
 *
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors: Application Security, Inc.
 */
package waffle.mock.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class SimpleHttpResponse.
 *
 * @author dblock[at]dblock[dot]org
 */
public class SimpleHttpResponse extends HttpServletResponseWrapper {

    /** The Constant LOGGER. */
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleHttpResponse.class);

    /** The status. */
    private int status = 500;

    /** The headers. */
    private final Map<String, List<String>> headers = new HashMap<>();

    /** The bytes. */
    final ByteArrayOutputStream bytes = new ByteArrayOutputStream();

    /** The out. */
    private final ServletOutputStream out = new ServletOutputStream() {
        @Override
        public void write(final int b) throws IOException {
            SimpleHttpResponse.this.bytes.write(b);
        }

        @Override
        public boolean isReady() {
            return false;
        }

        @Override
        public void setWriteListener(final WriteListener writeListener) {
            // Not used
        }
    };

    /** The writer. */
    private final PrintWriter writer = new PrintWriter(new OutputStreamWriter(this.bytes, StandardCharsets.UTF_8),
            true);

    /**
     * Instantiates a new simple http response.
     */
    public SimpleHttpResponse() {
        super(Mockito.mock(HttpServletResponse.class));
    }

    /**
     * Gets the status.
     *
     * @return the status
     */
    @Override
    public int getStatus() {
        return this.status;
    }

    @Override
    public void addHeader(final String headerName, final String headerValue) {
        List<String> current = this.headers.get(headerName);
        if (current == null) {
            current = new ArrayList<>();
        }
        current.add(headerValue);
        this.headers.put(headerName, current);
    }

    @Override
    public void setHeader(final String headerName, final String headerValue) {
        List<String> current = this.headers.get(headerName);
        if (current == null) {
            current = new ArrayList<>();
        } else {
            current.clear();
        }
        current.add(headerValue);
        this.headers.put(headerName, current);
    }

    /**
     * Gets the status string.
     *
     * @return the status string
     */
    public String getStatusString() {
        if (this.status == 401) {
            return "Unauthorized";
        }
        return "Unknown";
    }

    @Override
    public void flushBuffer() {
        SimpleHttpResponse.LOGGER.info("{}: {}", Integer.valueOf(this.status), this.getStatusString());
        for (final Entry<String, List<String>> header : this.headers.entrySet()) {
            for (final String headerValue : header.getValue()) {
                SimpleHttpResponse.LOGGER.info("{}: {}", header, headerValue);
            }
        }
    }

    /**
     * Use this for testing the number of headers.
     *
     * @return int header name size.
     */
    public int getHeaderNamesSize() {
        return this.headers.size();
    }

    /**
     * Gets the header values.
     *
     * @param headerName
     *            the header name
     * @return the header values
     */
    public String[] getHeaderValues(final String headerName) {
        final List<String> headerValues = this.headers.get(headerName);
        return headerValues == null ? null : headerValues.toArray(new String[0]);
    }

    /**
     * Gets the header.
     *
     * @param headerName
     *            the header name
     * @return the header
     */
    @Override
    public String getHeader(final String headerName) {
        final List<String> headerValues = this.headers.get(headerName);
        return headerValues == null ? null : String.join(", ", headerValues);
    }

    @Override
    public void sendError(final int rc, final String message) {
        this.status = rc;
    }

    @Override
    public void sendError(final int rc) {
        this.status = rc;
    }

    @Override
    public PrintWriter getWriter() {
        return this.writer;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return this.out;
    }

    /**
     * Gets the output text.
     *
     * @return the output text
     */
    public String getOutputText() {
        this.writer.flush();
        try {
            return this.bytes.toString(StandardCharsets.UTF_8.name());
        } catch (final UnsupportedEncodingException e) {
            SimpleHttpResponse.LOGGER.error("", e);
        }
        return null;
    }

}