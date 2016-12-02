/* 
 * polymap.org
 * Copyright (C) 2016, Falko Bräutigam. All rights reserved.
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

import static org.polymap.rhei.batik.app.SvgImageRegistryHelper.NORMAL24;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.viewers.ViewerCell;

import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.ConfigurationFactory;
import org.polymap.core.runtime.config.Mandatory;

import org.polymap.rhei.batik.BatikPlugin;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class RadioboxActionProvider
        extends ActionProvider {

    private static final Log log = LogFactory.getLog( RadioboxActionProvider.class );

    /** Defaults to: "radiobox-marked.svg", NORMAL24 */
    @Mandatory
    public Config2<RadioboxActionProvider,Image> selectedImage;

    /** Defaults to: "radiobox-blank.svg", NORMAL24 */
    @Mandatory
    public Config2<RadioboxActionProvider,Image> unselectedImage;

    /** The selected element, or null. */
    private Object                      selection;
    
    
    public RadioboxActionProvider() {
        ConfigurationFactory.inject( this );
        selectedImage.set( BatikPlugin.images().svgImage( "radiobox-marked.svg", NORMAL24 ) );
        unselectedImage.set( BatikPlugin.images().svgImage( "radiobox-blank.svg", NORMAL24 ) );
        selection = initSelection( null );
    }

    
    public boolean isSelected( Object elm ) {
        assert elm != null;
        return elm == selection;
    }
    
    
    /**
     * Updates the selection state of the given element. Should be called after the
     * model has been changed outside the viewer. This does not call
     * {@link #onSelection(MdListViewer, Object, boolean)} callback.
     *
     * @param elm The updated elm
     * @param selected The new selection state.
     */
    public void updateSelection( MdListViewer viewer, Object elm, boolean selected ) {
        assert elm != null;
        this.selection = selected ? elm : null;
        viewer.update( elm, null );
    }

    
    /**
     * The initial selected element. This method is called from the constructor.
     */
    protected abstract Object initSelection( MdListViewer viewer );

    protected abstract void onSelection( MdListViewer viewer, Object elm, boolean selected );
    
    
    @Override
    public void update( ViewerCell cell ) {
        cell.setImage( selection == cell.getElement() ? selectedImage.get() : unselectedImage.get() );
    }


    /**
     * This default implementation updates the {@link #isSelected()} state and
     * the image according to this new state. Override
     */
    @Override
    public void perform( MdListViewer viewer, Object elm ) {
        Object _selected = selection;
        // unselect
        if (selection != null) {
            onSelection( viewer, selection, false );
            selection = null;
            viewer.update( _selected, null );
        }
        // select
        if (elm != _selected) {
            onSelection( viewer, elm, true );
            selection = elm;
            viewer.update( elm, null );
        }
    }
    
}
