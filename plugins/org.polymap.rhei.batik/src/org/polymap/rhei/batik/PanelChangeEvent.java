/* 
 * polymap.org
 * Copyright (C) 2013-2014, Falko Bräutigam. All rights reserved.
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

import java.util.Arrays;
import java.util.EventObject;

import org.polymap.rhei.batik.IPanelSite.PanelStatus;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class PanelChangeEvent<V>
        extends EventObject {

    /** The types of {@link PanelChangeEvent}. */
    public enum EventType {
        /** The {@link IPanelSite#getPanelStatus()} {@link PanelStatus} has changed. */
        LIFECYCLE,
        /** The {@link IPanelSite#getStatus()} has changed. */
        STATUS,
        /** Titel or Icon has changed. */
        TITLE;

        public boolean isOnOf( EventType... types ) {
            return Arrays.asList( types ).contains( this );
        }
    }
    
    // instance *******************************************
    
    private EventType       type;
    
    private Object          previousValue;
    
    private Object          newValue;
    
    public PanelChangeEvent( IPanel source, EventType type, V newValue, V previousValue ) {
        super( source );
        this.type = type;
        this.previousValue = previousValue;
        this.newValue = newValue;
    }

    @Override
    public IPanel getSource() {
        return (IPanel)super.getSource();
    }

    /**
     * Convenience helper that cast the result of {@link #getSource()} to a concrete type.
     */
    public <T extends IPanel> T getPanel() {
        return (T)super.getSource();
    }

    public EventType getType() {
        return type;
    }
    
    public <T> T getPreviousValue() {
        return (T)previousValue;
    }
    
    public <T> T getNewValue() {
        return (T)newValue;
    }

    public String toString() {
        return getClass().getSimpleName() + "[source=" + source.getClass().getSimpleName() + ", type=" + type + "]";
    }

}
