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
package org.polymap.rhei.batik.contribution;

import org.polymap.rhei.batik.IPanel;
import org.polymap.rhei.batik.PanelSite;
import org.polymap.rhei.batik.IAppContext;
import org.polymap.rhei.batik.toolkit.IPanelToolkit;

/**
 * The site of an {@link IContributionProvider}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IContributionSite {

    /** The {@link IPanel} to contribute to. */
    public IPanel panel();
    
    /** The site of the {@link #panel()}. */
    public PanelSite panelSite();
    
    /** The context of the {@link #panel()}. */
    public IAppContext context();
    
    /** The toolkit of the {@link #panel()}. */
    public <T extends IPanelToolkit> T toolkit();

    /** The tags that identify to target to contribute to. */
    public String[] tags();
    
    /** True if {@link #tags()} contain the given tag. */
    public boolean tagsContain( String tag );
    
}
