/* 
 * polymap.org
 * Copyright (C) 2013-2014, Polymap GmbH. All rights reserved.
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
package org.polymap.rhei.batik;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.ServiceTracker;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.statushandlers.StatusAdapter;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.statushandlers.StatusManager.INotificationListener;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.ProgressProvider;

import org.polymap.rhei.batik.app.SvgImageRegistryHelper;

/**
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class BatikPlugin 
        extends AbstractUIPlugin {

    private static final Log log = LogFactory.getLog( BatikPlugin.class );
    
    public static final String          PLUGIN_ID = "org.polymap.rhei.batik";

    public static final String          CSS_PREFIX = "batik-";
    public static final String          CSS_TABLE_ACTION = CSS_PREFIX + "table-action";
    
    private static BatikPlugin          instance;
    
    public static BatikPlugin instance() {
        return instance;
    }

    /**
     * Shortcut for <code>instance().images</code>.
     */
    public static SvgImageRegistryHelper images() {
        return instance().images;
    }


    // instance *******************************************

    private ServiceTracker              httpServiceTracker;
    
    public SvgImageRegistryHelper       images;


    @Override
    public void start( BundleContext context ) throws Exception {
        super.start( context );
        instance = this;
        
        // images
        images = new SvgImageRegistryHelper( this );

        // StatusManager of the desktop layout registers itself as ProgressProvider
        
        // status
        StatusManager.getManager().addListener( new INotificationListener() {
            public void statusManagerNotified( int type, StatusAdapter[] adapters ) {
                for (StatusAdapter adapter : adapters) {
                    IStatus status = adapter.getStatus();
                    log.warn( "[STATUS] " + status.getMessage(), status.getException() );
                }
            }
        });

        // ProgressProvider        
        Job.getJobManager().setProgressProvider( new ProgressProvider() {
            @Override
            public IProgressMonitor createMonitor( Job job ) {
                return new NullProgressMonitor() {
                    @Override
                    public void beginTask( String name, int totalWork ) {
                        log.info( "[PROGRESS] '" + name + "' started" );
                    }
                    @Override
                    public void subTask( String name ) {
                        log.info( "[PROGRESS] subtask '" + name + "' started" );
                    }
                    @Override
                    public void worked( int work ) {
                        System.out.print( "." );
                    }
                    @Override
                    public void done() {
                        log.info( "[PROGRESS] '" + job.getName() + "' done" );
                    }
                };
            }
        });
        
        // register HTTP resource
        httpServiceTracker = new ServiceTracker( context, HttpService.class.getName(), null ) {
            public Object addingService( ServiceReference reference ) {
                HttpService httpService = (HttpService)super.addingService( reference );                
                if (httpService != null) {
                    try {
                        httpService.registerResources( "/batikres", "/resources", null );
                    }
                    catch (NamespaceException e) {
                        throw new RuntimeException( e );
                    }
                }
                return httpService;
            }
        };
        httpServiceTracker.open();
    }

    
    @Override
    public void stop( BundleContext context ) throws Exception {
        httpServiceTracker.close();
        httpServiceTracker = null;
        
        super.stop( context );
        instance = null;
    }

}
