/*
 * polymap.org
 * Copyright (C) 2013-2016, Falko Bräutigam. All rights reserved.
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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.ui.PlatformUI;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rap.rwt.application.EntryPoint;
import org.eclipse.rap.rwt.internal.serverpush.ServerPushManager;

import org.polymap.core.runtime.UIJob;
import org.polymap.core.ui.StatusDispatcher;
import org.polymap.core.ui.UIUtils;

import org.polymap.rhei.batik.app.IAppDesign;
import org.polymap.rhei.batik.app.IAppManager;
import org.polymap.rhei.batik.engine.BatikFactory;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
@SuppressWarnings( "restriction" )
public class BatikApplication
        implements EntryPoint {

    private static final Log log = LogFactory.getLog( BatikApplication.class );


    /**
     * @deprecated Use {@link UIUtils} instead.
     */
    public static Display sessionDisplay() {
        return UIUtils.sessionDisplay();
    }

//    public Point displayDPI() {
//        return sessionDisplay().getDPI();
//    }
//
//    public static Point toPixel() {
//        PixelConverter pc = new PixelConverter( JFaceResources.getDefaultFont() );
//        int width100 = pc.convertWidthInCharsToPixels( 100 );
//        int height100 = pc.convertHeightInCharsToPixels( 100 );
//        return new Point( width100, height100 );
//    }
    
    
    /**
     * Use {@link UIUtils} instead.
     */
    public static Shell shellToParentOn() {
        return UIUtils.shellToParentOn();
    }

    private static Map<Display,BatikApplication> instances = new ConcurrentHashMap();
    
    /**
     * The instance of the current thread/session.
     */
    public static BatikApplication instance() {
        return instances.get( UIUtils.sessionDisplay() );
    }

    // instance *******************************************

    private Display                     display;

    private Shell                       mainWindow;

    private IAppManager                 appManager;

    private IAppDesign                  appDesign;


    public IAppManager getAppManager() {
        return appManager;
    }

    public IAppContext getContext() {
        return getAppManager().getContext();
    }

    public IAppDesign getAppDesign() {
        return appDesign;
    }

    @Override
    public int createUI() {
        display = new Display();  // PlatformUI.createDisplay();

        initUICallback();
        
        instances.put( display, this );
        log.info( "Display DPI: " + display.getDPI().x + "x" + display.getDPI().y );

        appManager = BatikFactory.instance().createAppManager();
        appDesign = BatikFactory.instance().createAppDesign();
        try {
            appManager.init();
            appDesign.init();

            mainWindow = appDesign.createMainWindow( display );

            // main loop
            while (!mainWindow.isDisposed()) {
                try {
                    if (!display.readAndDispatch()) {
                        display.sleep();
                    }
                }
                catch (ThreadDeath e) {
                    throw e;
                }
                catch (Throwable e) {
                    StatusDispatcher.handleError( "Unable to perform this operation.", e );
                }
            }
            log.info( "Exiting..." );
        }
        finally {
            appDesign.close();
            appManager.close();
        }

        instances.remove( display );
        display.dispose();
        return PlatformUI.RETURN_OK;
    }
    
    /**
     * Initializes the UI callback behaviour for this session. Called from
     * {@link #createUI()}.
     */
    protected void initUICallback() {
        UIUtils.activateCallback( BatikApplication.class.getSimpleName() );
        ServerPushManager serverPush = ServerPushManager.getInstance();
        serverPush.setRequestCheckInterval( 30000 );
        
        // Adds a runnable every 30s; this causes the UI callback request to be woken up
        // and returning to the client; this re-news the request and prevents intermediate
        // proxies (nginx = 60s timeout) to simple close the request and get us in trouble
        //
        // Besides, the periodic request keeps the HTTP session open as long as the browser
        // windows is open; we have very short HTTP session timeouts that close session short
        // after browser stops those UI callback pings
        UIJob job = new UIJob( "ReleaseBlockedRequest" ) {
            @Override
            protected void runWithException( IProgressMonitor monitor ) throws Exception {
                if (!display.isDisposed()) {
                    display.asyncExec( () -> {
                        //System.out.print( "." );                
                    });
                    schedule( 30000 );
                }
            }
        };
        job.setSystem( true );
        job.schedule( 30000 );
    }
}
