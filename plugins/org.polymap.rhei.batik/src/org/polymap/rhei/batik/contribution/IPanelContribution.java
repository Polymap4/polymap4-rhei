/* 
 * polymap.org
 * Copyright (C) 2017, the @authors. All rights reserved.
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

import org.eclipse.swt.widgets.Composite;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public interface IPanelContribution
        extends IContributionProvider {

    /**
     * 
     *
     * @param site
     * @param parent The parent {@link Composite} of the panel.
     */
    public void fillPanel( IContributionSite site, Composite parent );

}
