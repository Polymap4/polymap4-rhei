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
package org.polymap.rhei.batik.toolkit.md;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.polymap.core.runtime.config.Concern;
import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.Immutable;
import org.polymap.core.runtime.config.Mandatory;

import org.polymap.rhei.batik.toolkit.md.MdToolbar2.Alignment;
import org.polymap.rhei.batik.toolkit.md.MdToolbar2.ToolItemEvent;

/**
 * 
 */
public class MdGroupItem
        extends MdItem
        implements MdItemContainer {

    @Mandatory
    @Immutable
    @Concern( ToolItemEvent.Fire.class )
    public Config2<MdGroupItem,String>      id;
    
    /** Defaults to {@link Alignment#Left}. */
    @Mandatory
    @Concern( ToolItemEvent.Fire.class )
    public Config2<MdGroupItem,Alignment>   align;
    
    private List<MdItem>                    items = new ArrayList();
    
    
    public MdGroupItem( MdItemContainer container, String id ) {
        super( container );
        this.id.set( id );
        this.align.set( Alignment.Left );
    }


    @Override
    public void addItem( MdItem item ) {
        items.add( item );
    }


    @Override
    public List<MdItem> items() {
        return Collections.unmodifiableList( items );
    }
    
}