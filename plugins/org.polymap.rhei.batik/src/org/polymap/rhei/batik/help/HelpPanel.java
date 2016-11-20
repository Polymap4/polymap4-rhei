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

import org.eclipse.swt.widgets.Composite;

import org.eclipse.ui.forms.events.ExpansionEvent;

import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;

import org.polymap.rhei.batik.DefaultPanel;
import org.polymap.rhei.batik.contribution.ContributionManager;
import org.polymap.rhei.batik.dashboard.Dashboard;
import org.polymap.rhei.batik.dashboard.IDashlet;

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

        // dashboard
        dashboard = new Dashboard( getSite(), DASHBOARD_ID ).defaultExpandable.put( true ).defaultBorder.put( true );
        ContributionManager.instance().contributeTo( dashboard, this, DASHBOARD_ID );
        // XXX collapse all but the highest priority
        dashboard.createContents( parent );
        
        for (IDashlet dashlet : dashboard.dashlets()) {
            assert dashlet instanceof HelpDashlet: "Make sure that help dashlets are instanceof HelpDashlet!";
            ((HelpDashlet)dashlet).init( new HelpSite() );
        }

        EventManager.instance().subscribe( this, ifType( ExpansionEvent.class, ev -> 
                dashboard.dashlets().stream().anyMatch( d -> d.site().getPanelSection() == ev.getSource() ) ) );
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
