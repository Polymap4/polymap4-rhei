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

import org.eclipse.swt.graphics.Image;

import org.polymap.core.runtime.config.Concern;
import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.Configurable;

import org.polymap.rhei.batik.toolkit.md.MdToolbar2.ToolItemEvent;

/**
 * 
 */
public abstract class MdItem
        extends Configurable {
    
    @Concern( ToolItemEvent.Fire.class )
    public Config2<MdItem,String>        text;
    
    @Concern( ToolItemEvent.Fire.class )
    public Config2<MdItem,String>        tooltip;
    
    @Concern( ToolItemEvent.Fire.class )
    public Config2<MdItem,Image>         icon;

    private MdItemContainer              container;
    
    
    public MdItem( MdItemContainer container ) {
        this.container = container;
        if (container != null) {
            container.addItem( this );
        }
    }
    
    
    /**
     * The container of this item, or null if this is a root container. 
     */
    public <C extends MdItemContainer> C container() {
        return (C)container;
    }
    
}