/*
 * polymap.org
 * Copyright 2013, Falko Bräutigam. All rights reserved.
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
package org.polymap.rhei.batik.layout.desktop;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.window.ApplicationWindow;

import org.eclipse.ui.forms.widgets.ScrolledPageBook;
import org.eclipse.ui.forms.widgets.SharedScrolledComposite;

import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.UIUtils;

import org.polymap.rhei.batik.PanelChangeEvent;
import org.polymap.rhei.batik.PanelChangeEvent.TYPE;
import org.polymap.rhei.batik.layout.desktop.DesktopAppManager.DesktopPanelSite;

/**
 * The main application window for the desktop.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class DesktopAppWindow
        extends ApplicationWindow {

    private static Log log = LogFactory.getLog( DesktopAppWindow.class );

    protected DesktopAppManager       appManager;
    
    protected Composite               panels;

    protected Composite               contents;

    protected StatusManager           statusManager;


    public DesktopAppWindow( DesktopAppManager appManager ) {
        super( null );
        this.appManager = appManager;
        statusManager = getStatusManager();
    }


    protected abstract Composite fillNavigationArea( Composite parent );

    protected abstract Composite fillPanelArea( Composite parent );

    protected abstract StatusManager getStatusManager();
    

    @Override
    protected Control createContents( Composite parent ) {
        if (contents == null) {
            contents = (Composite)super.createContents( parent );
            contents.setLayout( new FormLayout() );
        }

        // navi
        Composite navi = fillNavigationArea( contents );
        navi.setLayoutData( FormDataFactory.filled().bottom( -1 ).height( 30 ).create() );
        UIUtils.setVariant( navi, "atlas-navi"  );

        panels = fillPanelArea( contents );
        panels.setLayoutData( FormDataFactory.filled().top( navi, 10 ).create() );
        UIUtils.setVariant( panels, "atlas-panels" );

        appManager.getContext().addListener( this, new EventFilter<PanelChangeEvent>() {
            public boolean apply( PanelChangeEvent input ) {
                return input.getType() == TYPE.ACTIVATED;
            }
        });
        return contents;
    }

    

    @EventHandler(display=true)
    protected void panelChanged( PanelChangeEvent ev ) {
        DesktopPanelSite panelSite = (DesktopPanelSite)ev.getSource().getSite();
        getShell().setText( "Mosaic - " + panelSite.getTitle() );
        getShell().layout();
    }


    @Override
    protected void configureShell( final Shell shell ) {
        super.configureShell( shell );
        shell.setText( "Mosaic" );
        shell.setTouchEnabled( true );

        Rectangle bounds = Display.getCurrent().getBounds();
        shell.setBounds( 0, 60, bounds.width, bounds.height - 60 );
//        shell.setMaximized( true );
        
        shell.getDisplay().addListener( SWT.Resize, new Listener() {
            public void handleEvent( Event ev ) {
                log.info( "Display size: " + ev.width + "x" + ev.height );
                
                getShell().setBounds( 0, 60, ev.width, ev.height - 60 );
                //getShell().layout();
                //((ScrolledPageBook)panels).reflow( true );
                
                delayedRefresh( null );
            }
        });
    }


    private int refreshCount = (int)System.currentTimeMillis();
    
    public void delayedRefresh( final Shell shell ) {
        final Shell s = shell != null ? shell : getShell();

        // XXX this forces the content send twice to the client (measureString: calculate text height)
        // without layout fails sometimes (page to short, no content at all)
//        s.layout( true );
        ((SharedScrolledComposite)panels).reflow( true );
        
        // FIXME HACK! force re-layout after font sizes are known (?)
        UIUtils.activateCallback( DesktopAppWindow.class.getName() );
        s.getDisplay().timerExec( 1000, new Runnable() {
            public void run() {
                log.info( "layout..." );

//                Rectangle bounds = Display.getCurrent().getBounds();
//                int random = (refreshCount++ % 3);
//                s.setBounds( 0, 60, bounds.width, bounds.height - 60 - random );

                s.layout( true );
                ((ScrolledPageBook)panels).reflow( true );
                //((Composite)scrolled.getCurrentPage()).layout();
                
                UIUtils.deactivateCallback( DesktopAppWindow.class.getName() );
            }
        });
    }

    
    @Override
    protected int getShellStyle() {
        // no border, no title
        return SWT.NO_TRIM;
    }


    @Override
    protected boolean showTopSeperator() {
        return false;
    }

}
