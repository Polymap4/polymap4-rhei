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
package org.polymap.rhei.field;

import java.util.Date;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.eclipse.rap.rwt.RWT;

import org.polymap.core.runtime.CachedLazyInit;
import org.polymap.core.runtime.Lazy;

/**
 * Simple {@link Date} to {@link String} validator using {@link SimpleDateFormat}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class DateValidator
        implements IFormFieldValidator<String,Date> {

    private int                 format = -1;
    
    private Lazy<DateFormat>    df = new CachedLazyInit( () -> SimpleDateFormat.getDateInstance( format, RWT.getLocale() ) );
    
    
    /**
     * Use {@link DateFormat#MEDIUM} format.
     */
    public DateValidator() {
        this.format = DateFormat.MEDIUM;
    }

    /**
     * Allows to specify the format to be used by the {@link SimpleDateFormat} for
     * formating and parsing.
     * 
     * @param format The format to use for
     *        {@link SimpleDateFormat#getDateInstance(int, java.util.Locale)}
     */
    public DateValidator( int format ) {
        this.format = format;
    }

    @Override
    public String validate( String fieldValue ) {
        return null;
    }

    @Override
    public String transform2Field( Date modelValue ) throws Exception {
        return modelValue != null ? df.get().format( modelValue ) : null;
    }

    @Override
    public Date transform2Model( String fieldValue ) throws Exception {
        return fieldValue != null ? df.get().parse( fieldValue ) : null;
    }
    
}
