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

import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.Configurable;
import org.polymap.core.runtime.config.DefaultInt;
import org.polymap.core.runtime.config.Immutable;
import org.polymap.core.runtime.config.Mandatory;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormDataFactory.Alignment;


/**
 * The standard horizontal layout: label - field - decorator in a row.
 *
 * @author Falko Bräutigam
 */
public class HorizontalFieldLayout
        extends Configurable
        implements IFormFieldLayout {
    
    /** The horizontal layout with default label width. */
    public static final IFormFieldLayout INSTANCE = new HorizontalFieldLayout();

    /** The horizontal layout with no label at all. */
    public static final IFormFieldLayout NO_LABEL = new HorizontalFieldLayout().labelWidth.put( 0 );

    // instance *******************************************

    @Mandatory
    @Immutable
    @DefaultInt( 100 )
    public Config2<HorizontalFieldLayout,Integer>   labelWidth;

    
    @Override
    public void createLayout( Composite parent, Map<Part,Control> parts ) {
        parent.setLayout( new FormLayout() );

        Control labelControl = parts.get( Part.Label );
        Control fieldControl = parts.get( Part.Field );
        Control decoControl = parts.get( Part.Decorator );

        FormDataFactory.on( labelControl ).top( fieldControl, 0, Alignment.CENTER ).left( 0 ).width( labelWidth.get() );
        FormDataFactory.on( fieldControl ).top( 0 ).left( labelControl, 5 ).right( 100, -19 ).width( 50 );
        FormDataFactory.on( decoControl ).top( fieldControl, 0, Alignment.CENTER ).left( fieldControl, 0 ).right( 100 );

        parent.pack( true );
    }
    
}
