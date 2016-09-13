/* 
 * polymap.org
 * Copyright (C) 2016, Falko Bräutigam. All rights reserved.
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
package org.polymap.rhei.table;

import java.util.Optional;

/**
 * Provides default method implementations in order to support for feature selection
 * in table.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class DefaultFeatureTableElement
        implements IFeatureTableElement {

    @Override
    public <T> Optional<T> unwrap( Class<T> targetClass ) {
        return Optional.empty();
    }

    @Override
    public int hashCode() {
        return fid().hashCode();
    }
    
    @Override
    public boolean equals( Object obj ) {
        return obj instanceof IFeatureTableElement
                ? ((IFeatureTableElement)obj).fid().equals( fid() )
                : false;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [fid=" + fid() + "]";
    }

}
