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
package org.polymap.rhei.batik.help;

import org.polymap.rhei.batik.dashboard.DashletSite;
import org.polymap.rhei.batik.dashboard.DefaultDashlet;
import org.polymap.rhei.batik.help.HelpPanel.HelpSite;
import org.polymap.rhei.batik.toolkit.MinWidthConstraint;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public abstract class HelpDashlet
        extends DefaultDashlet {

    protected HelpSite          helpSite;
    

    @Override
    public void init( DashletSite site ) {
        super.init( site );
        site.addConstraint( new MinWidthConstraint( 300, 1 ) );
    }


    public void init( HelpSite site ) {
        this.helpSite = site;
    }

}
