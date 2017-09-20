/* 
 * polymap.org
 * Copyright 2013-2017, Polymap GmbH. All rights reserved.
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

import java.util.Collection;

import org.polymap.rhei.batik.engine.cp.IConstraint;
import org.polymap.rhei.batik.engine.cp.PercentScore;
import org.polymap.rhei.batik.engine.cp.Prioritized;
import org.polymap.rhei.batik.toolkit.ConstraintLayout.LayoutSolution;

/**
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class LayoutConstraint
        extends Prioritized
        implements IConstraint<LayoutSolution,PercentScore> {

    // factory ********************************************
    
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * This static factory method returns {@link #builder()} but the name of the
     * method makes for better readable code if static import is used.
     */
    public static Builder constraints() {
        return new Builder();
    }
    
    public static class Builder
            extends LayoutConstraintBuilder<Builder> {

        public LayoutConstraint[] get() {
            Collection<LayoutConstraint> list = constraints.constraints.values();
            return list.toArray( new LayoutConstraint[list.size()] );
        }
    }
    
    // instance *******************************************

    public LayoutConstraint( int priority ) {
        super( priority );
    }
    
}


