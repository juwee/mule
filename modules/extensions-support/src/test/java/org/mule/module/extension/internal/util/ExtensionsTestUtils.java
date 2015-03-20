/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extension.internal.util;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import org.mule.api.MuleEvent;
import org.mule.extension.introspection.DataType;
import org.mule.extension.introspection.Parameter;
import org.mule.module.extension.internal.runtime.resolver.ValueResolver;

import org.apache.commons.lang.ArrayUtils;

public abstract class ExtensionsTestUtils
{

    public static final String HELLO_WORLD = "Hello World!";

    public static ValueResolver getResolver(Object value, MuleEvent event, boolean dynamic, Class<?>... extraInterfaces) throws Exception
    {
        ValueResolver resolver;
        if (ArrayUtils.isEmpty(extraInterfaces))
        {
            resolver = mock(ValueResolver.class);
        }
        else
        {
            resolver = mock(ValueResolver.class, withSettings().extraInterfaces(extraInterfaces));
        }

        when(resolver.resolve(event)).thenReturn(value);
        when(resolver.isDynamic()).thenReturn(dynamic);

        return resolver;
    }

    public static Parameter getParameter(String name, Class<?> type)
    {
        Parameter parameter = mock(Parameter.class);
        when(parameter.getName()).thenReturn(name);
        when(parameter.getType()).thenReturn(DataType.of(type));

        return parameter;
    }
}
