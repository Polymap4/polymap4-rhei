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
package org.polymap.rhei.batik.toolkit.md;

import java.util.Optional;

import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerCell;

import org.polymap.rhei.batik.BatikApplication;
import org.polymap.rhei.batik.BatikPlugin;
import org.polymap.rhei.batik.IPanel;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.PanelPath;
import org.polymap.rhei.batik.app.SvgImageRegistryHelper;

/**
 * Provides default behaviour and default icon (chevron-right) for open-detail-panel
 * actions.
 *
 * @author Falko Bräutigam
 */
public class OpenPanelActionProvider<T>
        extends ActionProvider<T> {
    
    private static final OpenPanelActionProvider INSTANCE = new OpenPanelActionProvider();
    
    /**
     * Since instances of this class do not maintain any state, they can be shared
     * between multiple clients.
     */
    public static OpenPanelActionProvider instance() {
        return INSTANCE;
    }

    // instance *******************************************
    
    /**
     * Shortcut for:
     * <pre>
     *     BatikApplication.instance().getContext().openPanel( parentPath, panelId )
     * </pre>  
     */
    public <P extends IPanel> Optional<P> openPanel( PanelPath parentPath, PanelIdentifier panelId ) {
        return BatikApplication.instance().getContext().openPanel( parentPath, panelId );
    }

    
    /**
     * Default implementation fires an {@link OpenEvent} which triggers registered
     * {@link IOpenListener}s of the viewer.
     */
    @Override
    public void perform( MdListViewer viewer, T elm ) {
        viewer.fireOpen( new OpenEvent( viewer, new StructuredSelection( elm ) ) );
    }


    @Override
    public void update( ViewerCell cell ) {
        cell.setImage( BatikPlugin.images().svgImage( "chevron-right.svg", SvgImageRegistryHelper.NORMAL24 ) );
    }
    
}
