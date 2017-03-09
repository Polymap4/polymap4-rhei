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
package org.polymap.rhei.batik.engine;

import static java.util.Collections.reverseOrder;
import static java.util.Comparator.comparing;
import static org.polymap.core.ui.UIUtils.setVariant;
import static org.polymap.rhei.batik.IPanelSite.PanelStatus.INITIALIZED;
import static org.polymap.rhei.batik.IPanelSite.PanelStatus.VISIBLE;
import static org.polymap.rhei.batik.toolkit.md.MdAppDesign.dp;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.service.BrowserNavigation;
import org.eclipse.rap.rwt.client.service.BrowserNavigationEvent;
import org.eclipse.rap.rwt.client.service.BrowserNavigationListener;

import org.polymap.core.runtime.StreamIterable;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.core.ui.UIUtils;

import org.polymap.rhei.batik.BatikApplication;
import org.polymap.rhei.batik.BatikPlugin;
import org.polymap.rhei.batik.IPanel;
import org.polymap.rhei.batik.IPanelSite.PanelStatus;
import org.polymap.rhei.batik.PanelChangeEvent;
import org.polymap.rhei.batik.PanelChangeEvent.EventType;
import org.polymap.rhei.batik.PanelPath;
import org.polymap.rhei.batik.PanelSite;
import org.polymap.rhei.batik.app.IAppDesign;
import org.polymap.rhei.batik.app.SvgImageRegistryHelper;
import org.polymap.rhei.batik.engine.PageStack.Page;
import org.polymap.rhei.batik.engine.PageStack.PanelSizeSupplier;
import org.polymap.rhei.batik.toolkit.ConstraintLayout;
import org.polymap.rhei.batik.toolkit.DefaultToolkit;
import org.polymap.rhei.batik.toolkit.IPanelToolkit;
import org.polymap.rhei.batik.toolkit.LayoutSupplier;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class DefaultAppDesign
        implements IAppDesign, BrowserNavigationListener {

    private static final Log log = LogFactory.getLog( DefaultAppDesign.class );
    
    public static final String          CSS_PANELS = "atlas-panels";
    public static final String          CSS_PANEL = "atlas-panel";
    public static final String          CSS_ACTIONS = "atlas-actions";
    public static final String          CSS_HEADER = "atlas-header";
    public static final String          CSS_SHELL = "atlas-shell";
    public static final String          CSS_PANEL_HEADER = CSS_PANEL + "-header";
    public static final String          CSS_PANEL_CLIENT = CSS_PANEL + "-client";
    public static final String          CSS_SWITCHER = CSS_PANEL + "-switcher";

    private DefaultAppManager           appManager;

    protected Display                   display;
    
    protected Shell                     mainWindow;
    
    protected BrowserNavigation         browserHistory;

    protected DefaultUserPreferences    userPrefs;

    protected PageStack<PanelPath>      panelsArea;
    
    protected DefaultLayoutSupplier     panelLayoutSettings = new DefaultLayoutSupplier();
 
    protected DefaultLayoutSupplier     appLayoutSettings = new DefaultLayoutSupplier();

    private int                         pagePriorityCount;
    

    @Override
    public void init() {
        appManager = (DefaultAppManager)BatikApplication.instance().getAppManager();

        browserHistory = RWT.getClient().getService( BrowserNavigation.class );
        browserHistory.addBrowserNavigationListener( this );
    }


    @Override
    public void close() {
        if (browserHistory != null) {
            browserHistory.removeBrowserNavigationListener( this );
            browserHistory = null;
        }
        appManager.getContext().removeListener( this );
    }


    /** 
     * Browser history event. 
     */
    @Override
    public void navigated( BrowserNavigationEvent ev ) {
//        new SimpleDialog()
//                .setContents( parent -> {
//                    new Label( parent, SWT.WRAP ).setText( "Browser navigation is not supported yet." );
//                })
//                .addOkAction( () -> { 
//                    close(); return true; 
//                })
//                .open();
        
        // XXX start is not always the name of the first panel, this causes the first panel
        // to created, disposed and created again
        log.info( "navigated: " + ev.getState() + " - NOT SUPPORTED CURRENTLY!" );
//        if (!ev.getState().equals( "start" )) {
//            IAppContext context = appManager.getContext();
//            context.openPanel( PanelPath.ROOT, new PanelIdentifier( "start" ) );
//        }
    }


    @Override
    public IPanelToolkit createToolkit( PanelPath panelPath ) {
        PageStack<PanelPath>.Page panelParent = panelsArea.getPage( panelPath );
        return new DefaultToolkit( panelPath, panelParent );
    }


    @Override
    public Shell createMainWindow( @SuppressWarnings("hiding") Display display ) {
        this.display = display;
        mainWindow = new Shell( display, SWT.NO_TRIM );
        mainWindow.setMaximized( true );
        UIUtils.setVariant( mainWindow, CSS_SHELL );

        updateMainWindowLayout();
        
        mainWindow.addControlListener( new ControlAdapter() {
            private Rectangle lastDisplayArea = display.getBounds();
            @Override
            public void controlResized( ControlEvent ev ) {
                Rectangle displayArea = display.getBounds();
                if (!displayArea.equals( lastDisplayArea )) {
                    lastDisplayArea = displayArea;
                    updateMainWindowLayout();
                }
            }
        });
        
        // header
        Composite headerContainer = fillHeaderArea( mainWindow );
        if (headerContainer != null && headerContainer.getLayoutData() == null) {
            headerContainer.setLayoutData( FormDataFactory.filled().clearBottom().create() );
        }

        // panels
        Composite panelContainer = fillPanelsArea( mainWindow );
        panelContainer.setLayoutData( FormDataFactory.filled().top( 0, 3 ).create() );            
        if (headerContainer != null) {
            panelContainer.setLayoutData( FormDataFactory.filled().top( headerContainer ).create() );
        }

        appManager.getContext().addListener( this, ev -> ev.getType().isOnOf( EventType.LIFECYCLE, EventType.TITLE ) );
        
        mainWindow.open();
        return mainWindow;
    }


    protected Composite fillHeaderArea( Composite parent ) {
        return null;
    }


    protected Composite fillPanelsArea( Composite parent ) {
        // layout supplier
        DelegatingLayoutSupplier ls = new DelegatingLayoutSupplier( getAppLayoutSettings() ) {
            @Override
            public int getMarginLeft() { return 0; }
            @Override
            public int getMarginRight() { return 0; }
            @Override
            public int getMarginTop() { return getSpacing()/2; }
        };

        // panelsArea
        panelsArea = new PageStack<PanelPath>( parent, ls ) {
            
            private Set<Page>       previousShown;
            
            @Override
            protected void preUpdateLayout() {
                previousShown = getPages().stream()
                        .filter( page -> page.isShown )
                        .collect( Collectors.toSet() );                
                log.debug( "previousShown: " + previousShown );
            }
            
            @Override
            protected void postUpdateLayout() {
                log.debug( "now pages: " + panelsArea.getPages() );
                // update panel status according to isShown state
                getPages().stream()
                        .filter( page -> page.isShown != previousShown.contains( page ) )
                        .forEach( page -> {
                                PanelPath panelPath = page.key;
                                IPanel p = appManager.getPanel( panelPath );
                                PanelStatus panelStatus = p.site().panelStatus();

                                // new or focused pages are explicitly set isShown=true, they already have proper
                                // PanelStatus (>= VISIBLE); however, layout may decide to show pages when more space
                                // becomes available, those panels have status < VISIBLE and need to be updated
                                if (page.isShown && VISIBLE.ge( panelStatus )) {
                                    appManager.updatePanelStatus( p, VISIBLE );                                
                                }
                                //
                                if (!page.isShown && panelStatus.ge( VISIBLE )) {
                                    appManager.updatePanelStatus( p, INITIALIZED );                                
                                }
                        });
            }
        };
        panelsArea.showEmptyPage();
        return UIUtils.setVariant( panelsArea, CSS_PANELS );
    }

    
    @Override
    public LayoutSupplier getAppLayoutSettings() {
        return appLayoutSettings;
    }


    @Override
    public LayoutSupplier getPanelLayoutPreferences() {
        return panelLayoutSettings;
    }

    
    /**
     * Creates the contents of panel, including head and other decorations. Override
     * to change behaviour.
     */
    protected void createPanelContents( final IPanel panel, final Composite parent ) {
        // margins for shadow
        parent.setLayout( FormLayoutFactory.defaults()/*.margins( 1, 3, 0, 3 )*/.create() );
        //UIUtils.setVariant( parent, CSS_PANEL );
        
        // head
        Composite head = setVariant( new Composite( parent, SWT.BORDER | SWT.NO_FOCUS ), CSS_PANEL_HEADER );
        head.setLayoutData( FormDataFactory.filled().clearBottom().height( dp( 54 ) ).create() );
        head.setLayout( FormLayoutFactory.defaults().margins( 2 ).spacing( 2 ).create() );

        // decoration
        createPanelDecoration( panel, head );
      
        // title
        Label title = setVariant( new Label( head, SWT.NO_FOCUS|SWT.CENTER ), CSS_PANEL_HEADER );
        title.setData( "_type_", CSS_PANEL_HEADER );
        title.setText( panel.site().title.orElse( "..." ) );
        title.setLayoutData( FormDataFactory.filled()/*.left( center, 0, Alignment.CENTER )*/.top( 0, 5 ).create() );

        // scrolled
        ScrolledComposite scrolled = new ScrolledComposite( parent, SWT.NO_FOCUS | SWT.V_SCROLL );
        scrolled.setLayoutData( FormDataFactory.filled().top( 0, dp( 54 ) ).create() );
        scrolled.setExpandVertical( true );
        scrolled.setExpandHorizontal( true );
        scrolled.setTouchEnabled( true );
        scrolled.moveBelow( head );

        // panel
        Composite panelParent = setVariant( new Composite( scrolled, SWT.NO_FOCUS ), CSS_PANEL_CLIENT );
        panelParent.setLayout( new ConstraintLayout( new DelegatingLayoutSupplier( getPanelLayoutPreferences() ) {
            /** 
             * Add an extra margin on top of the panel; panel layout probably has 0
             * margin top in order to have compact sections
             */
            @Override
            public int getMarginTop() { return dp( 24 ); }
            @Override
            public int getMarginBottom() { return super.getMarginBottom() + dp( 16 ); }
        }));
        panel.createContents( panelParent );
        panelParent.setLayoutData( FormDataFactory.filled().top( 0, dp( 54 ) ).create() );

        scrolled.setContent( panelParent );
        scrolled.layout();
        
        // adapt size/scrollbars
        ControlAdapter resizeHandler = new ControlAdapter() {
            @Override
            public void controlResized( ControlEvent ev ) {
                Rectangle clientArea = scrolled.getClientArea();
                int scrollbarWidth = scrolled.getVerticalBar() != null ? scrolled.getVerticalBar().getSize().x : 0; 
                Point preferred = scrolled.getContent().computeSize( clientArea.width-scrollbarWidth, SWT.DEFAULT );
                // this also triggers the actual re-layout of the contents of the page
                // after PageStackLayout did a re-layout
                scrolled.setMinSize( preferred );

                //((Composite)scrolled.getContent()).layout( true );
            }
        };
        scrolled.addControlListener( resizeHandler );
        // called if client sent font measurements
        //scrolled.getContent().addControlListener( resizeHandler );
    }
    
    
    protected void createPanelDecoration( IPanel panel, Composite head  ) {
        // close btn
        if (panel.site().path().size() > 1) {
            Button closeBtn = UIUtils.setVariant( new Button( head, SWT.NO_FOCUS ), CSS_PANEL_HEADER );
            //closeBtn.setText( "x" );
            closeBtn.setImage( BatikPlugin.images().svgImage( "arrow-left.svg", SvgImageRegistryHelper.WHITE24 ) );
            closeBtn.setToolTipText( "Close this panel" );
            closeBtn.setLayoutData( FormDataFactory.filled().clearRight().width( dp( 50 ) ).create() );
            closeBtn.addSelectionListener( new SelectionAdapter() {
                @Override
                public void widgetSelected( SelectionEvent ev ) {
                    appManager.getContext().closePanel( panel.site().path() );
                }
            });
        }
        
        // switcher
        Composite switcher = new Composite( head, SWT.NONE );
//        switcher.setLayout( RowLayoutFactory.fillDefaults().margins( 1, 1 ).spacing( 5 ).fill( false ).create() );
        switcher.setLayout( FormLayoutFactory.defaults().spacing( 5 ).margins( 0, 0 ).create() );
        UIUtils.setVariant( switcher, CSS_SWITCHER );

        PanelPath panelPath = panel.site().path();
        //PanelPath prefix = panel.getSite().getPath().removeLast( 1 );
        StreamIterable.of( appManager.getContext().wantToBeShown( panelPath ) )
                .stream()
                .sorted( reverseOrder( comparing( p -> p.site().stackPriority.get() ) ) )
                .forEach( p -> {
                        int btnCount = switcher.getChildren().length;
                        Button btn = createSwitcherButton( switcher, p );
                        if (btn.getLayoutData() == null) {
                            btn.setLayoutData( btnCount == 0
                                    ? FormDataFactory.filled().noRight().create()
                                    : FormDataFactory.filled().noRight().left( switcher.getChildren()[btnCount-1] ).create() );
                        }

//                        IPanelSite panelSite = p.getSite();
//                        btn.setSelection( panelSite.getPanelStatus().ge( PanelStatus.VISIBLE ) );

                        btn.addSelectionListener( new SelectionAdapter() {
                            public void widgetSelected( SelectionEvent ev ) {
                                appManager.getContext().openPanel( panelPath, p.id() );
                            }
                        });
                });

        Point size = switcher.computeSize( SWT.DEFAULT, 30, true );
        switcher.setLayoutData( FormDataFactory.filled().clearLeft().width( size.x ).create() );
    }

    
    protected Button createSwitcherButton( Composite switcher, IPanel panel ) {
        final Button btn = UIUtils.setVariant( new Button( switcher, SWT.PUSH ), CSS_PANEL_HEADER );

        boolean showText = UIUtils.sessionDisplay().getClientArea().width > 900;
        
        // update title/icon
        Image icon = panel.site().icon.get();
        String title = panel.site().title.get();
        
        if (icon == null && title == null) {
            btn.setVisible( false );
        }
        else if (showText || icon == null) {
            btn.setText( title );
        }
        else {
            btn.setToolTipText( title );
        }
        if (icon != null) {
            btn.setImage( icon );
        }
        btn.setToolTipText( panel.site().tooltip.get() );
        return btn;
    }
    
    
    /**
     * Updates the contents of a panel, including head, after
     * {@link PanelChangeEvent}. Override to change behaviour.
     */
    protected void updatePanelContents( PanelSite panelSite ) {
        Page page = panelsArea.getPage( panelSite.path() );
        if (page != null) {
            UIUtils.visitChildren( page.control, child -> {
                if (CSS_PANEL_HEADER.equals( child.getData( "_type_" ) )) {
                    ((Label)child).setText( panelSite.title.orElse( "" ) );
                    return false;
                }
                else {
                    return true;
                }
            });
        }
    }

    
    /**
     * Sets/adapts the {@link #mainWindow} layout right after init and before
     * {@link #delayedRefresh()}. Override to change behaviour.
     */
    protected void updateMainWindowLayout() {
        Rectangle displayArea = Display.getCurrent().getBounds();

        int marginsWidth = -1;
        int spacing = -1;
        if (displayArea.width < 500) {
            marginsWidth = spacing = dp( 12 );
        }
        else if (displayArea.width < 1366) { // many current notebook displays?
            marginsWidth = spacing = dp( 24 );
        }
        else {
            marginsWidth = spacing = dp( 32 );
            marginsWidth += 100;
        }
        log.debug( "adjustLayout(): display width=" + displayArea.width + " -> spacing=" + spacing );
        
        // panel layout
        panelLayoutSettings.spacing = spacing;
        // space for section shadows
        // XXX fixes fucking, not working CSS margin; there is no way to add
        // margins to panelSections in CSS
        panelLayoutSettings.marginLeft = panelLayoutSettings.marginRight = 3;
        panelLayoutSettings.marginTop = panelLayoutSettings.marginBottom = 3;
        
        // app layout
        appLayoutSettings.spacing = spacing;
        appLayoutSettings.marginLeft = appLayoutSettings.marginRight = marginsWidth;
        appLayoutSettings.marginTop = 0;
        
        mainWindow.setLayout( FormLayoutFactory.defaults().margins( 
                appLayoutSettings.marginTop, appLayoutSettings.marginRight, 
                appLayoutSettings.marginBottom, appLayoutSettings.marginLeft ).create() );
        
        // propagate settings to PageStackLayout
        mainWindow.layout( true );
    }
    
    
    /**
     * Handle {@link EventType#LIFECYCLE} events.
     * <p/>
     * If <b>delay</b> is to <b>short</b> then intermediate states, such as panel is
     * closed before new panel gets opened, are displayed to the user. If it is to
     * <b>long</b> then there might be an remarkable delay in UI response.
     */
    @EventHandler( display=true, delay=75 )
    protected void panelChanged( List<PanelChangeEvent> evs ) {
        log.debug( "events: " + evs.stream().map( ev -> ev.toString() ).reduce( "", (r,s) -> r + "\n\t\t" + s ) );
        
        boolean layoutRefreshNeeded = false;
        Set<PanelSite> updatePanelSites = new HashSet();
        
        for (PanelChangeEvent ev : evs) {
            IPanel panel = ev.getPanel();

            // lifecycle event
            if (ev.getType() == EventType.LIFECYCLE) {
                PanelPath pageId = ev.getSource().path();
                PanelStatus newStatus = (PanelStatus)ev.getNewValue();
                PanelStatus oldStatus = (PanelStatus)ev.getOldValue();

                // visible
                if (newStatus == PanelStatus.VISIBLE && oldStatus == PanelStatus.INITIALIZED) {
                    if (!panelsArea.hasPage( pageId )) {
                        // all current panels fly-out to the left
                        panelsArea.getPages().stream().forEach( page -> UIUtils.setVariant( page.control, CSS_PANEL+"-left" ) );
                        
                        // every new panel is created on top
                        Composite page = panelsArea.createPage( pageId, pagePriorityCount++, new PanelSizeSupplier() {
                            @Override public int min() { return ev.getSource().minWidth.get(); }
                            @Override public int max() { return ev.getSource().maxWidth.get(); }
                            @Override public int preferred() { return ev.getSource().preferredWidth.get(); }
                        });
                        UIUtils.setVariant( page, CSS_PANEL+"-right" );
                        createPanelContents( panel, page );
                        layoutRefreshNeeded = true;
                    }

                    String title = ev.getSource().title.orElse( "" );
                    mainWindow.setText( title );
//                    browserHistory.pushState( panel.id().id(), "mapzone: " + StringUtils.abbreviate( title, 25 ) );
                }

                // disposed
                else if (newStatus == null) {
                    // not yet initialized panels have no page
                    if (panelsArea.hasPage( pageId )) {
                        Page page = panelsArea.getPage( pageId );
                        UIUtils.setVariant( page.control, CSS_PANEL+"-right" );
                        log.debug( "disposed: " + pageId );
                        panelsArea.removePage( pageId );
                        layoutRefreshNeeded = true;
                    }
                }
            }

            // title or icon changed
            else if (ev.getType() == EventType.TITLE) {
                updatePanelSites.add( ev.getSource() );
            }
        }
        
        if (layoutRefreshNeeded) {
            delayedRefresh();
        }
        for (PanelSite panelSite : updatePanelSites) {
            updatePanelContents( panelSite );
        }
    }


    @Override
    public void delayedRefresh() {
        updateMainWindowLayout();
        
        // XXX this forces the content send twice to the client (measureString: calculate text height)
        // without layout fails sometimes (page to short, no content at all)
          mainWindow.layout( true );
          panelsArea.reflow( true );
        
//        // FIXME HACK! force re-layout after font sizes are known (?)
////        UIUtils.activateCallback( DefaultAppDesign.class.getName() );
//        mainWindow.getDisplay().timerExec( 1000, new Runnable() {
//            public void run() {
//                log.debug( "layout..." );
//
//                mainWindow.layout( true );
//                panelsArea.reflow( true );
//                //((Composite)scrolled.getCurrentPage()).layout();
//                
////                UIUtils.deactivateCallback( DefaultAppDesign.class.getName() );
//            }
//        });
    }

    
    @Override
    public Composite panelParent( PanelPath path ) {
        return panelsArea.getPage( path ).control;
    }


    /**
     * 
     */
    public static class DefaultLayoutSupplier
            extends LayoutSupplier {
        
        public int marginLeft, marginRight, marginTop, marginBottom, spacing;
    
        @Override
        public int getMarginLeft() {
            return marginLeft;
        }
        @Override
        public int getMarginRight() {
            return marginRight;
        }
        @Override
        public int getMarginTop() {
            return marginTop;
        }
        @Override
        public int getMarginBottom() {
            return marginBottom;
        }
        @Override
        public int getSpacing() {
            return spacing;
        }
    }
    
    
    /**
     * 
     */
    public static class DelegatingLayoutSupplier
            extends LayoutSupplier {
        
        private LayoutSupplier          delegate;

        public DelegatingLayoutSupplier( LayoutSupplier delegate ) {
            this.delegate = delegate;
        }
        @Override
        public int getMarginLeft() {
            return delegate.getMarginLeft();
        }
        @Override
        public int getMarginRight() {
            return delegate.getMarginRight();
        }
        @Override
        public int getMarginTop() {
            return delegate.getMarginTop();
        }
        @Override
        public int getMarginBottom() {
            return delegate.getMarginBottom();
        }
        @Override
        public int getSpacing() {
            return delegate.getSpacing();
        }
        
    }
    
}
