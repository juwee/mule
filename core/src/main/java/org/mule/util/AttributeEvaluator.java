/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.util;

import org.mule.RequestContext;
import org.mule.api.MuleMessage;
import org.mule.api.expression.ExpressionManager;

import java.util.regex.Pattern;

import org.omg.CORBA.Request;

/**
 * This class acts as a wrapper for configuration attributes that support simple text, expression or regular
 * expressions. It can be extended to support other cases too.
 */
public class AttributeEvaluator
{
    private static final Pattern SINGLE_EXPRESSION_REGEX_PATTERN = Pattern.compile("^#\\[(?:(?!#\\[).)*\\]$");

    private enum AttributeType
    {
        EXPRESSION, STRING
    }

    private final String attributeValue;
    private ExpressionManager expressionManager;
    private AttributeType attributeType;

    public AttributeEvaluator(String attributeValue)
    {
        this.attributeValue = attributeValue;
    }

    public void initialize(final ExpressionManager expressionManager)
    {
        this.expressionManager = expressionManager;
        resolveAttributeType();
    }

    private void resolveAttributeType()
    {
        if (isSingleExpression(attributeValue))
        {
            this.attributeType = AttributeType.EXPRESSION;
        }
        else
        {
            this.attributeType = AttributeType.STRING;
        }
    }

    private boolean isSingleExpression(String expression)
    {
        return expression != null && SINGLE_EXPRESSION_REGEX_PATTERN.matcher(expression).matches();
    }

    public boolean isExpression()
    {
        return attributeType.equals(AttributeType.EXPRESSION);
    }

    public boolean isString()
    {
        return attributeType.equals(AttributeType.STRING);
    }

    public Object resolveValue(MuleMessage message)
    {
        if (isExpression())
        {
            return expressionManager.evaluate(attributeValue, RequestContext.getEvent());
        }
        else
        {
            return expressionManager.parse(attributeValue, RequestContext.getEvent());
        }
    }

    public String getRawValue()
    {
        return attributeValue;
    }
}
