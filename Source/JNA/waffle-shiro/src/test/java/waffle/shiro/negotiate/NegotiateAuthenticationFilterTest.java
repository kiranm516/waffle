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
import java.util.Base64;
import java.util.HashMap;

import javax.servlet.http.HttpServletResponse;

import mockit.Deencapsulation;
import mockit.Tested;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * The Class NegotiateAuthenticationFilterTest.
 */
public final class NegotiateAuthenticationFilterTest {

    /** The neg auth filter. */
    @Tested
    private NegotiateAuthenticationFilter negAuthFilter;

    /** The response. */
    MockServletResponse response;

    /** The out. */
    private byte[] out;

    /**
     * Sets the up.
     */
    @BeforeEach
    public void setUp() {
        this.response = Mockito.mock(MockServletResponse.class, Mockito.CALLS_REAL_METHODS);
        Deencapsulation.setField(this.response, "headers", new HashMap<>());
        Deencapsulation.setField(this.response, "headersAdded", new HashMap<>());
    }

    /**
     * Test is login attempt.
     */
    @Test
    public void testIsLoginAttempt() {
        Assertions.assertFalse(this.negAuthFilter.isLoginAttempt(""));
        Assertions.assertTrue(this.negAuthFilter.isLoginAttempt("NEGOTIATe"));
        Assertions.assertTrue(this.negAuthFilter.isLoginAttempt("ntlm"));
    }

    /**
     * Test send challenge during negotiate.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void testSendChallengeDuringNegotiate() throws IOException {

        final String myProtocol = "myProtocol";

        this.out = new byte[1];
        this.out[0] = -1;

        this.negAuthFilter.sendChallengeDuringNegotiate(myProtocol, this.response, this.out);

        Assertions.assertEquals(String.join(" ", myProtocol, Base64.getEncoder().encodeToString(this.out)),
                this.response.headers.get("WWW-Authenticate"));

        Assertions.assertEquals("keep-alive", this.response.headers.get("Connection"));

        Assertions.assertEquals(HttpServletResponse.SC_UNAUTHORIZED, this.response.errorCode);

        Assertions.assertFalse(this.response.isFlushed);
    }

    /**
     * Test send challenge initiate negotiate.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void testSendChallengeInitiateNegotiate() throws IOException {

        this.out = new byte[1];
        this.out[0] = -1;

        this.negAuthFilter.sendChallengeInitiateNegotiate(this.response);

        Assertions.assertEquals("Negotiate", this.response.headersAdded.get("WWW-Authenticate").get(0));
        Assertions.assertEquals("NTLM", this.response.headersAdded.get("WWW-Authenticate").get(1));

        Assertions.assertEquals("keep-alive", this.response.headers.get("Connection"));

        Assertions.assertEquals(HttpServletResponse.SC_UNAUTHORIZED, this.response.errorCode);

        Assertions.assertFalse(this.response.isFlushed);
    }

    /**
     * Test send challenge on failure.
     */
    @Test
    public void testSendChallengeOnFailure() {

        this.negAuthFilter.sendChallengeOnFailure(this.response);

        Assertions.assertEquals("Negotiate", this.response.headersAdded.get("WWW-Authenticate").get(0));
        Assertions.assertEquals("NTLM", this.response.headersAdded.get("WWW-Authenticate").get(1));

        Assertions.assertEquals("close", this.response.headers.get("Connection"));

        Assertions.assertEquals(HttpServletResponse.SC_UNAUTHORIZED, this.response.errorCode);

        Assertions.assertTrue(this.response.isFlushed);
    }

}
