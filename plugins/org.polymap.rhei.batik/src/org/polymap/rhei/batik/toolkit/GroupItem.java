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
package org.polymap.rhei.batik.toolkit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.polymap.core.runtime.config.Concern;
import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.Immutable;
import org.polymap.core.runtime.config.Mandatory;

import org.polymap.rhei.batik.toolkit.md.MdToolbar2.Alignment;

/**
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class GroupItem<T extends Item>
        extends Item
        implements ItemContainer<T> {

    @Mandatory
    @Immutable
    @Concern( NotDisposed.class )
    @Concern( ItemEvent.Fire.class )
    public Config2<GroupItem,String>    id;
    
    /** Defaults to {@link Alignment#Left}. */
    @Mandatory
    @Concern( NotDisposed.class )
    @Concern( ItemEvent.Fire.class )
    public Config2<GroupItem,Alignment> align;
    
    private List<T>                     items = new ArrayList();
    
    
    public GroupItem( ItemContainer container, String id ) {
        super( container );
        this.id.set( id );
        this.align.set( Alignment.Left );
    }


    @Override
    public boolean addItem( T item ) {
        return items.add( item );
    }


    @Override
    public boolean removeItem( T item ) {
        return items.remove( item );
    }


    @Override
    public List<T> items() {
        return Collections.unmodifiableList( items );
    }
    
}
