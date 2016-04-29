/* 
 * polymap.org
 * Copyright (C) 2016, Falko Bräutigam. All rights reserved.
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

import java.util.EventObject;
import java.util.Objects;

import org.polymap.core.runtime.config.Config;
import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.DefaultPropertyConcern;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;

/**
 * Fired when a {@link Config} property of an {@link Item} is changed.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ItemEvent
        extends EventObject {

    private Config          prop;
    
    private Object          newValue;
    
    public ItemEvent( Item source, Config prop, Object newValue ) {
        super( source );
        this.prop = prop;
        this.newValue = newValue;
    }

    public <T extends Item> T item() {
        return (T)super.getSource();
    }
    
    @Override
    public Item getSource() {
        return (Item)super.getSource();
    }
    
    public Config prop() {
        return prop;
    }
    
    public <V> V newValue() {
        return (V)newValue;
    }

    /**
     * 
     */
    public static class Fire
            extends DefaultPropertyConcern {

        /**
         * This is called *before* the {@link Config2} property is set. However, there is no
         * race condition between event handler thread, that might access property value, and
         * the current thread, that sets the property value, because most {@link EventHandler}s
         * are done in display thread.
         */
        @Override
        public Object doSet( Object obj, Config prop, Object newValue ) {
            Object currentValue = prop.info().getRawValue();
            if (!Objects.equals( currentValue, newValue )) {
                Item item = prop.info().getHostObject();
                EventManager.instance().publish( new ItemEvent( item, prop, newValue ) );
            }
            return newValue;
        }
    }
}