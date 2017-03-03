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

import java.util.function.Consumer;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class FunctionalLabelProvider
        extends CellLabelProvider {

    public static FunctionalLabelProvider of( Consumer<ViewerCell> handler ) {
        return new FunctionalLabelProvider( handler );
    }
    
    private Consumer<ViewerCell>    handler;
    
    
    protected FunctionalLabelProvider( Consumer<ViewerCell> handler ) {
        this.handler = handler;
    }

    @Override
    public void update( ViewerCell cell ) {
        handler.accept( cell );
    }

}
