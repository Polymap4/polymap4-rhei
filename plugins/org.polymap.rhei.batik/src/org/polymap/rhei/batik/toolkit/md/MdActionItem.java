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

import java.util.function.Consumer;

import org.eclipse.swt.events.SelectionEvent;
import org.polymap.core.runtime.config.Concern;
import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.Mandatory;

import org.polymap.rhei.batik.toolkit.md.MdToolbar2.ToolItemEvent;

/**
 * 
 */
public class MdActionItem
        extends MdItem {
    
    public MdActionItem( MdItemContainer container ) {
        super( container );
    }
    
    @Mandatory
    @Concern( ToolItemEvent.Fire.class )
    public Config2<MdActionItem,Consumer<SelectionEvent>> action;
    
}