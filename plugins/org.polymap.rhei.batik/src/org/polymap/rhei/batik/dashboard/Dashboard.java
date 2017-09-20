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
package org.polymap.rhei.batik.dashboard;

import static org.polymap.core.runtime.event.TypeEventFilter.ifType;
import static org.polymap.rhei.batik.toolkit.IPanelSection.EXPANDABLE;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import java.beans.PropertyChangeEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.ui.forms.events.ExpansionEvent;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.Configurable;
import org.polymap.core.runtime.config.DefaultBoolean;
import org.polymap.core.runtime.config.Mandatory;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;

import org.polymap.rhei.batik.BatikApplication;
import org.polymap.rhei.batik.IPanelSite;
import org.polymap.rhei.batik.toolkit.IPanelSection;
import org.polymap.rhei.batik.toolkit.IPanelToolkit;
import org.polymap.rhei.batik.toolkit.LayoutConstraint;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class Dashboard
        extends Configurable {

    private static final Log log = LogFactory.getLog( Dashboard.class );

    /**
     * The default border setting used for all dashlets with no
     * {@link DashletSite#border} set.
     */
    @Mandatory
    @DefaultBoolean( true )
    public Config2<Dashboard,Boolean>       defaultBorder;
    
    /**
     * The default expandable setting used for all dashlets with no
     * {@link DashletSite#expandable} set.
     */
    @Mandatory
    @DefaultBoolean( false )
    public Config2<Dashboard,Boolean>       defaultExpandable;
    
    private String                          id;
    
    private IPanelSite                      panelSite;
    
    private Map<IDashlet,DashletSiteImpl>   dashlets = new HashMap();
    
    private boolean                         isAccordion;
    
    
    public Dashboard( IPanelSite panelSite, String id ) {
        this.panelSite = panelSite;
        this.id = id;
    }
    
    
    public void dispose() {
        EventManager.instance().unsubscribe( this );
        dashlets.keySet().stream().forEach( dashlet -> dashlet.dispose() );
        dashlets = null;
    }
    
    
    public boolean isDisposed() {
        return dashlets == null;
    }
    
    
    public Dashboard addDashlet( IDashlet dashlet ) {
        assert !isDisposed();
        DashletSiteImpl site = new DashletSiteImpl( dashlet );
        BatikApplication.instance().getContext().propagate( dashlet );
        dashlets.put( dashlet, site );
        dashlet.init( site );
        return this;
    }
    
    
    public Set<IDashlet> dashlets() {
        assert !isDisposed();
        return Collections.unmodifiableSet( dashlets.keySet() );
    }

    
    public Composite createContents( Composite parent ) {
        assert !isDisposed();
        IPanelToolkit tk = panelSite.toolkit();
        
        for (Entry<IDashlet,DashletSiteImpl> entry : dashlets.entrySet()) {
            DashletSiteImpl dashletSite = entry.getValue();
            IDashlet dashlet = entry.getKey();
    
            String title = dashletSite.title.get();
            int border = dashletSite.border.orElse( defaultBorder ) ? SWT.BORDER : SWT.NONE;
            int expandable = dashletSite.expandable.orElse( defaultExpandable ) ? EXPANDABLE : SWT.NONE;
            IPanelSection section = tk.createPanelSection( parent, title, border, expandable );
            
            dashletSite.panelSection = Optional.of( section );
            
            List<LayoutConstraint> constraints = dashletSite.constraints.get();
            section.addConstraint( constraints.toArray( new LayoutConstraint[constraints.size()]) );

            // changes after createContents() have no effect and should fail fast
            dashletSite.constraints = null;

            setExpanded( dashlet, dashletSite.isExpanded() );
        }
    
        // listen to changes of the site made by the dashlet
        EventManager.instance().subscribe( this, ifType( PropertyChangeEvent.class, ev -> 
                dashlets.values().contains( ev.getSource() ) ) );
        
        EventManager.instance().subscribe( this, ifType( ExpansionEvent.class, ev -> true ) );
//        EventManager.instance().subscribe( this, ifType( ExpansionEvent.class, ev -> 
//                dashlets().stream().anyMatch( d -> d.site().getPanelSection() == ev.getSource() ) ) );

        return parent;
    }


    public Dashboard setExpanded( IDashlet dashlet, boolean expanded ) {
        assert !isDisposed();
        DashletSiteImpl dashletSite = dashlets.get( dashlet );

        dashletSite.setExpanded( expanded );

        dashletSite.panelSection.ifPresent( panelSection -> {
            if (expanded && panelSection.getBody().getChildren().length == 0) {
                dashlet.createContents( panelSection.getBody() );
            }
            panelSection.setExpanded( expanded );
            
            dashletSite.title.ifPresent( title -> {
                panelSection.setTitle( title + (expanded ? "" : " ...") );
            });
        });
        return this;
    }

    
    public boolean isExpanded( IDashlet dashlet ) {
        assert !isDisposed();
        DashletSiteImpl dashletSite = dashlets.get( dashlet );
        return dashletSite.isExpanded();
    }

    
    @EventHandler( display=true )
    protected void onSectionExpansion( ExpansionEvent ev ) {
        if (!isDisposed()) {
            dashlets.values().stream()
                    .filter( site -> site.panelSection.get() == ev.getSource() )
                    .findAny().ifPresent( dashletSite -> dashletSite.setExpanded( ev.getState() ) );
         
            if (isAccordion && ev.getState() ) {
                for (IDashlet dashlet : dashlets()) {
                    if (dashlet.site().isExpanded() && dashlet.site().getPanelSection() != ev.getSource()) {
                        dashlet.site().setExpanded( false );
                    }
                }
            }
        }
    }

    
    @EventHandler
    protected void onSitePropertyChange( PropertyChangeEvent ev ) {
        if (!isDisposed()) {
            if (ev.getPropertyName().equals( "title" )) {
                log.warn( "!!! Dashlet TITLE changed! !!!" );    
            }
        }
    }
    
    
    /**
     * True if any of the {@link ISubmitableDashlet}s is is dirty.
     */
    public boolean isDirty() {
        for (IDashlet dashlet : dashlets.keySet()) {
            if (dashlet instanceof ISubmitableDashlet && dashlet.site().isDirty()) {
                return true;
            }
        }
        return false;
    }
    
    
    /**
     * True if all of the {@link ISubmitableDashlet}s are valid.
     */
    public boolean isValid() {
        for (IDashlet dashlet : dashlets.keySet()) {
            if (dashlet instanceof ISubmitableDashlet && !dashlet.site().isValid()) {
                return false;
            }
        }
        return true;
    }
    
    
    public void submit( IProgressMonitor monitor ) throws Exception {
        for (IDashlet dashlet : dashlets.keySet()) {
            if (dashlet instanceof ISubmitableDashlet) {
                assert dashlet.site().isValid();
                if (dashlet.site().isDirty()) {
                    ((ISubmitableDashlet)dashlet).submit( monitor );
                
                    ((DashletSiteImpl)dashlet.site()).enableSubmit( false, true );
                }
            }
        }
    }
    
    
    /**
     * 
     */
    protected class DashletSiteImpl
            extends DashletSite {

        private IDashlet                dashlet;
        
        private boolean                 dirty, valid = true;

        /** Not there before {@link Dashboard#createContents(Composite)}. */
        private Optional<IPanelSection> panelSection = Optional.empty();
        
        private boolean                 expanded = true;

        public DashletSiteImpl( IDashlet dashlet ) {
            this.dashlet = dashlet;
        }

        @Override
        public DashletSite addConstraint( LayoutConstraint... constraint ) {
            constraints.get().addAll( Arrays.asList( constraint ) );
            return this;
        }

        @Override
        public IPanelSite panelSite() {
            return panelSite;
        }

        @Override
        public IPanelToolkit toolkit() {
            return panelSite.toolkit();
        }

        @Override
        @SuppressWarnings( "hiding" )
        public void enableSubmit( boolean dirty, boolean valid ) {
            assert dashlet instanceof ISubmitableDashlet;
            this.dirty = dirty;
            this.valid = valid;
            EventManager.instance().publish( new SubmitStatusChangeEvent( dashlet, Dashboard.this, dirty, valid ) );
        }
        
        @Override
        public boolean isDirty() {
            return dirty;
        }

        @Override
        public boolean isValid() {
            return valid;
        }

        @Override
        public Control getTitleControl() {
            assert panelSection.isPresent() : "getTitleControl() is not available in createContents()";
            return panelSection.get().getTitleControl();
        }
        
        @Override
        public IPanelSection getPanelSection() {
            assert panelSection.isPresent() : "getPanelSection() is not available in createContents()";
            return panelSection.get();
        }
        
        @Override
        public boolean isExpanded() {
            return expanded;
        }

        @Override
        public DashletSite setExpanded( boolean expanded ) {
            if (this.expanded != expanded) {
                this.expanded = expanded;
                Dashboard.this.setExpanded( dashlet, expanded );
            }
            return this;
        }
    }
    
}
