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

import java.util.Comparator;
import java.util.Date;
import java.util.Optional;

import org.eclipse.jface.viewers.ColumnLabelProvider;

/**
 * Provides default table column sorting behaviour for {@link String}, {@link Number}
 * , {@link Date} and null values.
 * 
 * @author Falko Bräutigam
 */
class DefaultColumnComparator
        implements Comparator<IFeatureTableElement> {

    private IFeatureTableColumn     column;

    private String                  sortPropName;

    private ColumnLabelProvider     lp;


    DefaultColumnComparator( IFeatureTableColumn column ) {
        this.column = column;
        sortPropName = this.column.getName();
        lp = this.column.getLabelProvider();
    }


    @Override
    public int compare( IFeatureTableElement elm1, IFeatureTableElement elm2 ) {
        // the value from the elm or String from LabelProvider as fallback
        Object value1 = Optional.ofNullable( elm1.getValue( sortPropName ) ).orElse( lp.getText( elm1 ) );
        Object value2 = Optional.ofNullable( elm2.getValue( sortPropName ) ).orElse( lp.getText( elm2 ) );
        
        if (value1 == null && value2 == null) {
            return 0;
        }
        else if (value1 == null) {
            return -1;
        }
        else if (value2 == null) {
            return 1;
        }
        else if (!value1.getClass().equals( value2.getClass() )) {
            throw new RuntimeException( "Column type do not match: " + value1.getClass().getSimpleName() + " - " + value1.getClass().getSimpleName() );
        }
        else if (value1 instanceof String) {
            return ((String)value1).compareToIgnoreCase( (String)value2 );
        }
        else if (value1 instanceof Number) {
            return (int)(((Number)value1).doubleValue() - ((Number)value2).doubleValue());
        }
        else if (value1 instanceof Date) {
            return ((Date)value1).compareTo( (Date)value2 );
        }
        else {
            return value1.toString().compareTo( value2.toString() );
        }
    }
}