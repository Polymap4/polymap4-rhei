/* 
 * polymap.org
 * Copyright (C) 2013-2016, Falko Bräutigam. All rights reserved.
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
package org.polymap.rhei.batik;

import java.util.EventObject;

/**
 * Fired when a {@link Context} property is accessed.
 * <p/>
 * Register via {@link Context#addListener(Object, EventFilter...)}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class PropertyAccessEvent
        extends EventObject {

    /** The types of {@link PropertyAccessEvent}. */
    public enum TYPE {
        /** @deprecated Do we really need events on get? */
        GET,
        SET
    }
    
    // instance *******************************************
    
    private TYPE            type;
    
    private Object          newValue, oldValue;
    
    public PropertyAccessEvent( Context source, TYPE type, Object newValue, Object oldValue ) {
        super( source );
        this.type = type;
        this.newValue = newValue;
        this.oldValue = oldValue;
    }

    @Override
    public Context getSource() {
        return (Context)super.getSource();
    }

    public TYPE getType() {
        return type;
    }

    public Object getNewValue() {
        return newValue;
    }
    
    public Object getOldValue() {
        return oldValue;
    }
    
}
