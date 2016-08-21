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
package org.polymap.rhei.fulltext.model2;

import com.google.common.base.Function;

import org.polymap.model2.Property;
import org.polymap.model2.PropertyBase;

/**
 * Calculates the name of the fields in the index out of a given {@link Property}.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
@FunctionalInterface
public interface FieldNameProvider 
        extends Function<PropertyBase,String> {
    
    /**
     * Use the name of the {@link Property} as field name. 
     */
    public static final FieldNameProvider STANDARD = prop -> prop.info().getName();

}