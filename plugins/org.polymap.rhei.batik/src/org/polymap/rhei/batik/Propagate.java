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
package org.polymap.rhei.batik;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Denotes the propagation of the value of a {@link Context} property between
 * child/parent panels.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Propagate {

    public enum Propagation {
        /** 
         * Propagate the value to child panels. 
         */
        UP,
        /** 
         * 
         */
        DOWN,
        /**
         * For {@link Propagation#UP} this specifies that the value is reset after a
         * child panel is disposed. This is useful if the {@link Context} property is
         * meant to be an argument for the child panel.
         */
        ONESHOT,
        /** @deprecated Not supported yet. */
        START_NEW
    }
    
    Propagation[] value() default {Propagation.UP, Propagation.DOWN};
}
