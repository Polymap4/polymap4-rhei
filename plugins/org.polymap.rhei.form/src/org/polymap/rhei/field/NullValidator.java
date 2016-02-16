/* 
 * polymap.org
 * Copyright (C) 2010-2014, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.rhei.field;

/**
 * The no-op validator.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class NullValidator<F,M>
        implements IFormFieldValidator<F,M> {

    public String validate( F value ) {
        return null;
    }

    public F transform2Field( M modelValue ) throws Exception {
        return (F)modelValue;
    }

    public M transform2Model( F fieldValue ) throws Exception {
        return (M)fieldValue;
    }

}
