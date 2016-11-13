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
package org.polymap.rhei.batik.toolkit.md;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.TreeItem;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerColumn;

import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.ConfigurationFactory;
import org.polymap.core.runtime.config.DefaultFloat;
import org.polymap.core.runtime.config.Mandatory;
import org.polymap.core.ui.HSLColor;
import org.polymap.core.ui.UIUtils;

import org.polymap.rhei.batik.toolkit.md.MdAppDesign.FontStyle;

/**
 * Emphasize the hierarchy level of the items of a {@link MdListViewer}.
 *
 * @author Falko Bräutigam
 */
public class TreeExpandStateDecorator
    extends CellLabelProvider
    implements ILabelProvider {

    private static final Log log = LogFactory.getLog( TreeExpandStateDecorator.class );

    private MdListViewer            viewer;
    
    private IBaseLabelProvider      delegate;
    
    /**
     * Adjust saturation of {@link #bgColor} by the given percent for each tree
     * hierarchy level.
     */
    @Mandatory
    @DefaultFloat( 50f )
    public Config2<TreeExpandStateDecorator,Float> saturationDelta;
    
    /**
     * Adjust saturation of {@link #bgColor} by the given percent for each tree
     * hierarchy level.
     */
    @Mandatory
    @DefaultFloat( -2f )
    public Config2<TreeExpandStateDecorator,Float> luminanceDelta;
    
    /**
     * The background color of expanded cells adjusted by {@link #luminanceDelta} and
     * {@link #saturationDelta} for each hierarchy level. Defaults to $action_bg.
     */
    @Mandatory
    public Config2<TreeExpandStateDecorator,Color> bgColor;
    
    
    public TreeExpandStateDecorator( MdListViewer viewer, IBaseLabelProvider delegate ) {
        ConfigurationFactory.inject( this );
        this.delegate = delegate;
        this.viewer = viewer;
        this.bgColor.set( UIUtils.getColor( 0x59, 0x9F, 0xE2 ) );
    }

    @Override
    public void update( ViewerCell cell ) {
        ((CellLabelProvider)delegate).update( cell );

        Object elm = cell.getElement();
        boolean expanded = viewer.getExpandedState( elm );
        
        TreeItem item = (TreeItem)cell.getItem();
        int level = 0;
        for (TreeItem i = item; i != null; i = i.getParentItem()) {
            level++;
        }

        HSLColor bg = bgColor
                .map( color -> new HSLColor( color).adjustLuminance( 34 ).adjustSaturation( -100 ) )
                .orElse( new HSLColor( viewer.getTree().getBackground() ) );

        if (expanded) {
            level--;
            cell.setFont( UIUtils.bold( MdAppDesign.font( FontStyle.Subhead ) ) );
        }
        else {
            level--;
            cell.setFont( MdAppDesign.font( FontStyle.Subhead ) );
        }

        float delta = saturationDelta.get() * level;
        HSLColor levelColor = bg.adjustSaturation( delta ).adjustLuminance( luminanceDelta.get() * level );
        item.setBackground( levelColor.toSWT() );
    }

    @Override
    public Image getImage( Object element ) {
        return ((ILabelProvider)delegate).getImage( element );
    }

    @Override
    public String getText( Object element ) {
        return ((ILabelProvider)delegate).getText( element );
    }
    
    // CellLabelProvider **********************************

    protected CellLabelProvider delegate() {
        return (CellLabelProvider)delegate;
    }
    
    public void addListener( ILabelProviderListener listener ) {
        delegate.addListener( listener );
    }

    public void dispose() {
        delegate.dispose();
    }

    public boolean isLabelProperty( Object element, String property ) {
        return delegate.isLabelProperty( element, property );
    }

    public void removeListener( ILabelProviderListener listener ) {
        delegate.removeListener( listener );
    }

    public Image getToolTipImage( Object object ) {
        return delegate().getToolTipImage( object );
    }

    public String getToolTipText( Object element ) {
        return delegate().getToolTipText( element );
    }

    public Color getToolTipBackgroundColor( Object object ) {
        return delegate().getToolTipBackgroundColor( object );
    }

    public Color getToolTipForegroundColor( Object object ) {
        return delegate().getToolTipForegroundColor( object );
    }

    public Font getToolTipFont( Object object ) {
        return delegate().getToolTipFont( object );
    }

    public Point getToolTipShift( Object object ) {
        return delegate().getToolTipShift( object );
    }

    public boolean useNativeToolTip( Object object ) {
        return delegate().useNativeToolTip( object );
    }

    public int getToolTipTimeDisplayed( Object object ) {
        return delegate().getToolTipTimeDisplayed( object );
    }

    public int getToolTipDisplayDelayTime( Object object ) {
        return delegate().getToolTipDisplayDelayTime( object );
    }

    public int getToolTipStyle( Object object ) {
        return delegate().getToolTipStyle( object );
    }

    public void dispose( @SuppressWarnings( "hiding" ) ColumnViewer viewer, ViewerColumn column ) {
        delegate().dispose( viewer, column );
    }
    
}
