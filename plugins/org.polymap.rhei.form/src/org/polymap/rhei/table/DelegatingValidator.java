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
package org.polymap.rhei.table;

import org.polymap.rhei.field.IFormFieldValidator;

/**
 * Delegates to an {@link IFormFieldValidator}.
 *
 * @author Falko Bräutigam
 */
public class DelegatingValidator<F,M>
        implements ITableFieldValidator<F,M> {

    private IFormFieldValidator<F,M>        delegate;

    public DelegatingValidator( IFormFieldValidator<F,M> delegate ) {
        this.delegate = delegate;
    }

    @Override
    public String validate( F fieldValue, ValidatorSite site ) {
        return delegate.validate( fieldValue );
    }
    @Override
    public M transform2Model( F fieldValue, ValidatorSite site ) throws Exception {
        return delegate.transform2Model( fieldValue );
    }
    @Override
    public F transform2Field( M modelValue, ValidatorSite site ) throws Exception {
        return delegate.transform2Field( modelValue );
    }
    
}
