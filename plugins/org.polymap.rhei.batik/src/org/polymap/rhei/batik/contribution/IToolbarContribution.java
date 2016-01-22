/* 
 * polymap.org
 * Copyright (C) 2015-2016, Falko Bräutigam. All rights reserved.
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

import org.polymap.rhei.batik.toolkit.md.MdToolbar2;
import org.polymap.rhei.batik.toolkit.md.MdToolkit;

/**
 * An {@link IContributionProvider} that adds a
 * {@link MdToolkit#createToolbar(String, int...) Toolbar} to a panel.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IToolbarContribution
        extends IContributionProvider {

    public void fillToolbar( IContributionSite site, MdToolbar2 toolbar );
    
}
