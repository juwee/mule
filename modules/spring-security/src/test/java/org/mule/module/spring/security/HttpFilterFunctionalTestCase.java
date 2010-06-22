/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.spring.security;

import org.mule.tck.FunctionalTestCase;
import org.mule.transport.http.HttpConstants;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;

public class HttpFilterFunctionalTestCase extends FunctionalTestCase
{

    protected String getConfigResources()
    {
        return "http-filter-test.xml";
    }

    public void testAuthenticationFailureNoContext() throws Exception
    {
        HttpClient client = new HttpClient();
        client.getParams().setAuthenticationPreemptive(true);
        GetMethod get = new GetMethod(getUrl());

        get.setDoAuthentication(false);

        try
        {
            int status = client.executeMethod(get);
            assertEquals(HttpConstants.SC_UNAUTHORIZED, status);
            assertTrue(get.getResponseBodyAsString().contains("/index.html"));
        }
        finally
        {
            get.releaseConnection();
        }
    }

    public void testAuthenticationFailureBadCredentials() throws Exception
    {
        doRequest(null, "localhost", "anonX", "anonX", getUrl(), true, false, 401);
    }

    protected String getUrl()
    {
        return "http://localhost:4567/index.html";
    }

    public void testAuthenticationAuthorised() throws Exception
    {
        doRequest(null, "localhost", "anon", "anon", getUrl(), false, true, 200);
    }

    public void testAuthenticationAuthorisedWithHandshake() throws Exception
    {
        doRequest(null, "localhost", "anon", "anon", getUrl(), true, false, 200);
    }

    public void testAuthenticationAuthorisedWithHandshakeAndBadRealm() throws Exception
    {
        doRequest("blah", "localhost", "anon", "anon", getUrl(), true, false, 401);
    }

    public void testAuthenticationAuthorisedWithHandshakeAndRealm() throws Exception
    {
        doRequest("mule-realm", "localhost", "ross", "ross", getUrl(), true, false,
            200);
    }

    private void doRequest(String realm,
                           String host,
                           String user,
                           String pass,
                           String url,
                           boolean handshake,
                           boolean preemtive,
                           int result) throws Exception
    {
        HttpClient client = new HttpClient();
        client.getParams().setAuthenticationPreemptive(preemtive);
        client.getState().setCredentials(new AuthScope(host, -1, realm),
            new UsernamePasswordCredentials(user, pass));
        GetMethod get = new GetMethod(url);
        get.setDoAuthentication(handshake);

        try
        {
            int status = client.executeMethod(get);
            assertEquals(result, status);
        }
        finally
        {
            get.releaseConnection();
        }
    }

}
