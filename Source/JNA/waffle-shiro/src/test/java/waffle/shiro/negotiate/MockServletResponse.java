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
package waffle.shiro.negotiate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

/**
 * The Class MockServletResponse.
 */
public abstract class MockServletResponse implements HttpServletResponse {

    /** The is flushed. */
    boolean isFlushed;

    /** The error code. */
    int errorCode;

    /** The headers. */
    Map<String, String> headers;

    /** The headers added. */
    Map<String, List<String>> headersAdded;

    /** The sc. */
    int sc;

    @Override
    public void addHeader(final String name, final String value) {
        if (this.headersAdded.containsKey(name)) {
            this.headersAdded.get(name).add(value);
            return;
        }

        final List<String> values = new ArrayList<>();
        values.add(value);
        this.headersAdded.put(name, values);
    }

    @Override
    public void flushBuffer() throws IOException {
        this.isFlushed = true;
    }

    @Override
    public void sendError(final int sendError) throws IOException {
        this.errorCode = sendError;
    }

    @Override
    public void setHeader(final String name, final String value) {
        this.headers.put(name, value);
    }

}
