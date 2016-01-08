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
package org.polymap.rhei.batik.toolkit;

import org.eclipse.swt.graphics.Image;

import org.polymap.core.runtime.config.Concern;
import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.Configurable;

/**
 * 
 */
public abstract class Item
        extends Configurable {
    
    @Concern( ItemEvent.Fire.class )
    public Config2<Item,String>        text;
    
    @Concern( ItemEvent.Fire.class )
    public Config2<Item,String>        tooltip;
    
    @Concern( ItemEvent.Fire.class )
    public Config2<Item,Image>         icon;

    private ItemContainer              container;
    
    
    public Item( ItemContainer container ) {
        this.container = container;
        if (container != null) {
            container.addItem( this );
        }
    }
    
    
    /**
     * The container of this item, or null if this is a root container. 
     */
    public <C extends ItemContainer> C container() {
        return (C)container;
    }
    
}