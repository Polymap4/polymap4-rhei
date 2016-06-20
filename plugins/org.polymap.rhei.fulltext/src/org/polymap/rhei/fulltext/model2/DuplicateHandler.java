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

import java.util.Arrays;

import com.google.common.base.Function;
import com.google.common.base.Joiner;

/**
 * Handles multiple values for the same key (aka property/field name) in
 * {@link EntityFeatureTransformer}.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
@FunctionalInterface
public interface DuplicateHandler extends Function<String[],String> {
    
    /**
     * Throws a {@link RuntimeException} if there are multiple values for the same
     * key.
     */
    public static final DuplicateHandler EXCEPTION = new DuplicateHandler() {
        @Override
        public String apply( String[] input ) {
            throw new RuntimeException( "Duplicate values are not allowed: " + Arrays.asList( input ) );
        }
    };

    
    /**
     * Concatenates multiple values with separator ' '. 
     */
    public static final DuplicateHandler CONCAT = new DuplicateHandler() {
        @Override
        public String apply( String[] input ) {
            return Joiner.on( ' ' ).join( input );
        }
    };
    
}
