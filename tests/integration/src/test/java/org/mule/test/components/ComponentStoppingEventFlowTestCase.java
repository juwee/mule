/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.test.components;

import org.mule.api.MuleMessage;
import org.mule.module.client.MuleClient;
import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.RequestContext;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ComponentStoppingEventFlowTestCase extends FunctionalTestCase
{

    @Override
    protected String getConfigResources()
    {
        return "org/mule/test/components/component-stopped-processing.xml";
    }

    @Test
    public void testNullReturnStopsFlow() throws Exception
    {
        while (true)
        {
            Thread.sleep(1000);
        }
    }

    public static final class ComponentStoppingFlow
    {

        public String process(String input)
        {
            RequestContext.getEventContext().setStopFurtherProcessing(true);
            return TEST_MESSAGE;
        }
    }
}
