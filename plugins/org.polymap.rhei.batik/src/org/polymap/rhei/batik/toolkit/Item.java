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

import org.eclipse.swt.graphics.Image;

import org.polymap.core.runtime.config.Concern;
import org.polymap.core.runtime.config.Config;
import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.Configurable;
import org.polymap.core.runtime.config.DefaultBoolean;
import org.polymap.core.runtime.config.DefaultPropertyConcern;
import org.polymap.core.runtime.config.Immutable;

/**
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class Item
        extends Configurable {
    
    @Concern( NotDisposed.class )
    @Concern( ItemEvent.Fire.class )
    public Config2<Item,String>         text;
    
    @Concern( NotDisposed.class )
    @Concern( ItemEvent.Fire.class )
    public Config2<Item,String>         tooltip;
    
    @Concern( NotDisposed.class )
    @Concern( ItemEvent.Fire.class )
    public Config2<Item,Image>          icon;

    /**
     * Just handle state and throw event, check/set via {@link #dispose()} and
     * {@link #isDisposed()}.
     */
    @Immutable
    @DefaultBoolean( false )
    @Concern( ItemEvent.Fire.class )
    private Config2<Item,Boolean>       disposed;

    private ItemContainer               container;
    
    
    public Item( ItemContainer container ) {
        this.container = container;
        if (container != null) {
            boolean added = container.addItem( this );
            assert added;
        }
    }

    /**
     * Dispose this item and remove any visible representation. Attempts to access
     * properties of this item will throw {@link IllegalStateException}.
     */
    public void dispose() {
        disposed.set( true );
        
        if (container != null) {
            boolean removed = container.removeItem( this );
            assert removed;
        }
    }
    
    public boolean isDisposed() {
        return disposed.get();
    }
    
    /**
     * The container of this item, or null if this is a root container. 
     */
    public <C extends ItemContainer> C container() {
        return (C)container;
    }
    
    
    /**
     * 
     */
    public static class NotDisposed
            extends DefaultPropertyConcern {

        @Override
        public Object doGet( Object obj, Config prop, Object value ) {
            Item item = prop.info().getHostObject();
            if (item.disposed.get()) {
                throw new IllegalStateException( "Item is disposed" );
            }
            return super.doGet( obj, prop, value );
        }

        @Override
        public Object doSet( Object obj, Config prop, Object newValue ) {
            Item item = prop.info().getHostObject();
            if (item.disposed.get()) {
                throw new IllegalStateException( "Item is disposed" );
            }
            return super.doSet( obj, prop, newValue );
        }
        
    }

}