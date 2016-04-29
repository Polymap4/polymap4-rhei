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
import org.polymap.core.runtime.config.DefaultBoolean;
import org.polymap.core.runtime.config.Mandatory;

/**
 * Just one {@link RadioItem} can be selected in the parent {@link GroupItem}.
 * Selecting one deselects another.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class RadioItem
        extends Item {

    /**
     * Current selection state of this item. Changing this deselects a maybe
     * currently selected other item, calls its {@link #onUnselected} handler and
     * then calls the {@link #onSelected} handler.
     */
    @Mandatory
    @DefaultBoolean( false )
    @Concern( ItemEvent.Fire.class )
    public Config2<RadioItem,Boolean>                   selected;

    /**
     * This handler is called when this item is selected (by UI or setting
     * {@link #selected}).
     */
    @Mandatory
    @Concern( ItemEvent.Fire.class )
    public Config2<RadioItem,Consumer<SelectionEvent>>  onSelected;

    /**
     * This handler is called when this item is unselected (by UI or setting
     * {@link #selected}).
     */
    @Concern( ItemEvent.Fire.class )
    public Config2<RadioItem,Consumer<SelectionEvent>>  onUnselected;


    public RadioItem( ItemContainer container ) {
        super( container );
    }


    @Override
    public String toString() {
        return "RadioItem[selected=" + selected.get() + ", text=" + text.get() + "]";
    }
    
}
