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

import java.util.function.Consumer;

import org.eclipse.swt.events.SelectionEvent;

import org.polymap.core.runtime.config.Concern;
import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.Mandatory;

/**
 * A two state item that is notified when un/selected.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ToggleItem
        extends Item {

    /**
     * Not yet implemented.
     */
    @Mandatory
    @Concern( NotDisposed.class )
    @Concern( ItemEvent.Fire.class )
    public Config2<ToggleItem,Boolean>                  selected;

    /**
     * The action to be performed when the item is selected.
     */
    @Mandatory
    @Concern( NotDisposed.class )
    @Concern( ItemEvent.Fire.class )
    public Config2<ToggleItem,Consumer<SelectionEvent>> onSelected;

    /**
     * The action to be performed when the item is unselected.
     */
    @Concern( NotDisposed.class )
    @Concern( ItemEvent.Fire.class )
    public Config2<ToggleItem,Consumer<SelectionEvent>> onUnselected;


    public ToggleItem( ItemContainer container ) {
        super( container );
    }
    
}
