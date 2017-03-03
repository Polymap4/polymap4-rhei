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
package org.polymap.rhei.batik.toolkit.md;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;

/**
 * Provides one, static icon for each cell.
 *
 * @author Falko Bräutigam
 */
public class StaticIconLabelProvider
        extends CellLabelProvider {

    private Image           icon;
    
    public StaticIconLabelProvider( Image image ) {
        this.icon = image;
    }

    @Override
    public void update( ViewerCell cell ) {
        cell.setImage( icon );
    }

}
