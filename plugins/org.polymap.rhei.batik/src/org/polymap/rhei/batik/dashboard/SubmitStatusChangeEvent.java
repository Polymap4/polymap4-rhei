/* 
 * polymap.org
 * Copyright (C) 2016, the @authors. All rights reserved.
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
package org.polymap.rhei.batik.dashboard;

import java.util.EventObject;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class SubmitStatusChangeEvent
        extends EventObject {

    private Dashboard               dashboard;
    
    private boolean                 submitable;

    public SubmitStatusChangeEvent( IDashlet dashlet, Dashboard dashboard, boolean submitable ) {
        super( dashlet );
        this.dashboard = dashboard;
        this.submitable = submitable;
    }

    @Override
    public ISubmitableDashlet getSource() {
        return getSource();
    }
    
    public Dashboard getDashboard() {
        return dashboard;
    }
    
    public boolean getsSubmitable() {
        return submitable;
    }
    
}
