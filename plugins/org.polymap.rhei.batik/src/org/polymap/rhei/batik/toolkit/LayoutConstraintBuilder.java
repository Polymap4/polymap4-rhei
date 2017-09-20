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
package org.polymap.rhei.batik.toolkit;

import org.polymap.rhei.batik.toolkit.NeighborhoodConstraint.Neighborhood;

/**
 * 
 * 
 * @author Falko Bräutigam
 */
public class LayoutConstraintBuilder<R extends LayoutConstraintBuilder> {

    public static final int     DEFAULT_CONSTRAINT_PRIORITY = 0;
    
    protected ConstraintData    constraints = new ConstraintData();
    
    public R priority( int value ) {
        constraints.add( new PriorityConstraint( value, DEFAULT_CONSTRAINT_PRIORITY ) );
        return (R)this;
    }
    
    public R maxWidth( int value ) {
        constraints.add( new MaxWidthConstraint( value, DEFAULT_CONSTRAINT_PRIORITY ) );
        return (R)this;
    }
    
    public R minHeight( int value ) {
        constraints.add( new MinHeightConstraint( value, DEFAULT_CONSTRAINT_PRIORITY ) );
        return (R)this;
    }
    
    public R minWidth( int value ) {
        constraints.add( new MinWidthConstraint( value, DEFAULT_CONSTRAINT_PRIORITY ) );
        return (R)this;
    }
    
    public R neighbor( Object control, Neighborhood neighborhood ) {
        constraints.add( new NeighborhoodConstraint( control, neighborhood, DEFAULT_CONSTRAINT_PRIORITY ) );
        return (R)this;
    }
    
}