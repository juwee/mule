/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extension.internal;

import org.mule.api.MuleContext;
import org.mule.api.MuleException;
import org.mule.api.MuleRuntimeException;
import org.mule.api.context.MuleContextAware;
import org.mule.api.registry.SPIServiceRegistry;
import org.mule.api.registry.ServiceRegistry;
import org.mule.common.MuleVersion;
import org.mule.extension.ExtensionManager;
import org.mule.extension.introspection.Configuration;
import org.mule.extension.introspection.Extension;
import org.mule.module.extension.internal.introspection.DefaultExtensionFactory;
import org.mule.module.extension.internal.introspection.ExtensionDiscoverer;
import org.mule.util.Preconditions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link ExtensionManager}
 *
 * @since 3.7.0
 */
public final class DefaultExtensionManager implements ExtensionManager, MuleContextAware
{

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultExtensionManager.class);

    private MuleContext muleContext;
    private final Map<String, Extension> extensions = Collections.synchronizedMap(new LinkedHashMap<String, Extension>());
    private final ServiceRegistry serviceRegistry = new SPIServiceRegistry();
    private ExtensionDiscoverer extensionDiscoverer = new DefaultExtensionDiscoverer(new DefaultExtensionFactory(serviceRegistry), serviceRegistry);

    @Override
    public List<Extension> discoverExtensions(ClassLoader classLoader)
    {
        LOGGER.info("Starting discovery of extensions");

        List<Extension> discovered = extensionDiscoverer.discover(classLoader);
        LOGGER.info("Discovered {} extensions", discovered.size());

        ImmutableList.Builder<Extension> accepted = ImmutableList.builder();

        for (Extension extension : discovered)
        {
            if (registerExtension(extension))
            {
                accepted.add(extension);
            }
        }

        return accepted.build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean registerExtension(Extension extension)
    {
        LOGGER.info("Registering extension (version {})", extension.getName(), extension.getVersion());
        final String extensionName = extension.getName();

        if (extensions.containsKey(extensionName))
        {
            return maybeUpdateExtension(extension, extensionName);
        }
        else
        {
            doRegisterExtension(extension, extensionName);
            return true;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> void registerConfigurationInstance(Configuration configuration, String name, T instance)
    {
        try
        {
            muleContext.getRegistry().registerObject(getRegistryName(name, instance), instance);
        }
        catch (MuleException e)
        {
            throw new MuleRuntimeException(e);
        }
    }

    private String getRegistryName(String configName, Object config)
    {
        return String.format("_extensionConfig_%s@%d", configName, System.identityHashCode(config));
    }

    private boolean maybeUpdateExtension(Extension extension, String extensionName)
    {
        Extension actual = extensions.get(extensionName);
        MuleVersion newVersion;
        try
        {
            newVersion = new MuleVersion(extension.getVersion());
        }
        catch (IllegalArgumentException e)
        {
            LOGGER.warn(
                    String.format("Found extensions %s with invalid version %s. Skipping registration",
                                  extension.getName(), extension.getVersion()), e);

            return false;
        }

        if (newVersion.newerThan(actual.getVersion()))
        {
            logExtensionHotUpdate(extension, actual);
            doRegisterExtension(extension, extensionName);

            return true;
        }
        else
        {
            LOGGER.info("Found extension {} but version {} was already registered. Keeping existing definition",
                        extension.getName(),
                        extension.getVersion());

            return false;
        }
    }

    private void doRegisterExtension(Extension extension, String extensionName)
    {
        extensions.put(extensionName, extension);
    }

    private void logExtensionHotUpdate(Extension extension, Extension actual)
    {
        if (LOGGER.isInfoEnabled())
        {
            LOGGER.info(String.format(
                    "Found extension %s which was already registered with version %s. New version %s " +
                    "was found. Hot updating extension definition",
                    extension.getName(),
                    actual.getVersion(),
                    extension.getVersion()));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Extension> getExtensions()
    {
        return ImmutableSet.copyOf(extensions.values());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Extension> getExtensionsCapableOf(Class<?> capabilityType)
    {
        Preconditions.checkArgument(capabilityType != null, "capability type cannot be null");
        ImmutableSet.Builder<Extension> capables = ImmutableSet.builder();
        for (Extension extension : getExtensions())
        {
            if (extension.isCapableOf(capabilityType))
            {
                capables.add(extension);
            }
        }

        return capables.build();
    }

    @Override
    public void setMuleContext(MuleContext muleContext)
    {
        this.muleContext = muleContext;
    }

    protected void setExtensionsDiscoverer(ExtensionDiscoverer discoverer)
    {
        extensionDiscoverer = discoverer;
    }
}
