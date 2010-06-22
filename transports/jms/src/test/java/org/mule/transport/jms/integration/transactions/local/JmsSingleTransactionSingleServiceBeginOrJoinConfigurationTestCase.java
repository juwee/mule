/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.jms.integration.transactions.local;

import org.mule.transport.jms.integration.AbstractJmsSingleTransactionSingleServiceTestCase;

/**
 * Test all combinations of (inbound) BEGIN_OR_JOIN.  They should all pass.
 */
public class JmsSingleTransactionSingleServiceBeginOrJoinConfigurationTestCase extends
    AbstractJmsSingleTransactionSingleServiceTestCase
{
    protected String getConfigResources()
    {
        return "integration/transactions/local/jms-single-tx-single-service-begin-or-join.xml";
    }
}
