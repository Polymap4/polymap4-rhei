/* 
 * polymap.org
 * Copyright (C) 2015-2016, Falko Bräutigam. All rights reserved.
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

import static com.google.common.collect.Iterables.concat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.runtime.session.SessionContext;
import org.polymap.core.runtime.session.SessionSingleton;

import org.polymap.rhei.batik.BatikApplication;
import org.polymap.rhei.batik.IAppContext;
import org.polymap.rhei.batik.IPanel;
import org.polymap.rhei.batik.PanelSite;
import org.polymap.rhei.batik.toolkit.IPanelToolkit;

/**
 * Registry of contribution providers and API to contribute to UI. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ContributionManager
        extends SessionSingleton {

    private static Log log = LogFactory.getLog( ContributionManager.class );
    
    private static final ContributionHandler[]          handlers = { 
            new ContributionHandler.PanelFabHandler(),
            new ContributionHandler.ToolbarHandler() };
    
    private static List<ContributionProviderExtension>  staticSuppliers = new CopyOnWriteArrayList();
    
    
    /**
     * The instance of the current {@link SessionContext}.
     */
    public static ContributionManager instance() {
        return instance( ContributionManager.class );
    }
    
    /**
     * Programmatically register an {@link IContributionProvider} extension.
     */
    public static boolean registerExtension( ContributionProviderExtension supplier ) {
        return staticSuppliers.add( supplier );
    }
    
    
    public static boolean unregisterExtension( ContributionProviderExtension supplier ) {
        return staticSuppliers.remove( supplier );
    }
    
    
    // instance *******************************************

    /**
     * Add contributions to the given target of the given panel. 
     *
     * @param target
     * @param panel
     * @param tags Tags that identify the target.
     */
    public void contributeTo( Object target, IPanel panel, String... tags ) {
        IContributionSite site = newSite( panel, tags );
        for (ContributionHandler handler : handlers) {
            try {
                if (handler.test( target )) {
                    for (ContributionProviderExtension supplier : suppliers()) {
                        IContributionProvider provider = supplier.createProvider();
                        BatikApplication.instance().getContext().propagate( provider );

                        try {
                            handler.handle( site, provider, target );
                        }
                        catch (ClassCastException e) {
                            // type parameter does not match, ignore and next
                        }
                    }
                }
            }
            catch (ClassCastException e) {
                // type parameter does not match when calling test(), ignore and next
            }
        }
    }

    
    /**
     * All suppliers: {@link #staticSuppliers} + {@link ContributionProviderExtension#all}
     */
    protected Iterable<ContributionProviderExtension> suppliers() {
        return concat( staticSuppliers, ContributionProviderExtension.all.get() );
    }

    
    protected IContributionSite newSite( IPanel panel, String[] tags ) {
        assert panel != null;
        assert tags != null;
        
        Set<String> tagsSet = new HashSet();
        if (tags.length > 0 && !tagsSet.addAll( Arrays.asList( tags ) )) {
            throw new IllegalArgumentException( "Duplicate tags in: " + Arrays.asList( tags ) );
        }

        return new IContributionSite() {
            @Override
            public IPanel panel() {
                return panel;
            }
            @Override
            public PanelSite panelSite() {
                return panel.site();
            }
            @Override
            public IAppContext context() {
                return BatikApplication.instance().getContext();
            }
            @Override
            public <T extends IPanelToolkit> T toolkit() {
                return (T)panelSite().toolkit();
            }
            @Override
            public String[] tags() {
                return tags;
            }
            @Override
            public boolean tagsContain( String tag ) {
                return tagsSet.contains( tag );
            }
        };
    }
    
    
}
