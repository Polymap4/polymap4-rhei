/* 
 * polymap.org
 * Copyright (C) 2016, the @authors. All rights reserved.
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
package org.polymap.rhei.batik.help;

import static org.polymap.core.runtime.event.TypeEventFilter.ifType;

import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.ui.forms.events.ExpansionEvent;

import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;

import org.polymap.rhei.batik.DefaultPanel;
import org.polymap.rhei.batik.contribution.ContributionManager;
import org.polymap.rhei.batik.dashboard.Dashboard;
import org.polymap.rhei.batik.dashboard.IDashlet;
import org.polymap.rhei.batik.toolkit.LayoutConstraint;
import org.polymap.rhei.batik.toolkit.PriorityConstraint;

/**
 * Register this panel to activate help system in your application.
 *
 * @author Falko Bräutigam
 */
public abstract class HelpPanel
        extends DefaultPanel {

    public static final String          DASHBOARD_ID = "org.polymap.rhei.batik.help";
    
    private Dashboard                   dashboard;


    /**
     * Override this method to set a panel icon, maybe adjust tooltip and set proper
     * size of the panel.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    public boolean beforeInit() {
        return parentPanel()
                .filter( parent -> !(parent instanceof HelpPanel) )
                .map( parent -> {
                    site().title.set( "" );
                    site().tooltip.set( "Help / Feedback" );
                    return true;
                })
                .orElse( false );
    }


    @Override
    public void createContents( Composite parent ) {
        site().title.set( "Help / Feedback" );
        ContributionManager.instance().contributeTo( this, this );

        dashboard = new Dashboard( getSite(), DASHBOARD_ID ).defaultExpandable.put( true ).defaultBorder.put( true );
        ContributionManager.instance().contributeTo( dashboard, this, DASHBOARD_ID );
        
        // collaps all but the highest priority
        List<IDashlet> sorted = dashboard.dashlets().stream()
                .sorted( (d1,d2) -> {
                    Comparable p1 = priorityOf( d1 );
                    Comparable p2 = priorityOf( d2 );
                    return p2.compareTo( p1 );
                })
                .collect( Collectors.toList() );
        sorted.forEach( dashlet -> dashlet.site().setExpanded( false ) );
        sorted.stream().findFirst().ifPresent( first -> first.site().setExpanded( true ) );
        
        dashboard.createContents( parent );
        
        for (IDashlet dashlet : dashboard.dashlets()) {
            assert dashlet instanceof HelpDashlet: "Make sure that help dashlets are instanceof HelpDashlet!";
            ((HelpDashlet)dashlet).init( new HelpSite() );
        }

        EventManager.instance().subscribe( this, ifType( ExpansionEvent.class, ev -> 
                dashboard.dashlets().stream().anyMatch( d -> d.site().getPanelSection() == ev.getSource() ) ) );
    }

    
    protected Comparable priorityOf( IDashlet dashlet ) {
        for (LayoutConstraint c : dashlet.site().constraints.get()) {
            if (c instanceof PriorityConstraint ) {
                return ((PriorityConstraint)c).getValue();
            }
        }
        throw new IllegalStateException( "HelpDashlet must have priority set: " + dashlet );
    }

    
    /** Makes sure that at most one dashlet is open. */ 
    @EventHandler( display=true )
    protected void onDashletExpansion( ExpansionEvent ev ) {
        if (ev.getState()) {
            for (IDashlet dashlet : dashboard.dashlets()) {
                if (dashlet.site().isExpanded() && dashlet.site().getPanelSection() != ev.getSource()) {
                    dashlet.site().setExpanded( false );
                }
            }
        }
    }


    /**
     * 
     */
    public class HelpSite {
    }
    
}
