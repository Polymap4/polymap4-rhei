/* 
 * polymap.org
 * Copyright (C) 2015-2018, Falko Bräutigam. All rights reserved.
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

import org.eclipse.jface.viewers.CellLabelProvider;

/**
 * Provides a secondary action or info for a {@link MdListViewer}.
 *  
 * @param <T> The type of the elemts on the list.
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class ActionProvider<T>
        extends CellLabelProvider {

    public abstract void perform( MdListViewer viewer, T elm );
    
}
