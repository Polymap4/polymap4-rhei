/* 
 * polymap.org
 * Copyright (C) 2015, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.rhei.batik.contribution;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import org.polymap.core.runtime.CachedLazyInit;
import org.polymap.core.runtime.Lazy;

import org.polymap.rhei.batik.BatikPlugin;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ContributionProviderExtension {
    
    private static Log log = LogFactory.getLog( ContributionManager.class );

    static final String         EXTENSION_POINT_ID = BatikPlugin.PLUGIN_ID + ".contributions";

    
    // XXX does not check plugin start/stop
    protected static Lazy<List<ContributionProviderExtension>> all = new CachedLazyInit( () -> {
        IConfigurationElement[] elms = Platform.getExtensionRegistry()
                .getConfigurationElementsFor( EXTENSION_POINT_ID );

        return Arrays.stream( elms )
                .map( elm -> new ContributionProviderExtension( elm ) )
                .collect( Collectors.toList() );
    });
    
    
    // instance *******************************************
    
    private IConfigurationElement       elm;

    /**
     * 
     */
    public ContributionProviderExtension() {
    }

    protected ContributionProviderExtension( IConfigurationElement elm ) {
        this.elm = elm;
    }

    public IContributionProvider createProvider() {
        assert elm != null : "Created without IConfigurationElement, must override createProvider()";
        try {
            return (IContributionProvider)elm.createExecutableExtension( "class" );
        }
        catch (CoreException e) {
            throw new RuntimeException( e );
        }
    }
    
}
