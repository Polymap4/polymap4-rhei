/* 
 * polymap.org
 * Copyright (C) 2014, Falko Bräutigam. All rights reserved.
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
package org.polymap.rhei.batik.app;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.rhei.batik.IPanel;

/**
 * Handles multiple {@link IStatus} coming from different senders from within an
 * {@link IPanel}. The highest severity status is set as active status of the panel.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class MultiStatusManager {

    private static Log log = LogFactory.getLog( MultiStatusManager.class );
    
    private Map<Object,IStatus>         status = new HashMap();
    
    
    public IStatus updateStatusOf( Object sender, IStatus newStatus ) {
        assert sender != null;
        IStatus result = newStatus != null
            ? status.put( sender, newStatus )
            : status.remove( sender );
        updateUI();
        return result;
    }

    
    public IStatus highestSeverity() {
        IStatus result = Status.OK_STATUS;
        for (IStatus s : status.values()) {
            result = result == Status.OK_STATUS || result.getSeverity() < s.getSeverity() ? s : result;
            log.debug( "    checking: " + s + " -> highest: " + result );
        }
        return result;
    }
    
    
    protected void updateUI() {
        doUpdateUI( highestSeverity() );
    }
    
    
    protected abstract void doUpdateUI( IStatus highestSeverity );
    
}
