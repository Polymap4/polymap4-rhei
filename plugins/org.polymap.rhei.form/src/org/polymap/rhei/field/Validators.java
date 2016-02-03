/* 
 * polymap.org
 * Copyright (C) 2014, Falko Br�utigam. All rights reserved.
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
package org.polymap.rhei.field;

import org.polymap.rhei.table.IFeatureTableElement;
import org.polymap.rhei.table.ITableFieldValidator;

/**
 * Static methods to work with validators.
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public class Validators {

    /**
     * Concatenates the given validators. The delegates are called in the given order.
     *
     * @param delegates The validators the concat.
     * @return A newly created validator that calls the delegates.
     */
    public static <V extends IFormFieldValidator> V AND( final V... delegates ) {
        return (V)new ITableFieldValidator() {
    
            @Override
            public void init( IFeatureTableElement elm ) {
                for (IFormFieldValidator delegate : delegates) {
                    if (delegate instanceof ITableFieldValidator) {
                        ((ITableFieldValidator)delegate).init( elm );
                    }
                }
            }

            @Override
            public String validate( Object fieldValue ) {
                for (IFormFieldValidator delegate : delegates) {
                    String result = delegate.validate( fieldValue );
                    if (result != null) {
                        return result;
                    }
                }
                return null;
            }
            
            @Override
            public Object transform2Model( Object fieldValue ) throws Exception {
                Object transformed = fieldValue;
                for (IFormFieldValidator delegate : delegates) {
                    transformed = delegate.transform2Model( transformed );
                }
                return transformed;
            }
            
            @Override
            public Object transform2Field( Object modelValue ) throws Exception {
                Object transformed = modelValue;
                for (IFormFieldValidator delegate : delegates) {
                    transformed = delegate.transform2Field( transformed );
                }
                return transformed;
            }
        };
    }

}
