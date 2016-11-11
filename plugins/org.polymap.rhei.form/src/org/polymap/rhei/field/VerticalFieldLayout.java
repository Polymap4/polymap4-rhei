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
package org.polymap.rhei.field;

import java.util.Map;

import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.resource.JFaceResources;

import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.Configurable;
import org.polymap.core.runtime.config.DefaultInt;
import org.polymap.core.runtime.config.Mandatory;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormDataFactory.Alignment;

/**
 * The standard vertical layout: label above field.
 *
 * @author Falko Bräutigam
 */
public class VerticalFieldLayout
        extends Configurable
        implements IFormFieldLayout {
    
    /** The default vertical layout. */
    public static final IFormFieldLayout INSTANCE = new VerticalFieldLayout();

    // instance ********************************************

    /** Space between label and field. */
    @Mandatory
    @DefaultInt( 0 )
    public Config2<VerticalFieldLayout,Integer>   spacing;

    
    @Override
    public void createLayout( Composite parent, Map<Part,Control> parts ) {
        parent.setLayout( new FormLayout() );

        Control labelControl = parts.get( Part.Label );
        Control fieldControl = parts.get( Part.Field );
        Control decoControl = parts.get( Part.Decorator );

        FormDataFactory.on( labelControl ).fill().noBottom();
        FormDataFactory.on( fieldControl ).top( labelControl, spacing.get() ).left( 0 ).right( 100, -19 ).width( 50 );
        FormDataFactory.on( decoControl ).top( fieldControl, 0, Alignment.CENTER ).left( fieldControl, 0 ).right( 100 );

        labelControl.setFont( JFaceResources.getFontRegistry().getBold( JFaceResources.DEFAULT_FONT ) );

        parent.pack( true );
    }
    
}
