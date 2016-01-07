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

import static com.google.common.collect.Iterables.concat;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
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
    
    public static ContributionManager instance() {
        return instance( ContributionManager.class );
    }
    
    private static final ContributionHandler[]          handlers = { new ContributionHandler.PanelFabHandler() };
    
    private static List<ContributionProviderExtension>  staticSuppliers = new CopyOnWriteArrayList();
    
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

    private Cache<Class,Pair<IContributionProvider,ContributionHandler>> cache = CacheBuilder.newBuilder().softValues().build();
    
            
    public void contributeTo( Object target, IPanel panel ) {
        Pair<IContributionProvider,ContributionHandler> contrib = contributionForTarget( target );
        IContributionSite site = newSite( panel );
        ContributionHandler handler = contrib.getRight();
        IContributionProvider provider = contrib.getLeft();
        handler.handle( site, provider, target );
    }

    
    protected Pair<IContributionProvider,ContributionHandler> contributionForTarget( Object target ) {
        try {
            return cache.get( target.getClass(), () -> {
                Iterable<ContributionProviderExtension> suppliers = concat( staticSuppliers, ContributionProviderExtension.all.get() );
                
                for (ContributionHandler handler : handlers) {
                    for (ContributionProviderExtension supplier : suppliers) {
                        IContributionProvider provider = supplier.createProvider();
                        try {
                            if (handler.test( provider, target )) {
                                BatikApplication.instance().getContext().propagate( provider );
                                return Pair.of( provider, handler );
                            }
                        }
                        catch (ClassCastException e) {
                            // type parameter does not match, ignore and next
                        }
                    }
                }
                return null;
            });
        }
        catch (ExecutionException e) {
            throw new RuntimeException( e );
        }
    }

    
//    /**
//     * Make contributions to the FAB of the given panel.
//     *
//     * @param panel The panel to contribute to.
//     */
//    public void contributeFab( IPanel panel ) {
//        factories().forEach( factory -> factory.fillFab( newSite( panel ) ) );
//    }
//
//    
//    /**
//     * Make contributions to the given toolbar of the given panel.
//     *
//     * @param panel The panel to contribute to.
//     * @param tb2 
//     */
//    public void contributeToolbar( IPanel panel, Object toolbar ) {
//        factories().forEach( factory -> factory.fillToolbar( newSite( panel ), toolbar ) );
//    }
//
//    
//    protected Iterable<IContributionProvider> providers( Class<? extends IContributionProvider> type ) {
//        IAppContext context = BatikApplication.instance().getContext();
//        
//        return Streams.iterable(
//                concat( staticSuppliers.stream(), ContributionProviderExtension.all.get().stream() )
//                .map( supplier -> {
//                    IContributionProvider provider = supplier.createProvider();
//                    context.propagate( provider );
//                    return provider;
//                }));
//    }

    
    protected IContributionSite newSite( IPanel panel ) {
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
        };
    }
    
    
}
