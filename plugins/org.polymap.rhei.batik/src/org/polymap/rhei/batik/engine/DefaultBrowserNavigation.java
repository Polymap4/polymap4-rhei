/* 
 * polymap.org
 * Copyright (C) 2018, the @authors. All rights reserved.
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
package org.polymap.rhei.batik.engine;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.service.BrowserNavigation;
import org.eclipse.rap.rwt.client.service.BrowserNavigationEvent;
import org.eclipse.rap.rwt.client.service.BrowserNavigationListener;
import org.eclipse.rap.rwt.client.service.JavaScriptExecutor;

import org.polymap.core.runtime.Predicates;
import org.polymap.core.runtime.event.EventHandler;

import org.polymap.rhei.batik.BatikApplication;
import org.polymap.rhei.batik.IPanel;
import org.polymap.rhei.batik.IPanelSite.PanelStatus;
import org.polymap.rhei.batik.PanelChangeEvent;
import org.polymap.rhei.batik.PanelChangeEvent.EventType;
import org.polymap.rhei.batik.PanelPath;

/**
 * Used by {@link DefaultAppDesign} to handle browser Back button navigation.
 * <p/>
 * Simple implementation. Always one history entry. No document title support.
 * Restart app when "start" panel.
 *
 * @author Falko Bräutigam
 */
public class DefaultBrowserNavigation
        implements BrowserNavigationListener {

    private static final Log log = LogFactory.getLog( DefaultBrowserNavigation.class );
    
    private JavaScriptExecutor      js;

    private BrowserNavigation       browserHistory;

    private DefaultAppManager       appManager;
    
    private PanelPath               topPanelPath;
    
    
    void init() {
        appManager = (DefaultAppManager)BatikApplication.instance().getAppManager();
        appManager.getContext().addListener( this, ev -> ev.getType().isOnOf( EventType.LIFECYCLE ) );

        browserHistory = RWT.getClient().getService( BrowserNavigation.class );
        browserHistory.addBrowserNavigationListener( this );
        
        js = RWT.getClient().getService( JavaScriptExecutor.class );
        
        // there is always just one state (history entry); this state represents
        // the #topPanelPath; this is managed by #onPanelChanged(); browser back
        // button invokes #navigated() which restores the top state
        pushState( "start", "Start" );
    }


    void close() {
        browserHistory.removeBrowserNavigationListener( this );
        appManager.getContext().removeListener( this );
    }


    protected boolean isStartPanel( PanelPath path ) {
        return path.size() == 1; //lastSegment().id().equalsIgnoreCase( "start" );
    }

    
    /**
     * Handle {@link PanelChangeEvent}.
     * <p/>
     * Called after UI handlers have passed: <i>delay=500</i>
     */
    @EventHandler( display=true, delay=500 )
    protected void onPanelChanged( List<PanelChangeEvent> evs ) {
        log.warn( "Events: " + evs.stream().map( ev -> ev.toString() ).reduce( "", (r,s) -> r + "\n\t\t" + s ) );
        for (PanelChangeEvent ev : Lists.reverse( evs )) {
            PanelStatus newStatus = (PanelStatus)ev.getNewValue();
            PanelStatus oldStatus = (PanelStatus)ev.getOldValue();
            // newly created/opened panel
            if (newStatus == PanelStatus.VISIBLE && oldStatus == PanelStatus.INITIALIZED) {
                IPanel panel = ev.getPanel();
                topPanelPath = new PanelPath( panel.site().path() );
                replaceState( panel.id().id(), ev.getPanel().site().title.get() );
                break;
            }
            // top panel closed (by UI or browser back button)
            if (newStatus == null) {
                topPanelPath = ev.getSource().path().removeLast( 1 );
                IPanel panel = appManager.getPanel( topPanelPath );
                replaceState( panel.id().id(), panel.site().title.get() );
                break;
            }
        }
    }
    
    
    /** 
     * Browser history event. 
     */
    @Override
    public void navigated( BrowserNavigationEvent ev ) {
        if (topPanelPath != null
                // for every push/replaceState() there is an immediate event;
                // default RAP javascript code handels this - for our home grown code
                // we have to do it here on the server
                && !ev.getState().equals( "top" )
                && !ev.getState().equals( topPanelPath.lastSegment().id() )) {
            
            if (appManager.findPanels( Predicates.alwaysTrue() ).size() == 1) {
                js.execute( "window.location.reload();" );                
            }
            else {
                boolean closed = appManager.closePanel( topPanelPath );
                if (closed) {
                    // restore the top state
                    pushState( "top", "Top" );
                }
            }
        }
    }


    protected void pushState( String state, String title ) {
        assert !StringUtils.isBlank( state );
        log.warn( "pushState(): " + state + " / " + title );
        js.execute( Joiner.on( "" ).useForNull( "" ).join( 
                "window.history.pushState({},'", title, "','#", state, "');" ) );
                //"document.title='", title , "';" ) );
    }
    
    
    protected void replaceState( String state, String title ) {
        assert !StringUtils.isBlank( state );
        log.warn( "replaceState(): " + state + " / " + title );
        js.execute( Joiner.on( "" ).useForNull( "" ).join( 
                "window.history.replaceState({},'", title, "','#", state, "');" ) ); 
                //"document.title='", title , "';" ) );
    }
    
}
